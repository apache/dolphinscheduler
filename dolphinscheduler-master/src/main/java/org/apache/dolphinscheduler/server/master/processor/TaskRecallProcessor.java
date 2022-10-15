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
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskRejectCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEventService;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * task recall processor
 */
@Component
public class TaskRecallProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskRecallProcessor.class);

    @Autowired
    private TaskEventService taskEventService;

    /**
     * task ack process
     *
     * @param channel channel channel
     * @param command command TaskExecuteAckCommand
     */
    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_REJECT == command.getType(), String.format("invalid command type : %s", command.getType()));
        TaskRejectCommand recallCommand = JSONUtils.parseObject(command.getBody(), TaskRejectCommand.class);
        TaskEvent taskEvent = TaskEvent.newRecallEvent(recallCommand, channel);
        try {
            LoggerUtils.setWorkflowAndTaskInstanceIDMDC(recallCommand.getProcessInstanceId(), recallCommand.getTaskInstanceId());
            logger.info("Receive task recall command: {}", recallCommand);
            taskEventService.addEvent(taskEvent);
        } finally {
            LoggerUtils.removeWorkflowAndTaskInstanceIdMDC();
        }
    }
}
