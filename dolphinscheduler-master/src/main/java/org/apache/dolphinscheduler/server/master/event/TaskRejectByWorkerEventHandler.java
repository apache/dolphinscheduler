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

import org.apache.dolphinscheduler.common.enums.TaskEventType;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.TaskRejectAckCommand;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEvent;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskRejectByWorkerEventHandler implements TaskEventHandler {

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private MasterConfig masterConfig;

    @Override
    public void handleTaskEvent(TaskEvent taskEvent) throws TaskEventHandleError {
        int taskInstanceId = taskEvent.getTaskInstanceId();
        int processInstanceId = taskEvent.getProcessInstanceId();

        WorkflowExecuteRunnable workflowExecuteRunnable = this.processInstanceExecCacheManager.getByProcessInstanceId(
                processInstanceId);
        if (workflowExecuteRunnable == null) {
            sendAckToWorker(taskEvent);
            throw new TaskEventHandleError(
                    "Handle task reject event error, cannot find related workflow instance from cache, will discard this event");
        }
        TaskInstance taskInstance = workflowExecuteRunnable.getTaskInstance(taskInstanceId).orElseThrow(() -> {
            sendAckToWorker(taskEvent);
            return new TaskEventHandleError(
                    "Handle task reject event error, cannot find the taskInstance from cache, will discord this event");
        });
        try {
            // todo: If the worker submit multiple reject response to master, the task instance may be dispatch
            // multiple,
            // we need to control the worker overload by master rather than worker
            // if the task resubmit and the worker failover, this task may be dispatch twice?
            // todo: we need to clear the taskInstance host and rollback the status to submit.
            workflowExecuteRunnable.resubmit(taskInstance.getTaskCode());
            sendAckToWorker(taskEvent);
        } catch (Exception ex) {
            throw new TaskEventHandleError("Handle task reject event error", ex);
        }

    }

    public void sendAckToWorker(TaskEvent taskEvent) {
        TaskRejectAckCommand taskRejectAckMessage = new TaskRejectAckCommand(true,
                taskEvent.getTaskInstanceId(),
                masterConfig.getMasterAddress(),
                taskEvent.getWorkerAddress(),
                System.currentTimeMillis());
        taskEvent.getChannel().writeAndFlush(taskRejectAckMessage.convert2Command());
    }

    @Override
    public TaskEventType getHandleEventType() {
        return TaskEventType.WORKER_REJECT;
    }
}
