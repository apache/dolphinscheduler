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

import org.apache.dolphinscheduler.common.CommonConfiguration;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.DefaultUncaughtExceptionHandler;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceProcessorProvider;
import org.apache.dolphinscheduler.plugin.storage.api.StorageConfiguration;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ProcessUtils;
import org.apache.dolphinscheduler.registry.api.RegistryConfiguration;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;
import org.apache.dolphinscheduler.server.worker.metrics.WorkerServerMetrics;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistryClient;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerRpcServer;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecutor;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecutorHolder;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Slf4j
@Import({CommonConfiguration.class,
        StorageConfiguration.class,
        RegistryConfiguration.class})
@SpringBootApplication
public class WorkerServer implements IStoppable {

    @Autowired
    private WorkerRegistryClient workerRegistryClient;

    @Autowired
    private WorkerRpcServer workerRpcServer;

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Autowired
    private MetricsProvider metricsProvider;

    /**
     * worker server startup, not use web service
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        WorkerServerMetrics.registerUncachedException(DefaultUncaughtExceptionHandler::getUncaughtExceptionCount);
        Thread.setDefaultUncaughtExceptionHandler(DefaultUncaughtExceptionHandler.getInstance());
        Thread.currentThread().setName(Constants.THREAD_NAME_WORKER_SERVER);
        SpringApplication.run(WorkerServer.class);
    }

    @PostConstruct
    public void run() {
        this.workerRpcServer.start();
        TaskPluginManager.loadPlugin();
        DataSourceProcessorProvider.initialize();

        this.workerRegistryClient.setRegistryStoppable(this);
        this.workerRegistryClient.start();

        this.messageRetryRunner.start();

        WorkerServerMetrics.registerWorkerCpuUsageGauge(() -> {
            SystemMetrics systemMetrics = metricsProvider.getSystemMetrics();
            return systemMetrics.getSystemCpuUsagePercentage();
        });
        WorkerServerMetrics.registerWorkerMemoryAvailableGauge(() -> {
            SystemMetrics systemMetrics = metricsProvider.getSystemMetrics();
            return (systemMetrics.getSystemMemoryMax() - systemMetrics.getSystemMemoryUsed()) / 1024.0 / 1024 / 1024;
        });
        WorkerServerMetrics.registerWorkerMemoryUsageGauge(() -> {
            SystemMetrics systemMetrics = metricsProvider.getSystemMetrics();
            return systemMetrics.getJvmMemoryUsedPercentage();
        });

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
            log.warn("WorkerServer is already stopped, current cause: {}", cause);
            return;
        }
        ThreadUtils.sleep(Constants.SERVER_CLOSE_WAIT_TIME.toMillis());

        try (
                WorkerRpcServer closedWorkerRpcServer = workerRpcServer;
                WorkerRegistryClient closedRegistryClient = workerRegistryClient) {
            log.info("Worker server is stopping, current cause : {}", cause);
            // todo: we need to remove this method
            // since for some task, we need to take-over the remote task after the worker restart
            // and if the worker crash, the `killAllRunningTasks` will not be execute, this will cause there exist two
            // kind of situation:
            // 1. If the worker is stop by kill, the tasks will be kill.
            // 2. If the worker is stop by kill -9, the tasks will not be kill.
            // So we don't need to kill the tasks.
            this.killAllRunningTasks();
        } catch (Exception e) {
            log.error("Worker server stop failed, current cause: {}", cause, e);
            return;
        }
        log.info("Worker server stopped, current cause: {}", cause);
    }

    @Override
    public void stop(String cause) {
        close(cause);
    }

    public void killAllRunningTasks() {
        Collection<WorkerTaskExecutor> workerTaskExecutors = WorkerTaskExecutorHolder.getAllTaskExecutor();
        if (CollectionUtils.isEmpty(workerTaskExecutors)) {
            return;
        }
        log.info("Worker begin to kill all cache task, task size: {}", workerTaskExecutors.size());
        int killNumber = 0;
        for (WorkerTaskExecutor workerTaskExecutor : workerTaskExecutors) {
            // kill task when it's not finished yet
            try {
                TaskExecutionContext taskExecutionContext = workerTaskExecutor.getTaskExecutionContext();
                LogUtils.setTaskInstanceIdMDC(taskExecutionContext.getTaskInstanceId());
                if (ProcessUtils.kill(taskExecutionContext)) {
                    killNumber++;
                }
            } finally {
                LogUtils.removeWorkflowAndTaskInstanceIdMDC();
            }
        }
        log.info("Worker after kill all cache task, task size: {}, killed number: {}", workerTaskExecutors.size(),
                killNumber);
    }
}
