package org.apache.dolphinscheduler.server.worker.task;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.common.Constants;
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

    private final long serverStartupTime;

    private final int processId;

    public WorkerHeartBeatTask(@NonNull WorkerConfig workerConfig,
                               @NonNull RegistryClient registryClient,
                               @NonNull Supplier<Integer> workerWaitingTaskCount) {
        super("WorkerHeartBeatTask", workerConfig.getHeartbeatInterval().toMillis());
        this.workerConfig = workerConfig;
        this.registryClient = registryClient;
        this.workerWaitingTaskCount = workerWaitingTaskCount;
        this.serverStartupTime = System.currentTimeMillis();
        this.processId = OSUtils.getProcessID();
    }

    @Override
    public WorkerHeartBeat getHeartBeat() {
        return WorkerHeartBeat.builder()
                .startupTime(serverStartupTime)
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
        for (String workerGroupRegistryPath : workerConfig.getWorkerRegistryPaths()) {
            if (registryClient.checkIsDeadServer(workerGroupRegistryPath, Constants.WORKER_TYPE)) {
                registryClient.getStoppable().stop("i was judged to death, release resources and stop myself");
                shutdown();
                return;
            }
            registryClient.persistEphemeral(workerGroupRegistryPath, JSONUtils.toJsonString(workerHeartBeat));
        }
    }
}
