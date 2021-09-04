/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.server.master.processor.queue;

import org.apache.dolphinscheduler.common.enums.Event;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.dq.CheckType;
import org.apache.dolphinscheduler.common.enums.dq.DqFailureStrategy;
import org.apache.dolphinscheduler.common.enums.dq.DqTaskState;
import org.apache.dolphinscheduler.common.enums.dq.OperatorType;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.dao.entity.DqExecuteResult;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.DBTaskAckCommand;
import org.apache.dolphinscheduler.remote.command.DBTaskResponseCommand;
import org.apache.dolphinscheduler.server.utils.AlertManager;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

/**
 * task manager
 */
@Component
public class TaskResponseService {

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(TaskResponseService.class);

    /**
     * attemptQueue
     */
    private final BlockingQueue<TaskResponseEvent> eventQueue = new LinkedBlockingQueue<>(5000);

    /**
     * process service
     */
    @Autowired
    private ProcessService processService;

    /**
     * task response worker
     */
    private Thread taskResponseWorker;

    /**
     * alert manager
     */
    private AlertManager alertManager = new AlertManager();

    @PostConstruct
    public void start() {
        this.taskResponseWorker = new TaskResponseWorker();
        this.taskResponseWorker.setName("TaskResponseWorker");
        this.taskResponseWorker.start();
    }

    @PreDestroy
    public void stop() {
        this.taskResponseWorker.interrupt();
        if (!eventQueue.isEmpty()) {
            List<TaskResponseEvent> remainEvents = new ArrayList<>(eventQueue.size());
            eventQueue.drainTo(remainEvents);
            for (TaskResponseEvent event : remainEvents) {
                this.persist(event);
            }
        }
    }

    /**
     * put task to attemptQueue
     *
     * @param taskResponseEvent taskResponseEvent
     */
    public void addResponse(TaskResponseEvent taskResponseEvent) {
        try {
            eventQueue.put(taskResponseEvent);
        } catch (InterruptedException e) {
            logger.error("put task : {} error :{}", taskResponseEvent, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * task worker thread
     */
    class TaskResponseWorker extends Thread {

        @Override
        public void run() {

            while (Stopper.isRunning()) {
                try {
                    // if not task , blocking here
                    TaskResponseEvent taskResponseEvent = eventQueue.take();
                    persist(taskResponseEvent);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("persist task error", e);
                }
            }
            logger.info("TaskResponseWorker stopped");
        }
    }

    /**
     * persist  taskResponseEvent
     *
     * @param taskResponseEvent taskResponseEvent
     */
    private void persist(TaskResponseEvent taskResponseEvent) {
        Event event = taskResponseEvent.getEvent();
        Channel channel = taskResponseEvent.getChannel();

        switch (event) {
            case ACK:
                try {
                    TaskInstance taskInstance = processService.findTaskInstanceById(taskResponseEvent.getTaskInstanceId());
                    if (taskInstance != null) {
                        ExecutionStatus status = taskInstance.getState().typeIsFinished() ? taskInstance.getState() : taskResponseEvent.getState();
                        processService.changeTaskState(taskInstance, status,
                            taskResponseEvent.getStartTime(),
                            taskResponseEvent.getWorkerAddress(),
                            taskResponseEvent.getExecutePath(),
                            taskResponseEvent.getLogPath());
                    }
                    // if taskInstance is null (maybe deleted) . retry will be meaningless . so ack success
                    DBTaskAckCommand taskAckCommand = new DBTaskAckCommand(ExecutionStatus.SUCCESS.getCode(), taskResponseEvent.getTaskInstanceId());
                    channel.writeAndFlush(taskAckCommand.convert2Command());
                } catch (Exception e) {
                    logger.error("worker ack master error", e);
                    DBTaskAckCommand taskAckCommand = new DBTaskAckCommand(ExecutionStatus.FAILURE.getCode(), -1);
                    channel.writeAndFlush(taskAckCommand.convert2Command());
                }
                break;
            case RESULT:
                try {
                    TaskInstance taskInstance = processService.findTaskInstanceById(taskResponseEvent.getTaskInstanceId());
                    if (taskInstance != null) {

                        operateDqExecuteResult(taskResponseEvent, taskInstance);

                        processService.changeTaskState(taskInstance, taskResponseEvent.getState(),
                                taskResponseEvent.getEndTime(),
                                taskResponseEvent.getProcessId(),
                                taskResponseEvent.getAppIds(),
                                taskResponseEvent.getVarPool()
                        );
                    }
                    // if taskInstance is null (maybe deleted) . retry will be meaningless . so response success
                    DBTaskResponseCommand taskResponseCommand = new DBTaskResponseCommand(ExecutionStatus.SUCCESS.getCode(), taskResponseEvent.getTaskInstanceId());
                    channel.writeAndFlush(taskResponseCommand.convert2Command());
                } catch (Exception e) {
                    logger.error("worker response master error", e);
                    DBTaskResponseCommand taskResponseCommand = new DBTaskResponseCommand(ExecutionStatus.FAILURE.getCode(), -1);
                    channel.writeAndFlush(taskResponseCommand.convert2Command());
                }
                break;
            default:
                throw new IllegalArgumentException("invalid event type : " + event);
        }
    }

    private void operateDqExecuteResult(TaskResponseEvent taskResponseEvent, TaskInstance taskInstance) {
        if (TaskType.DATA_QUALITY == TaskType.valueOf(taskInstance.getTaskType())) {
            int taskInstanceId = taskResponseEvent.getTaskInstanceId();
            if (taskResponseEvent.getState().typeIsFailure()
                    || taskResponseEvent.getState().typeIsCancel()) {
                processService.deleteDqExecuteResultByTaskInstanceId(taskInstanceId);
                processService.deleteTaskStatisticsValueByTaskInstanceId(taskInstanceId);
                return;
            }

            processService.updateDqExecuteResultUserId(taskInstanceId);
            DqExecuteResult dqExecuteResult =
                    processService.getDqExecuteResultByTaskInstanceId(taskInstanceId);
            if (dqExecuteResult != null) {
                //check the result ,if result is failure do some operator by failure strategy
                checkDqExecuteResult(taskResponseEvent, dqExecuteResult);
            }
        }
    }

    private void checkDqExecuteResult(TaskResponseEvent taskResponseEvent, DqExecuteResult dqExecuteResult) {
        if (isFailure(dqExecuteResult)) {
            DqFailureStrategy dqFailureStrategy = DqFailureStrategy.of(dqExecuteResult.getFailureStrategy());
            if (dqFailureStrategy != null) {
                dqExecuteResult.setState(DqTaskState.FAILURE);
                switch (dqFailureStrategy) {
                    case ALERT:
                        sendAlert(dqExecuteResult);
                        logger.info("task is failre, continue and alert");
                        break;
                    case BLOCK:
                        taskResponseEvent.setState(ExecutionStatus.FAILURE);
                        sendAlert(dqExecuteResult);
                        logger.info("task is failre, end and alert");
                        break;
                    default:
                        break;
                }
            }
        } else {
            dqExecuteResult.setState(DqTaskState.SUCCESS);
        }

        processService.updateDqExecuteResultState(dqExecuteResult);
    }

    private boolean isFailure(DqExecuteResult dqExecuteResult) {
        CheckType checkType = dqExecuteResult.getCheckType();

        double statisticsValue = dqExecuteResult.getStatisticsValue();
        double comparisonValue = dqExecuteResult.getComparisonValue();
        double threshold = dqExecuteResult.getThreshold();

        OperatorType operatorType = OperatorType.of(dqExecuteResult.getOperator());

        boolean isFailure = false;
        if (operatorType != null) {
            double srcValue = 0;
            switch (checkType) {
                case COMPARISON_MINUS_STATISTICS:
                    srcValue = comparisonValue - statisticsValue;
                    isFailure = getCompareResult(operatorType,srcValue,threshold);
                    break;
                case STATISTICS_MINUS_COMPARISON:
                    srcValue = statisticsValue - comparisonValue;
                    isFailure = getCompareResult(operatorType,srcValue,threshold);
                    break;
                case STATISTICS_COMPARISON_PERCENTAGE:
                    if (comparisonValue > 0) {
                        srcValue = statisticsValue / comparisonValue * 100;
                    }
                    isFailure = getCompareResult(operatorType,srcValue,threshold);
                    break;
                case STATISTICS_COMPARISON_DIFFERENCE_COMPARISON_PERCENTAGE:
                    if (comparisonValue > 0) {
                        srcValue = (comparisonValue - statisticsValue) / comparisonValue * 100;
                    }
                    isFailure = getCompareResult(operatorType,srcValue,threshold);
                    break;
                default:
                    break;
            }

        }
        return isFailure;
    }

    private void sendAlert(DqExecuteResult dqExecuteResult) {
        alertManager.
                sendAlterDataQualityTask(dqExecuteResult,
                        processService.findProcessInstanceDetailById(
                                Integer.parseInt(String.valueOf(dqExecuteResult.getProcessInstanceId()))));
    }

    private static boolean getCompareResult(OperatorType operatorType, double srcValue, double targetValue) {
        BigDecimal src = BigDecimal.valueOf(srcValue);
        BigDecimal target = BigDecimal.valueOf(targetValue);
        switch (operatorType) {
            case EQ:
                return src.compareTo(target) == 0;
            case LT:
                return src.compareTo(target) <= -1;
            case LE:
                return src.compareTo(target) == 0 || src.compareTo(target) <= -1;
            case GT:
                return src.compareTo(target) >= 1;
            case GE:
                return src.compareTo(target) == 0 || src.compareTo(target) >= 1;
            case NE:
                return src.compareTo(target) != 0;
            default:
                return true;
        }
    }

    public BlockingQueue<TaskResponseEvent> getEventQueue() {
        return eventQueue;
    }
}
