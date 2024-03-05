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

package org.apache.dolphinscheduler.server.master.events;

import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.dag.IWorkflowExecutionContext;
import org.apache.dolphinscheduler.server.master.dag.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.dag.WorkflowExecuteRunnableRepository;

import org.apache.commons.lang3.time.StopWatch;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventEngine extends BaseDaemonThread {

    @Autowired
    private WorkflowExecuteRunnableRepository workflowExecuteRunnableRepository;

    @Autowired
    private EventFirer eventFirer;

    private final Set<Integer> firingWorkflowInstanceIds = ConcurrentHashMap.newKeySet();

    public EventEngine() {
        super("EventEngine");
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
                LogUtils.setWorkflowInstanceIdMDC(workflowInstanceId);
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
                LogUtils.removeWorkflowInstanceIdMDC();
            }
        }
    }

}
