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

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.registry.api.ha.AbstractHAServer;
import org.apache.dolphinscheduler.registry.api.ha.AbstractServerStatusChangeListener;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.failover.IFailoverCoordinator;
import org.apache.dolphinscheduler.server.master.utils.MasterThreadFactory;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * The MasterCoordinator is singleton at the clusters, which is used to do some control work, e.g manage the {@link ITaskGroupCoordinator}
 */
@Slf4j
@Component
public class MasterCoordinator extends AbstractHAServer {

    private final ITaskGroupCoordinator taskGroupCoordinator;

    private final IFailoverCoordinator failoverCoordinator;

    private final IWorkflowSerialCoordinator workflowSerialCoordinator;

    public MasterCoordinator(final Registry registry,
                             final MasterConfig masterConfig,
                             final ITaskGroupCoordinator taskGroupCoordinator,
                             final IFailoverCoordinator failoverCoordinator,
                             final IWorkflowSerialCoordinator workflowSerialCoordinator) {
        super(
                registry,
                RegistryNodeType.MASTER_COORDINATOR.getRegistryPath(),
                masterConfig.getMasterAddress());
        this.taskGroupCoordinator = taskGroupCoordinator;
        this.failoverCoordinator = failoverCoordinator;
        this.workflowSerialCoordinator = workflowSerialCoordinator;
        addServerStatusChangeListener(
                new MasterCoordinatorListener(taskGroupCoordinator, failoverCoordinator, workflowSerialCoordinator));
    }

    @Override
    public void start() {
        super.start();
        log.info("MasterCoordinator started...");
    }

    @Override
    public void close() {
        taskGroupCoordinator.close();
        log.info("MasterCoordinator shutdown...");
    }

    public static class MasterCoordinatorListener extends AbstractServerStatusChangeListener {

        private final ITaskGroupCoordinator taskGroupCoordinator;

        private final IFailoverCoordinator failoverCoordinator;

        private final IWorkflowSerialCoordinator workflowSerialCoordinator;
        private Future<?> failoverCoordinatorFuture;

        public MasterCoordinatorListener(ITaskGroupCoordinator taskGroupCoordinator,
                                         IFailoverCoordinator failoverCoordinator,
                                         IWorkflowSerialCoordinator workflowSerialCoordinator) {
            this.taskGroupCoordinator = checkNotNull(taskGroupCoordinator);
            this.failoverCoordinator = checkNotNull(failoverCoordinator);
            this.workflowSerialCoordinator = checkNotNull(workflowSerialCoordinator);
        }

        @Override
        public void changeToActive() {
            taskGroupCoordinator.start();
            workflowSerialCoordinator.start();
            failoverCoordinatorFuture =
                    MasterThreadFactory.getDefaultSchedulerThreadExecutor().scheduleWithFixedDelay(() -> {
                        try {
                            failoverCoordinator.cleanHistoryFailoverFinishedMarks();
                        } catch (Exception e) {
                            log.error("FailoverCoordinator cleanHistoryFailoverFinishedMarks failed", e);
                        }
                    }, 0, 1, TimeUnit.DAYS);
        }

        @Override
        public void changeToStandBy() {
            taskGroupCoordinator.close();
            workflowSerialCoordinator.close();
            if (failoverCoordinatorFuture != null) {
                failoverCoordinatorFuture.cancel(true);
            }
        }
    }

}
