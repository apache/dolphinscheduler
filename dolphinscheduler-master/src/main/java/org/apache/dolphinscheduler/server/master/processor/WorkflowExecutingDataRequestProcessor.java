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
import org.apache.dolphinscheduler.remote.command.WorkflowExecutingDataRequestCommand;
import org.apache.dolphinscheduler.remote.command.WorkflowExecutingDataResponseCommand;
import org.apache.dolphinscheduler.remote.dto.WorkflowExecuteDto;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.master.service.ExecutingService;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * workflow executing data process from api/master
 */
@Component
public class WorkflowExecutingDataRequestProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(WorkflowExecutingDataRequestProcessor.class);

    @Autowired
    private ExecutingService executingService;

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.WORKFLOW_EXECUTING_DATA_REQUEST == command.getType(), String.format("invalid command type: %s", command.getType()));

        WorkflowExecutingDataRequestCommand requestCommand = JSONUtils.parseObject(command.getBody(), WorkflowExecutingDataRequestCommand.class);

        logger.info("received command, processInstanceId:{}", requestCommand.getProcessInstanceId());

        Optional<WorkflowExecuteDto> workflowExecuteDtoOptional = executingService.queryWorkflowExecutingData(requestCommand.getProcessInstanceId());

        WorkflowExecutingDataResponseCommand responseCommand = new WorkflowExecutingDataResponseCommand();
        workflowExecuteDtoOptional.ifPresent(responseCommand::setWorkflowExecuteDto);
        channel.writeAndFlush(responseCommand.convert2Command(command.getOpaque()));
    }
}
