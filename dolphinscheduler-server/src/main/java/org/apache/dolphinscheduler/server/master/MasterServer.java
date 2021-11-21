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

package org.apache.dolphinscheduler.server.master;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.processor.StateEventProcessor;
import org.apache.dolphinscheduler.server.master.processor.TaskAckProcessor;
import org.apache.dolphinscheduler.server.master.processor.TaskKillResponseProcessor;
import org.apache.dolphinscheduler.server.master.processor.TaskResponseProcessor;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistryClient;
import org.apache.dolphinscheduler.server.master.runner.EventExecuteService;
import org.apache.dolphinscheduler.server.master.runner.MasterSchedulerService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *  master server
 */
@ComponentScan(value = "org.apache.dolphinscheduler", excludeFilters = {
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
        "org.apache.dolphinscheduler.server.worker.*",
        "org.apache.dolphinscheduler.server.monitor.*",
        "org.apache.dolphinscheduler.server.log.*",
        "org.apache.dolphinscheduler.alert.*"
    })
})
@EnableTransactionManagement
public class MasterServer implements IStoppable {
    private static final Logger logger = LoggerFactory.getLogger(MasterServer.class);

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private SpringApplicationContext springApplicationContext;

    private NettyRemotingServer nettyRemotingServer;

    @Autowired
    private MasterRegistryClient masterRegistryClient;

    @Autowired
    private MasterSchedulerService masterSchedulerService;

    @Autowired
    private EventExecuteService eventExecuteService;

    public static void main(String[] args) {
        Thread.currentThread().setName(Constants.THREAD_NAME_MASTER_SERVER);
        new SpringApplicationBuilder(MasterServer.class)
            .profiles("master")
            .web(WebApplicationType.NONE).run(args);
    }

    /**
     * run master server
     */
    @PostConstruct
    public void run() {
        // init remoting server
        NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(masterConfig.getListenPort());
        this.nettyRemotingServer = new NettyRemotingServer(serverConfig);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_RESPONSE, new TaskResponseProcessor());
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_ACK, new TaskAckProcessor());
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_KILL_RESPONSE, new TaskKillResponseProcessor());
        this.nettyRemotingServer.registerProcessor(CommandType.STATE_EVENT_REQUEST, new StateEventProcessor());
        this.nettyRemotingServer.start();

        // self tolerant
        this.masterRegistryClient.init();
        this.masterRegistryClient.start();
        this.masterRegistryClient.setRegistryStoppable(this);

        this.eventExecuteService.init();
        this.eventExecuteService.start();
        this.masterSchedulerService.init();
        this.masterSchedulerService.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (Stopper.isRunning()) {
                close("shutdownHook");
            }
        }));
    }

    /**
     * gracefully close
     *
     * @param cause close cause
     */
    public void close(String cause) {

        try {
            // execute only once
            if (Stopper.isStopped()) {
                return;
            }

            logger.info("master server is stopping ..., cause : {}", cause);

            // set stop signal is true
            Stopper.stop();

            try {
                // thread sleep 3 seconds for thread quietly stop
                Thread.sleep(3000L);
            } catch (Exception e) {
                logger.warn("thread sleep exception ", e);
            }
            // close
            this.masterSchedulerService.close();
            this.nettyRemotingServer.close();
            this.masterRegistryClient.closeRegistry();
            // close spring Context and will invoke method with @PreDestroy annotation to destory beans. like ServerNodeManager,HostManager,TaskResponseService,CuratorZookeeperClient,etc
            springApplicationContext.close();
        } catch (Exception e) {
            logger.error("master server stop exception ", e);
        }
    }

    @Override
    public void stop(String cause) {
        close(cause);
    }
}
