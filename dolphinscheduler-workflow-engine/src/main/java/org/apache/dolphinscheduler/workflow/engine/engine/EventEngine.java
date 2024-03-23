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

package org.apache.dolphinscheduler.workflow.engine.engine;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionContext;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionRunnableRepository;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;

@Slf4j
public class EventEngine extends BaseDaemonThread implements IEventEngine {

    private final IWorkflowExecutionRunnableRepository workflowExecuteRunnableRepository;

    private final IEventFirer eventFirer;

    private volatile boolean stop = false;

    public EventEngine(IWorkflowExecutionRunnableRepository workflowExecuteRunnableRepository,
                       IEventFirer eventFirer) {
        super("EventEngine");
        this.workflowExecuteRunnableRepository = workflowExecuteRunnableRepository;
        this.eventFirer = eventFirer;
    }

    @Override
    public synchronized void start() {
        this.stop = false;
        super.start();
        log.info(getClass().getName() + " started");
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                Collection<IWorkflowExecutionRunnable> workflowExecutionRunnableCollection =
                        workflowExecuteRunnableRepository.getActiveWorkflowExecutionRunnable();
                if (CollectionUtils.isEmpty(workflowExecutionRunnableCollection)) {
                    log.debug("There is no active WorkflowExecutionRunnable");
                    this.wait(3_000);
                    continue;
                }
                fireAllActiveEvents(workflowExecutionRunnableCollection);
            } catch (Throwable throwable) {
                log.error("Fire active event error", throwable);
                ThreadUtils.sleep(3_000);
            }
        }
    }

    public void fireAllActiveEvents(Collection<IWorkflowExecutionRunnable> workflowExecutionRunnableList) {
        StopWatch stopWatch = StopWatch.createStarted();

        log.info("Fire all active events cost: {} ms", stopWatch.getTime());
        stopWatch.stop();
        for (IWorkflowExecutionRunnable workflowExecutionRunnable : workflowExecutionRunnableList) {
            IWorkflowExecutionContext workflowExecutionContext =
                    workflowExecutionRunnable.getWorkflowExecutionContext();
            final Integer workflowInstanceId = workflowExecutionContext.getWorkflowInstanceId();
            final String workflowInstanceName = workflowExecutionContext.getWorkflowInstanceName();
            try {
                MDC.put(Constants.WORKFLOW_INSTANCE_ID_MDC_KEY, String.valueOf(workflowInstanceId));
                if (workflowExecutionRunnable.isEventFiring()) {
                    log.debug("WorkflowExecutionRunnable: {} is already in firing", workflowInstanceName);
                    continue;
                }
                eventFirer.fireActiveEvents(workflowExecutionRunnable)
                        .whenComplete((fireCount, ex) -> {
                            workflowExecutionRunnable.setEventFiring(false);
                            if (ex != null) {
                                log.error("Fire event for WorkflowExecutionRunnable: {} error", workflowInstanceName,
                                        ex);
                            } else {
                                if (fireCount > 0) {
                                    log.info("Fire {} events for WorkflowExecutionRunnable: {} success", fireCount,
                                            workflowInstanceName);
                                }
                            }
                        });
            } finally {
                MDC.remove(Constants.WORKFLOW_INSTANCE_ID_MDC_KEY);
            }
        }
    }

    @Override
    public synchronized void shutdown() {
        if (stop) {
            log.warn("EventEngine has already stopped");
            return;
        }
        this.stop = true;
        eventFirer.shutdown();
    }

}
