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

package org.apache.dolphinscheduler.server.master.processor;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.task.TaskKillRequest;
import org.apache.dolphinscheduler.remote.processor.MasterRpcProcessor;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.MasterDelayTaskExecuteRunnableDelayQueue;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecuteRunnableHolder;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecutionContextHolder;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

@Slf4j
@Component
public class MasterTaskKillProcessor implements MasterRpcProcessor {

    @Autowired
    private MasterDelayTaskExecuteRunnableDelayQueue masterDelayTaskExecuteRunnableDelayQueue;

    @Override
    public void process(Channel channel, Message message) {
        TaskKillRequest taskKillRequest = JSONUtils.parseObject(message.getBody(), TaskKillRequest.class);
        log.info("Master receive task kill request: {}", taskKillRequest);
        int taskInstanceId = taskKillRequest.getTaskInstanceId();
        try (LogUtils.MDCAutoClosableContext mdcAutoClosableContext = LogUtils.setTaskInstanceIdMDC(taskInstanceId)) {
            TaskExecutionContext taskExecutionContext =
                    MasterTaskExecutionContextHolder.getTaskExecutionContext(taskInstanceId);
            if (taskExecutionContext == null) {
                log.error("Cannot find the TaskExecutionContext, this task may already been killed");
                return;
            }
            MasterTaskExecuteRunnable masterTaskExecuteRunnable =
                    MasterTaskExecuteRunnableHolder.getMasterTaskExecuteRunnable(taskInstanceId);
            if (masterTaskExecuteRunnable == null) {
                log.error("Cannot find the MasterTaskExecuteRunnable, this task may already been killed");
                return;
            }
            try {
                masterTaskExecuteRunnable.cancelTask();
                masterDelayTaskExecuteRunnableDelayQueue
                        .removeMasterDelayTaskExecuteRunnable(masterTaskExecuteRunnable);
            } catch (MasterTaskExecuteException e) {
                log.error("Cancel MasterTaskExecuteRunnable failed ", e);
            } finally {
                MasterTaskExecutionContextHolder.removeTaskExecutionContext(taskInstanceId);
                MasterTaskExecuteRunnableHolder.removeMasterTaskExecuteRunnable(taskInstanceId);
            }
        }
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.TASK_KILL_REQUEST;
    }

}
