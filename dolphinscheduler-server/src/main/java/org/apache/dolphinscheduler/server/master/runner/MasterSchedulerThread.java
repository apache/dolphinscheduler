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

import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.zk.ZKMasterClient;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.zk.AbstractZKClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *  master scheduler thread
 */
public class MasterSchedulerThread implements Runnable {

    /**
     * logger of MasterSchedulerThread
     */
    private static final Logger logger = LoggerFactory.getLogger(MasterSchedulerThread.class);

    /**
     * master exec service
     */
    private final ExecutorService masterExecService;

    /**
     * dolphinscheduler database interface
     */
    private final ProcessService processService;

    /**
     * zookeeper master client
     */
    private final ZKMasterClient zkMasterClient ;

    /**
     * master exec thread num
     */
    private int masterExecThreadNum;

    /**
     * master config
     */
    private MasterConfig masterConfig;

    /**
     *  netty remoting client
     */
    private NettyRemotingClient nettyRemotingClient;


    /**
     * constructor of MasterSchedulerThread
     * @param zkClient              zookeeper master client
     * @param processService            process service
     * @param masterExecThreadNum   master exec thread num
     */
    public MasterSchedulerThread(ZKMasterClient zkClient, ProcessService processService, int masterExecThreadNum){
        this.processService = processService;
        this.zkMasterClient = zkClient;
        this.masterExecThreadNum = masterExecThreadNum;
        this.masterExecService = ThreadUtils.newDaemonFixedThreadExecutor("Master-Exec-Thread",masterExecThreadNum);
        this.masterConfig = SpringApplicationContext.getBean(MasterConfig.class);
        //
        NettyClientConfig clientConfig = new NettyClientConfig();
        this.nettyRemotingClient = new NettyRemotingClient(clientConfig);
    }

    /**
     * run of MasterSchedulerThread
     */
    @Override
    public void run() {
        logger.info("master scheduler start successfully...");
        while (Stopper.isRunning()){

            // process instance
            ProcessInstance processInstance = null;

            InterProcessMutex mutex = null;
            try {

                boolean runCheckFlag = OSUtils.checkResource(masterConfig.getMasterMaxCpuloadAvg(), masterConfig.getMasterReservedMemory());
                if(!runCheckFlag) {
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                    continue;
                }
                if (zkMasterClient.getZkClient().getState() == CuratorFrameworkState.STARTED) {

                    // create distributed lock with the root node path of the lock space as /dolphinscheduler/lock/masters
                    String znodeLock = zkMasterClient.getMasterLockPath();

                    mutex = new InterProcessMutex(zkMasterClient.getZkClient(), znodeLock);
                    mutex.acquire();

                    ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) masterExecService;
                    int activeCount = poolExecutor.getActiveCount();
                    // make sure to scan and delete command  table in one transaction
                    Command command = processService.findOneCommand();
                    if (command != null) {
                        logger.info(String.format("find one command: id: %d, type: %s", command.getId(),command.getCommandType().toString()));

                        try{
                            processInstance = processService.handleCommand(logger, OSUtils.getHost(), this.masterExecThreadNum - activeCount, command);
                            if (processInstance != null) {
                                logger.info("start master exec thread , split DAG ...");
                                masterExecService.execute(new MasterExecThread(processInstance, processService, nettyRemotingClient));
                            }
                        }catch (Exception e){
                            logger.error("scan command error ", e);
                            processService.moveToErrorCommand(command, e.toString());
                        }
                    } else{
                        //indicate that no command ,sleep for 1s
                        Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                    }
                }
            }catch (Exception e){
                logger.error("master scheduler thread exception",e);
            }finally{
                AbstractZKClient.releaseMutex(mutex);
            }
        }
        nettyRemotingClient.close();
        logger.info("master server stopped...");
    }


}
