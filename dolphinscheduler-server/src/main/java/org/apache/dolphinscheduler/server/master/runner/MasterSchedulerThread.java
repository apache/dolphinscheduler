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
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.zk.AbstractZKClient;
import org.apache.dolphinscheduler.dao.ProcessDao;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.zk.ZKMasterClient;
import org.apache.commons.configuration.Configuration;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
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
    private final ProcessDao processDao;

    /**
     * zookeeper master client
     */
    private final ZKMasterClient zkMasterClient ;

    /**
     * master exec thread num
     */
    private int masterExecThreadNum;

    /**
     * Configuration of MasterSchedulerThread
     */
    private final Configuration conf;

    /**
     * constructor of MasterSchedulerThread
     * @param zkClient              zookeeper master client
     * @param processDao            process dao
     * @param conf                  conf
     * @param masterExecThreadNum   master exec thread num
     */
    public MasterSchedulerThread(ZKMasterClient zkClient, ProcessDao processDao, Configuration conf, int masterExecThreadNum){
        this.processDao = processDao;
        this.zkMasterClient = zkClient;
        this.conf = conf;
        this.masterExecThreadNum = masterExecThreadNum;
        this.masterExecService = ThreadUtils.newDaemonFixedThreadExecutor("Master-Exec-Thread",masterExecThreadNum);
    }

    /**
     * run of MasterSchedulerThread
     */
    @Override
    public void run() {
        while (Stopper.isRunning()){

            // process instance
            ProcessInstance processInstance = null;

            InterProcessMutex mutex = null;
            try {

                if(OSUtils.checkResource(conf, true)){
                    if (zkMasterClient.getZkClient().getState() == CuratorFrameworkState.STARTED) {

                        // create distributed lock with the root node path of the lock space as /dolphinscheduler/lock/failover/master
                        String znodeLock = zkMasterClient.getMasterLockPath();

                        mutex = new InterProcessMutex(zkMasterClient.getZkClient(), znodeLock);
                        mutex.acquire();

                        ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) masterExecService;
                        int activeCount = poolExecutor.getActiveCount();
                        // make sure to scan and delete command  table in one transaction
                        Command command = processDao.findOneCommand();
                        if (command != null) {
                            logger.info(String.format("find one command: id: %d, type: %s", command.getId(),command.getCommandType().toString()));

                            try{
                                processInstance = processDao.handleCommand(logger, OSUtils.getHost(), this.masterExecThreadNum - activeCount, command);
                                if (processInstance != null) {
                                    logger.info("start master exec thread , split DAG ...");
                                    masterExecService.execute(new MasterExecThread(processInstance,processDao));
                                }
                            }catch (Exception e){
                                logger.error("scan command error ", e);
                                processDao.moveToErrorCommand(command, e.toString());
                            }
                        }
                    }
                }

                // accessing the command table every SLEEP_TIME_MILLIS milliseconds
                Thread.sleep(Constants.SLEEP_TIME_MILLIS);

            }catch (Exception e){
                logger.error("master scheduler thread exception : " + e.getMessage(),e);
            }finally{
                AbstractZKClient.releaseMutex(mutex);
            }
        }
    }


}
