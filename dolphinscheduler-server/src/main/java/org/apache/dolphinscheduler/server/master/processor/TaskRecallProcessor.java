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

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskRecallCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskResponseEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskResponseService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * task recall processor
 */
public class TaskRecallProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskRecallProcessor.class);

    /**
     * process service
     */
    private final TaskResponseService taskResponseService;

    public TaskRecallProcessor() {
        this.taskResponseService = SpringApplicationContext.getBean(TaskResponseService.class);
    }

    /**
     * task ack process
     *
     * @param channel channel channel
     * @param command command TaskExecuteAckCommand
     */
    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_RECALL == command.getType(), String.format("invalid command type : %s", command.getType()));
        TaskRecallCommand recallCommand = JSONUtils.parseObject(command.getBody(), TaskRecallCommand.class);
        logger.info("taskRecallCommand: {}, opaque: {}", recallCommand, command.getOpaque());
        // TaskResponseEvent
        TaskResponseEvent taskResponseEvent = TaskResponseEvent.newRecall(ExecutionStatus.of(recallCommand.getStatus()), recallCommand.getEvent(),
                recallCommand.getTaskInstanceId(), recallCommand.getProcessInstanceId(), channel, command.getOpaque());
        taskResponseService.addResponse(taskResponseEvent);
    }
}
