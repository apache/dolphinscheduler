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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.SlotCheckState;
import org.apache.dolphinscheduler.common.expand.CuringGlobalParamsService;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.server.master.metrics.MasterServerMetrics;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.server.master.registry.ServerNodeManager;
import org.apache.dolphinscheduler.service.alert.ProcessAlertManager;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Master scheduler thread, this thread will consume the commands from database and trigger processInstance executed.
 */
@Service
public class MasterSchedulerService extends BaseDaemonThread {

    /**
     * logger of MasterSchedulerService
     */
    private static final Logger logger = LoggerFactory.getLogger(MasterSchedulerService.class);

    @Autowired
    private ProcessService processService;

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private ProcessAlertManager processAlertManager;

    private NettyRemotingClient nettyRemotingClient;

    @Autowired
    private NettyExecutorManager nettyExecutorManager;

    /**
     * master prepare exec service
     */
    private ThreadPoolExecutor masterPrepareExecService;

    /**
     * workflow exec service
     */
    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private StateWheelExecuteThread stateWheelExecuteThread;

    @Autowired
    private CuringGlobalParamsService curingGlobalParamsService;

    private String masterAddress;

    protected MasterSchedulerService() {
        super("MasterCommandLoopThread");
    }

    /**
     * constructor of MasterSchedulerService
     */
    public void init() {
        this.masterPrepareExecService = (ThreadPoolExecutor) ThreadUtils.newDaemonFixedThreadExecutor("MasterPreExecThread", masterConfig.getPreExecThreads());
        NettyClientConfig clientConfig = new NettyClientConfig();
        this.nettyRemotingClient = new NettyRemotingClient(clientConfig);
        this.masterAddress = NetUtils.getAddr(masterConfig.getListenPort());
    }

    @Override
    public synchronized void start() {
        logger.info("Master schedule service starting..");
        this.stateWheelExecuteThread.start();
        super.start();
        logger.info("Master schedule service started...");
    }

    public void close() {
        logger.info("Master schedule service stopping...");
        nettyRemotingClient.close();
        logger.info("Master schedule service stopped...");
    }

    /**
     * run of MasterSchedulerService
     */
    @Override
    public void run() {
        while (Stopper.isRunning()) {
            try {
                boolean isOverload = OSUtils.isOverload(masterConfig.getMaxCpuLoadAvg(), masterConfig.getReservedMemory());
                if (isOverload) {
                    MasterServerMetrics.incMasterOverload();
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                    continue;
                }
                scheduleWorkflow();
            } catch (InterruptedException interruptedException) {
                logger.warn("Master schedule service interrupted, close the loop", interruptedException);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Master schedule service loop command error", e);
            }
        }
    }

    /**
     * Query command from database by slot, and transform to workflow instance, then submit to workflowExecuteThreadPool.
     */
    private void scheduleWorkflow() throws InterruptedException {
        List<Command> commands = findCommands();
        if (CollectionUtils.isEmpty(commands)) {
            // indicate that no command ,sleep for 1s
            Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            return;
        }

        List<ProcessInstance> processInstances = command2ProcessInstance(commands);
        if (CollectionUtils.isEmpty(processInstances)) {
            return;
        }
        MasterServerMetrics.incMasterConsumeCommand(commands.size());

        for (ProcessInstance processInstance : processInstances) {
            try {
                LoggerUtils.setWorkflowInstanceIdMDC(processInstance.getId());
                logger.info("Master schedule service starting workflow instance");
                final WorkflowExecuteRunnable workflowExecuteRunnable = new WorkflowExecuteRunnable(
                    processInstance
                    , processService
                    , nettyExecutorManager
                    , processAlertManager
                    , masterConfig
                    , stateWheelExecuteThread
                    , curingGlobalParamsService);

                this.processInstanceExecCacheManager.cache(processInstance.getId(), workflowExecuteRunnable);
                if (processInstance.getTimeout() > 0) {
                    stateWheelExecuteThread.addProcess4TimeoutCheck(processInstance);
                }
                ProcessInstanceMetrics.incProcessInstanceSubmit();
                workflowExecuteThreadPool.submit(workflowExecuteRunnable);
                logger.info("Master schedule service started workflow instance");

            } catch (Exception ex) {
                processInstanceExecCacheManager.removeByProcessInstanceId(processInstance.getId());
                stateWheelExecuteThread.removeProcess4TimeoutCheck(processInstance.getId());
                logger.info("Master submit workflow to thread pool failed, will remove workflow runnable from cache manager", ex);
            } finally {
                LoggerUtils.removeWorkflowInstanceIdMDC();
            }
        }
    }

    private List<ProcessInstance> command2ProcessInstance(List<Command> commands) throws InterruptedException {
        long commandTransformStartTime = System.currentTimeMillis();
        logger.info("Master schedule service transforming command to ProcessInstance, commandSize: {}", commands.size());
        List<ProcessInstance> processInstances = Collections.synchronizedList(new ArrayList<>(commands.size()));
        CountDownLatch latch = new CountDownLatch(commands.size());
        for (final Command command : commands) {
            masterPrepareExecService.execute(() -> {
                try {
                    // todo: this check is not safe, the slot may change after command transform.
                    // slot check again
                    SlotCheckState slotCheckState = slotCheck(command);
                    if (slotCheckState.equals(SlotCheckState.CHANGE) || slotCheckState.equals(SlotCheckState.INJECT)) {
                        logger.info("Master handle command {} skip, slot check state: {}", command.getId(), slotCheckState);
                        return;
                    }
                    ProcessInstance processInstance = processService.handleCommand(masterAddress, command);
                    if (processInstance != null) {
                        processInstances.add(processInstance);
                        logger.info("Master handle command {} end, create process instance {}", command.getId(), processInstance.getId());
                    }
                } catch (Exception e) {
                    logger.error("Master handle command {} error ", command.getId(), e);
                    processService.moveToErrorCommand(command, e.toString());
                } finally {
                    latch.countDown();
                }
            });
        }

        // make sure to finish handling command each time before next scan
        latch.await();
        logger.info("Master schedule service transformed command to ProcessInstance, commandSize: {}, processInstanceSize: {}",
            commands.size(), processInstances.size());
        ProcessInstanceMetrics.recordProcessInstanceGenerateTime(System.currentTimeMillis() - commandTransformStartTime);
        return processInstances;
    }

    private List<Command> findCommands() {
        long scheduleStartTime = System.currentTimeMillis();
        int thisMasterSlot = ServerNodeManager.getSlot();
        int masterCount = ServerNodeManager.getMasterSize();
        if (masterCount <= 0) {
            logger.warn("Master count: {} is invalid, the current slot: {}", masterCount, thisMasterSlot);
            return Collections.emptyList();
        }
        int pageNumber = 0;
        int pageSize = masterConfig.getFetchCommandNum();
        final List<Command> result = processService.findCommandPageBySlot(pageSize, pageNumber, masterCount, thisMasterSlot);
        if (CollectionUtils.isNotEmpty(result)) {
            logger.info("Master schedule service loop command success, command size: {}, current slot: {}, total slot size: {}",
                result.size(), thisMasterSlot, masterCount);
        }
        ProcessInstanceMetrics.recordCommandQueryTime(System.currentTimeMillis() - scheduleStartTime);
        return result;
    }

    private SlotCheckState slotCheck(Command command) {
        int slot = ServerNodeManager.getSlot();
        int masterSize = ServerNodeManager.getMasterSize();
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
