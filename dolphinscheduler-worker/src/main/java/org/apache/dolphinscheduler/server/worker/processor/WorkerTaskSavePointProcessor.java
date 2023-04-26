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

package org.apache.dolphinscheduler.server.worker.processor;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.stream.StreamTask;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.task.TaskSavePointRequest;
import org.apache.dolphinscheduler.remote.command.task.TaskSavePointResponse;
import org.apache.dolphinscheduler.remote.processor.WorkerRpcProcessor;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecuteRunnable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * task save point processor
 */
@Component
@Slf4j
public class WorkerTaskSavePointProcessor implements WorkerRpcProcessor {

    /**
     * task execute manager
     */
    @Autowired
    private WorkerManagerThread workerManager;

    /**
     * task save point process
     *
     * @param channel channel channel
     * @param message command command
     */
    @Override
    public void process(Channel channel, Message message) {
        TaskSavePointRequest taskSavePointRequest =
                JSONUtils.parseObject(message.getBody(), TaskSavePointRequest.class);
        if (taskSavePointRequest == null) {
            log.error("task savepoint request command is null");
            return;
        }
        log.info("Receive task savepoint command : {}", taskSavePointRequest);

        int taskInstanceId = taskSavePointRequest.getTaskInstanceId();
        TaskExecutionContext taskExecutionContext =
                TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId);
        if (taskExecutionContext == null) {
            log.error("taskRequest cache is null, taskInstanceId: {}",
                    taskSavePointRequest.getTaskInstanceId());
            return;
        }

        try {
            LogUtils.setTaskInstanceIdMDC(taskInstanceId);
            doSavePoint(taskInstanceId);

            sendTaskSavePointResponseCommand(channel, taskExecutionContext);
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
        }
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.TASK_SAVEPOINT_REQUEST;
    }

    private void sendTaskSavePointResponseCommand(Channel channel, TaskExecutionContext taskExecutionContext) {
        TaskSavePointResponse taskSavePointResponse = new TaskSavePointResponse();
        taskSavePointResponse.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        channel.writeAndFlush(taskSavePointResponse.convert2Command()).addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    log.error("Submit kill response to master error, kill command: {}",
                            taskSavePointResponse);
                } else
                    log.info("Submit kill response to master success, kill command: {}",
                            taskSavePointResponse);
            }
        });
    }

    protected void doSavePoint(int taskInstanceId) {
        WorkerTaskExecuteRunnable workerTaskExecuteRunnable = workerManager.getTaskExecuteThread(taskInstanceId);
        if (workerTaskExecuteRunnable == null) {
            log.warn("taskExecuteThread not found, taskInstanceId:{}", taskInstanceId);
            return;
        }
        AbstractTask task = workerTaskExecuteRunnable.getTask();
        if (task == null) {
            log.warn("task not found, taskInstanceId:{}", taskInstanceId);
            return;
        }
        if (!(task instanceof StreamTask)) {
            log.warn("task is not stream task");
            return;
        }
        try {
            ((StreamTask) task).savePoint();
        } catch (Exception e) {
            log.error("task save point error", e);
        }
    }
}
