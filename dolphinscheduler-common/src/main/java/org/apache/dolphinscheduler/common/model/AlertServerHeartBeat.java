package org.apache.dolphinscheduler.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlertServerHeartBeat implements HeartBeat {

    private int processId;
    private long startupTime;
    private long reportTime;
    private double cpuUsage;
    private double memoryUsage;

    private AlertConfigProperty alertConfigProperty;
    private AlertMetricsProperty alertMetricsProperty;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlertConfigProperty {

        private String databaseUrl;
        private int listenPort;
        private int waitTimeout;
        private long heartbeatInterval;
        private String alertServerAddress;
        private String alertServerRegistryPath;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlertMetricsProperty {

        private double sendSuccessNum;
        private double sendFailedNum;
    }
}
