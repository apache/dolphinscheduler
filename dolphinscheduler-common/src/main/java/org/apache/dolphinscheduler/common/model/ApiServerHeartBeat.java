package org.apache.dolphinscheduler.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiServerHeartBeat implements HeartBeat {
    private int processId;
    private long startupTime;
    private long reportTime;
    private double cpuUsage;
    private double memoryUsage;

    private ApiServerConfigProperty apiServerConfigProperty;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApiServerConfigProperty {
        private String databaseUrl;
        private String apiServerAddress;
        private String apiServerRegistryPath;
    }

}
