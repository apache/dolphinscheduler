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
package cn.escheduler.server.master;

import cn.escheduler.server.quartz.ProcessScheduleJob;
import cn.escheduler.server.quartz.QuartzExecutors;
import cn.escheduler.common.Constants;
import cn.escheduler.common.IStoppable;
import cn.escheduler.common.thread.Stopper;
import cn.escheduler.common.thread.ThreadPoolExecutors;
import cn.escheduler.common.thread.ThreadUtils;
import cn.escheduler.common.utils.OSUtils;
import cn.escheduler.dao.AlertDao;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.ServerDao;
import cn.escheduler.server.master.runner.MasterSchedulerThread;
import cn.escheduler.server.zk.ZKMasterClient;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *   master server
 */
@ComponentScan("cn.escheduler")
public class MasterServer implements CommandLineRunner, IStoppable {

    private static final Logger logger = LoggerFactory.getLogger(MasterServer.class);

    /**
     *  conf
     */
    private static Configuration conf;

    /**
     *  object lock
     */
    private final Object lock = new Object();

    /**
     * whether or not to close the state
     */
    private boolean terminated = false;

    /**
     *  zk master client
     */
    private static ZKMasterClient zkMasterClient=null;


    /**
     *  master dao database access
     */
    private ServerDao serverDao = null;

    /**
     *  alert database access
     */
    private AlertDao alertDao = null;

    /**
     *  escheduler database interface
     */
    @Autowired
    private ProcessDao processDao;

    /**
     *  heartbeat thread pool
     */
    private ScheduledExecutorService heartbeatMasterService;


    /**
     *  master exec thread pool
     */
    private final ExecutorService masterSchedulerService = ThreadUtils.newDaemonSingleThreadExecutor("Master-Scheduler-Thread");

    /**
     *  heartbeat interval, unit second
     */
    private int heartBeatInterval;

    static {
        try {
            conf = new PropertiesConfiguration(Constants.MASTER_PROPERTIES_PATH);
        }catch (ConfigurationException e){
            logger.error("load configuration failed : " + e.getMessage(),e);
            System.exit(1);
        }
    }

    public MasterServer(){}

    public MasterServer(ProcessDao processDao){
        zkMasterClient = ZKMasterClient.getZKMasterClient(processDao);
    }
    public void run(ProcessDao processDao){

        // heartbeat interval
        heartBeatInterval = conf.getInt(Constants.MASTER_HEARTBEAT_INTERVAL,
                Constants.defaultMasterHeartbeatInterval);

        // master exec thread pool num
        int masterExecThreadNum = conf.getInt(Constants.MASTER_EXEC_THREADS,
                Constants.defaultMasterExecThreadNum);


        heartbeatMasterService = ThreadUtils.newDaemonThreadScheduledExecutor("Master-Main-Thread",Constants.defaulMasterHeartbeatThreadNum);

        // heartbeat thread implement
        Runnable heartBeatThread = heartBeatThread();

        zkMasterClient.setStoppable(this);

        // regular heartbeat
        // delay 5 seconds, send heartbeat every 30 seconds
        heartbeatMasterService.
                scheduleAtFixedRate(heartBeatThread, 5, heartBeatInterval, TimeUnit.SECONDS);

        // master scheduler thread
        MasterSchedulerThread masterSchedulerThread = new MasterSchedulerThread(
                zkMasterClient,
                processDao,conf,
                masterExecThreadNum);

        // submit master scheduler thread
        masterSchedulerService.execute(masterSchedulerThread);

        // start QuartzExecutors
        // TODO...
        // what system should do if exception
        try {
            ProcessScheduleJob.init(processDao);
            QuartzExecutors.getInstance().start();
        } catch (Exception e) {
            try {
                QuartzExecutors.getInstance().shutdown();
            } catch (SchedulerException e1) {
                logger.error("QuartzExecutors shutdown failed : " + e1.getMessage(), e1);
            }
            logger.error("start Quartz failed : " + e.getMessage(), e);
        }


        /**
         *  register hooks, which are called before the process exits
         */
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("master server stopped");
                if (zkMasterClient.getActiveMasterNum() <= 1) {
                    for (int i = 0; i < Constants.ESCHEDULER_WARN_TIMES_FAILOVER;i++) {
                        zkMasterClient.getAlertDao().sendServerStopedAlert(
                                1, OSUtils.getHost(), "Master-Server");
                    }
                }
            }
        }));
    }


    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MasterServer.class);
        app.run(args);
    }


    /**
     *  blocking implement
     * @throws InterruptedException
     */
    public void awaitTermination() throws InterruptedException {
        synchronized (lock) {
            while (!terminated) {
                lock.wait();
            }
        }
    }

    /**
     *  heartbeat thread implement
     * @return
     */
    public Runnable heartBeatThread(){
        Runnable heartBeatThread  = new Runnable() {
            @Override
            public void run() {
                if(Stopper.isRunning()) {
                    // send heartbeat to zk
                    if (StringUtils.isBlank(zkMasterClient.getMasterZNode())) {
                        logger.error("master send heartbeat to zk failed: can't find zookeeper path of master server");
                        return;
                    }

                    zkMasterClient.heartBeatForZk(zkMasterClient.getMasterZNode(), Constants.MASTER_PREFIX);
                }
            }
        };
        return heartBeatThread;
    }

    @Override
    public void run(String... strings) throws Exception {

        MasterServer masterServer = new MasterServer(processDao);

        masterServer.run(processDao);

        logger.info("master server started");
        // blocking
        masterServer.awaitTermination();


    }

    /**
     * gracefully stop
     * @param cause why stopping
     */
    @Override
    public synchronized void stop(String cause) {

        try {
            //execute only once
            if(Stopper.isStoped()){
                return;
            }

            logger.info("master server is stopping ..., cause : {}", cause);

            // set stop signal is true
            Stopper.stop();

            try {
                //thread sleep 3 seconds for thread quitely stop
                Thread.sleep(3000L);
            }catch (Exception e){
                logger.warn("thread sleep exception:" + e.getMessage(), e);
            }
            try {
                heartbeatMasterService.shutdownNow();
            }catch (Exception e){
                logger.warn("heartbeat service stopped exception");
            }

            logger.info("heartbeat service stopped");

            //close quartz
            try{
                QuartzExecutors.getInstance().shutdown();
            }catch (Exception e){
                logger.warn("Quartz service stopped exception:{}",e.getMessage());
            }

            logger.info("Quartz service stopped");

            try {
                ThreadPoolExecutors.getInstance().shutdown();
            }catch (Exception e){
                logger.warn("threadpool service stopped exception:{}",e.getMessage());
            }

            logger.info("threadpool service stopped");

            try {
                masterSchedulerService.shutdownNow();
            }catch (Exception e){
                logger.warn("master scheduler service stopped exception:{}",e.getMessage());
            }

            logger.info("master scheduler service stopped");

            try {
                zkMasterClient.close();
            }catch (Exception e){
                logger.warn("zookeeper service stopped exception:{}",e.getMessage());
            }

            logger.info("zookeeper service stopped");

            synchronized (lock) {
                terminated = true;
                lock.notifyAll();
            }

        } catch (Exception e) {
            logger.error("master server stop exception : " + e.getMessage(), e);
            System.exit(-1);
        }
    }
}

