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

package org.apache.dolphinscheduler.server.worker.rpc;

import org.apache.dolphinscheduler.extract.master.transportor.ITaskExecutionEvent.TaskInstanceExecutionEventType;
import org.apache.dolphinscheduler.extract.worker.ITaskInstanceExecutionEventAckListener;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutionFailedEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutionKilledEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutionPausedEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutionSuccessEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceExecutionDispatchedEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceExecutionRunningEventAck;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskInstanceExecutionEventAckListenerImpl implements ITaskInstanceExecutionEventAckListener {

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Override
    public void handleTaskInstanceDispatchedEventAck(TaskInstanceExecutionDispatchedEventAck taskInstanceExecutionDispatchedEventAck) {
        try {
            final int taskInstanceId = taskInstanceExecutionDispatchedEventAck.getTaskInstanceId();
            LogUtils.setTaskInstanceIdMDC(taskInstanceId);
            log.info("Receive TaskInstanceDispatchedEventAck: {}", taskInstanceExecutionDispatchedEventAck);
            if (taskInstanceExecutionDispatchedEventAck.isSuccess()) {
                messageRetryRunner.removeRetryMessage(taskInstanceId, TaskInstanceExecutionEventType.DISPATCH);
            } else {
                log.warn("TaskInstanceDispatchedEvent handle failed: {}", taskInstanceExecutionDispatchedEventAck);
            }
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
        }
    }

    @Override
    public void handleTaskInstanceExecutionRunningEventAck(TaskInstanceExecutionRunningEventAck taskInstanceExecutionRunningEventAck) {
        try {
            final int taskInstanceId = taskInstanceExecutionRunningEventAck.getTaskInstanceId();
            LogUtils.setTaskInstanceIdMDC(taskInstanceId);
            log.info("Receive TaskInstanceExecutionRunningEventAck: {}", taskInstanceExecutionRunningEventAck);
            if (taskInstanceExecutionRunningEventAck.isSuccess()) {
                messageRetryRunner.removeRetryMessage(taskInstanceId, TaskInstanceExecutionEventType.RUNNING);
            } else {
                log.warn("TaskInstanceExecutionRunningEvent handle failed: {}", taskInstanceExecutionRunningEventAck);
            }
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
        }
    }

    @Override
    public void handleTaskExecutionSuccessEventAck(TaskExecutionSuccessEventAck taskExecutionSuccessEventAck) {
        try {
            final int taskInstanceId = taskExecutionSuccessEventAck.getTaskInstanceId();
            LogUtils.setTaskInstanceIdMDC(taskInstanceId);
            log.info("Receive TaskExecutionSuccessEventAck: {}", taskExecutionSuccessEventAck);
            if (taskExecutionSuccessEventAck.isSuccess()) {
                messageRetryRunner.removeRetryMessage(taskInstanceId, TaskInstanceExecutionEventType.SUCCESS);
            } else {
                log.warn("TaskExecutionSuccessEvent handle failed: {}", taskExecutionSuccessEventAck);
            }
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
        }
    }

    @Override
    public void handleTaskExecutionPausedEventAck(TaskExecutionPausedEventAck taskExecutionPausedEventAck) {
        try {
            final int taskInstanceId = taskExecutionPausedEventAck.getTaskInstanceId();
            LogUtils.setTaskInstanceIdMDC(taskInstanceId);
            log.info("Receive TaskExecutionPausedEventAck: {}", taskExecutionPausedEventAck);
            if (taskExecutionPausedEventAck.isSuccess()) {
                messageRetryRunner.removeRetryMessage(taskInstanceId, TaskInstanceExecutionEventType.PAUSED);
            } else {
                log.warn("TaskExecutionPausedEvent handle failed: {}", taskExecutionPausedEventAck);
            }
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
        }
    }

    @Override
    public void handleTaskExecutionFailedEventAck(TaskExecutionFailedEventAck taskExecutionFailedEventAck) {
        try {
            final int taskInstanceId = taskExecutionFailedEventAck.getTaskInstanceId();
            LogUtils.setTaskInstanceIdMDC(taskInstanceId);
            log.info("Receive TaskExecutionFailedEventAck: {}", taskExecutionFailedEventAck);
            if (taskExecutionFailedEventAck.isSuccess()) {
                messageRetryRunner.removeRetryMessage(taskInstanceId, TaskInstanceExecutionEventType.FAILED);
            } else {
                log.warn("TaskExecutionFailedEvent handle failed: {}", taskExecutionFailedEventAck);
            }
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
        }
    }

    @Override
    public void handleTaskExecutionKilledEventAck(TaskExecutionKilledEventAck taskExecutionKilledEventAck) {
        try {
            final int taskInstanceId = taskExecutionKilledEventAck.getTaskInstanceId();
            LogUtils.setTaskInstanceIdMDC(taskInstanceId);
            log.info("Receive TaskExecutionKilledEventAck: {}", taskExecutionKilledEventAck);
            if (taskExecutionKilledEventAck.isSuccess()) {
                messageRetryRunner.removeRetryMessage(taskInstanceId, TaskInstanceExecutionEventType.KILLED);
            } else {
                log.warn("TaskExecutionKilledEvent handle failed: {}", taskExecutionKilledEventAck);
            }
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
        }
    }

}
