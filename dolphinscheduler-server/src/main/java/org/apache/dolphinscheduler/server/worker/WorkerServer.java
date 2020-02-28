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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadPoolExecutors;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.processor.TaskExecuteProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskKillProcessor;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistry;
import org.apache.dolphinscheduler.server.zk.ZKWorkerClient;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.queue.ITaskQueue;
import org.apache.dolphinscheduler.service.queue.TaskQueueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 *  worker server
 */
@ComponentScan("org.apache.dolphinscheduler")
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
     *  alert database access
     */
    @Autowired
    private AlertDao alertDao;

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

    /**
     * CountDownLatch latch
     */
    private CountDownLatch latch;

    @Value("${server.is-combined-server:false}")
    private Boolean isCombinedServer;

    /**
     *  worker config
     */
    @Autowired
    private WorkerConfig workerConfig;

    /**
     *  zookeeper registry center
     */
    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    /**
     *  netty remote server
     */
    private NettyRemotingServer nettyRemotingServer;

    /**
     *  worker registry
     */
    private WorkerRegistry workerRegistry;

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

        //init remoting server
        NettyServerConfig serverConfig = new NettyServerConfig();
        this.nettyRemotingServer = new NettyRemotingServer(serverConfig);
        this.nettyRemotingServer.registerProcessor(CommandType.EXECUTE_TASK_REQUEST, new TaskExecuteProcessor());
        this.nettyRemotingServer.registerProcessor(CommandType.KILL_TASK_REQUEST, new TaskKillProcessor());
        this.nettyRemotingServer.start();

        this.workerRegistry = new WorkerRegistry(zookeeperRegistryCenter, serverConfig.getListenPort(), workerConfig.getWorkerHeartbeatInterval(), workerConfig.getWorkerGroup());
        this.workerRegistry.registry();

        this.zkWorkerClient.init();

        this.taskQueue = TaskQueueFactory.getTaskQueueInstance();

        this.killExecutorService = ThreadUtils.newDaemonSingleThreadExecutor("Worker-Kill-Thread-Executor");

        this.fetchTaskExecutorService = ThreadUtils.newDaemonSingleThreadExecutor("Worker-Fetch-Thread-Executor");

        zkWorkerClient.setStoppable(this);

        /**
         * register hooks, which are called before the process exits
         */
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                stop("shutdownhook");
            }
        }));

        //let the main thread await
        latch = new CountDownLatch(1);
        if (!isCombinedServer) {
            try {
                latch.await();
            } catch (InterruptedException ignore) {
            }
        }
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

            this.nettyRemotingServer.close();
            this.workerRegistry.unRegistry();

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
            latch.countDown();
            logger.info("zookeeper service stopped");

        } catch (Exception e) {
            logger.error("worker server stop exception ", e);
            System.exit(-1);
        }
    }

}

