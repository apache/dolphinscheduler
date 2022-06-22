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

import static org.apache.dolphinscheduler.common.Constants.SPRING_DATASOURCE_DRIVER_CLASS_NAME;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.processor.CacheProcessor;
import org.apache.dolphinscheduler.server.master.processor.StateEventProcessor;
import org.apache.dolphinscheduler.server.master.processor.TaskAckProcessor;
import org.apache.dolphinscheduler.server.master.processor.TaskKillResponseProcessor;
import org.apache.dolphinscheduler.server.master.processor.TaskRecallProcessor;
import org.apache.dolphinscheduler.server.master.processor.TaskResponseProcessor;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistryClient;
import org.apache.dolphinscheduler.server.master.runner.EventExecuteService;
import org.apache.dolphinscheduler.server.master.runner.FailoverExecuteThread;
import org.apache.dolphinscheduler.server.master.runner.MasterSchedulerService;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.quartz.QuartzExecutors;

import java.util.concurrent.ConcurrentHashMap;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.event.EventListener;
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
@EnableCaching
public class MasterServer implements IStoppable {
    /**
     * logger of MasterServer
     */
    private static final Logger logger = LoggerFactory.getLogger(MasterServer.class);

    /**
     * master config
     */
    @Autowired
    private MasterConfig masterConfig;

    /**
     * spring application context
     * only use it for initialization
     */
    @Autowired
    private SpringApplicationContext springApplicationContext;

    /**
     * netty remote server
     */
    private NettyRemotingServer nettyRemotingServer;

    /**
     * zk master client
     */
    @Autowired
    private MasterRegistryClient masterRegistryClient;

    /**
     * scheduler service
     */
    @Autowired
    private MasterSchedulerService masterSchedulerService;

    @Autowired
    private EventExecuteService eventExecuteService;

    @Autowired
    private FailoverExecuteThread failoverExecuteThread;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    private ConcurrentHashMap<Integer, WorkflowExecuteThread> processInstanceExecMaps = new ConcurrentHashMap<>();

    /**
     * master server startup, not use web service
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        Thread.currentThread().setName(Constants.THREAD_NAME_MASTER_SERVER);
        new SpringApplicationBuilder(MasterServer.class).web(WebApplicationType.NONE).run(args);
    }

    /**
     * run master server
     */
    @EventListener
    public void run(ApplicationReadyEvent ignored) {
        PropertyUtils.setValue(SPRING_DATASOURCE_DRIVER_CLASS_NAME, driverClassName);

        // init remoting server
        NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(masterConfig.getListenPort());
        this.nettyRemotingServer = new NettyRemotingServer(serverConfig);
        TaskAckProcessor ackProcessor = new TaskAckProcessor();
        ackProcessor.init(processInstanceExecMaps);
        TaskResponseProcessor taskResponseProcessor = new TaskResponseProcessor();
        taskResponseProcessor.init(processInstanceExecMaps);
        TaskKillResponseProcessor taskKillResponseProcessor = new TaskKillResponseProcessor();
        taskKillResponseProcessor.init(processInstanceExecMaps);
        StateEventProcessor stateEventProcessor = new StateEventProcessor();
        stateEventProcessor.init(processInstanceExecMaps);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_RESPONSE, taskResponseProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_ACK, ackProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_KILL_RESPONSE, taskKillResponseProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_RECALL, new TaskRecallProcessor());
        this.nettyRemotingServer.registerProcessor(CommandType.STATE_EVENT_REQUEST, stateEventProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.CACHE_EXPIRE, new CacheProcessor());
        this.nettyRemotingServer.start();

        // self tolerant
        this.masterRegistryClient.init(this.processInstanceExecMaps);
        this.masterRegistryClient.setRegistryStoppable(this);
        this.masterRegistryClient.start();

        this.eventExecuteService.init(this.processInstanceExecMaps);
        this.eventExecuteService.start();
        // scheduler start
        this.masterSchedulerService.init(this.processInstanceExecMaps);

        this.masterSchedulerService.start();

        this.failoverExecuteThread.start();

        // start QuartzExecutors
        // what system should do if exception
        try {
            logger.info("start Quartz server...");
            QuartzExecutors.getInstance().start();
        } catch (Exception e) {
            try {
                QuartzExecutors.getInstance().shutdown();
            } catch (SchedulerException e1) {
                logger.error("QuartzExecutors shutdown failed : " + e1.getMessage(), e1);
            }
            logger.error("start Quartz failed", e);
        }

        /**
         * register hooks, which are called before the process exits
         */
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
            // close quartz
            try {
                QuartzExecutors.getInstance().shutdown();
                logger.info("Quartz service stopped");
            } catch (Exception e) {
                logger.warn("Quartz service stopped exception:{}", e.getMessage());
            }
            // close spring Context and will invoke method with @PreDestroy annotation to destory beans. like ServerNodeManager,HostManager,TaskResponseService,CuratorZookeeperClient,etc
            springApplicationContext.close();
            logger.info("springApplicationContext close");
            try {
                // thread sleep 60 seconds for quietly stop
                Thread.sleep(60000L);
            } catch (Exception e) {
                logger.warn("thread sleep exception ", e);
            }
            // Since close will be executed in hook, so we can't use System.exit here.
            Runtime.getRuntime().halt(0);
        } catch (Exception e) {
            logger.error("master server stop exception ", e);
            Runtime.getRuntime().halt(1);
        }
    }

    @Override
    public void stop(String cause) {
        close(cause);
    }
}
