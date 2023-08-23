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

package org.apache.dolphinscheduler.server.worker.runner.listener;

import org.apache.dolphinscheduler.extract.master.transportor.ITaskInstanceExecutionEvent;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceExecutionRunningEventAck;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskInstanceExecutionRunningEventAckListenFunction
        implements
            ITaskInstanceExecutionEventAckListenFunction<TaskInstanceExecutionRunningEventAck> {

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Override
    public void handleTaskInstanceExecutionEventAck(TaskInstanceExecutionRunningEventAck taskInstanceExecutionRunningEventAck) {
        try {
            final int taskInstanceId = taskInstanceExecutionRunningEventAck.getTaskInstanceId();
            LogUtils.setTaskInstanceIdMDC(taskInstanceId);
            log.info("Receive TaskInstanceExecutionRunningEventAck: {}", taskInstanceExecutionRunningEventAck);
            if (taskInstanceExecutionRunningEventAck.isSuccess()) {
                messageRetryRunner.removeRetryMessage(taskInstanceId,
                        ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType.RUNNING);
            } else {
                log.warn("TaskInstanceExecutionRunningEventAck failed: {}", taskInstanceExecutionRunningEventAck);
            }
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
        }
    }
}
