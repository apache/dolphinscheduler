package org.apache.dolphinscheduler.server.worker.task;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.model.BaseHeartBeatTask;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.util.function.Supplier;

@Slf4j
public class WorkerHeartBeatTask extends BaseHeartBeatTask<WorkerHeartBeat> {

    private final WorkerConfig workerConfig;
    private final RegistryClient registryClient;

    private final Supplier<Integer> workerWaitingTaskCount;

    private final int processId;

    public WorkerHeartBeatTask(@NonNull WorkerConfig workerConfig,
                               @NonNull RegistryClient registryClient,
                               @NonNull Supplier<Integer> workerWaitingTaskCount) {
        super("WorkerHeartBeatTask", workerConfig.getHeartbeatInterval().toMillis());
        this.workerConfig = workerConfig;
        this.registryClient = registryClient;
        this.workerWaitingTaskCount = workerWaitingTaskCount;
        this.processId = OSUtils.getProcessID();
    }

    @Override
    public WorkerHeartBeat getHeartBeat() {
        return WorkerHeartBeat.builder()
                .startupTime(ServerLifeCycleManager.getServerStartupTime())
                .reportTime(System.currentTimeMillis())
                .cpuUsage(OSUtils.cpuUsage())
                .loadAverage(OSUtils.loadAverage())
                .availablePhysicalMemorySize(OSUtils.availablePhysicalMemorySize())
                .maxCpuloadAvg(workerConfig.getMaxCpuLoadAvg())
                .reservedMemory(workerConfig.getReservedMemory())
                .processId(processId)
                .workerHostWeight(workerConfig.getHostWeight())
                .workerWaitingTaskCount(workerWaitingTaskCount.get())
                .workerExecThreadCount(workerConfig.getExecThreads())
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
}
