package org.apache.dolphinscheduler.api.task;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.model.ApiServerHeartBeat;
import org.apache.dolphinscheduler.common.model.BaseHeartBeatTask;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class ApiServerHeartbeatTask extends BaseHeartBeatTask<ApiServerHeartBeat> {

    @Autowired
    private RegistryClient registryClient;

    private final DataSourceProperties dataSourceProperties;
    private final ServerProperties serverProperties;
    private final int processId;
    private final ApiServerHeartBeat.ApiServerConfigProperty apiServerConfigProperty;

    public ApiServerHeartbeatTask(@NonNull DataSourceProperties dataSourceProperties,
                                  @NonNull ServerProperties serverProperties) {
        super("ApiServerHeartbeatTask", Duration.ofSeconds(60).toMillis());
        this.dataSourceProperties = dataSourceProperties;
        this.serverProperties = serverProperties;
        this.processId = OSUtils.getProcessID();
        this.apiServerConfigProperty = getApiServerConfigProperty();
    }

    @Override
    public ApiServerHeartBeat getHeartBeat() {
        return ApiServerHeartBeat.builder()
                .processId(processId)
                .startupTime(ServerLifeCycleManager.getServerStartupTime())
                .reportTime(System.currentTimeMillis())
                .cpuUsage(OSUtils.cpuUsage())
                .memoryUsage(OSUtils.memoryUsage())
                .apiServerConfigProperty(apiServerConfigProperty)
                .build();
    }

    @Override
    public void writeHeartBeat(ApiServerHeartBeat heartBeat) {
        String heartbeatJson = JSONUtils.toJsonString(heartBeat);
        registryClient.persistEphemeral(apiServerConfigProperty.getApiServerRegistryPath(), heartbeatJson);
        log.info("ApiServer write heart beat success, heartBeatInfo: {}", heartbeatJson);
    }

    private ApiServerHeartBeat.ApiServerConfigProperty getApiServerConfigProperty() {
        String apiServerAddress = NetUtils.getAddr(serverProperties.getPort());
        return ApiServerHeartBeat.ApiServerConfigProperty.builder()
                .databaseUrl(dataSourceProperties.getUrl())
                .apiServerAddress(apiServerAddress)
                .apiServerRegistryPath(NodeType.API_SERVER.getRegistryPath() + "/" + apiServerAddress)
                .build();
    }

}
