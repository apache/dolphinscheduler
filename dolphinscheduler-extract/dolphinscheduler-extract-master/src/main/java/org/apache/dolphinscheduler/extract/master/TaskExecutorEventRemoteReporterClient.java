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

package org.apache.dolphinscheduler.extract.master;

import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorEventRemoteReporterClient;
import org.apache.dolphinscheduler.task.executor.events.IReportableTaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorFailedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorKilledLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorPausedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorRuntimeContextChangedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorStartedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorSuccessLifecycleEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskExecutorEventRemoteReporterClient implements ITaskExecutorEventRemoteReporterClient {

    public void reportTaskExecutionEventToMaster(final IReportableTaskExecutorLifecycleEvent taskExecutorLifecycleEvent) {
        try {
            taskExecutorLifecycleEvent.setLatestReportTime(System.currentTimeMillis());
            switch (taskExecutorLifecycleEvent.getType()) {
                case DISPATCHED:
                    reportTaskDispatchedEventToMaster(
                            (TaskExecutorDispatchedLifecycleEvent) taskExecutorLifecycleEvent);
                    break;
                case RUNNING:
                    reportTaskRunningEventToMaster((TaskExecutorStartedLifecycleEvent) taskExecutorLifecycleEvent);
                    break;
                case RUNTIME_CONTEXT_CHANGE:
                    reportTaskRuntimeContextChangeEventToMaster(
                            (TaskExecutorRuntimeContextChangedLifecycleEvent) taskExecutorLifecycleEvent);
                    break;
                case PAUSED:
                    reportTaskPausedEventToMaster((TaskExecutorPausedLifecycleEvent) taskExecutorLifecycleEvent);
                    break;
                case KILLED:
                    reportTaskKilledEventToMaster((TaskExecutorKilledLifecycleEvent) taskExecutorLifecycleEvent);
                    break;
                case FAILED:
                    reportTaskFailedEventToMaster((TaskExecutorFailedLifecycleEvent) taskExecutorLifecycleEvent);
                    break;
                case SUCCESS:
                    reportTaskSuccessEventToMaster((TaskExecutorSuccessLifecycleEvent) taskExecutorLifecycleEvent);
                    break;
                default:
                    log.warn("Unsupported TaskExecutionEvent: {}", taskExecutorLifecycleEvent);
            }
            log.info("Report: {} to master success", taskExecutorLifecycleEvent);
        } catch (Throwable throwable) {
            log.error("Report ITaskExecutorLifecycleEvent: {} to master failed", taskExecutorLifecycleEvent, throwable);
        }
    }

    private static void reportTaskDispatchedEventToMaster(final TaskExecutorDispatchedLifecycleEvent taskExecutionDispatchedEvent) {
        Clients
                .withService(ITaskExecutorEventListener.class)
                .withHost(taskExecutionDispatchedEvent.getWorkflowInstanceHost())
                .onTaskExecutorDispatched(taskExecutionDispatchedEvent);
    }

    private static void reportTaskRunningEventToMaster(final TaskExecutorStartedLifecycleEvent taskExecutionRunningEvent) {
        Clients
                .withService(ITaskExecutorEventListener.class)
                .withHost(taskExecutionRunningEvent.getWorkflowInstanceHost())
                .onTaskExecutorRunning(taskExecutionRunningEvent);
    }

    private static void reportTaskRuntimeContextChangeEventToMaster(final TaskExecutorRuntimeContextChangedLifecycleEvent taskExecutorLifecycleEvent) {
        Clients
                .withService(ITaskExecutorEventListener.class)
                .withHost(taskExecutorLifecycleEvent.getWorkflowInstanceHost())
                .onTaskExecutorRuntimeContextChanged(taskExecutorLifecycleEvent);
    }

    private static void reportTaskPausedEventToMaster(final TaskExecutorPausedLifecycleEvent taskExecutionPausedEvent) {
        Clients
                .withService(ITaskExecutorEventListener.class)
                .withHost(taskExecutionPausedEvent.getWorkflowInstanceHost())
                .onTaskExecutorPaused(taskExecutionPausedEvent);
    }

    private static void reportTaskKilledEventToMaster(final TaskExecutorKilledLifecycleEvent taskExecutionKilledEvent) {
        Clients
                .withService(ITaskExecutorEventListener.class)
                .withHost(taskExecutionKilledEvent.getWorkflowInstanceHost())
                .onTaskExecutorKilled(taskExecutionKilledEvent);
    }

    private static void reportTaskFailedEventToMaster(final TaskExecutorFailedLifecycleEvent taskExecutionFailedEvent) {
        Clients
                .withService(ITaskExecutorEventListener.class)
                .withHost(taskExecutionFailedEvent.getWorkflowInstanceHost())
                .onTaskExecutorFailed(taskExecutionFailedEvent);
    }

    private static void reportTaskSuccessEventToMaster(final TaskExecutorSuccessLifecycleEvent taskExecutionSuccessEvent) {
        Clients
                .withService(ITaskExecutorEventListener.class)
                .withHost(taskExecutionSuccessEvent.getWorkflowInstanceHost())
                .onTaskExecutorSuccess(taskExecutionSuccessEvent);
    }
}
