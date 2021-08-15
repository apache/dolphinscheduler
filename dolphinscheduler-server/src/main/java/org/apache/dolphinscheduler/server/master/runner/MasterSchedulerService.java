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
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.StateEventChangeCommand;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.remote.processor.StateEventCallbackService;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistryClient;
import org.apache.dolphinscheduler.server.master.registry.ServerNodeManager;
import org.apache.dolphinscheduler.service.alert.ProcessAlertManager;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.parquet.column.values.bitpacking.IntPacker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.h2.pagestore.db.PageBtreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 *  master scheduler thread
 */
@Service
public class MasterSchedulerService extends Thread {

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
     * zookeeper master client
     */
    @Autowired
    private MasterRegistryClient masterRegistryClient;

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
     *  netty remoting client
     */
    private NettyRemotingClient nettyRemotingClient;

    /**
     * master exec service
     */
    private ThreadPoolExecutor masterExecService;

    /**
     *
     */
    private StateEventCallbackService stateEventCallbackService;
    private StateWheelExecuteThread stateWheelExecuteThread;

    private ConcurrentHashMap<Integer, MasterExecThread> processInstanceExecMaps ;
    private ConcurrentHashMap<String, MasterExecThread> eventHandlerMap = new ConcurrentHashMap();
    ListeningExecutorService listeningExecutorService;
    ConcurrentHashMap<Integer,ProcessInstance> processTimeoutCheckList = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer,TaskInstance> taskTimeoutCheckList = new ConcurrentHashMap<>();

    /**
     * constructor of MasterSchedulerService
     */
//    @PostConstruct
    public void init(ConcurrentHashMap<Integer, MasterExecThread> processInstanceExecMaps) {
        this.processInstanceExecMaps = processInstanceExecMaps;
        this.masterExecService = (ThreadPoolExecutor)ThreadUtils.newDaemonFixedThreadExecutor("Master-Exec-Thread", masterConfig.getMasterExecThreads());
        this.stateEventCallbackService = SpringApplicationContext.getBean(StateEventCallbackService.class);
        NettyClientConfig clientConfig = new NettyClientConfig();
        this.nettyRemotingClient = new NettyRemotingClient(clientConfig);
        ExecutorService eventService = ThreadUtils.newDaemonFixedThreadExecutor("MasterEventExecution", masterConfig.getMasterExecThreads());
        listeningExecutorService = MoreExecutors.listeningDecorator(eventService);
        stateWheelExecuteThread = new StateWheelExecuteThread(processTimeoutCheckList,
                taskTimeoutCheckList,
                processInstanceExecMaps,
                masterConfig.getStateWheelInterval() * Constants.SLEEP_TIME_MILLIS);
    }

    @Override
    public synchronized void start() {
        super.setName("MasterSchedulerService");
        super.start();
        stateWheelExecuteThread.start();
    }

    public void close() {
        masterExecService.shutdown();
        boolean terminated = false;
        try {
            terminated = masterExecService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
        if (!terminated) {
            logger.warn("masterExecService shutdown without terminated, increase await time");
        }
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
                boolean runCheckFlag = OSUtils.checkResource(masterConfig.getMasterMaxCpuloadAvg(), masterConfig.getMasterReservedMemory());
                if (!runCheckFlag) {
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                    continue;
                }
                scheduleProcess();
                eventHandler();
            } catch (Exception e) {
                logger.error("master scheduler thread error", e);
            }
        }
    }
    private void eventHandler() {
        for (MasterExecThread masterExecThread : this.processInstanceExecMaps.values()) {
            if (masterExecThread.eventSize() == 0
                    || StringUtils.isEmpty(masterExecThread.getKey())
                    || eventHandlerMap.containsKey(masterExecThread.getKey())) {
                continue;
            }
            int processInstanceId = masterExecThread.getProcessInstance().getId();
            logger.info("handle process instance : {} events, count:{}",
                    processInstanceId,
                    masterExecThread.eventSize());
            logger.info("already exists handler process size:{}", this.eventHandlerMap.size());
            eventHandlerMap.put(masterExecThread.getKey(), masterExecThread);
            ListenableFuture future = this.listeningExecutorService.submit(masterExecThread);
            FutureCallback futureCallback = new FutureCallback() {
                @Override
                public void onSuccess(Object o) {
                    if (masterExecThread.workFlowFinish()) {
                        processInstanceExecMaps.remove(processInstanceId);
                        notifyProcessChanged();
                        logger.info("process instance {} finished.", processInstanceId);
                    }
                    if(masterExecThread.getProcessInstance().getId() != processInstanceId){
                        processInstanceExecMaps.remove(processInstanceId);
                        processInstanceExecMaps.put(masterExecThread.getProcessInstance().getId(), masterExecThread);

                    }
                    eventHandlerMap.remove(masterExecThread.getKey());
                }

                private void notifyProcessChanged() {
                    Map<ProcessInstance, TaskInstance> fatherMaps = processService.notifyProcessList(processInstanceId, 0);

                    for(ProcessInstance processInstance : fatherMaps.keySet()){
                        if(processInstance.getHost().equalsIgnoreCase(getLocalAddress())){
                            notifyMyself(processInstance, fatherMaps.get(processInstance));
                        } else{
                            notifyProcess(processInstance, fatherMaps.get(processInstance));
                        }
                    }
                }

                private void notifyMyself(ProcessInstance processInstance,TaskInstance taskInstance){
                    logger.info("notify process {} task {} state change", processInstance.getId(), taskInstance.getId());
                    if(!processInstanceExecMaps.containsKey(processInstance.getId())){
                        return;
                    }
                    MasterExecThread masterExecThreadNotify = processInstanceExecMaps.get(processInstance.getId());
                    StateEvent stateEvent = new StateEvent();
                    stateEvent.setTaskInstanceId(taskInstance.getId());
                    stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
                    stateEvent.setProcessInstanceId(processInstance.getId());
                    stateEvent.setExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
                    masterExecThreadNotify.addStateEvent(stateEvent);
                }

                private void notifyProcess(ProcessInstance processInstance, TaskInstance taskInstance){
                    String host = processInstance.getHost();
                    logger.info("notify process {} task {} state change, host:{}",
                            processInstance.getId(), taskInstance.getId(), host);
                    StateEventChangeCommand stateEventChangeCommand = new StateEventChangeCommand(
                            processInstanceId, 0, masterExecThread.getProcessInstance().getState(), processInstance.getId(), taskInstance.getId()
                    );

                    stateEventCallbackService.sendResult(host, stateEventChangeCommand.convert2Command());
                }

                @Override
                public void onFailure(Throwable throwable) {
                }
            };
            Futures.addCallback(future, futureCallback, this.listeningExecutorService);
        }
    }

    /**
     * 1. 根据槽数获取command
     * 2. 当槽处于锁的情况下，空转或者停止
     * @throws Exception
     */
    private void scheduleProcess() throws Exception {

        try {
//            masterRegistryClient.blockAcquireMutex();

            int activeCount = masterExecService.getActiveCount();
            // make sure to scan and delete command  table in one transaction
            Command command = findOneCommand();
            if (command != null) {
                logger.info("find one command: id: {}, type: {}", command.getId(),command.getCommandType());
                try {
                    ProcessInstance processInstance = processService.handleCommand(logger,
                            getLocalAddress(),
                            this.masterConfig.getMasterExecThreads() - activeCount, command);
                    if (processInstance != null) {
                        MasterExecThread masterExecThread = new MasterExecThread(
                                processInstance
                                , processService
                                , nettyRemotingClient
                                , processAlertManager
                                , masterConfig
                                , taskTimeoutCheckList);

                        this.processInstanceExecMaps.put(processInstance.getId(), masterExecThread);
                        if(processInstance.getTimeout() > 0){
                            this.processTimeoutCheckList.put(processInstance.getId(),processInstance);
                        }
                        masterExecService.execute(masterExecThread);
                    }
                } catch (Exception e) {
                    logger.error("scan command error ", e);
                    processService.moveToErrorCommand(command, e.toString());
                }
            } else {
                //indicate that no command ,sleep for 1s
                Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            }
        } finally {
        }
    }

    private Command findOneCommand(){
        if(ServerNodeManager.SLOT_LIST.size() ==0){
            return null;
        }

        int pageSize = ServerNodeManager.MASTER_SIZE;
        int pageNumber = 0;

        Command result = null;
        List<Command> commandList = processService.findCommandPage(pageSize, pageNumber);
        int slot = ServerNodeManager.SLOT_LIST.get(0);
        if(slot == 0 || commandList.size() == 0){
            return null;
        }
        while(commandList.size() > 0){
            for(Command command : commandList){
                if(command.getId() % slot == 0){
                    result = command;
                    break;
                }
            }
            pageNumber += 1;
            commandList = processService.findCommandPage(pageSize, pageNumber + 1);
        }
        return result;
    }

    private String getLocalAddress() {
        return NetUtils.getAddr(masterConfig.getListenPort());
    }
}
