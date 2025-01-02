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

package org.apache.dolphinscheduler.server.worker.executor;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.TaskEngine;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorReassignMasterRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PhysicalTaskEngineDelegator implements AutoCloseable {

    private final TaskEngine taskEngine;

    private final PhysicalTaskExecutorFactory physicalTaskExecutorFactory;

    private final PhysicalTaskExecutorLifecycleEventReporter physicalTaskExecutorEventReporter;

    private final PhysicalTaskExecutorRepository physicalTaskExecutorRepository;

    public PhysicalTaskEngineDelegator(final PhysicalTaskEngineFactory physicalTaskEngineFactory,
                                       final PhysicalTaskExecutorFactory physicalTaskExecutorFactory,
                                       final PhysicalTaskExecutorRepository physicalTaskExecutorRepository,
                                       final PhysicalTaskExecutorLifecycleEventReporter physicalTaskExecutorEventReporter) {
        this.physicalTaskExecutorFactory = physicalTaskExecutorFactory;
        this.taskEngine = physicalTaskEngineFactory.createTaskEngine();
        this.physicalTaskExecutorRepository = physicalTaskExecutorRepository;
        this.physicalTaskExecutorEventReporter = physicalTaskExecutorEventReporter;
    }

    public void start() {
        taskEngine.start();
        physicalTaskExecutorEventReporter.start();
        log.info("PhysicalTaskEngineDelegator started");
    }

    public void dispatchLogicTask(final TaskExecutionContext taskExecutionContext) {
        final ITaskExecutor taskExecutor = physicalTaskExecutorFactory.createTaskExecutor(taskExecutionContext);
        taskEngine.submitTask(taskExecutor);
    }

    public void killLogicTask(final int taskInstanceId) {
        taskEngine.killTask(taskInstanceId);
    }

    public void pauseLogicTask(final int taskInstanceId) {
        taskEngine.pauseTask(taskInstanceId);
    }

    public void ackPhysicalTaskExecutorLifecycleEventACK(final ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck taskExecutorLifecycleEventAck) {
        physicalTaskExecutorEventReporter.receiveTaskExecutorLifecycleEventACK(taskExecutorLifecycleEventAck);
    }

    public boolean reassignWorkflowInstanceHost(final TaskExecutorReassignMasterRequest taskExecutorReassignMasterRequest) {
        final int taskInstanceId = taskExecutorReassignMasterRequest.getTaskInstanceId();
        final String workflowHost = taskExecutorReassignMasterRequest.getWorkflowHost();
        // todo: Is this reassign can make sure there is no concurrent problem?
        physicalTaskExecutorRepository.get(taskInstanceId).ifPresent(
                taskExecutor -> taskExecutor.getTaskExecutionContext().setWorkflowInstanceHost(workflowHost));
        return physicalTaskExecutorEventReporter.reassignWorkflowInstanceHost(taskInstanceId, workflowHost);
    }

    @Override
    public void close() {
        taskEngine.close();
    }
}
