package org.apache.dolphinscheduler.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerHeartBeat implements HeartBeat {

    private long startupTime;
    private long reportTime;
    private double cpuUsage;
    private double memoryUsage;
    private double loadAverage;
    private double availablePhysicalMemorySize;
    private double maxCpuloadAvg;
    private double reservedMemory;
    private int serverStatus;
    private int processId;

    private int workerHostWeight; // worker host weight
    private int workerWaitingTaskCount; // worker waiting task count
    private int workerExecThreadCount; // worker thread pool thread count


}
