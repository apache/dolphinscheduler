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

import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.scheduler.api.SchedulerApi;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistryClient;
import org.apache.dolphinscheduler.server.master.rpc.MasterRpcServer;
import org.apache.dolphinscheduler.server.master.runner.EventExecuteService;
import org.apache.dolphinscheduler.server.master.runner.FailoverExecuteThread;
import org.apache.dolphinscheduler.server.master.runner.MasterSchedulerBootstrap;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan("org.apache.dolphinscheduler")
@EnableTransactionManagement
@EnableCaching
@Slf4j
public class MasterServer implements IStoppable {

    @Autowired
    private SpringApplicationContext springApplicationContext;

    @Autowired
    private MasterRegistryClient masterRegistryClient;

    @Autowired
    private TaskPluginManager taskPluginManager;

    @Autowired
    private MasterSchedulerBootstrap masterSchedulerBootstrap;

    @Autowired
    private SchedulerApi schedulerApi;

    @Autowired
    private EventExecuteService eventExecuteService;

    @Autowired
    private FailoverExecuteThread failoverExecuteThread;

    @Autowired
    private MasterRpcServer masterRPCServer;

    public static void main(String[] args) {
        Thread.currentThread().setName(Constants.THREAD_NAME_MASTER_SERVER);
        SpringApplication.run(MasterServer.class);
    }

    /**
     * run master server
     */
    @PostConstruct
    public void run() throws SchedulerException {
        // init rpc server
        this.masterRPCServer.start();

        // install task plugin
        this.taskPluginManager.loadPlugin();

        // self tolerant
        this.masterRegistryClient.start();
        this.masterRegistryClient.setRegistryStoppable(this);

        this.masterSchedulerBootstrap.start();

        this.eventExecuteService.start();
        this.failoverExecuteThread.start();

        this.schedulerApi.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!ServerLifeCycleManager.isStopped()) {
                close("MasterServer shutdownHook");
            }
        }));
    }

    /**
     * gracefully close
     *
     * @param cause close cause
     */
    public void close(String cause) {
        // set stop signal is true
        // execute only once
        if (!ServerLifeCycleManager.toStopped()) {
            log.warn("MasterServer is already stopped, current cause: {}", cause);
            return;
        }
        // thread sleep 3 seconds for thread quietly stop
        ThreadUtils.sleep(Constants.SERVER_CLOSE_WAIT_TIME.toMillis());
        try (
                SchedulerApi closedSchedulerApi = schedulerApi;
                MasterSchedulerBootstrap closedSchedulerBootstrap = masterSchedulerBootstrap;
                MasterRpcServer closedRpcServer = masterRPCServer;
                MasterRegistryClient closedMasterRegistryClient = masterRegistryClient;
                // close spring Context and will invoke method with @PreDestroy annotation to destroy beans.
                // like ServerNodeManager,HostManager,TaskResponseService,CuratorZookeeperClient,etc
                SpringApplicationContext closedSpringContext = springApplicationContext) {

            log.info("Master server is stopping, current cause : {}", cause);
        } catch (Exception e) {
            log.error("MasterServer stop failed, current cause: {}", cause, e);
            return;
        }
        log.info("MasterServer stopped, current cause: {}", cause);
    }

    @Override
    public void stop(String cause) {
        close(cause);
    }
}
