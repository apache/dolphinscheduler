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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.task.api.ProcessUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistryClient;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerRpcClient;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerRpcServer;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import java.util.Collection;

@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages = "org.apache.dolphinscheduler", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
                "org.apache.dolphinscheduler.service.process.*",
                "org.apache.dolphinscheduler.service.queue.*",
        })
})
public class WorkerServer implements IStoppable {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(WorkerServer.class);

    /**
     * spring application context
     * only use it for initialization
     */
    @Autowired
    private SpringApplicationContext springApplicationContext;

    /**
     * alert model netty remote server
     */
    @Autowired
    private AlertClientService alertClientService;

    @Autowired
    private WorkerManagerThread workerManagerThread;

    /**
     * worker registry
     */
    @Autowired
    private WorkerRegistryClient workerRegistryClient;

    @Autowired
    private TaskPluginManager taskPluginManager;

    @Autowired
    private WorkerRpcServer workerRpcServer;

    @Autowired
    private WorkerRpcClient workerRpcClient;

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Autowired
    private WorkerConfig workerConfig;

    /**
     * worker server startup, not use web service
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        Thread.currentThread().setName(Constants.THREAD_NAME_WORKER_SERVER);
        SpringApplication.run(WorkerServer.class);
    }

    @PostConstruct
    public void run() {
        this.workerRpcServer.start();
        this.workerRpcClient.start();
        this.taskPluginManager.loadPlugin();

        this.workerRegistryClient.setRegistryStoppable(this);
        this.workerRegistryClient.start();

        this.workerManagerThread.start();

        this.messageRetryRunner.start();

        /*
         * registry hooks, which are called before the process exits
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!ServerLifeCycleManager.isStopped()) {
                close("WorkerServer shutdown hook");
            }
        }));
    }

    public void close(String cause) {
        if (!ServerLifeCycleManager.toStopped()) {
            logger.warn("WorkerServer is already stopped, current cause: {}", cause);
            return;
        }
        ThreadUtils.sleep(Constants.SERVER_CLOSE_WAIT_TIME.toMillis());

        try (
                WorkerRpcServer closedWorkerRpcServer = workerRpcServer;
                WorkerRegistryClient closedRegistryClient = workerRegistryClient;
                AlertClientService closedAlertClientService = alertClientService;
                SpringApplicationContext closedSpringContext = springApplicationContext;) {
            logger.info("Worker server is stopping, current cause : {}", cause);
            // kill running tasks
            this.killAllRunningTasks();
        } catch (Exception e) {
            logger.error("Worker server stop failed, current cause: {}", cause, e);
            return;
        }
        logger.info("Worker server stopped, current cause: {}", cause);
    }

    @Override
    public void stop(String cause) {
        close(cause);
    }

    /**
     * kill all tasks which are running
     */
    public void killAllRunningTasks() {
        Collection<TaskExecutionContext> taskRequests = TaskExecutionContextCacheManager.getAllTaskRequestList();
        if (CollectionUtils.isEmpty(taskRequests)) {
            return;
        }
        logger.info("Worker begin to kill all cache task, task size: {}", taskRequests.size());
        int killNumber = 0;
        for (TaskExecutionContext taskRequest : taskRequests) {
            // kill task when it's not finished yet
            try {
                LoggerUtils.setWorkflowAndTaskInstanceIDMDC(taskRequest.getProcessInstanceId(),
                        taskRequest.getTaskInstanceId());
                if (ProcessUtils.kill(taskRequest)) {
                    killNumber++;
                }
            } finally {
                LoggerUtils.removeWorkflowAndTaskInstanceIdMDC();
            }
        }
        logger.info("Worker after kill all cache task, task size: {}, killed number: {}", taskRequests.size(),
                killNumber);
    }
}
