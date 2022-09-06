package org.apache.dolphinscheduler.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dolphinscheduler.common.lifecycle.ServerStatus;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerHeartBeat implements HeartBeat {

    private ServerStatus workerServerStatus;
    private WorkerConfigProperty workerConfigProperty;
    private WorkerMetricsProperty workerMetricsProperty;

    // todo: the below property is used in old ui, we may need to refactor these;
    private int processId;
    private long startupTime;
    private long reportTime;
    private double cpuUsage;
    private double memoryUsage;
    private double loadAverage;
    private double availablePhysicalMemorySize;
    private double maxCpuloadAvg;
    private double reservedMemory;
    private int serverStatus;

    private int workerHostWeight; // worker host weight
    private int workerWaitingTaskCount; // worker waiting task count
    private int workerExecThreadCount; // worker thread pool thread count

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkerConfigProperty {

        private String databaseUrl;
        private int execThreads;
        private long heartbeatInterval;
        private int hostWeight;
        private boolean tenantAutoCreate;
        private boolean tenantDistributedUser;
        private int maxCpuLoadAvg;
        private double reservedMemory;
        private Set<String> groups;
        private String alertListenHost;
        private int alertListenPort;
        private String registryDisconnectStrategy;
        private String workerAddress;
        private Set<String> workerGroupRegistryPaths;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkerMetricsProperty {

        private int workerSyncRunningTaskNum;
        private long workerSyncWaitingTaskNum;
        private int workerAsyncRunningTaskNum;
        private int workerRetryMessageNum;
    }

}
