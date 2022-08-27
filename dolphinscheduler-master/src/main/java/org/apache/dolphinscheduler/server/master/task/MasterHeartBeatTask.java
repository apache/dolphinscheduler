package org.apache.dolphinscheduler.server.master.task;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.common.Constants;
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

    private final long serverStartupTime;

    private final int processId;

    public MasterHeartBeatTask(@NonNull MasterConfig masterConfig,
                               @NonNull RegistryClient registryClient) {
        super("MasterHeartBeatTask", masterConfig.getHeartbeatInterval().toMillis());
        this.masterConfig = masterConfig;
        this.registryClient = registryClient;
        this.heartBeatPath = null;
        this.serverStartupTime = System.currentTimeMillis();
        this.processId = OSUtils.getProcessID();
    }

    @Override
    public MasterHeartBeat getHeartBeat() {
        return MasterHeartBeat.builder()
                .startupTime(serverStartupTime)
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
        if (registryClient.checkIsDeadServer(heartBeatPath, Constants.MASTER_TYPE)) {
            registryClient.getStoppable().stop("i was judged to death, release resources and stop myself");
            shutdown();
            return;
        }
        registryClient.persistEphemeral(heartBeatPath, JSONUtils.toJsonString(masterHeartBeat));
    }
}
