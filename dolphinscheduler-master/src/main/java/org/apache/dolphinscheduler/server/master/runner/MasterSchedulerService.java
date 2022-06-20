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
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
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

    /**
     * dolphinscheduler database interface
     */
    @Autowired
    private ProcessService processService;

    /**
     * master config
     */
    @Autowired
    private MasterConfig masterConfig;

    /**
     * alert manager
     */
    @Autowired
    private ProcessAlertManager processAlertManager;

    /**
     * netty remoting client
     */
    private NettyRemotingClient nettyRemotingClient;

    @Autowired
    NettyExecutorManager nettyExecutorManager;

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

    protected MasterSchedulerService() {
        super("MasterCommandLoopThread");
    }

    /**
     * constructor of MasterSchedulerService
     */
    public void init() {
        this.masterPrepareExecService = (ThreadPoolExecutor) ThreadUtils.newDaemonFixedThreadExecutor("Master-Pre-Exec-Thread", masterConfig.getPreExecThreads());
        NettyClientConfig clientConfig = new NettyClientConfig();
        this.nettyRemotingClient = new NettyRemotingClient(clientConfig);
    }

    @Override
    public synchronized void start() {
        this.stateWheelExecuteThread.start();
        super.start();
    }

    public void close() {
        nettyRemotingClient.close();
        logger.info("master schedule service stopped...");
    }

    /**
     * run of MasterSchedulerService
     */
    @Override
    public void run() {
        logger.info("master scheduler started");
        while (Stopper.isRunning()) {
            try {
                boolean runCheckFlag = OSUtils.checkResource(masterConfig.getMaxCpuLoadAvg(), masterConfig.getReservedMemory());
                if (!runCheckFlag) {
                    MasterServerMetrics.incMasterOverload();
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                    continue;
                }
                scheduleProcess();
            } catch (Exception e) {
                logger.error("master scheduler thread error", e);
            }
        }
    }

    /**
     * 1. get command by slot
     * 2. donot handle command if slot is empty
     */
    private void scheduleProcess() throws Exception {
        List<Command> commands = findCommands();
        if (CollectionUtils.isEmpty(commands)) {
            //indicate that no command ,sleep for 1s
            Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            return;
        }

        List<ProcessInstance> processInstances = command2ProcessInstance(commands);
        if (CollectionUtils.isEmpty(processInstances)) {
            return;
        }
        MasterServerMetrics.incMasterConsumeCommand(commands.size());

        for (ProcessInstance processInstance : processInstances) {

            WorkflowExecuteRunnable workflowExecuteRunnable = new WorkflowExecuteRunnable(
                    processInstance
                    , processService
                    , nettyExecutorManager
                    , processAlertManager
                    , masterConfig
                    , stateWheelExecuteThread);

            this.processInstanceExecCacheManager.cache(processInstance.getId(), workflowExecuteRunnable);
            if (processInstance.getTimeout() > 0) {
                stateWheelExecuteThread.addProcess4TimeoutCheck(processInstance);
            }
            workflowExecuteThreadPool.startWorkflow(workflowExecuteRunnable);
        }
    }

    private List<ProcessInstance> command2ProcessInstance(List<Command> commands) {
        List<ProcessInstance> processInstances = Collections.synchronizedList(new ArrayList<>(commands.size()));
        CountDownLatch latch = new CountDownLatch(commands.size());
        for (final Command command : commands) {
            masterPrepareExecService.execute(() -> {
                try {
                    // slot check again
                    SlotCheckState slotCheckState = slotCheck(command);
                    if (slotCheckState.equals(SlotCheckState.CHANGE) || slotCheckState.equals(SlotCheckState.INJECT)) {
                        logger.info("handle command {} skip, slot check state: {}", command.getId(), slotCheckState);
                        return;
                    }
                    ProcessInstance processInstance = processService.handleCommand(logger,
                            getLocalAddress(),
                            command);
                    if (processInstance != null) {
                        processInstances.add(processInstance);
                        logger.info("handle command {} end, create process instance {}", command.getId(), processInstance.getId());
                    }
                } catch (Exception e) {
                    logger.error("handle command {} error ", command.getId(), e);
                    processService.moveToErrorCommand(command, e.toString());
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            // make sure to finish handling command each time before next scan
            latch.await();
        } catch (InterruptedException e) {
            logger.error("countDownLatch await error ", e);
        }

        return processInstances;
    }

    private List<Command> findCommands() {
        int pageNumber = 0;
        int pageSize = masterConfig.getFetchCommandNum();
        List<Command> result = new ArrayList<>();
        if (Stopper.isRunning()) {
            int thisMasterSlot = ServerNodeManager.getSlot();
            int masterCount = ServerNodeManager.getMasterSize();
            if (masterCount > 0) {
                result = processService.findCommandPageBySlot(pageSize, pageNumber, masterCount, thisMasterSlot);
            }
        }
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

    private String getLocalAddress() {
        return NetUtils.getAddr(masterConfig.getListenPort());
    }
}
