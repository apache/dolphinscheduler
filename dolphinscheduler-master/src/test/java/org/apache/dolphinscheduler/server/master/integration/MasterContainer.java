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

package org.apache.dolphinscheduler.server.master.integration;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.eventbus.AbstractDelayEventBus;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBusFireWorkers;
import org.apache.dolphinscheduler.server.master.engine.executor.LogicTaskExecutorContainerProvider;
import org.apache.dolphinscheduler.server.master.engine.executor.LogicTaskExecutorLifecycleEventReporter;
import org.apache.dolphinscheduler.server.master.engine.executor.LogicTaskExecutorRepository;
import org.apache.dolphinscheduler.server.master.engine.system.SystemEventBus;
import org.apache.dolphinscheduler.server.master.engine.task.dispatcher.WorkerGroupDispatcherCoordinator;
import org.apache.dolphinscheduler.task.executor.container.SharedThreadTaskExecutorContainer;
import org.apache.dolphinscheduler.task.executor.container.TaskExecutorAssignmentTable;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MasterContainer {

    @Autowired
    private SystemEventBus systemEventBus;

    @Autowired
    private IWorkflowRepository workflowRepository;

    @Autowired
    private WorkflowEventBusFireWorkers workflowEventBusFireWorkers;

    @Autowired
    private LogicTaskExecutorRepository logicTaskExecutorRepository;

    @Autowired
    private LogicTaskExecutorContainerProvider logicTaskExecutorContainerProvider;

    @Autowired
    private LogicTaskExecutorLifecycleEventReporter logicTaskExecutorLifecycleEventReporter;

    @Autowired
    private WorkerGroupDispatcherCoordinator workerGroupDispatcherCoordinator;

    public void assertAllResourceReleased() {
        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(this::doAssertAllResourceReleased);
    }

    private void doAssertAllResourceReleased() {
        assertWorkflowReleased();
        assertWorkflowEventBusReleased();

        assertSystemEventBusReleased();

        assertLogicTaskEngineReleased();

        assertWorkerGroupDispatcherReleased();
    }

    private void assertWorkflowReleased() {
        assertThat(workflowRepository.getAll()).isEmpty();
        assertThat(workflowEventBusFireWorkers.getWorkers())
                .allMatch(worker -> worker.getRegisteredWorkflowExecuteRunnableSize() == 0);
    }

    private void assertWorkflowEventBusReleased() {
        assertThat(workflowEventBusFireWorkers.getWorkers())
                .allMatch(worker -> worker.getRegisteredWorkflowExecuteRunnableSize() == 0);
    }

    private void assertSystemEventBusReleased() {
        assertThat(systemEventBus).matches(AbstractDelayEventBus::isEmpty);
    }

    private void assertLogicTaskEngineReleased() {
        assertThat(logicTaskExecutorRepository.getAll()).isEmpty();

        final SharedThreadTaskExecutorContainer executorContainer =
                logicTaskExecutorContainerProvider.getExecutorContainer();
        assertThat(executorContainer.getTaskExecutorAssignmentTable()).matches(TaskExecutorAssignmentTable::isEmpty);

        assertThat(executorContainer.getTaskExecutorWorkers().getWorkers())
                .allMatch(taskExecutorWorker -> taskExecutorWorker.getRegisteredTaskExecutorSize() == 0)
                .allMatch(taskExecutorWorker -> taskExecutorWorker.getFiredTaskExecutorSize() == 0);

        assertThat(logicTaskExecutorLifecycleEventReporter.getEventChannels()).isEmpty();
    }

    private void assertWorkerGroupDispatcherReleased() {
        assertThat(workerGroupDispatcherCoordinator.workerGroupDispatchers().values())
                .allMatch(dispatcher -> dispatcher.dispatchEventCount() == 0)
                .allMatch(dispatcher -> dispatcher.waitingDispatchTaskCount() == 0);
    }

}
