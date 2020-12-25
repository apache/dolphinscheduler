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

import io.netty.channel.Channel;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.Preconditions;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.ChannelUtils;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;
import org.apache.dolphinscheduler.server.master.cache.TaskInstanceCacheManager;
import org.apache.dolphinscheduler.server.master.cache.impl.TaskInstanceCacheManagerImpl;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskResponseEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskResponseService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *  task ack processor
 */
public class TaskAckProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskAckProcessor.class);

    /**
     * process service
     */
    private final TaskResponseService taskResponseService;

    /**
     * taskInstance cache manager
     */
    private final TaskInstanceCacheManager taskInstanceCacheManager;

    public TaskAckProcessor(){
        this.taskResponseService = SpringApplicationContext.getBean(TaskResponseService.class);
        this.taskInstanceCacheManager = SpringApplicationContext.getBean(TaskInstanceCacheManagerImpl.class);
    }

    /**
     * task ack process
     * @param channel channel channel
     * @param command command TaskExecuteAckCommand
     */
    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_EXECUTE_ACK == command.getType(), String.format("invalid command type : %s", command.getType()));
        TaskExecuteAckCommand taskAckCommand = FastJsonSerializer.deserialize(command.getBody(), TaskExecuteAckCommand.class);
        logger.info("taskAckCommand : {}", taskAckCommand);

        taskInstanceCacheManager.cacheTaskInstance(taskAckCommand);

        String workerAddress = ChannelUtils.toAddress(channel).getAddress();

        ExecutionStatus ackStatus = ExecutionStatus.of(taskAckCommand.getStatus());

        // TaskResponseEvent
        TaskResponseEvent taskResponseEvent = TaskResponseEvent.newAck(ackStatus,
                taskAckCommand.getStartTime(),
                workerAddress,
                taskAckCommand.getExecutePath(),
                taskAckCommand.getLogPath(),
                taskAckCommand.getTaskInstanceId(),
                channel);

        taskResponseService.addResponse(taskResponseEvent);
    }

}
