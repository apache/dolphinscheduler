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

package org.apache.dolphinscheduler.server.master.event;

import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.enums.TaskEventType;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.utils.TaskInstanceUtils;
import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.worker.ITaskInstanceExecutionEventAckListener;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceExecutionFinishEventAck;
import org.apache.dolphinscheduler.server.master.cache.IWorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEvent;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.server.master.utils.DataQualityResultOperator;
import org.apache.dolphinscheduler.server.master.workflow.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.service.process.ProcessService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TaskResultEventHandler implements TaskEventHandler {

    @Autowired
    private IWorkflowExecuteRunnableRepository IWorkflowExecuteRunnableRepository;

    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    @Autowired
    private DataQualityResultOperator dataQualityResultOperator;

    @Autowired
    private ProcessService processService;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private MasterConfig masterConfig;

    @Override
    public void handleTaskEvent(TaskEvent taskEvent) throws TaskEventHandleError, TaskEventHandleException {
        int taskInstanceId = taskEvent.getTaskInstanceId();
        int processInstanceId = taskEvent.getProcessInstanceId();

        IWorkflowExecutionRunnable workflowExecuteRunnable =
                this.IWorkflowExecuteRunnableRepository.getByProcessInstanceId(
                        processInstanceId);
        if (workflowExecuteRunnable == null) {
            sendAckToWorker(taskEvent);
            throw new TaskEventHandleError(
                    "Handle task result event error, cannot find related workflow instance from cache, will discard this event");
        }
        TaskInstance taskInstance = workflowExecuteRunnable.getTaskExecutionRunnableById(taskInstanceId)
                .getTaskExecutionRunnableContext().getTaskInstance();
        if (taskInstance == null) {
            sendAckToWorker(taskEvent);
            throw new TaskEventHandleError(
                    "Handle task result event error, cannot find the taskInstance from cache, will discord this event");
        }
        if (taskInstance.getState().isFinished()) {
            sendAckToWorker(taskEvent);
            throw new TaskEventHandleError(
                    "Handle task result event error, the task instance is already finished, will discord this event");
        }
        dataQualityResultOperator.operateDqExecuteResult(taskEvent, taskInstance);

        TaskInstance oldTaskInstance = new TaskInstance();
        TaskInstanceUtils.copyTaskInstance(taskInstance, oldTaskInstance);
        try {
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
            taskInstanceDao.updateById(taskInstance);
        } catch (Exception ex) {
            TaskInstanceUtils.copyTaskInstance(oldTaskInstance, taskInstance);
            throw new TaskEventHandleError("Handle task result event error, save taskInstance to db error", ex);
        }

        sendAckToWorker(taskEvent);

        TaskStateEvent stateEvent = TaskStateEvent.builder()
                .processInstanceId(taskEvent.getProcessInstanceId())
                .taskInstanceId(taskEvent.getTaskInstanceId())
                .status(taskEvent.getState())
                .type(StateEventType.TASK_STATE_CHANGE)
                .build();
        workflowExecuteThreadPool.submitStateEvent(stateEvent);

    }

    public void sendAckToWorker(TaskEvent taskEvent) {
        try {
            ITaskInstanceExecutionEventAckListener instanceExecutionEventAckListener =
                    SingletonJdkDynamicRpcClientProxyFactory
                            .getProxyClient(taskEvent.getWorkerAddress(), ITaskInstanceExecutionEventAckListener.class);
            instanceExecutionEventAckListener.handleTaskInstanceExecutionFinishEventAck(
                    TaskInstanceExecutionFinishEventAck.success(taskEvent.getTaskInstanceId()));
        } catch (Exception e) {
            // master ignore the exception, worker will retry to send this TaskEventType.RESULT event again.
            log.warn("send ack to worker error, taskInstanceId: {}", taskEvent.getTaskInstanceId(), e);
        }
    }

    @Override
    public TaskEventType getHandleEventType() {
        return TaskEventType.RESULT;
    }
}
