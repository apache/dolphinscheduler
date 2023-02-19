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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.SlotCheckState;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.server.master.event.WorkflowEvent;
import org.apache.dolphinscheduler.server.master.event.WorkflowEventQueue;
import org.apache.dolphinscheduler.server.master.event.WorkflowEventType;
import org.apache.dolphinscheduler.server.master.exception.MasterException;
import org.apache.dolphinscheduler.server.master.metrics.MasterServerMetrics;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.server.master.registry.ServerNodeManager;
import org.apache.dolphinscheduler.service.alert.ProcessAlertManager;
import org.apache.dolphinscheduler.service.command.CommandService;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Master scheduler thread, this thread will consume the commands from database and trigger processInstance executed.
 */
@Service
@Slf4j
public class MasterSchedulerBootstrap extends BaseDaemonThread implements AutoCloseable {

    @Autowired
    private ProcessService processService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private ProcessInstanceDao processInstanceDao;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private ProcessAlertManager processAlertManager;

    @Autowired
    private NettyExecutorManager nettyExecutorManager;

    /**
     * master prepare exec service
     */
    private ThreadPoolExecutor masterPrepareExecService;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private StateWheelExecuteThread stateWheelExecuteThread;

    @Autowired
    private CuringParamsService curingGlobalParamsService;

    @Autowired
    private WorkflowEventQueue workflowEventQueue;

    @Autowired
    private WorkflowEventLooper workflowEventLooper;

    @Autowired
    private ServerNodeManager serverNodeManager;

    private String masterAddress;

    protected MasterSchedulerBootstrap() {
        super("MasterCommandLoopThread");
    }

    /**
     * constructor of MasterSchedulerService
     */
    public void init() {
        this.masterPrepareExecService = (ThreadPoolExecutor) ThreadUtils
                .newDaemonFixedThreadExecutor("MasterPreExecThread", masterConfig.getPreExecThreads());
        this.masterAddress = NetUtils.getAddr(masterConfig.getListenPort());
    }

    @Override
    public synchronized void start() {
        log.info("Master schedule bootstrap starting..");
        super.start();
        workflowEventLooper.start();
        log.info("Master schedule bootstrap started...");
    }

    @Override
    public void close() {
        log.info("Master schedule bootstrap stopping...");
        log.info("Master schedule bootstrap stopped...");
    }

    /**
     * run of MasterSchedulerService
     */
    @Override
    public void run() {
        while (!ServerLifeCycleManager.isStopped()) {
            try {
                if (!ServerLifeCycleManager.isRunning()) {
                    // the current server is not at running status, cannot consume command.
                    log.warn("The current server {} is not at running status, cannot consumes commands.",
                            this.masterAddress);
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                }
                // todo: if the workflow event queue is much, we need to handle the back pressure
                boolean isOverload =
                        OSUtils.isOverload(masterConfig.getMaxCpuLoadAvg(), masterConfig.getReservedMemory());
                if (isOverload) {
                    log.warn("The current server {} is overload, cannot consumes commands.", this.masterAddress);
                    MasterServerMetrics.incMasterOverload();
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                    continue;
                }
                List<Command> commands = findCommands();
                if (CollectionUtils.isEmpty(commands)) {
                    // indicate that no command ,sleep for 1s
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                    continue;
                }

                List<ProcessInstance> processInstances = command2ProcessInstance(commands);
                if (CollectionUtils.isEmpty(processInstances)) {
                    // indicate that the command transform to processInstance error, sleep for 1s
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                    continue;
                }
                MasterServerMetrics.incMasterConsumeCommand(commands.size());

                processInstances.forEach(processInstance -> {
                    try {
                        LoggerUtils.setWorkflowInstanceIdMDC(processInstance.getId());
                        if (processInstanceExecCacheManager.contains(processInstance.getId())) {
                            log.error(
                                    "The workflow instance is already been cached, this case shouldn't be happened");
                        }
                        WorkflowExecuteRunnable workflowRunnable = new WorkflowExecuteRunnable(processInstance,
                                commandService,
                                processService,
                                processInstanceDao,
                                nettyExecutorManager,
                                processAlertManager,
                                masterConfig,
                                stateWheelExecuteThread,
                                curingGlobalParamsService,
                                taskInstanceDao,
                                taskDefinitionLogDao);
                        processInstanceExecCacheManager.cache(processInstance.getId(), workflowRunnable);
                        workflowEventQueue.addEvent(new WorkflowEvent(WorkflowEventType.START_WORKFLOW,
                                processInstance.getId()));
                    } finally {
                        LoggerUtils.removeWorkflowInstanceIdMDC();
                    }
                });
            } catch (InterruptedException interruptedException) {
                log.warn("Master schedule bootstrap interrupted, close the loop", interruptedException);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Master schedule workflow error", e);
                // sleep for 1s here to avoid the database down cause the exception boom
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
            }
        }
    }

    private List<ProcessInstance> command2ProcessInstance(List<Command> commands) throws InterruptedException {
        long commandTransformStartTime = System.currentTimeMillis();
        log.info("Master schedule bootstrap transforming command to ProcessInstance, commandSize: {}",
                commands.size());
        List<ProcessInstance> processInstances = Collections.synchronizedList(new ArrayList<>(commands.size()));
        CountDownLatch latch = new CountDownLatch(commands.size());
        for (final Command command : commands) {
            masterPrepareExecService.execute(() -> {
                try {
                    // Note: this check is not safe, the slot may change after command transform.
                    // We use the database transaction in `handleCommand` so that we can guarantee the command will
                    // always be executed
                    // by only one master
                    SlotCheckState slotCheckState = slotCheck(command);
                    if (slotCheckState.equals(SlotCheckState.CHANGE) || slotCheckState.equals(SlotCheckState.INJECT)) {
                        log.info("Master handle command {} skip, slot check state: {}", command.getId(),
                                slotCheckState);
                        return;
                    }
                    ProcessInstance processInstance = processService.handleCommand(masterAddress, command);
                    if (processInstance != null) {
                        processInstances.add(processInstance);
                        log.info("Master handle command {} end, create process instance {}", command.getId(),
                                processInstance.getId());
                    }
                } catch (Exception e) {
                    log.error("Master handle command {} error ", command.getId(), e);
                    commandService.moveToErrorCommand(command, e.toString());
                } finally {
                    latch.countDown();
                }
            });
        }

        // make sure to finish handling command each time before next scan
        latch.await();
        log.info(
                "Master schedule bootstrap transformed command to ProcessInstance, commandSize: {}, processInstanceSize: {}",
                commands.size(), processInstances.size());
        ProcessInstanceMetrics
                .recordProcessInstanceGenerateTime(System.currentTimeMillis() - commandTransformStartTime);
        return processInstances;
    }

    private List<Command> findCommands() throws MasterException {
        try {
            long scheduleStartTime = System.currentTimeMillis();
            int thisMasterSlot = serverNodeManager.getSlot();
            int masterCount = serverNodeManager.getMasterSize();
            if (masterCount <= 0) {
                log.warn("Master count: {} is invalid, the current slot: {}", masterCount, thisMasterSlot);
                return Collections.emptyList();
            }
            int pageSize = masterConfig.getFetchCommandNum();
            final List<Command> result =
                    commandService.findCommandPageBySlot(pageSize, masterCount, thisMasterSlot);
            if (CollectionUtils.isNotEmpty(result)) {
                long cost = System.currentTimeMillis() - scheduleStartTime;
                log.info(
                        "Master schedule bootstrap loop command success, fetch command size: {}, cost: {}ms, current slot: {}, total slot size: {}",
                        result.size(), cost, thisMasterSlot, masterCount);
                ProcessInstanceMetrics.recordCommandQueryTime(cost);
            }
            return result;
        } catch (Exception ex) {
            throw new MasterException("Master loop command from database error", ex);
        }
    }

    private SlotCheckState slotCheck(Command command) {
        int slot = serverNodeManager.getSlot();
        int masterSize = serverNodeManager.getMasterSize();
        SlotCheckState state;
        if (masterSize <= 0) {
            state = SlotCheckState.CHANGE;
        } else if (command.getId() % masterSize == slot) {
            state = SlotCheckState.PASS;
        } else {
            state = SlotCheckState.INJECT;
        }
        return state;
    }

}
