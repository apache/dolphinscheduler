package org.apache.dolphinscheduler.server.master.task;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.model.BaseHeartBeatTask;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

@Slf4j
public class MasterHeartBeatTask extends BaseHeartBeatTask<MasterHeartBeat> {

    private final MasterConfig masterConfig;

    private final RegistryClient registryClient;

    private final String heartBeatPath;

    private final int processId;

    public MasterHeartBeatTask(@NonNull MasterConfig masterConfig,
                               @NonNull RegistryClient registryClient) {
        super("MasterHeartBeatTask", masterConfig.getHeartbeatInterval().toMillis());
        this.masterConfig = masterConfig;
        this.registryClient = registryClient;
        this.heartBeatPath = masterConfig.getMasterRegistryPath();
        this.processId = OSUtils.getProcessID();
    }

    @Override
    public MasterHeartBeat getHeartBeat() {
        return MasterHeartBeat.builder()
                .startupTime(ServerLifeCycleManager.getServerStartupTime())
                .reportTime(System.currentTimeMillis())
                .cpuUsage(OSUtils.cpuUsage())
                .loadAverage(OSUtils.loadAverage())
                .availablePhysicalMemorySize(OSUtils.availablePhysicalMemorySize())
                .maxCpuloadAvg(masterConfig.getMaxCpuLoadAvg())
                .reservedMemory(masterConfig.getReservedMemory())
                .processId(processId)
                .build();
    }

    @Override
    public void writeHeartBeat(MasterHeartBeat masterHeartBeat) {
        String heartBeatJson = JSONUtils.toJsonString(masterHeartBeat);
        registryClient.persistEphemeral(heartBeatPath, heartBeatJson);
        log.info("Master write heart beat success, heartBeatInfo: {}", heartBeatJson);
    }
}
