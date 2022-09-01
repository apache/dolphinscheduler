package org.apache.dolphinscheduler.alert.task;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.alert.AlertConfig;
import org.apache.dolphinscheduler.alert.AlertServerMetrics;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.model.AlertServerHeartBeat;
import org.apache.dolphinscheduler.common.model.BaseHeartBeatTask;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AlertHeartbeatTask extends BaseHeartBeatTask<AlertServerHeartBeat> {

    private final AlertConfig alertConfig;

    @Autowired
    private RegistryClient registryClient;

    private final DataSourceProperties dataSourceProperties;

    private final AlertServerHeartBeat.AlertConfigProperty alertConfigProperty;

    private final int processId;

    public AlertHeartbeatTask(@NonNull AlertConfig alertConfig,
                              DataSourceProperties dataSourceProperties) {
        super("AlertHeartbeatTask", alertConfig.getHeartbeatInterval().toMillis());
        this.alertConfig = alertConfig;
        this.dataSourceProperties = dataSourceProperties;
        alertConfigProperty = getAlertServerConfigProperty();
        processId = OSUtils.getProcessID();
    }

    @Override
    public AlertServerHeartBeat getHeartBeat() {
        return AlertServerHeartBeat.builder()
                .processId(processId)
                .startupTime(ServerLifeCycleManager.getServerStartupTime())
                .reportTime(System.currentTimeMillis())
                .cpuUsage(OSUtils.cpuUsage())
                .memoryUsage(OSUtils.memoryUsage())
                .alertConfigProperty(alertConfigProperty)
                .alertMetricsProperty(getAlertServerMetricsProperty())
                .build();
    }

    @Override
    public void writeHeartBeat(AlertServerHeartBeat heartBeat) {
        String heartBeatJson = JSONUtils.toJsonString(heartBeat);
        registryClient.persistEphemeral(alertConfig.getAlertServerRegistryPath(), heartBeatJson);
        log.info("AlertServer write heart beat success, heartBeatInfo: {}", heartBeatJson);
    }

    private AlertServerHeartBeat.AlertMetricsProperty getAlertServerMetricsProperty() {
        return AlertServerHeartBeat.AlertMetricsProperty.builder()
                .sendSuccessNum(AlertServerMetrics.getAlertSuccessCount())
                .sendFailedNum(AlertServerMetrics.getAlertFailCount())
                .build();
    }

    private AlertServerHeartBeat.AlertConfigProperty getAlertServerConfigProperty() {
        return AlertServerHeartBeat.AlertConfigProperty.builder()
                .databaseUrl(dataSourceProperties.getUrl())
                .listenPort(alertConfig.getListenPort())
                .waitTimeout(alertConfig.getWaitTimeout())
                .heartbeatInterval(alertConfig.getHeartbeatInterval().toMillis())
                .alertServerAddress(alertConfig.getAlertServerAddress())
                .alertServerRegistryPath(alertConfig.getAlertServerRegistryPath())
                .build();
    }
}
