/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.server.master.failover;

import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.registry.api.utils.RegistryUtils;
import org.apache.dolphinscheduler.server.master.cluster.ClusterManager;
import org.apache.dolphinscheduler.server.master.cluster.MasterServerMetadata;
import org.apache.dolphinscheduler.server.master.cluster.WorkerServerMetadata;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.system.event.GlobalMasterFailoverEvent;
import org.apache.dolphinscheduler.server.master.engine.system.event.MasterFailoverEvent;
import org.apache.dolphinscheduler.server.master.engine.system.event.WorkerFailoverEvent;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

import org.apache.commons.lang3.time.StopWatch;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Component
public class FailoverCoordinator implements IFailoverCoordinator {

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private IWorkflowRepository workflowRepository;

    @Autowired
    private TaskFailover taskFailover;

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private WorkflowFailover workflowFailover;

    @Override
    public void globalMasterFailover(final GlobalMasterFailoverEvent globalMasterFailoverEvent) {
        final StopWatch failoverTimeCost = StopWatch.createStarted();
        log.info("Global master failover starting");
        final List<String> masterAddressWhichContainsUnFinishedWorkflow =
                workflowInstanceDao.queryNeedFailoverMasters();
        for (final String masterAddress : masterAddressWhichContainsUnFinishedWorkflow) {
            final Optional<MasterServerMetadata> aliveMasterOptional =
                    clusterManager.getMasterClusters().getServer(masterAddress);
            if (aliveMasterOptional.isPresent()) {
                final MasterServerMetadata aliveMasterServerMetadata = aliveMasterOptional.get();
                log.info("The master[{}] is alive, do global master failover on it", aliveMasterServerMetadata);
                doMasterFailover(aliveMasterServerMetadata.getAddress(),
                        aliveMasterServerMetadata.getServerStartupTime());
            } else {
                log.info("The master[{}] is not alive, do global master failover on it", masterAddress);
                doMasterFailover(masterAddress, globalMasterFailoverEvent.getEventTime().getTime());
            }
        }

        failoverTimeCost.stop();
        log.info("Global master failover finished, cost: {}/ms", failoverTimeCost.getTime());
    }

    @Override
    public void failoverMaster(final MasterFailoverEvent masterFailoverEvent) {
        final MasterServerMetadata masterServerMetadata = masterFailoverEvent.getMasterServerMetadata();
        log.info("Master[{}] failover starting", masterServerMetadata);

        final Optional<MasterServerMetadata> aliveMasterOptional =
                clusterManager.getMasterClusters().getServer(masterServerMetadata.getAddress());
        if (aliveMasterOptional.isPresent()) {
            final MasterServerMetadata aliveMasterServerMetadata = aliveMasterOptional.get();
            if (aliveMasterServerMetadata.getServerStartupTime() == masterServerMetadata.getServerStartupTime()) {
                log.info("The master[{}] is alive, maybe it reconnect to registry skip failover", masterServerMetadata);
            } else {
                log.info("The master[{}] is alive, but the startup time is different, will failover on {}",
                        masterServerMetadata,
                        aliveMasterServerMetadata);
                doMasterFailover(aliveMasterServerMetadata.getAddress(),
                        aliveMasterServerMetadata.getServerStartupTime());
            }
        } else {
            log.info("The master[{}] is not alive, will failover", masterServerMetadata);
            doMasterFailover(masterServerMetadata.getAddress(), masterServerMetadata.getServerStartupTime());
        }
    }

    /**
     * Do master failover.
     * <p> Will failover the workflow which is scheduled by the master and the workflow's fire time is before the maxWorkflowFireTime.
     */
    private void doMasterFailover(final String masterAddress, final long masterStartupTime) {
        // We use lock to avoid multiple master failover at the same time.
        // Once the workflow has been failovered, then it's state will be changed to FAILOVER
        // Once the FAILOVER workflow has been refired, then it's host will be changed to the new master and have a new
        // start time.
        // So if a master has been failovered multiple times, there is no problem.
        final StopWatch failoverTimeCost = StopWatch.createStarted();
        registryClient.getLock(RegistryNodeType.MASTER_FAILOVER_LOCK.getRegistryPath());
        try {
            final String failoverFinishedNodePath =
                    RegistryUtils.getFailoverFinishedNodePath(masterAddress, masterStartupTime);
            if (registryClient.exists(failoverFinishedNodePath)) {
                log.error("The master[{}-{}] is exist at: {}, means it has already been failovered, skip failover",
                        masterAddress,
                        masterStartupTime,
                        failoverFinishedNodePath);
                return;
            }
            final List<WorkflowInstance> needFailoverWorkflows =
                    getFailoverWorkflowsForMaster(masterAddress, new Date(masterStartupTime));
            needFailoverWorkflows.forEach(workflowFailover::failoverWorkflow);
            failoverTimeCost.stop();
            registryClient.persist(failoverFinishedNodePath, String.valueOf(System.currentTimeMillis()));
            log.info("Master[{}] failover {} workflows finished, cost: {}/ms",
                    masterAddress,
                    needFailoverWorkflows.size(),
                    failoverTimeCost.getTime());
        } finally {
            registryClient.releaseLock(RegistryNodeType.MASTER_FAILOVER_LOCK.getRegistryPath());
        }
    }

    private List<WorkflowInstance> getFailoverWorkflowsForMaster(final String masterAddress,
                                                                 final Date masterCrashTime) {
        // todo: use page query
        final List<WorkflowInstance> workflowInstances =
                workflowInstanceDao.queryNeedFailoverWorkflowInstances(masterAddress);
        return workflowInstances.stream()
                .filter(workflowInstance -> {

                    if (workflowRepository.contains(workflowInstance.getId())) {
                        return false;
                    }

                    // todo: If the first time run workflow have the restartTime, then we can only check this
                    final Date restartTime = workflowInstance.getRestartTime();
                    if (restartTime != null) {
                        return restartTime.before(masterCrashTime);
                    }

                    final Date startTime = workflowInstance.getStartTime();
                    return startTime.before(masterCrashTime);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void failoverWorker(final WorkerFailoverEvent workerFailoverEvent) {
        final WorkerServerMetadata workerServerMetadata = workerFailoverEvent.getWorkerServerMetadata();
        log.info("Worker[{}] failover starting", workerServerMetadata);

        final Optional<WorkerServerMetadata> aliveWorkerOptional =
                clusterManager.getWorkerClusters().getServer(workerServerMetadata.getAddress());
        if (aliveWorkerOptional.isPresent()) {
            final WorkerServerMetadata aliveWorkerServerMetadata = aliveWorkerOptional.get();
            if (aliveWorkerServerMetadata.getServerStartupTime() == workerServerMetadata.getServerStartupTime()) {
                log.info("The worker[{}] is alive, maybe it reconnect to registry skip failover", workerServerMetadata);
            } else {
                log.info("The worker[{}] is alive, but the startup time is different, will failover on {}",
                        workerServerMetadata,
                        aliveWorkerServerMetadata);
                doWorkerFailover(aliveWorkerServerMetadata.getAddress(),
                        aliveWorkerServerMetadata.getServerStartupTime());
            }
        } else {
            log.info("The worker[{}] is not alive, will failover", workerServerMetadata);
            doWorkerFailover(workerServerMetadata.getAddress(), workerServerMetadata.getServerStartupTime());
        }
    }

    private void doWorkerFailover(final String workerAddress, final long workerCrashTime) {
        final StopWatch failoverTimeCost = StopWatch.createStarted();

        final List<ITaskExecutionRunnable> needFailoverTasks =
                getFailoverTaskForWorker(workerAddress, new Date(workerCrashTime));
        needFailoverTasks.forEach(taskFailover::failoverTask);

        registryClient.persist(
                RegistryUtils.getFailoverFinishedNodePath(workerAddress, workerCrashTime),
                String.valueOf(System.currentTimeMillis()));
        failoverTimeCost.stop();
        log.info("Worker[{}] failover {} tasks finished, cost: {}/ms",
                workerAddress,
                needFailoverTasks.size(),
                failoverTimeCost.getTime());
    }

    private List<ITaskExecutionRunnable> getFailoverTaskForWorker(final String workerAddress,
                                                                  final Date workerCrashTime) {
        return workflowRepository.getAll()
                .stream()
                .map(IWorkflowExecutionRunnable::getWorkflowExecutionGraph)
                .flatMap(workflowExecutionGraph -> workflowExecutionGraph.getActiveTaskExecutionRunnable().stream())
                .filter(ITaskExecutionRunnable::isTaskInstanceInitialized)
                .filter(taskExecutionRunnable -> workerAddress
                        .equals(taskExecutionRunnable.getTaskInstance().getHost()))
                .filter(taskExecutionRunnable -> {
                    final TaskExecutionStatus state = taskExecutionRunnable.getTaskInstance().getState();
                    return state == TaskExecutionStatus.DISPATCH || state == TaskExecutionStatus.RUNNING_EXECUTION;
                })
                .filter(taskExecutionRunnable -> {
                    // The submitTime should not be null.
                    // This is a bad case unless someone manually set the submitTime to null.
                    final Date submitTime = taskExecutionRunnable.getTaskInstance().getSubmitTime();
                    return submitTime != null && submitTime.before(workerCrashTime);
                })
                .collect(Collectors.toList());
    }

}
