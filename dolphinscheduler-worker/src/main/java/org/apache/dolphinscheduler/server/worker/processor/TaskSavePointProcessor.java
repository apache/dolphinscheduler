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
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskSavePointRequestCommand;
import org.apache.dolphinscheduler.remote.command.TaskSavePointResponseCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecuteRunnable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * task save point processor
 */
@Component
@Slf4j
public class TaskSavePointProcessor implements NettyRequestProcessor {

    /**
     * task execute manager
     */
    @Autowired
    private WorkerManagerThread workerManager;

    /**
     * task save point process
     *
     * @param channel channel channel
     * @param command command command
     */
    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_SAVEPOINT_REQUEST == command.getType(),
                String.format("invalid command type : %s", command.getType()));
        TaskSavePointRequestCommand taskSavePointRequestCommand =
                JSONUtils.parseObject(command.getBody(), TaskSavePointRequestCommand.class);
        if (taskSavePointRequestCommand == null) {
            log.error("task savepoint request command is null");
            return;
        }
        log.info("Receive task savepoint command : {}", taskSavePointRequestCommand);

        int taskInstanceId = taskSavePointRequestCommand.getTaskInstanceId();
        TaskExecutionContext taskExecutionContext =
                TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId);
        if (taskExecutionContext == null) {
            log.error("taskRequest cache is null, taskInstanceId: {}",
                    taskSavePointRequestCommand.getTaskInstanceId());
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

    private void sendTaskSavePointResponseCommand(Channel channel, TaskExecutionContext taskExecutionContext) {
        TaskSavePointResponseCommand taskSavePointResponseCommand = new TaskSavePointResponseCommand();
        taskSavePointResponseCommand.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        channel.writeAndFlush(taskSavePointResponseCommand.convert2Command()).addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    log.error("Submit kill response to master error, kill command: {}",
                            taskSavePointResponseCommand);
                } else
                    log.info("Submit kill response to master success, kill command: {}",
                            taskSavePointResponseCommand);
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
