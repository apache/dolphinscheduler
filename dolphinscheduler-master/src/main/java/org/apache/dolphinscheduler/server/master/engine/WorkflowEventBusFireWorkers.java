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

package org.apache.dolphinscheduler.server.master.engine;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;

@Slf4j
@Component
public class WorkflowEventBusFireWorkers implements AutoCloseable {

    @Autowired
    private List<ILifecycleEventHandler> eventHandlers;

    @Autowired
    private MasterConfig masterConfig;

    private static final long DEFAULT_FIRE_INTERVAL = 100;

    private WorkflowEventBusFireWorker[] workflowEventBusFireWorkers;

    private ScheduledExecutorService workflowEventBusFireThreadPool;

    public void start() {
        final int workflowEventBusFireThreadCount = masterConfig.getWorkflowEventBusFireThreadCount();
        workflowEventBusFireThreadPool = Executors.newScheduledThreadPool(
                workflowEventBusFireThreadCount,
                ThreadUtils.newDaemonThreadFactory("ds-workflow-eventbus-worker-%d"));
        workflowEventBusFireWorkers = new WorkflowEventBusFireWorker[workflowEventBusFireThreadCount];

        for (int i = 0; i < workflowEventBusFireThreadCount; i++) {
            final WorkflowEventBusFireWorker workflowEventBusFireWorker = new WorkflowEventBusFireWorker();
            eventHandlers.forEach(workflowEventBusFireWorker::registerEventHandler);
            workflowEventBusFireWorkers[i] = workflowEventBusFireWorker;

            workflowEventBusFireThreadPool.scheduleWithFixedDelay(
                    workflowEventBusFireWorker::fireAllRegisteredEvent,
                    DEFAULT_FIRE_INTERVAL,
                    // todo: do not use a fixed interval for all worker, each worker use wait notify to control the fire
                    // interval
                    DEFAULT_FIRE_INTERVAL,
                    TimeUnit.MILLISECONDS);
        }
        log.info("WorkflowEventBusFireWorkers started, worker size: {}", workflowEventBusFireThreadCount);
    }

    public WorkflowEventBusFireWorker getWorker(Integer workerSlot) {
        return workflowEventBusFireWorkers[workerSlot];
    }

    @VisibleForTesting
    public WorkflowEventBusFireWorker[] getWorkers() {
        return workflowEventBusFireWorkers;
    }

    public int getWorkerSize() {
        return masterConfig.getWorkflowEventBusFireThreadCount();
    }

    @Override
    public void close() throws Exception {
        if (workflowEventBusFireThreadPool != null) {
            workflowEventBusFireThreadPool.shutdown();
        }
        log.info("WorkflowEventBusFireWorkers closed");
    }
}
