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
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.remote.command.TaskExecuteResponseAckCommand;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRunningAckCommand;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.server.utils.DataQualityResultOperator;
import org.apache.dolphinscheduler.service.process.ProcessService;

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
public class TaskEventService {

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(TaskEventService.class);

    /**
     * attemptQueue
     */
    private final BlockingQueue<TaskEvent> eventQueue = new LinkedBlockingQueue<>();

    /**
     * process service
     */
    @Autowired
    private ProcessService processService;

    /**
     * data quality result operator
     */
    @Autowired
    private DataQualityResultOperator dataQualityResultOperator;

    /**
     * task event worker
     */
    private Thread taskEventWorker;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    @PostConstruct
    public void start() {
        this.taskEventWorker = new TaskEventWorker();
        this.taskEventWorker.setName("TaskStateEventWorker");
        this.taskEventWorker.start();
    }

    @PreDestroy
    public void stop() {
        try {
            this.taskEventWorker.interrupt();
            if (!eventQueue.isEmpty()) {
                List<TaskEvent> remainEvents = new ArrayList<>(eventQueue.size());
                eventQueue.drainTo(remainEvents);
                for (TaskEvent event : remainEvents) {
                    this.persist(event);
                }
            }
        } catch (Exception e) {
            logger.error("stop error:", e);
        }
    }

    /**
     * add event to queue
     *
     * @param taskEvent taskEvent
     */
    public void addEvent(TaskEvent taskEvent) {
        try {
            eventQueue.put(taskEvent);
        } catch (InterruptedException e) {
            logger.error("add task event : {} error :{}", taskEvent, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * task worker thread
     */
    class TaskEventWorker extends Thread {

        @Override
        public void run() {

            while (Stopper.isRunning()) {
                try {
                    // if not task , blocking here
                    TaskEvent taskEvent = eventQueue.take();
                    persist(taskEvent);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("persist task error", e);
                }
            }
            logger.info("StateEventResponseWorker stopped");
        }
    }

    /**
     * persist task event
     *
     * @param taskEvent taskEvent
     */
    private void persist(TaskEvent taskEvent) {
        Event event = taskEvent.getEvent();
        int taskInstanceId = taskEvent.getTaskInstanceId();
        int processInstanceId = taskEvent.getProcessInstanceId();

        TaskInstance taskInstance;
        WorkflowExecuteThread workflowExecuteThread = this.processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);
        if (workflowExecuteThread != null && workflowExecuteThread.checkTaskInstanceById(taskInstanceId)) {
            taskInstance = workflowExecuteThread.getTaskInstance(taskInstanceId);
        } else {
            taskInstance = processService.findTaskInstanceById(taskInstanceId);
        }

        switch (event) {
            case DISPATCH:
                handleDispatchEvent(taskEvent, taskInstance);
                // dispatch event do not need to submit state event
                return;
            case DELAY:
            case RUNNING:
                handleRunningEvent(taskEvent, taskInstance);
                break;
            case RESULT:
                handleResultEvent(taskEvent, taskInstance);
                break;
            default:
                throw new IllegalArgumentException("invalid event type : " + event);
        }

        StateEvent stateEvent = new StateEvent();
        stateEvent.setProcessInstanceId(taskEvent.getProcessInstanceId());
        stateEvent.setTaskInstanceId(taskEvent.getTaskInstanceId());
        stateEvent.setExecutionStatus(taskEvent.getState());
        stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

    /**
     * handle dispatch event
     */
    private void handleDispatchEvent(TaskEvent taskEvent, TaskInstance taskInstance) {
        if (taskInstance == null) {
            logger.error("taskInstance is null");
            return;
        }
        if (taskInstance.getState() != ExecutionStatus.SUBMITTED_SUCCESS) {
            return;
        }
        taskInstance.setState(ExecutionStatus.DISPATCH);
        taskInstance.setHost(taskEvent.getWorkerAddress());
        processService.saveTaskInstance(taskInstance);
    }

    /**
     * handle running event
     */
    private void handleRunningEvent(TaskEvent taskEvent, TaskInstance taskInstance) {
        Channel channel = taskEvent.getChannel();
        try {
            if (taskInstance != null) {
                if (taskInstance.getState().typeIsFinished()) {
                    logger.warn("task is finish, running event is meaningless, taskInstanceId:{}, state:{}", taskInstance.getId(), taskInstance.getState());
                } else {
                    taskInstance.setState(taskEvent.getState());
                    taskInstance.setStartTime(taskEvent.getStartTime());
                    taskInstance.setHost(taskEvent.getWorkerAddress());
                    taskInstance.setLogPath(taskEvent.getLogPath());
                    taskInstance.setExecutePath(taskEvent.getExecutePath());
                    taskInstance.setPid(taskEvent.getProcessId());
                    taskInstance.setAppLink(taskEvent.getAppIds());
                    processService.saveTaskInstance(taskInstance);
                }
            }
            // if taskInstance is null (maybe deleted) or finish. retry will be meaningless . so ack success
            TaskExecuteRunningAckCommand taskExecuteRunningAckCommand = new TaskExecuteRunningAckCommand(ExecutionStatus.SUCCESS.getCode(), taskEvent.getTaskInstanceId());
            channel.writeAndFlush(taskExecuteRunningAckCommand.convert2Command());
        } catch (Exception e) {
            logger.error("worker ack master error", e);
            TaskExecuteRunningAckCommand taskExecuteRunningAckCommand = new TaskExecuteRunningAckCommand(ExecutionStatus.FAILURE.getCode(), -1);
            channel.writeAndFlush(taskExecuteRunningAckCommand.convert2Command());
        }
    }

    /**
     * handle result event
     */
    private void handleResultEvent(TaskEvent taskEvent, TaskInstance taskInstance) {
        Channel channel = taskEvent.getChannel();
        try {
            if (taskInstance != null) {
                dataQualityResultOperator.operateDqExecuteResult(taskEvent, taskInstance);

                taskInstance.setStartTime(taskEvent.getStartTime());
                taskInstance.setHost(taskEvent.getWorkerAddress());
                taskInstance.setLogPath(taskEvent.getLogPath());
                taskInstance.setExecutePath(taskEvent.getExecutePath());
                taskInstance.setPid(taskEvent.getProcessId());
                taskInstance.setAppLink(taskEvent.getAppIds());
                taskInstance.setState(taskEvent.getState());
                taskInstance.setEndTime(taskEvent.getEndTime());
                taskInstance.setVarPool(taskEvent.getVarPool());
                processService.changeOutParam(taskInstance);
                processService.saveTaskInstance(taskInstance);
            }
            // if taskInstance is null (maybe deleted) . retry will be meaningless . so response success
            TaskExecuteResponseAckCommand taskExecuteResponseAckCommand = new TaskExecuteResponseAckCommand(ExecutionStatus.SUCCESS.getCode(), taskEvent.getTaskInstanceId());
            channel.writeAndFlush(taskExecuteResponseAckCommand.convert2Command());
        } catch (Exception e) {
            logger.error("worker response master error", e);
            TaskExecuteResponseAckCommand taskExecuteResponseAckCommand = new TaskExecuteResponseAckCommand(ExecutionStatus.FAILURE.getCode(), -1);
            channel.writeAndFlush(taskExecuteResponseAckCommand.convert2Command());
        }
    }
}