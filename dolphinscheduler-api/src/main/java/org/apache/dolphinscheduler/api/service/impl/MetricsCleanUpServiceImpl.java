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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.metrics.ApiServerMetrics;
import org.apache.dolphinscheduler.api.rpc.ApiRpcClient;
import org.apache.dolphinscheduler.api.service.MetricsCleanUpService;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.remote.command.workflow.WorkflowMetricsCleanUpRequest;
import org.apache.dolphinscheduler.remote.utils.Host;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MetricsCleanUpServiceImpl implements MetricsCleanUpService {

    @Autowired
    private ApiRpcClient apiRpcClient;

    @Autowired
    private RegistryClient registryClient;

    @Override
    public void cleanUpWorkflowMetricsByDefinitionCode(String workflowDefinitionCode) {
        WorkflowMetricsCleanUpRequest workflowMetricsCleanUpRequest = new WorkflowMetricsCleanUpRequest();
        workflowMetricsCleanUpRequest.setProcessDefinitionCode(workflowDefinitionCode);
        List<Server> masterNodeList = registryClient.getServerList(RegistryNodeType.MASTER);
        for (Server server : masterNodeList) {
            try {
                final String host = String.format("%s:%s", server.getHost(), server.getPort());
                apiRpcClient.send(Host.of(host), workflowMetricsCleanUpRequest.convert2Command());
            } catch (Exception e) {
                log.error(
                        "Fail to clean up workflow related metrics on {} when deleting workflow definition {}, error message {}",
                        server.getHost(), workflowDefinitionCode, e.getMessage());
            }
        }
    }

    @Override
    public void cleanUpApiResponseTimeMetricsByUserId(int userId) {
        ApiServerMetrics.cleanUpApiResponseTimeMetricsByUserId(userId);
    }
}
