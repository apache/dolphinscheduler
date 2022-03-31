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
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.remote.command.TaskExecuteResponseAckCommand;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRunningAckCommand;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.server.utils.DataQualityResultOperator;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * task execute thread
 */
public class TaskExecuteThread {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteThread.class);

    private final int processInstanceId;

    private final ConcurrentLinkedQueue<TaskEvent> events = new ConcurrentLinkedQueue<>();

    private ProcessService processService;

    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    private DataQualityResultOperator dataQualityResultOperator;

    public TaskExecuteThread(int processInstanceId, ProcessService processService, WorkflowExecuteThreadPool workflowExecuteThreadPool,
                             ProcessInstanceExecCacheManager processInstanceExecCacheManager, DataQualityResultOperator dataQualityResultOperator) {
        this.processInstanceId = processInstanceId;
        this.processService = processService;
        this.workflowExecuteThreadPool = workflowExecuteThreadPool;
        this.processInstanceExecCacheManager = processInstanceExecCacheManager;
        this.dataQualityResultOperator = dataQualityResultOperator;
    }

    public void run() {
        while (!this.events.isEmpty()) {
            TaskEvent event = this.events.peek();
            try {
                persist(event);
            } catch (Exception e) {
                logger.error("persist error, event:{}, error: {}", event, e);
            } finally {
                this.events.remove(event);
            }
        }
    }

    public String getKey() {
        return String.valueOf(processInstanceId);
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

    public boolean addEvent(TaskEvent event) {
        if (event.getProcessInstanceId() != this.processInstanceId) {
            logger.warn("event would be abounded, task instance id:{}, process instance id:{}, this.processInstanceId:{}",
                    event.getTaskInstanceId(), event.getProcessInstanceId(), this.processInstanceId);
            return false;
        }
        return this.events.add(event);
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
