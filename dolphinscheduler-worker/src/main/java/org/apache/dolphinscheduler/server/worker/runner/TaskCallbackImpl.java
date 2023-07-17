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

package org.apache.dolphinscheduler.server.worker.runner;

import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.model.ApplicationInfo;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class TaskCallbackImpl implements TaskCallBack {

    private final WorkerMessageSender workerMessageSender;

    private final TaskExecutionContext taskExecutionContext;

    public TaskCallbackImpl(WorkerMessageSender workerMessageSender, TaskExecutionContext taskExecutionContext) {
        this.workerMessageSender = workerMessageSender;
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void updateRemoteApplicationInfo(int taskInstanceId, ApplicationInfo applicationInfo) {
        TaskExecutionContext taskExecutionContext =
                TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId);
        if (taskExecutionContext == null) {
            log.error("task execution context is empty, taskInstanceId: {}, applicationInfo:{}", taskInstanceId,
                    applicationInfo);
            return;
        }

        log.info("send remote application info {}", applicationInfo);
        taskExecutionContext.setAppIds(applicationInfo.getAppIds());
        workerMessageSender.sendMessageWithRetry(taskExecutionContext, MessageType.TASK_EXECUTE_RUNNING_MESSAGE);
    }

    @Override
    public void updateTaskInstanceInfo(int taskInstanceId) {
        TaskExecutionContext taskExecutionContext =
                TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId);
        if (taskExecutionContext == null) {
            log.error("task execution context is empty, taskInstanceId: {}", taskInstanceId);
            return;
        }

        workerMessageSender.sendMessageWithRetry(taskExecutionContext, MessageType.TASK_UPDATE_RUNTIME_MESSAGE);
    }

}
