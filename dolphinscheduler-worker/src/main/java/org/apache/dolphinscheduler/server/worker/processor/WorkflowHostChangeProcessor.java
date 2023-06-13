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
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.task.WorkflowHostChangeRequest;
import org.apache.dolphinscheduler.remote.command.task.WorkflowHostChangeResponse;
import org.apache.dolphinscheduler.remote.processor.WorkerRpcProcessor;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * update process host
 * this used when master failover
 */
@Component
public class WorkflowHostChangeProcessor implements WorkerRpcProcessor {

    private final Logger logger = LoggerFactory.getLogger(WorkflowHostChangeProcessor.class);

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Override
    public void process(Channel channel, Message message) {
        WorkflowHostChangeRequest workflowHostChangeRequest =
                JSONUtils.parseObject(message.getBody(), WorkflowHostChangeRequest.class);
        if (workflowHostChangeRequest == null) {
            logger.error("host update command is null");
            return;
        }
        logger.info("Received workflow host change command : {}", workflowHostChangeRequest);
        try (
                final LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                        LogUtils.setTaskInstanceIdMDC(workflowHostChangeRequest.getTaskInstanceId())) {
            WorkflowHostChangeResponse workflowHostChangeResponse;
            TaskExecutionContext taskExecutionContext =
                    TaskExecutionContextCacheManager.getByTaskInstanceId(workflowHostChangeRequest.getTaskInstanceId());
            if (taskExecutionContext != null) {
                taskExecutionContext.setWorkflowInstanceHost(workflowHostChangeRequest.getWorkflowHost());
                messageRetryRunner.updateMessageHost(workflowHostChangeRequest.getTaskInstanceId(),
                        workflowHostChangeRequest.getWorkflowHost());
                workflowHostChangeResponse = WorkflowHostChangeResponse.success();
                logger.info("Success update workflow host, taskInstanceId : {}, workflowHost: {}",
                        workflowHostChangeRequest.getTaskInstanceId(), workflowHostChangeRequest.getWorkflowHost());
            } else {
                workflowHostChangeResponse = WorkflowHostChangeResponse.failed();
                logger.error("Cannot find the taskExecutionContext, taskInstanceId : {}",
                        workflowHostChangeRequest.getTaskInstanceId());
            }
            channel.writeAndFlush(workflowHostChangeResponse.convert2Command(message.getOpaque())).addListener(
                    (ChannelFutureListener) channelFuture -> {
                        if (!channelFuture.isSuccess()) {
                            logger.error("send host update response failed");
                        }
                    });
        }
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.WORKFLOW_HOST_CHANGE_REQUEST;
    }

}
