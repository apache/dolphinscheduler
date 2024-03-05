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

package org.apache.dolphinscheduler.workflow.engine.event;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionContext;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionRunnable;

import org.apache.commons.lang3.time.StopWatch;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;

@Slf4j
public class EventEngine extends BaseDaemonThread {

    private final IWorkflowExecuteRunnableRepository workflowExecuteRunnableRepository;

    private final EventFirer eventFirer;

    private final Set<Integer> firingWorkflowInstanceIds;

    public EventEngine(IWorkflowExecuteRunnableRepository workflowExecuteRunnableRepository,
                       EventFirer eventFirer) {
        super("EventEngine");
        this.workflowExecuteRunnableRepository = workflowExecuteRunnableRepository;
        this.eventFirer = eventFirer;
        this.firingWorkflowInstanceIds = ConcurrentHashMap.newKeySet();
    }

    @Override
    public synchronized void start() {
        super.start();
        log.info(getClass().getName() + " started");
    }

    @Override
    public void run() {
        for (;;) {
            try {
                StopWatch stopWatch = StopWatch.createStarted();
                fireAllActiveEvents();
                stopWatch.stop();
                log.info("Fire all active events cost: {} ms", stopWatch.getTime());
                this.wait(5_000);
            } catch (Throwable throwable) {
                log.error("Fire active event error", throwable);
                ThreadUtils.sleep(3_000);
            }
        }
    }

    public void fireAllActiveEvents() {
        Collection<IWorkflowExecutionRunnable> workflowExecutionRunnableCollection =
                workflowExecuteRunnableRepository.getActiveWorkflowExecutionRunnable();
        for (IWorkflowExecutionRunnable workflowExecutionRunnable : workflowExecutionRunnableCollection) {
            IWorkflowExecutionContext workflowExecutionContext =
                    workflowExecutionRunnable.getWorkflowExecutionContext();
            final Integer workflowInstanceId = workflowExecutionContext.getWorkflowInstanceId();
            final String workflowInstanceName = workflowExecutionContext.getWorkflowInstanceName();
            try {
                MDC.put(Constants.WORKFLOW_INSTANCE_ID_MDC_KEY, String.valueOf(workflowInstanceId));
                if (firingWorkflowInstanceIds.contains(workflowInstanceId)) {
                    log.debug("WorkflowExecutionRunnable: {} is already in firing", workflowInstanceName);
                    return;
                }
                IEventRepository workflowEventRepository = workflowExecutionRunnable.getEventRepository();
                firingWorkflowInstanceIds.add(workflowInstanceId);
                eventFirer.fireActiveEvents(workflowEventRepository)
                        .whenComplete((fireCount, ex) -> {
                            firingWorkflowInstanceIds.remove(workflowInstanceId);
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

}
