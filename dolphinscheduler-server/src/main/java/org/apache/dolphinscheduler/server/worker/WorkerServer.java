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
package org.apache.dolphinscheduler.server.worker;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadPoolExecutors;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.MasterServer;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.runner.FetchTaskThread;
import org.apache.dolphinscheduler.server.zk.ZKWorkerClient;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.ITaskQueue;
import org.apache.dolphinscheduler.service.queue.TaskQueueFactory;
import org.apache.dolphinscheduler.service.zk.AbstractZKClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *  worker server
 */
@SpringBootApplication
@ComponentScan(value = "org.apache.dolphinscheduler", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {MasterServer.class})
})
public class WorkerServer implements IStoppable {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(WorkerServer.class);


    /**
     *  zk worker client
     */
    @Autowired
    private ZKWorkerClient zkWorkerClient = null;


    /**
     *  process service
     */
    @Autowired
    private ProcessService processService;

    /**
     *  alert database access
     */
    @Autowired
    private AlertDao alertDao;

    /**
     * heartbeat thread pool
     */
    private ScheduledExecutorService heartbeatWorkerService;

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

    @Value("${server.is-combined-server:false}")
    private Boolean isCombinedServer;

    @Autowired
    private WorkerConfig workerConfig;

    /**
     *  spring application context
     *  only use it for initialization
     */
    @Autowired
    private SpringApplicationContext springApplicationContext;

    /**
     * master server startup
     *
     * master server not use web service
     * @param args arguments
     */
    public static void main(String[] args) {
        Thread.currentThread().setName(Constants.THREAD_NAME_WORKER_SERVER);
        new SpringApplicationBuilder(WorkerServer.class).web(WebApplicationType.NONE).run(args);
    }


    /**
     * worker server run
     */
    @PostConstruct
    public void run(){
        logger.info("start worker server...");

        zkWorkerClient.init();

        this.taskQueue = TaskQueueFactory.getTaskQueueInstance();

        this.killExecutorService = ThreadUtils.newDaemonSingleThreadExecutor("Worker-Kill-Thread-Executor");

        this.fetchTaskExecutorService = ThreadUtils.newDaemonSingleThreadExecutor("Worker-Fetch-Thread-Executor");

        heartbeatWorkerService = ThreadUtils.newThreadScheduledExecutor("Worker-Heartbeat-Thread-Executor", Constants.DEFAUL_WORKER_HEARTBEAT_THREAD_NUM, false);

        // heartbeat thread implement
        Runnable heartBeatThread = heartBeatThread();

        zkWorkerClient.setStoppable(this);

        // regular heartbeat
        // delay 5 seconds, send heartbeat every 30 seconds
        heartbeatWorkerService.scheduleAtFixedRate(heartBeatThread, 5, workerConfig.getWorkerHeartbeatInterval(), TimeUnit.SECONDS);

        // kill process thread implement
        Runnable killProcessThread = getKillProcessThread();

        // submit kill process thread
        killExecutorService.execute(killProcessThread);

        // new fetch task thread
        FetchTaskThread fetchTaskThread = new FetchTaskThread(zkWorkerClient, processService, taskQueue);

        // submit fetch task thread
        fetchTaskExecutorService.execute(fetchTaskThread);
    }

    @PreDestroy
    public void destroy() {
        // worker server exit alert
        if (zkWorkerClient.getActiveMasterNum() <= 1) {
            alertDao.sendServerStopedAlert(1, OSUtils.getHost(), "Worker-Server");
        }
        stop("shutdownhook");
    }

    @Override
    public synchronized void stop(String cause) {

        try {
            //execute only once
            if(Stopper.isStopped()){
                return;
            }

            logger.info("worker server is stopping ..., cause : {}", cause);

            // set stop signal is true
            Stopper.stop();

            try {
                //thread sleep 3 seconds for thread quitely stop
                Thread.sleep(3000L);
            }catch (Exception e){
                logger.warn("thread sleep exception", e);
            }

            try {
                heartbeatWorkerService.shutdownNow();
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

        } catch (Exception e) {
            logger.error("worker server stop exception ", e);
            System.exit(-1);
        }
    }


    /**
     * heartbeat thread implement
     *
     * @return
     */
    private Runnable heartBeatThread(){
        logger.info("start worker heart beat thread...");
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
     * kill process thread implement
     *
     * @return kill process thread
     */
    private Runnable getKillProcessThread(){
        Runnable killProcessThread  = new Runnable() {
            @Override
            public void run() {
                logger.info("start listening kill process thread...");
                while (Stopper.isRunning()){
                    Set<String> taskInfoSet = taskQueue.smembers(Constants.DOLPHINSCHEDULER_TASKS_KILL);
                    if (CollectionUtils.isNotEmpty(taskInfoSet)){
                        for (String taskInfo : taskInfoSet){
                            killTask(taskInfo, processService);
                            removeKillInfoFromQueue(taskInfo);
                        }
                    }
                    try {
                        Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                    } catch (InterruptedException e) {
                        logger.error("interrupted exception",e);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        return killProcessThread;
    }

    /**
     * kill task
     *
     * @param taskInfo  task info
     * @param pd        process dao
     */
    private void killTask(String taskInfo, ProcessService pd) {
        logger.info("get one kill command from tasks kill queue: " + taskInfo);
        String[] taskInfoArray = taskInfo.split("-");
        if(taskInfoArray.length != 2){
            logger.error("error format kill info: " + taskInfo);
            return ;
        }
        String host = taskInfoArray[0];
        int taskInstanceId = Integer.parseInt(taskInfoArray[1]);
        TaskInstance taskInstance = pd.getTaskInstanceDetailByTaskId(taskInstanceId);
        if(taskInstance == null){
            logger.error("cannot find the kill task :" + taskInfo);
            return;
        }

        if(host.equals(Constants.NULL) && StringUtils.isEmpty(taskInstance.getHost())){
            deleteTaskFromQueue(taskInstance, pd);
            taskInstance.setState(ExecutionStatus.KILL);
            pd.saveTaskInstance(taskInstance);
        }else{
            if(taskInstance.getTaskType().equals(TaskType.DEPENDENT.toString())){
                taskInstance.setState(ExecutionStatus.KILL);
                pd.saveTaskInstance(taskInstance);
            }else if(!taskInstance.getState().typeIsFinished()){
                ProcessUtils.kill(taskInstance);
            }else{
                logger.info("the task aleady finish: task id: " + taskInstance.getId()
                        + " state: " + taskInstance.getState().toString());
            }
        }
    }

    /**
     * delete task from queue
     *
     * @param taskInstance
     * @param pd process dao
     */
    private void deleteTaskFromQueue(TaskInstance taskInstance, ProcessService pd){
        // creating distributed locks, lock path /dolphinscheduler/lock/worker
        InterProcessMutex mutex = null;
        logger.info("delete task from tasks queue: " + taskInstance.getId());

        try {
            mutex = zkWorkerClient.acquireZkLock(zkWorkerClient.getZkClient(),
                    zkWorkerClient.getWorkerLockPath());
            if(pd.checkTaskExistsInTaskQueue(taskInstance)){
                String taskQueueStr = pd.taskZkInfo(taskInstance);
                taskQueue.removeNode(Constants.DOLPHINSCHEDULER_TASKS_QUEUE, taskQueueStr);
            }

        } catch (Exception e){
            logger.error("remove task thread failure" ,e);
        }finally {
            AbstractZKClient.releaseMutex(mutex);
        }
    }

    /**
     * remove Kill info from queue
     *
     * @param taskInfo task info
     */
    private void removeKillInfoFromQueue(String taskInfo){
        taskQueue.srem(Constants.DOLPHINSCHEDULER_TASKS_KILL,taskInfo);
    }

}

