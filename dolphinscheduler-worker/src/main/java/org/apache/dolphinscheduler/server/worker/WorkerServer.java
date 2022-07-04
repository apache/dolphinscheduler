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
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.plugin.task.api.ProcessUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerRpcServer;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistryClient;
import org.apache.dolphinscheduler.server.worker.runner.RetryReportTaskStatusThread;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages = "org.apache.dolphinscheduler",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
                        "org.apache.dolphinscheduler.service.process.*",
                        "org.apache.dolphinscheduler.service.queue.*",
                })
        }
)
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
    private RetryReportTaskStatusThread retryReportTaskStatusThread;

    @Autowired
    private WorkerManagerThread workerManagerThread;

    /**
     * worker registry
     */
    @Autowired
    private WorkerRegistryClient workerRegistryClient;

    // todo: Can we just load the task spi, and don't install into mysql?
    //  we don't need to rely the dao module in worker.
    @Autowired
    private TaskPluginManager taskPluginManager;

    @Autowired
    private WorkerRpcServer workerRpcServer;

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

        this.taskPluginManager.installPlugin();

        this.workerRegistryClient.registry();
        this.workerRegistryClient.setRegistryStoppable(this);
        Set<String> workerZkPaths = this.workerRegistryClient.getWorkerZkPaths();
        this.workerRegistryClient.handleDeadServer(workerZkPaths, NodeType.WORKER, Constants.DELETE_OP);

        this.workerManagerThread.start();

        this.retryReportTaskStatusThread.start();

        /*
         * registry hooks, which are called before the process exits
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (Stopper.isRunning()) {
                close("WorkerServer shutdown hook");
            }
        }));
    }

    public void close(String cause) {
        try {
            // execute only once
            // set stop signal is true
            if (!Stopper.stop()) {
                logger.warn("WorkerServer is already stopped, current cause: {}", cause);
                return;
            }

            logger.info("Worker server is stopping, current cause : {}", cause);

            try {
                // thread sleep 3 seconds for thread quitely stop
                Thread.sleep(Constants.SERVER_CLOSE_WAIT_TIME.toMillis());
            } catch (Exception e) {
                logger.warn("Worker server close wait error", e);
            }

            // close
            this.workerRpcServer.close();
            this.workerRegistryClient.unRegistry();
            this.alertClientService.close();

            // kill running tasks
            this.killAllRunningTasks();

            // close the application context
            this.springApplicationContext.close();
            logger.info("Worker server stopped, current cause: {}", cause);
        } catch (Exception e) {
            logger.error("Worker server stop failed, current cause: {}", cause, e);
        }
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
                LoggerUtils.setWorkflowAndTaskInstanceIDMDC(taskRequest.getProcessInstanceId(), taskRequest.getTaskInstanceId());
                if (ProcessUtils.kill(taskRequest)) {
                    killNumber++;
                }
            } finally {
                LoggerUtils.removeWorkflowAndTaskInstanceIdMDC();
            }
        }
        logger.info("Worker after kill all cache task, task size: {}, killed number: {}", taskRequests.size(), killNumber);
    }
}
