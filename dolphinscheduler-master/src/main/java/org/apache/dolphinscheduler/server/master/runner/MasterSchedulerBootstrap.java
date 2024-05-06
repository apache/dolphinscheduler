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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.command.ICommandFetcher;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.config.MasterServerLoadProtection;
import org.apache.dolphinscheduler.server.master.event.WorkflowEvent;
import org.apache.dolphinscheduler.server.master.event.WorkflowEventQueue;
import org.apache.dolphinscheduler.server.master.event.WorkflowEventType;
import org.apache.dolphinscheduler.server.master.exception.WorkflowCreateException;
import org.apache.dolphinscheduler.server.master.metrics.MasterServerMetrics;
import org.apache.dolphinscheduler.service.command.CommandService;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Master scheduler thread, this thread will consume the commands from database and trigger processInstance executed.
 */
@Service
@Slf4j
public class MasterSchedulerBootstrap extends BaseDaemonThread implements AutoCloseable {

    @Autowired
    private ICommandFetcher commandFetcher;

    @Autowired
    private CommandService commandService;

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private WorkflowExecuteRunnableFactory workflowExecuteRunnableFactory;

    @Autowired
    private WorkflowEventQueue workflowEventQueue;

    @Autowired
    private WorkflowEventLooper workflowEventLooper;

    @Autowired
    private MasterTaskExecutorBootstrap masterTaskExecutorBootstrap;

    @Autowired
    private MetricsProvider metricsProvider;

    protected MasterSchedulerBootstrap() {
        super("MasterCommandLoopThread");
    }

    @Override
    public synchronized void start() {
        log.info("MasterSchedulerBootstrap starting..");
        super.start();
        workflowEventLooper.start();
        masterTaskExecutorBootstrap.start();
        log.info("MasterSchedulerBootstrap started...");
    }

    @Override
    public void close() throws Exception {
        log.info("MasterSchedulerBootstrap stopping...");
        try (
                final WorkflowEventLooper workflowEventLooper1 = workflowEventLooper;
                final MasterTaskExecutorBootstrap masterTaskExecutorBootstrap1 = masterTaskExecutorBootstrap) {
            // closed the resource
        }
        log.info("MasterSchedulerBootstrap stopped...");
    }

    @Override
    public void run() {
        MasterServerLoadProtection serverLoadProtection = masterConfig.getServerLoadProtection();
        while (!ServerLifeCycleManager.isStopped()) {
            try {
                if (!ServerLifeCycleManager.isRunning()) {
                    // the current server is not at running status, cannot consume command.
                    log.warn("The current server is not at running status, cannot consumes commands.");
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                }
                // todo: if the workflow event queue is much, we need to handle the back pressure
                SystemMetrics systemMetrics = metricsProvider.getSystemMetrics();
                if (serverLoadProtection.isOverload(systemMetrics)) {
                    log.warn("The current server is overload, cannot consumes commands.");
                    MasterServerMetrics.incMasterOverload();
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                    continue;
                }
                List<Command> commands = commandFetcher.fetchCommands();
                if (CollectionUtils.isEmpty(commands)) {
                    // indicate that no command ,sleep for 1s
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                    continue;
                }

                commands.parallelStream()
                        .forEach(command -> {
                            try {
                                Optional<WorkflowExecuteRunnable> workflowExecuteRunnableOptional =
                                        workflowExecuteRunnableFactory.createWorkflowExecuteRunnable(command);
                                if (!workflowExecuteRunnableOptional.isPresent()) {
                                    log.warn(
                                            "The command execute success, will not trigger a WorkflowExecuteRunnable, this workflowInstance might be in serial mode");
                                    return;
                                }
                                WorkflowExecuteRunnable workflowExecuteRunnable = workflowExecuteRunnableOptional.get();
                                ProcessInstance processInstance = workflowExecuteRunnable
                                        .getWorkflowExecuteContext().getWorkflowInstance();
                                if (processInstanceExecCacheManager.contains(processInstance.getId())) {
                                    log.error(
                                            "The workflow instance is already been cached, this case shouldn't be happened");
                                }
                                processInstanceExecCacheManager.cache(processInstance.getId(), workflowExecuteRunnable);
                                workflowEventQueue.addEvent(
                                        new WorkflowEvent(WorkflowEventType.START_WORKFLOW, processInstance.getId()));
                            } catch (WorkflowCreateException workflowCreateException) {
                                log.error("Master handle command {} error ", command.getId(), workflowCreateException);
                                commandService.moveToErrorCommand(command, workflowCreateException.toString());
                            }
                        });
                MasterServerMetrics.incMasterConsumeCommand(commands.size());
            } catch (InterruptedException interruptedException) {
                log.warn("Master schedule bootstrap interrupted, close the loop", interruptedException);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Master schedule workflow error", e);
                // sleep for 1s here to avoid the database down cause the exception boom
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
            }
        }
    }

}
