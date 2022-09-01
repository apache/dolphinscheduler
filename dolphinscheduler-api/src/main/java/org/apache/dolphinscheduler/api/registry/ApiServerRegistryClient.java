package org.apache.dolphinscheduler.api.registry;

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.api.task.ApiServerHeartbeatTask;
import org.apache.dolphinscheduler.common.model.ApiServerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApiServerRegistryClient implements AutoCloseable {

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private ApiServerHeartbeatTask apiServerHeartbeatTask;

    public synchronized void start() {
        registry();
    }

    private void registry() {
        ApiServerHeartBeat heartBeat = apiServerHeartbeatTask.getHeartBeat();
        String apiServerAddress = heartBeat.getApiServerConfigProperty().getApiServerAddress();
        String apiServerRegistryPath = heartBeat.getApiServerConfigProperty().getApiServerRegistryPath();
        registryClient.persistEphemeral(apiServerRegistryPath, JSONUtils.toJsonString(heartBeat));
        log.info("ApiServer: {} registered to registry: {}", apiServerAddress, apiServerRegistryPath);
        apiServerHeartbeatTask.start();
    }

    @Override
    public void close() throws Exception {
        registryClient.close();
        log.info("Closed registry client");
        apiServerHeartbeatTask.shutdown();
        log.info("Closed apiServerHeartbeatTask");
    }
}
