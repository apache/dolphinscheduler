package org.apache.dolphinscheduler.server.master.task;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.model.BaseHeartBeatTask;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.WorkflowEventQueue;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskExecuteThreadPool;
import org.apache.dolphinscheduler.server.master.registry.ServerNodeManager;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MasterHeartBeatTask extends BaseHeartBeatTask<MasterHeartBeat> {

    @Autowired
    private RegistryClient registryClient;
    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;
    @Autowired
    private TaskExecuteThreadPool taskExecuteThreadPool;
    @Autowired
    private WorkflowEventQueue workflowEventQueue;
    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    private final MasterConfig masterConfig;
    private final DataSourceProperties dataSourceProperties;
    private final MasterHeartBeat.MasterConfigProperty masterConfigProperty;
    private final int processId;

    public MasterHeartBeatTask(@NonNull MasterConfig masterConfig,
                               @NonNull DataSourceProperties dataSourceProperties) {
        super("MasterHeartBeatTask", masterConfig.getHeartbeatInterval().toMillis());
        this.masterConfig = masterConfig;
        this.dataSourceProperties = dataSourceProperties;
        this.masterConfigProperty = getMasterConfigProperty();
        this.processId = OSUtils.getProcessID();
    }

    @Override
    public MasterHeartBeat getHeartBeat() {
        return MasterHeartBeat.builder()
                .masterServerStatus(ServerLifeCycleManager.getServerStatus())
                .masterConfigProperty(masterConfigProperty)
                .processId(processId)
                .masterMetricsProperty(getMasterMetricsProperty())
                .startupTime(ServerLifeCycleManager.getServerStartupTime())
                .reportTime(System.currentTimeMillis())
                .cpuUsage(OSUtils.cpuUsage())
                .memoryUsage(OSUtils.memoryUsage())
                .loadAverage(OSUtils.loadAverage())
                .availablePhysicalMemorySize(OSUtils.availablePhysicalMemorySize())
                .build();
    }

    @Override
    public void writeHeartBeat(MasterHeartBeat masterHeartBeat) {
        String heartBeatJson = JSONUtils.toJsonString(masterHeartBeat);
        registryClient.persistEphemeral(masterConfig.getMasterRegistryPath(), heartBeatJson);
        log.info("Master write heart beat success, heartBeatInfo: {}", heartBeatJson);
    }

    private MasterHeartBeat.MasterConfigProperty getMasterConfigProperty() {
        return MasterHeartBeat.MasterConfigProperty.builder()
                .databaseUrl(dataSourceProperties.getUrl())
                .fetchCommandNum(masterConfig.getFetchCommandNum())
                .preExecThreads(masterConfig.getPreExecThreads())
                .execThreads(masterConfig.getExecThreads())
                .dispatchTaskNum(masterConfig.getDispatchTaskNumber())
                .hostSelect(masterConfig.getHostSelector().name())
                .heartbeatInterval(masterConfig.getHeartbeatInterval().toMillis())
                .taskCommitRetryTimes(masterConfig.getTaskCommitRetryTimes())
                .taskCommitInterval(masterConfig.getTaskCommitInterval().toMillis())
                .stateWheelInterval(masterConfig.getStateWheelInterval().toMillis())
                .maxCpuloadAvg(masterConfig.getMaxCpuLoadAvg())
                .reservedMemory(masterConfig.getReservedMemory())
                .failoverInterval(masterConfig.getFailoverInterval().toMillis())
                .killYarnJobWhenTaskFailover(masterConfig.isKillYarnJobWhenTaskFailover())
                .registryDisconnectStrategy(JSONUtils.toJsonString(masterConfig.getRegistryDisconnectStrategy()))
                .masterAddress(masterConfig.getMasterAddress())
                .masterRegistryPath(masterConfig.getMasterRegistryPath())
                .build();
    }

    private MasterHeartBeat.MasterMetricsProperty getMasterMetricsProperty() {
        return MasterHeartBeat.MasterMetricsProperty.builder()
                .slot(ServerNodeManager.getSlot())
                .totalMasterCount(ServerNodeManager.getMasterSize())
                .workflowEventQueueWaitingEventNum(workflowEventQueue.getWaitingEventNum())
                .runningWorkflowNum(processInstanceExecCacheManager.getSize())
                .workflowExecuteThreadPoolRunningThreadNum(workflowExecuteThreadPool.getActiveCount())
                .workflowExecuteThreadPoolWaitingJobNum(
                        workflowExecuteThreadPool.getThreadPoolExecutor().getQueue().size())
                .taskExecuteThreadPoolRunningThreadNum(taskExecuteThreadPool.getActiveCount())
                .taskExecuteThreadPoolWaitingJobNum(taskExecuteThreadPool.getThreadPoolExecutor().getQueue().size())
                .build();
    }
}
