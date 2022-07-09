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
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.remote.command.TaskExecuteResponseAckCommand;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRunningAckCommand;
import org.apache.dolphinscheduler.remote.command.TaskRecallAckCommand;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.event.StateEvent;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.server.utils.DataQualityResultOperator;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * task execute thread
 */
public class TaskExecuteRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteRunnable.class);

    private final int processInstanceId;

    private final ConcurrentLinkedQueue<TaskEvent> events = new ConcurrentLinkedQueue<>();

    private ProcessService processService;

    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    private DataQualityResultOperator dataQualityResultOperator;

    public TaskExecuteRunnable(int processInstanceId, ProcessService processService, WorkflowExecuteThreadPool workflowExecuteThreadPool,
                               ProcessInstanceExecCacheManager processInstanceExecCacheManager, DataQualityResultOperator dataQualityResultOperator) {
        this.processInstanceId = processInstanceId;
        this.processService = processService;
        this.workflowExecuteThreadPool = workflowExecuteThreadPool;
        this.processInstanceExecCacheManager = processInstanceExecCacheManager;
        this.dataQualityResultOperator = dataQualityResultOperator;
    }

    @Override
    public void run() {
        while (!this.events.isEmpty()) {
            TaskEvent event = this.events.peek();
            try {
                LoggerUtils.setWorkflowAndTaskInstanceIDMDC(event.getProcessInstanceId(), event.getTaskInstanceId());
                persist(event);
            } catch (Exception e) {
                logger.error("persist task event error, event:{}", event, e);
            } finally {
                this.events.remove(event);
                LoggerUtils.removeWorkflowAndTaskInstanceIdMDC();
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
    private void persist(TaskEvent taskEvent) throws Exception {
        Event event = taskEvent.getEvent();
        int taskInstanceId = taskEvent.getTaskInstanceId();
        int processInstanceId = taskEvent.getProcessInstanceId();

        Optional<TaskInstance> taskInstance;
        WorkflowExecuteRunnable workflowExecuteRunnable =
            this.processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);
        if (workflowExecuteRunnable != null && workflowExecuteRunnable.checkTaskInstanceById(taskInstanceId)) {
            taskInstance = workflowExecuteRunnable.getTaskInstance(taskInstanceId);
        } else {
            taskInstance = Optional.ofNullable(processService.findTaskInstanceById(taskInstanceId));
        }

        boolean needToSendEvent = true;
        switch (event) {
            case DISPATCH:
                needToSendEvent = handleDispatchEvent(taskEvent, taskInstance);
                // dispatch event do not need to submit state event
                break;
            case DELAY:
            case RUNNING:
                needToSendEvent = handleRunningEvent(taskEvent, taskInstance);
                break;
            case RESULT:
                needToSendEvent = handleResultEvent(taskEvent, taskInstance);
                break;
            case WORKER_REJECT:
                needToSendEvent =
                    handleWorkerRejectEvent(taskEvent.getChannel(), taskInstance, workflowExecuteRunnable);
                break;
            default:
                throw new IllegalArgumentException("invalid event type : " + event);
        }
        if (!needToSendEvent) {
            logger.info("Handle task event: {} success, there is no need to send a StateEvent", taskEvent);
            return;
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
    private boolean handleDispatchEvent(TaskEvent taskEvent, Optional<TaskInstance> taskInstanceOptional) {
        if (!taskInstanceOptional.isPresent()) {
            logger.error("taskInstance is null");
            return false;
        }
        TaskInstance taskInstance = taskInstanceOptional.get();
        if (taskInstance.getState() != ExecutionStatus.SUBMITTED_SUCCESS) {
            return false;
        }
        taskInstance.setState(ExecutionStatus.DISPATCH);
        taskInstance.setHost(taskEvent.getWorkerAddress());
        processService.saveTaskInstance(taskInstance);
        return true;
    }

    /**
     * handle running event
     */
    private boolean handleRunningEvent(TaskEvent taskEvent, Optional<TaskInstance> taskInstanceOptional) {
        Channel channel = taskEvent.getChannel();
        if (taskInstanceOptional.isPresent()) {
            TaskInstance taskInstance = taskInstanceOptional.get();
            if (taskInstance.getState().typeIsFinished()) {
                logger.warn("task is finish, running event is meaningless, taskInstanceId:{}, state:{}",
                    taskInstance.getId(),
                    taskInstance.getState());
                return false;
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
        // send ack to worker
        TaskExecuteRunningAckCommand taskExecuteRunningAckCommand =
            new TaskExecuteRunningAckCommand(ExecutionStatus.SUCCESS.getCode(), taskEvent.getTaskInstanceId());
        channel.writeAndFlush(taskExecuteRunningAckCommand.convert2Command());
        return true;
    }

    /**
     * handle result event
     */
    private boolean handleResultEvent(TaskEvent taskEvent, Optional<TaskInstance> taskInstanceOptional) {
        Channel channel = taskEvent.getChannel();
        if (taskInstanceOptional.isPresent()) {
            TaskInstance taskInstance = taskInstanceOptional.get();
            if (taskInstance.getState().typeIsFinished()) {
                logger.warn("The current taskInstance has already been finished, taskEvent: {}", taskEvent);
                return false;
            }

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
        TaskExecuteResponseAckCommand taskExecuteResponseAckCommand =
            new TaskExecuteResponseAckCommand(ExecutionStatus.SUCCESS.getCode(), taskEvent.getTaskInstanceId());
        channel.writeAndFlush(taskExecuteResponseAckCommand.convert2Command());
        return true;
    }

    /**
     * handle result event
     */
    private boolean handleWorkerRejectEvent(Channel channel,
                                            Optional<TaskInstance> taskInstanceOptional,
                                            WorkflowExecuteRunnable executeThread) throws Exception {
        TaskInstance taskInstance =
            taskInstanceOptional.orElseThrow(() -> new RuntimeException("taskInstance is null"));
        if (executeThread != null) {
            executeThread.resubmit(taskInstance.getTaskCode());
        }
        if (channel != null) {
            TaskRecallAckCommand taskRecallAckCommand =
                new TaskRecallAckCommand(ExecutionStatus.SUCCESS.getCode(), taskInstance.getId());
            channel.writeAndFlush(taskRecallAckCommand.convert2Command());
        }
        return true;
    }
}
