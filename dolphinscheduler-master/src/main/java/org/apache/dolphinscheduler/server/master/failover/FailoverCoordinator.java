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
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.system.event.GlobalMasterFailoverEvent;
import org.apache.dolphinscheduler.server.master.engine.system.event.MasterFailoverEvent;
import org.apache.dolphinscheduler.server.master.engine.system.event.WorkerFailoverEvent;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Date;
import java.util.List;
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
    public void globalMasterFailover(GlobalMasterFailoverEvent globalMasterFailoverEvent) {
        final StopWatch failoverTimeCost = StopWatch.createStarted();
        log.info("Global master failover starting");
        final List<MasterFailoverEvent> masterFailoverEvents = workflowInstanceDao.queryNeedFailoverMasters()
                .stream()
                .map(masterAddress -> MasterFailoverEvent.of(masterAddress, globalMasterFailoverEvent.getEventTime()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(masterFailoverEvents)) {
            log.info("There are {} masters need to failover", masterFailoverEvents.size());
            masterFailoverEvents.forEach(this::failoverMaster);
        }

        failoverTimeCost.stop();
        log.info("Global master failover finished, cost: {}/ms", failoverTimeCost.getTime());
    }

    @Override
    public void failoverMaster(final MasterFailoverEvent masterFailoverEvent) {
        final StopWatch failoverTimeCost = StopWatch.createStarted();
        final String masterAddress = masterFailoverEvent.getMasterAddress();
        log.info("Master[{}] failover starting", masterAddress);

        registryClient.getLock(RegistryNodeType.MASTER_FAILOVER_LOCK.getRegistryPath());
        try {
            final List<WorkflowInstance> needFailoverWorkflows = getFailoverWorkflowsForMaster(masterFailoverEvent);
            needFailoverWorkflows.forEach(workflowFailover::failoverWorkflow);

            failoverTimeCost.stop();
            log.info("Master[{}] failover {} workflows finished, cost: {}/ms",
                    masterAddress,
                    needFailoverWorkflows.size(),
                    failoverTimeCost.getTime());
        } finally {
            registryClient.releaseLock(RegistryNodeType.MASTER_FAILOVER_LOCK.getRegistryPath());
        }
    }

    private List<WorkflowInstance> getFailoverWorkflowsForMaster(final MasterFailoverEvent masterFailoverEvent) {
        // todo: use page query
        final List<WorkflowInstance> workflowInstances = workflowInstanceDao.queryNeedFailoverWorkflowInstances(
                masterFailoverEvent.getMasterAddress());
        return workflowInstances.stream()
                .filter(workflowInstance -> {

                    if (workflowRepository.contains(workflowInstance.getId())) {
                        return false;
                    }

                    // todo: If the first time run workflow have the restartTime, then we can only check this
                    final Date restartTime = workflowInstance.getRestartTime();
                    if (restartTime != null) {
                        return restartTime.before(masterFailoverEvent.getEventTime());
                    }

                    final Date startTime = workflowInstance.getStartTime();
                    return startTime.before(masterFailoverEvent.getEventTime());
                })
                .collect(Collectors.toList());
    }

    @Override
    public void failoverWorker(final WorkerFailoverEvent workerFailoverEvent) {
        final StopWatch failoverTimeCost = StopWatch.createStarted();

        final String workerAddress = workerFailoverEvent.getWorkerAddress();
        log.info("Worker[{}] failover starting", workerAddress);

        final List<ITaskExecutionRunnable> needFailoverTasks = getFailoverTaskForWorker(workerFailoverEvent);
        needFailoverTasks.forEach(taskFailover::failoverTask);

        failoverTimeCost.stop();
        log.info("Worker[{}] failover {} tasks finished, cost: {}/ms",
                workerAddress,
                needFailoverTasks.size(),
                failoverTimeCost.getTime());
    }

    private List<ITaskExecutionRunnable> getFailoverTaskForWorker(final WorkerFailoverEvent workerFailoverEvent) {
        final String workerAddress = workerFailoverEvent.getWorkerAddress();
        final Date workerCrashTime = workerFailoverEvent.getEventTime();
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
