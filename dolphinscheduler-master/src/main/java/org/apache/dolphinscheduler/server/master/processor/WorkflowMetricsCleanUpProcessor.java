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
import org.apache.dolphinscheduler.remote.command.workflow.WorkflowMetricsCleanUpRequest;
import org.apache.dolphinscheduler.remote.processor.MasterRpcProcessor;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;

import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

@Component
public class WorkflowMetricsCleanUpProcessor implements MasterRpcProcessor {

    @Override
    public void process(Channel channel, Message message) {
        WorkflowMetricsCleanUpRequest workflowMetricsCleanUpRequest =
                JSONUtils.parseObject(message.getBody(), WorkflowMetricsCleanUpRequest.class);

        ProcessInstanceMetrics.cleanUpProcessInstanceCountMetricsByDefinitionCode(
                workflowMetricsCleanUpRequest.getProcessDefinitionCode());
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.WORKFLOW_METRICS_CLEANUP;
    }

}
