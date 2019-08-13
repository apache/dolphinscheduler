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
package cn.escheduler.server.worker;

import cn.escheduler.common.Constants;
import cn.escheduler.common.IStoppable;
import cn.escheduler.common.enums.ExecutionStatus;
import cn.escheduler.common.enums.TaskType;
import cn.escheduler.common.queue.ITaskQueue;
import cn.escheduler.common.queue.TaskQueueFactory;
import cn.escheduler.common.thread.Stopper;
import cn.escheduler.common.thread.ThreadPoolExecutors;
import cn.escheduler.common.thread.ThreadUtils;
import cn.escheduler.common.utils.CollectionUtils;
import cn.escheduler.common.utils.OSUtils;
import cn.escheduler.dao.AlertDao;
import cn.escheduler.dao.DaoFactory;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.ServerDao;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.dao.model.TaskInstance;
import cn.escheduler.server.utils.ProcessUtils;
import cn.escheduler.server.worker.runner.FetchTaskThread;
import cn.escheduler.server.zk.ZKWorkerClient;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *  worker server
 */
public class WorkerServer implements IStoppable {

    private static final Logger logger = LoggerFactory.getLogger(WorkerServer.class);

    /**
     * conf
     */
    private static Configuration conf;

    /**
     * object lock
     */
    private final Object lock = new Object();

    /**
     * whether or not to close the state
     */
    private boolean terminated = false;

    /**
     *  zk worker client
     */
    private static ZKWorkerClient zkWorkerClient = null;

    /**
     *  worker dao database access
     */
    private ServerDao serverDao = null;

    /**
     *  process database access
     */
    private final ProcessDao processDao;

    /**
     *  alert database access
     */
    private final AlertDao alertDao;

    /**
     * heartbeat thread pool
     */
    private ScheduledExecutorService heartbeatWorerService;

    /**
     * heartbeat interval, unit second
     */
    private int heartBeatInterval;

    /**
     * task queue impl
     */
    protected ITaskQueue taskQueue;

    /**
     * kill executor service
     */
    private ExecutorService killExecutorService;

    /**
     *  fetch task executor service
     */
    private ExecutorService fetchTaskExecutorService;

    static {
        try {
            conf = new PropertiesConfiguration(Constants.WORKER_PROPERTIES_PATH);
        }catch (ConfigurationException e){
            logger.error("load configuration failed",e);
            System.exit(1);
        }
    }

    public WorkerServer(){
        zkWorkerClient = ZKWorkerClient.getZKWorkerClient();
        this.serverDao = zkWorkerClient.getServerDao();
        this.alertDao = DaoFactory.getDaoInstance(AlertDao.class);
        this.processDao = DaoFactory.getDaoInstance(ProcessDao.class);
        taskQueue = TaskQueueFactory.getTaskQueueInstance();

        killExecutorService = ThreadUtils.newDaemonSingleThreadExecutor("Worker-Kill-Thread-Executor");

        fetchTaskExecutorService = ThreadUtils.newDaemonSingleThreadExecutor("Worker-Fetch-Thread-Executor");

    }

    public void run(){

        //  heartbeat interval
        heartBeatInterval = conf.getInt(Constants.WORKER_HEARTBEAT_INTERVAL,
                Constants.defaultWorkerHeartbeatInterval);

        heartbeatWorerService = ThreadUtils.newDaemonThreadScheduledExecutor("Worker-Heartbeat-Thread-Executor", Constants.defaulWorkerHeartbeatThreadNum);

        // heartbeat thread implement
        Runnable heartBeatThread = heartBeatThread();

        zkWorkerClient.setStoppable(this);

        // regular heartbeat
        // delay 5 seconds, send heartbeat every 30 seconds
        heartbeatWorerService.
                scheduleAtFixedRate(heartBeatThread, 5, heartBeatInterval, TimeUnit.SECONDS);

        // kill process thread implement
        Runnable killProcessThread = getKillProcessThread();

        // submit kill process thread
        killExecutorService.execute(killProcessThread);

        /**
         * register hooks, which are called before the process exits
         */
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {

                logger.warn("worker server stopped");
                // worker server exit alert
                if (zkWorkerClient.getActiveMasterNum() <= 1) {
                    for (int i = 0; i < Constants.ESCHEDULER_WARN_TIMES_FAILOVER;i++) {
                        alertDao.sendServerStopedAlert(1, OSUtils.getHost(), "Worker-Server");
                    }
                }

            }
        }));

        // get worker number of concurrent tasks
        int taskNum = conf.getInt(Constants.WORKER_FETCH_TASK_NUM,Constants.defaultWorkerFetchTaskNum);

        // new fetch task thread
        FetchTaskThread fetchTaskThread = new FetchTaskThread(taskNum,zkWorkerClient, processDao,conf, taskQueue);

        // submit fetch task thread
        fetchTaskExecutorService.execute(fetchTaskThread);


    }

    public static void main(String[] args)throws Exception{

        // set the name of the current thread
        Thread.currentThread().setName("Worker-Main-Thread");

        WorkerServer workerServer = new WorkerServer();

        workerServer.run();

        logger.info("worker server started");

        // blocking
        workerServer.awaitTermination();


    }


    /**
     * blocking implement
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
     * heartbeat thread implement
     * @return
     */
    public Runnable heartBeatThread(){
        Runnable heartBeatThread  = new Runnable() {
            @Override
            public void run() {
                // send heartbeat to zk
                if (StringUtils.isEmpty(zkWorkerClient.getWorkerZNode())){
                    logger.error("worker send heartbeat to zk failed");
                }

                zkWorkerClient.heartBeatForZk(zkWorkerClient.getWorkerZNode() , Constants.WORKER_PREFIX);
            }
        };
        return heartBeatThread;
    }

    /**
     *  kill process thread implement
     * @return
     */
    public Runnable getKillProcessThread(){
        Runnable killProcessThread  = new Runnable() {
            @Override
            public void run() {
                Set<String> taskInfoSet = taskQueue.smembers(Constants.SCHEDULER_TASKS_KILL);
                while (Stopper.isRunning()){
                    try {
                        Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                    } catch (InterruptedException e) {
                        logger.error("interrupted exception",e);
                    }
                    // if set is null , return
                    if (CollectionUtils.isNotEmpty(taskInfoSet)){
                        for (String taskInfo : taskInfoSet){
                            // task info start with current host
                            if (taskInfo.startsWith(OSUtils.getHost())){
                                String[] taskInfoArr = taskInfo.split("-");
                                if (taskInfoArr.length != 2){
                                    continue;
                                }else {
                                    int taskInstId=Integer.parseInt(taskInfoArr[1]);
                                    TaskInstance taskInstance = processDao.getTaskInstanceRelationByTaskId(taskInstId);

                                    if(taskInstance.getTaskType().equals(TaskType.DEPENDENT.toString())){
                                        taskInstance.setState(ExecutionStatus.KILL);
                                        processDao.saveTaskInstance(taskInstance);
                                    }else{
                                        ProcessUtils.kill(taskInstance);
                                    }
                                    taskQueue.srem(Constants.SCHEDULER_TASKS_KILL,taskInfo);
                                }
                            }
                        }
                    }

                    taskInfoSet = taskQueue.smembers(Constants.SCHEDULER_TASKS_KILL);
                }
            }
        };
        return killProcessThread;
    }



    @Override
    public synchronized void stop(String cause) {

        try {
            //execute only once
            if(Stopper.isStoped()){
                return;
            }

            logger.info("worker server is stopping ..., cause : {}", cause);

            // set stop signal is true
            Stopper.stop();

            try {
                //thread sleep 3 seconds for thread quitely stop
                Thread.sleep(3000L);
            }catch (Exception e){
                logger.warn("thread sleep exception:" + e.getMessage(), e);
            }

            try {
                heartbeatWorerService.shutdownNow();
            }catch (Exception e){
                logger.warn("heartbeat service stopped exception");
            }
            logger.info("heartbeat service stopped");

            try {
                ThreadPoolExecutors.getInstance().shutdown();
            }catch (Exception e){
                logger.warn("threadpool service stopped exception:{}",e.getMessage());
            }

            logger.info("threadpool service stopped");

            try {
                killExecutorService.shutdownNow();
            }catch (Exception e){
                logger.warn("worker kill executor service stopped exception:{}",e.getMessage());
            }
            logger.info("worker kill executor service stopped");

            try {
                fetchTaskExecutorService.shutdownNow();
            }catch (Exception e){
                logger.warn("worker fetch task service stopped exception:{}",e.getMessage());
            }
            logger.info("worker fetch task service stopped");

            try{
                zkWorkerClient.close();
            }catch (Exception e){
                logger.warn("zookeeper service stopped exception:{}",e.getMessage());
            }
            logger.info("zookeeper service stopped");

            //notify
            synchronized (lock) {
                terminated = true;
                lock.notifyAll();
            }
        } catch (Exception e) {
            logger.error("worker server stop exception : " + e.getMessage(), e);
            System.exit(-1);
        }
    }
}

