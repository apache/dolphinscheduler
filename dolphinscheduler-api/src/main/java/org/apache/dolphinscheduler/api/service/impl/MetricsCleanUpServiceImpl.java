package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.rpc.ApiRpcClient;
import org.apache.dolphinscheduler.api.service.MetricsCleanUpService;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.remote.command.WorkflowMetricsCleanUpCommand;
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
        WorkflowMetricsCleanUpCommand workflowMetricsCleanUpCommand = new WorkflowMetricsCleanUpCommand();
        workflowMetricsCleanUpCommand.setProcessDefinitionCode(workflowDefinitionCode);
        List<Server> masterNodeList = registryClient.getServerList(NodeType.MASTER);
        for (Server server : masterNodeList) {
            try {
                final String host = String.format("%s:%s", server.getHost(), server.getPort());
                apiRpcClient.send(Host.of(host), workflowMetricsCleanUpCommand.convert2Command());
            } catch (Exception e) {
                log.error(
                        "Fail to clean up workflow related metrics on {} when deleting workflow definition {}, error message {}",
                        server.getHost(), workflowDefinitionCode, e.getMessage());
            }
        }
    }

}
