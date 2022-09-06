package org.apache.dolphinscheduler.server.worker.task;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.model.BaseHeartBeatTask;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;
import org.apache.dolphinscheduler.server.worker.runner.AsyncTaskDelayQueue;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkerHeartBeatTask extends BaseHeartBeatTask<WorkerHeartBeat> {

    private final WorkerConfig workerConfig;

    private final DataSourceProperties dataSourceProperties;
    @Autowired
    private RegistryClient registryClient;
    @Autowired
    private WorkerManagerThread workerManagerThread;
    @Autowired
    private MessageRetryRunner messageRetryRunner;

    private final int processId;
    private final WorkerHeartBeat.WorkerConfigProperty workerConfigProperty;

    public WorkerHeartBeatTask(@NonNull WorkerConfig workerConfig,
                               @NonNull DataSourceProperties dataSourceProperties) {
        super("WorkerHeartBeatTask", workerConfig.getHeartbeatInterval().toMillis());
        this.workerConfig = workerConfig;
        this.dataSourceProperties = dataSourceProperties;

        this.workerConfigProperty = getWorkerConfigProperty();
        this.processId = OSUtils.getProcessID();
    }

    @Override
    public WorkerHeartBeat getHeartBeat() {
        double loadAverage = OSUtils.loadAverage();
        double cpuUsage = OSUtils.cpuUsage();
        double memoryUsage = OSUtils.memoryUsage();
        int maxCpuLoadAvg = workerConfig.getMaxCpuLoadAvg();
        double availablePhysicalMemorySize = OSUtils.availablePhysicalMemorySize();
        double reservedMemory = workerConfig.getReservedMemory();
        int execThreads = workerConfig.getExecThreads();
        int waitSubmitQueueSize = workerManagerThread.getWaitSubmitQueueSize();

        return WorkerHeartBeat.builder()
                .workerServerStatus(ServerLifeCycleManager.getServerStatus())
                .workerConfigProperty(workerConfigProperty)
                .workerMetricsProperty(getWorkerMetricsProperty())
                .processId(processId)
                .startupTime(ServerLifeCycleManager.getServerStartupTime())
                .reportTime(System.currentTimeMillis())
                .cpuUsage(cpuUsage)
                .memoryUsage(memoryUsage)
                .loadAverage(loadAverage)
                .availablePhysicalMemorySize(availablePhysicalMemorySize)
                .maxCpuloadAvg(maxCpuLoadAvg)
                .reservedMemory(reservedMemory)
                .serverStatus(getServerStatus(cpuUsage, maxCpuLoadAvg, availablePhysicalMemorySize, reservedMemory,
                        execThreads, waitSubmitQueueSize))
                .workerHostWeight(workerConfig.getHostWeight())
                .workerWaitingTaskCount(waitSubmitQueueSize)
                .workerExecThreadCount(execThreads)
                .build();
    }

    @Override
    public void writeHeartBeat(WorkerHeartBeat workerHeartBeat) {
        for (String workerGroupRegistryPath : workerConfig.getWorkerGroupRegistryPaths()) {
            String workerHeartBeatJson = JSONUtils.toJsonString(workerHeartBeat);
            registryClient.persistEphemeral(workerGroupRegistryPath, JSONUtils.toJsonString(workerHeartBeat));
            log.info("Worker write heart beat info success, heartBeatInfo: {}", workerHeartBeatJson);
        }
    }

    public int getServerStatus(double cpuUsage,
                               double maxCpuloadAvg,
                               double availablePhysicalMemorySize,
                               double reservedMemory,
                               int workerExecThreadCount,
                               int workerWaitingTaskCount) {
        if (cpuUsage > maxCpuloadAvg || availablePhysicalMemorySize < reservedMemory) {
            log.warn(
                    "current cpu load average {} is too high or available memory {}G is too low, under max.cpuload.avg={} and reserved.memory={}G",
                    cpuUsage, availablePhysicalMemorySize, maxCpuloadAvg, reservedMemory);
            return Constants.ABNORMAL_NODE_STATUS;
        } else if (workerWaitingTaskCount > workerExecThreadCount) {
            log.warn("current waiting task count {} is large than worker thread count {}, worker is busy",
                    workerWaitingTaskCount, workerExecThreadCount);
            return Constants.BUSY_NODE_STATUE;
        } else {
            return Constants.NORMAL_NODE_STATUS;
        }
    }

    private WorkerHeartBeat.WorkerConfigProperty getWorkerConfigProperty() {
        return WorkerHeartBeat.WorkerConfigProperty.builder()
                .databaseUrl(dataSourceProperties.getUrl())
                .execThreads(workerConfig.getExecThreads())
                .heartbeatInterval(workerConfig.getHeartbeatInterval().toMillis())
                .hostWeight(workerConfig.getHostWeight())
                .tenantAutoCreate(workerConfig.isTenantAutoCreate())
                .tenantDistributedUser(workerConfig.isTenantDistributedUser())
                .maxCpuLoadAvg(workerConfig.getMaxCpuLoadAvg())
                .reservedMemory(workerConfig.getReservedMemory())
                .groups(workerConfig.getGroups())
                .alertListenHost(workerConfig.getAlertListenHost())
                .alertListenPort(workerConfig.getAlertListenPort())
                .registryDisconnectStrategy(JSONUtils.toJsonString(workerConfig.getRegistryDisconnectStrategy()))
                .workerAddress(workerConfig.getWorkerAddress())
                .workerGroupRegistryPaths(workerConfig.getWorkerGroupRegistryPaths())
                .build();
    }

    private WorkerHeartBeat.WorkerMetricsProperty getWorkerMetricsProperty() {
        return WorkerHeartBeat.WorkerMetricsProperty.builder()
                .workerSyncRunningTaskNum(workerManagerThread.getThreadPoolRunningTaskNum())
                .workerSyncWaitingTaskNum(workerManagerThread.getThreadPoolWaitingTaskNum())
                .workerAsyncRunningTaskNum(AsyncTaskDelayQueue.getAsyncTaskRunningNum())
                .workerRetryMessageNum(messageRetryRunner.getRetryMessageSize())
                .build();
    }

}
