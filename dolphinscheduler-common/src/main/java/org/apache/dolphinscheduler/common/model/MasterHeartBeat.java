package org.apache.dolphinscheduler.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dolphinscheduler.common.lifecycle.ServerStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterHeartBeat implements HeartBeat {

    private ServerStatus masterServerStatus;
    private MasterConfigProperty masterConfigProperty;
    private MasterMetricsProperty masterMetricsProperty;

    // todo: The below property is used in old ui, we may need to refactor these.
    private int processId;
    private long startupTime;
    private long reportTime;
    private double cpuUsage;
    private double memoryUsage;
    private double loadAverage;
    private double availablePhysicalMemorySize;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MasterConfigProperty {
        private String databaseUrl;
        private int fetchCommandNum;
        private int preExecThreads;
        private int execThreads;
        private int dispatchTaskNum;
        private String hostSelect;
        private long heartbeatInterval;
        private int taskCommitRetryTimes;
        private long taskCommitInterval;
        private long stateWheelInterval;
        private double maxCpuloadAvg;
        private double reservedMemory;
        private long failoverInterval;
        private boolean killYarnJobWhenTaskFailover;
        private String registryDisconnectStrategy;
        private String masterAddress;
        private String masterRegistryPath;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MasterMetricsProperty {
        private int slot;
        private int totalMasterCount;
        private int workflowEventQueueWaitingEventNum;
        private int runningWorkflowNum;
        private int workflowExecuteThreadPoolRunningThreadNum;
        private long workflowExecuteThreadPoolWaitingJobNum;
        private int taskExecuteThreadPoolRunningThreadNum;
        private long taskExecuteThreadPoolWaitingJobNum;

    }
}
