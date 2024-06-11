package org.apache.dolphinscheduler.server.master.runner;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MasterTriggerBootstrap extends BaseDaemonThread implements AutoCloseable {

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

    protected MasterTriggerBootstrap() {
        super("MasterTriggerBootstrap");
    }

    @Override
    public synchronized void start() {
        log.info("MasterTriggerBootstrap starting..");
        super.start();
//        workflowEventLooper.start();
//        masterTaskExecutorBootstrap.start();
        log.info("MasterTriggerBootstrap started...");
    }

    @Override
    public void close() throws Exception {
        log.info("MasterTriggerBootstrap stopping...");
//        try (
//                final WorkflowEventLooper workflowEventLooper1 = workflowEventLooper;
//                final MasterTaskExecutorBootstrap masterTaskExecutorBootstrap1 = masterTaskExecutorBootstrap) {
//            // closed the resource
//        }
        log.info("MasterTriggerBootstrap stopped...");
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
//
//                commands.parallelStream()
//                        .forEach(command -> {
//                            try {
//                                Optional<WorkflowExecuteRunnable> workflowExecuteRunnableOptional =
//                                        workflowExecuteRunnableFactory.createWorkflowExecuteRunnable(command);
//                                if (!workflowExecuteRunnableOptional.isPresent()) {
//                                    log.warn(
//                                            "The command execute success, will not trigger a WorkflowExecuteRunnable, this workflowInstance might be in serial mode");
//                                    return;
//                                }
//                                WorkflowExecuteRunnable workflowExecuteRunnable = workflowExecuteRunnableOptional.get();
//                                ProcessInstance processInstance = workflowExecuteRunnable
//                                        .getWorkflowExecuteContext().getWorkflowInstance();
//                                if (processInstanceExecCacheManager.contains(processInstance.getId())) {
//                                    log.error(
//                                            "The workflow instance is already been cached, this case shouldn't be happened");
//                                }
//                                processInstanceExecCacheManager.cache(processInstance.getId(), workflowExecuteRunnable);
//                                workflowEventQueue.addEvent(
//                                        new WorkflowEvent(WorkflowEventType.START_WORKFLOW, processInstance.getId()));
//                            } catch (WorkflowCreateException workflowCreateException) {
//                                log.error("Master handle command {} error ", command.getId(), workflowCreateException);
//                                commandService.moveToErrorCommand(command, workflowCreateException.toString());
//                            }
//                        });
//                MasterServerMetrics.incMasterConsumeCommand(commands.size());
            } catch (InterruptedException interruptedException) {
                log.warn("MasterTriggerBootstrap interrupted, close the loop", interruptedException);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("MasterTriggerBootstrap error", e);
                // sleep for 1s here to avoid the database down cause the exception boom
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
            }
        }
    }

}

