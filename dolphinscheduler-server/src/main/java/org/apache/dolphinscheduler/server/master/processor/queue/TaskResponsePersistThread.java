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
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.DBTaskAckCommand;
import org.apache.dolphinscheduler.remote.command.DBTaskResponseCommand;
import org.apache.dolphinscheduler.remote.command.TaskKillAckCommand;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread;
import org.apache.dolphinscheduler.server.master.runner.task.ITaskProcessor;
import org.apache.dolphinscheduler.server.master.runner.task.TaskAction;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

public class TaskResponsePersistThread implements Runnable {

    /**
     * logger of TaskResponsePersistThread
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskResponsePersistThread.class);

    private final ConcurrentLinkedQueue<TaskResponseEvent>  events = new ConcurrentLinkedQueue<>();

    private final Integer processInstanceId;

    /**
     * process service
     */
    private ProcessService processService;

    private ConcurrentHashMap<Integer, WorkflowExecuteThread> processInstanceMapper;

    public TaskResponsePersistThread(ProcessService processService,
                                     ConcurrentHashMap<Integer, WorkflowExecuteThread> processInstanceMapper,
                                     Integer processInstanceId) {
        this.processService = processService;
        this.processInstanceMapper = processInstanceMapper;
        this.processInstanceId = processInstanceId;
    }

    @Override
    public void run() {
        while (!this.events.isEmpty()) {
            TaskResponseEvent event = this.events.peek();
            try {
                boolean result = persist(event);
                if (!result) {
                    logger.error("persist meta error, task id:{}, instance id:{}", event.getTaskInstanceId(), event.getProcessInstanceId());
                }
            } catch (Exception e) {
                logger.error("persist error, task id:{}, instance id:{}, error: {}", event.getTaskInstanceId(), event.getProcessInstanceId(), e);
            } finally {
                this.events.remove(event);
            }
        }
    }

    /**
     * persist  taskResponseEvent
     *
     * @param taskResponseEvent taskResponseEvent
     */
    private boolean persist(TaskResponseEvent taskResponseEvent) {
        Event event = taskResponseEvent.getEvent();
        Channel channel = taskResponseEvent.getChannel();

        TaskInstance taskInstance = processService.findTaskInstanceById(taskResponseEvent.getTaskInstanceId());

        boolean result = true;

        switch (event) {
            case ACK:
                try {
                    if (taskInstance != null) {
                        ExecutionStatus status = taskInstance.getState().typeIsFinished() ? taskInstance.getState() : taskResponseEvent.getState();
                        processService.changeTaskState(taskInstance, status,
                                taskResponseEvent.getStartTime(),
                                taskResponseEvent.getWorkerAddress(),
                                taskResponseEvent.getExecutePath(),
                                taskResponseEvent.getLogPath(),
                                taskResponseEvent.getTaskInstanceId());
                        logger.debug("changeTaskState in ACK , changed in meta:{} ,task instance state:{}, task response event state:{}, taskInstance id:{},taskInstance host:{}",
                                result, taskInstance.getState(), taskResponseEvent.getState(), taskInstance.getId(), taskInstance.getHost());
                    }
                    // if taskInstance is null (maybe deleted) . retry will be meaningless . so ack success
                    DBTaskAckCommand taskAckCommand = new DBTaskAckCommand(ExecutionStatus.SUCCESS.getCode(), taskResponseEvent.getTaskInstanceId());
                    channel.writeAndFlush(taskAckCommand.convert2Command());
                    logger.debug("worker ack master success, taskInstance id:{},taskInstance host:{}", taskInstance.getId(), taskInstance.getHost());
                } catch (Exception e) {
                    result = false;
                    logger.error("worker ack master error", e);
                    DBTaskAckCommand taskAckCommand = new DBTaskAckCommand(ExecutionStatus.FAILURE.getCode(), taskInstance == null ? -1 : taskInstance.getId());
                    channel.writeAndFlush(taskAckCommand.convert2Command());
                }
                break;
            case RESULT:
                try {
                    if (taskInstance != null) {
                        result = processService.changeTaskState(taskInstance, taskResponseEvent.getState(),
                                taskResponseEvent.getEndTime(),
                                taskResponseEvent.getProcessId(),
                                taskResponseEvent.getAppIds(),
                                taskResponseEvent.getTaskInstanceId(),
                                taskResponseEvent.getVarPool()
                        );
                        logger.debug("changeTaskState in RESULT , changed in meta:{} task instance state:{}, task response event state:{}, taskInstance id:{},taskInstance host:{}",
                                result, taskInstance.getState(), taskResponseEvent.getState(), taskInstance.getId(), taskInstance.getHost());
                    }
                    if (!result) {
                        DBTaskResponseCommand taskResponseCommand = new DBTaskResponseCommand(ExecutionStatus.FAILURE.getCode(), taskResponseEvent.getTaskInstanceId());
                        channel.writeAndFlush(taskResponseCommand.convert2Command());
                        logger.debug("worker response master failure, taskInstance id:{},taskInstance host:{}", taskInstance.getId(), taskInstance.getHost());
                    } else {
                        // if taskInstance is null (maybe deleted) . retry will be meaningless . so response success
                        DBTaskResponseCommand taskResponseCommand = new DBTaskResponseCommand(ExecutionStatus.SUCCESS.getCode(), taskResponseEvent.getTaskInstanceId());
                        channel.writeAndFlush(taskResponseCommand.convert2Command());
                        logger.debug("worker response master success, taskInstance id:{},taskInstance host:{}", taskInstance.getId(), taskInstance.getHost());
                    }
                } catch (Exception e) {
                    result = false;
                    logger.error("worker response master error", e);
                    DBTaskResponseCommand taskResponseCommand = new DBTaskResponseCommand(ExecutionStatus.FAILURE.getCode(), -1);
                    channel.writeAndFlush(taskResponseCommand.convert2Command());
                }
                break;
            case ACTION_STOP:
                WorkflowExecuteThread workflowExecuteThread = this.processInstanceMapper.get(taskResponseEvent.getProcessInstanceId());
                if (workflowExecuteThread != null) {
                    ITaskProcessor taskProcessor = workflowExecuteThread.getActiveTaskProcessorMaps().get(taskResponseEvent.getTaskInstanceId());
                    if (taskProcessor != null) {
                        taskProcessor.persist(TaskAction.STOP);
                        logger.debug("ACTION_STOP: task instance id:{}, process instance id:{}", taskResponseEvent.getTaskInstanceId(), taskResponseEvent.getProcessInstanceId());
                    }
                }

                if (channel != null) {
                    channel.writeAndFlush(taskKillAckCommand.convert2Command());
                    TaskKillAckCommand taskKillAckCommand = new TaskKillAckCommand(ExecutionStatus.SUCCESS.getCode(), taskResponseEvent.getTaskInstanceId());
                }

                break;
            default:
                throw new IllegalArgumentException("invalid event type : " + event);
        }

        WorkflowExecuteThread workflowExecuteThread = this.processInstanceMapper.get(taskResponseEvent.getProcessInstanceId());
        if (workflowExecuteThread != null) {
            StateEvent stateEvent = new StateEvent();
            stateEvent.setProcessInstanceId(taskResponseEvent.getProcessInstanceId());
            stateEvent.setTaskInstanceId(taskResponseEvent.getTaskInstanceId());
            stateEvent.setExecutionStatus(taskResponseEvent.getState());
            stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
            workflowExecuteThread.addStateEvent(stateEvent);
        }
        return result;
    }

    public boolean addEvent(TaskResponseEvent event) {
        if (event.getProcessInstanceId() != this.processInstanceId) {
            logger.info("event would be abounded, task instance id:{}, process instance id:{}, this.processInstanceId:{}",
                    event.getTaskInstanceId(), event.getProcessInstanceId(), this.processInstanceId);
            return false;
        }
        return this.events.add(event);
    }

    public int eventSize() {
        return this.events.size();
    }

    public boolean isEmpty() {
        return this.events.isEmpty();
    }

    public Integer getProcessInstanceId() {
        return processInstanceId;
    }

    public String getKey() {
        return String.valueOf(processInstanceId);
    }
}