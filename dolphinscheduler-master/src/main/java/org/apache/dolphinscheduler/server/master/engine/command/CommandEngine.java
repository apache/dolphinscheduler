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

package org.apache.dolphinscheduler.server.master.engine.command;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.config.MasterServerLoadProtection;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBusCoordinator;
import org.apache.dolphinscheduler.server.master.engine.exceptions.CommandDuplicateHandleException;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowStartLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.WorkflowExecutionRunnableFactory;
import org.apache.dolphinscheduler.server.master.metrics.MasterServerMetrics;
import org.apache.dolphinscheduler.service.command.CommandService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Master scheduler thread, this thread will consume the commands from database and trigger processInstance executed.
 */
@Service
@Slf4j
public class CommandEngine extends BaseDaemonThread implements AutoCloseable {

    @Autowired
    private ICommandFetcher commandFetcher;

    @Autowired
    private CommandService commandService;

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private IWorkflowRepository workflowRepository;

    @Autowired
    private WorkflowExecutionRunnableFactory workflowExecutionRunnableFactory;

    @Autowired
    private MetricsProvider metricsProvider;

    @Autowired
    private WorkflowEventBusCoordinator workflowEventBusCoordinator;

    private ExecutorService commandHandleThreadPool;

    private boolean flag = false;

    protected CommandEngine() {
        super("MasterCommandLoopThread");
    }

    @Override
    public synchronized void start() {
        log.info("MasterSchedulerBootstrap starting..");
        this.commandHandleThreadPool = ThreadUtils.newDaemonFixedThreadExecutor("MasterCommandHandleThreadPool",
                Runtime.getRuntime().availableProcessors());
        flag = true;
        super.start();
        log.info("MasterSchedulerBootstrap started...");
    }

    @Override
    public void close() throws Exception {
        log.info("MasterSchedulerBootstrap stopping...");
        flag = false;
        log.info("MasterSchedulerBootstrap stopped...");
    }

    @Override
    public void run() {
        MasterServerLoadProtection serverLoadProtection = masterConfig.getServerLoadProtection();
        while (flag) {
            try {
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

                List<CompletableFuture<Void>> allCompleteFutures = new ArrayList<>();
                for (Command command : commands) {
                    CompletableFuture<Void> completableFuture = bootstrapCommand(command)
                            .thenAccept(this::bootstrapWorkflowExecutionRunnable)
                            .thenAccept((unused) -> bootstrapSuccess(command))
                            .exceptionally(throwable -> bootstrapError(command, throwable));
                    allCompleteFutures.add(completableFuture);
                }
                CompletableFuture.allOf(allCompleteFutures.toArray(new CompletableFuture[0])).join();
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

    private CompletableFuture<IWorkflowExecutionRunnable> bootstrapCommand(Command command) {
        return supplyAsync(
                () -> workflowExecutionRunnableFactory.createWorkflowExecuteRunnable(command), commandHandleThreadPool);
    }

    private CompletableFuture<Void> bootstrapWorkflowExecutionRunnable(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        final WorkflowInstance workflowInstance =
                workflowExecutionRunnable.getWorkflowExecuteContext().getWorkflowInstance();
        if (workflowInstance.getState() == WorkflowExecutionStatus.SERIAL_WAIT) {
            log.info("The workflow {} state is: {} will not be trigger now",
                    workflowInstance.getName(),
                    workflowInstance.getState());
            return CompletableFuture.completedFuture(null);
        }

        workflowRepository.put(workflowExecutionRunnable);
        workflowEventBusCoordinator.registerWorkflowEventBus(workflowExecutionRunnable);
        workflowExecutionRunnable.getWorkflowEventBus()
                .publish(WorkflowStartLifecycleEvent.of(workflowExecutionRunnable));
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> bootstrapSuccess(Command command) {
        log.info("Success bootstrap command {}", JSONUtils.toPrettyJsonString(command));
        MasterServerMetrics.incMasterConsumeCommand(1);
        return CompletableFuture.completedFuture(null);
    }

    private Void bootstrapError(Command command, Throwable throwable) {
        if (throwable instanceof CommandDuplicateHandleException) {
            log.warn("Handle command failed, the command: {} has been handled by other master",
                    command,
                    throwable);
            return null;
        }
        log.error("Failed bootstrap command {} ", JSONUtils.toPrettyJsonString(command), throwable);
        commandService.moveToErrorCommand(command, ExceptionUtils.getStackTrace(throwable));
        return null;
    }

}
