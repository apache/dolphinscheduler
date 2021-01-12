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
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.processor.DBTaskAckProcessor;
import org.apache.dolphinscheduler.server.worker.processor.DBTaskResponseProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskExecuteProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskKillProcessor;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistry;
import org.apache.dolphinscheduler.server.worker.runner.RetryReportTaskStatusThread;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;

/**
 *  worker server
 */
@ComponentScan("org.apache.dolphinscheduler")
public class WorkerServer {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(WorkerServer.class);

    /**
     *  netty remote server
     */
    private NettyRemotingServer nettyRemotingServer;

    /**
     *  worker registry
     */
    @Autowired
    private WorkerRegistry workerRegistry;

    /**
     *  worker config
     */
    @Autowired
    private WorkerConfig workerConfig;

    /**
     *  spring application context
     *  only use it for initialization
     */
    @Autowired
    private SpringApplicationContext springApplicationContext;

    @Autowired
    private RetryReportTaskStatusThread retryReportTaskStatusThread;

    /**
     * worker server startup
     *
     * worker server not use web service
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
        serverConfig.setListenPort(workerConfig.getListenPort());
        this.nettyRemotingServer = new NettyRemotingServer(serverConfig);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_REQUEST, new TaskExecuteProcessor());
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_KILL_REQUEST, new TaskKillProcessor());
        this.nettyRemotingServer.registerProcessor(CommandType.DB_TASK_ACK, new DBTaskAckProcessor());
        this.nettyRemotingServer.registerProcessor(CommandType.DB_TASK_RESPONSE, new DBTaskResponseProcessor());
        this.nettyRemotingServer.start();

        // worker registry
        this.workerRegistry.registry();

        // retry report task status
        this.retryReportTaskStatusThread.start();

        /**
         * register hooks, which are called before the process exits
         */
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                close("shutdownHook");
            }
        }));
    }

    public void close(String cause) {

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

        } catch (Exception e) {
            logger.error("worker server stop exception ", e);
            System.exit(-1);
        }
    }

}
