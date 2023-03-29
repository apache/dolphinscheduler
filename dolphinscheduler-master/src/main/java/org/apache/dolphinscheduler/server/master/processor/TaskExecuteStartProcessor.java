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
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.task.TaskExecuteStartMessage;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.master.runner.StreamTaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.StreamTaskExecuteThreadPool;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

/**
 * task execute start processor, from api to master
 */
@Component
@Slf4j
public class TaskExecuteStartProcessor implements NettyRequestProcessor {

    @Autowired
    private StreamTaskExecuteThreadPool streamTaskExecuteThreadPool;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Override
    public void process(Channel channel, Command command) {
        TaskExecuteStartMessage taskExecuteStartMessage =
                JSONUtils.parseObject(command.getBody(), TaskExecuteStartMessage.class);
        log.info("taskExecuteStartCommand: {}", taskExecuteStartMessage);

        TaskDefinition taskDefinition = taskDefinitionDao.findTaskDefinition(
                taskExecuteStartMessage.getTaskDefinitionCode(), taskExecuteStartMessage.getTaskDefinitionVersion());
        if (taskDefinition == null) {
            log.error("Task definition can not be found, taskDefinitionCode:{}, taskDefinitionVersion:{}",
                    taskExecuteStartMessage.getTaskDefinitionCode(),
                    taskExecuteStartMessage.getTaskDefinitionVersion());
            return;
        }
        streamTaskExecuteThreadPool.execute(new StreamTaskExecuteRunnable(taskDefinition, taskExecuteStartMessage));

        // response
        Command response = new Command(command.getOpaque());
        response.setType(CommandType.TASK_EXECUTE_START);
        response.setBody(new byte[0]);
        channel.writeAndFlush(response);
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.TASK_EXECUTE_START;
    }

}
