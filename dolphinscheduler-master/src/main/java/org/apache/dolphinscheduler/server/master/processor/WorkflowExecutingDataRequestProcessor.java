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
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.workflow.WorkflowExecutingDataRequest;
import org.apache.dolphinscheduler.remote.command.workflow.WorkflowExecutingDataResponse;
import org.apache.dolphinscheduler.remote.dto.WorkflowExecuteDto;
import org.apache.dolphinscheduler.remote.processor.MasterRpcProcessor;
import org.apache.dolphinscheduler.server.master.service.ExecutingService;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

/**
 * workflow executing data process from api/master
 */
@Component
@Slf4j
public class WorkflowExecutingDataRequestProcessor implements MasterRpcProcessor {

    @Autowired
    private ExecutingService executingService;

    @Override
    public void process(Channel channel, Message message) {
        WorkflowExecutingDataRequest requestCommand =
                JSONUtils.parseObject(message.getBody(), WorkflowExecutingDataRequest.class);

        log.info("received command, processInstanceId:{}", requestCommand.getProcessInstanceId());

        Optional<WorkflowExecuteDto> workflowExecuteDtoOptional =
                executingService.queryWorkflowExecutingData(requestCommand.getProcessInstanceId());

        WorkflowExecutingDataResponse responseCommand = new WorkflowExecutingDataResponse();
        workflowExecuteDtoOptional.ifPresent(responseCommand::setWorkflowExecuteDto);
        channel.writeAndFlush(responseCommand.convert2Command(message.getOpaque()));
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.WORKFLOW_EXECUTING_DATA_REQUEST;
    }
}
