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
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.server.log.LoggerRequestProcessor;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.processor.HostUpdateProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskExecuteProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskExecuteResponseAckProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskExecuteRunningAckProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskKillProcessor;
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
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@ComponentScan("org.apache.dolphinscheduler")
public class WorkerServer implements IStoppable {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(WorkerServer.class);

    /**
     * netty remote server
     */
    private NettyRemotingServer nettyRemotingServer;

    /**
     * worker config
     */
    @Autowired
    private WorkerConfig workerConfig;

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

    @Autowired
    private TaskPluginManager taskPluginManager;

    @Autowired
    private TaskExecuteProcessor taskExecuteProcessor;

    @Autowired
    private TaskKillProcessor taskKillProcessor;

    @Autowired
    private TaskExecuteRunningAckProcessor taskExecuteRunningAckProcessor;

    @Autowired
    private TaskExecuteResponseAckProcessor taskExecuteResponseAckProcessor;

    @Autowired
    private HostUpdateProcessor hostUpdateProcessor;

    @Autowired
    private LoggerRequestProcessor loggerRequestProcessor;

    /**
     * worker server startup, not use web service
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        Thread.currentThread().setName(Constants.THREAD_NAME_WORKER_SERVER);
        SpringApplication.run(WorkerServer.class);
    }

    /**
     * worker server run
     */
    @PostConstruct
    public void run() {
        // init remoting server
        NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(workerConfig.getListenPort());
        this.nettyRemotingServer = new NettyRemotingServer(serverConfig);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_REQUEST, taskExecuteProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_KILL_REQUEST, taskKillProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_RUNNING_ACK, taskExecuteRunningAckProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_RESPONSE_ACK, taskExecuteResponseAckProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.PROCESS_HOST_UPDATE_REQUEST, hostUpdateProcessor);

        // logger server
        this.nettyRemotingServer.registerProcessor(CommandType.GET_LOG_BYTES_REQUEST, loggerRequestProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.ROLL_VIEW_LOG_REQUEST, loggerRequestProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.VIEW_WHOLE_LOG_REQUEST, loggerRequestProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.REMOVE_TAK_LOG_REQUEST, loggerRequestProcessor);

        this.nettyRemotingServer.start();

        // install task plugin
        this.taskPluginManager.installPlugin();

        // worker registry
        try {
            this.workerRegistryClient.registry();
            this.workerRegistryClient.setRegistryStoppable(this);
            Set<String> workerZkPaths = this.workerRegistryClient.getWorkerZkPaths();

            this.workerRegistryClient.handleDeadServer(workerZkPaths, NodeType.WORKER, Constants.DELETE_OP);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        // task execute manager
        this.workerManagerThread.start();

        // retry report task status
        this.retryReportTaskStatusThread.start();

        /*
         * registry hooks, which are called before the process exits
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (Stopper.isRunning()) {
                close("shutdownHook");
            }
        }));
    }

    public void close(String cause) {
        try {
            // execute only once
            if (Stopper.isStopped()) {
                return;
            }

            logger.info("worker server is stopping ..., cause : {}", cause);

            // set stop signal is true
            Stopper.stop();

            try {
                // thread sleep 3 seconds for thread quitely stop
                Thread.sleep(3000L);
            } catch (Exception e) {
                logger.warn("thread sleep exception", e);
            }

            // close
            this.nettyRemotingServer.close();
            this.workerRegistryClient.unRegistry();
            this.alertClientService.close();

            // kill running tasks
            this.killAllRunningTasks();

            // close the application context
            this.springApplicationContext.close();
        } catch (Exception e) {
            logger.error("worker server stop exception ", e);
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
        logger.info("ready to kill all cache job, job size:{}", taskRequests.size());

        if (CollectionUtils.isEmpty(taskRequests)) {
            return;
        }

        for (TaskExecutionContext taskRequest : taskRequests) {
            // kill task when it's not finished yet
            org.apache.dolphinscheduler.plugin.task.api.ProcessUtils.kill(taskRequest);
        }
    }
}
