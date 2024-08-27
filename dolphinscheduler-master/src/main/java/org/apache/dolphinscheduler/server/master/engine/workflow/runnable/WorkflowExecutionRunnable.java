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

package org.apache.dolphinscheduler.server.master.engine.workflow.runnable;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.workflow.listener.IWorkflowLifecycleListener;
import org.apache.dolphinscheduler.server.master.runner.IWorkflowExecuteContext;

import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;

@Slf4j
public class WorkflowExecutionRunnable implements IWorkflowExecutionRunnable {

    @Getter
    private final IWorkflowExecuteContext workflowExecuteContext;

    private final ProcessInstance workflowInstance;

    @Getter
    private final IWorkflowExecutionGraph workflowExecutionGraph;

    @Getter
    private final WorkflowEventBus workflowEventBus;

    @Getter
    private final List<IWorkflowLifecycleListener> workflowInstanceLifecycleListeners;

    public WorkflowExecutionRunnable(WorkflowExecutionRunnableBuilder workflowExecutionRunnableBuilder) {
        final ApplicationContext applicationContext = workflowExecutionRunnableBuilder.getApplicationContext();
        this.workflowExecuteContext = workflowExecutionRunnableBuilder.getWorkflowExecuteContextBuilder().build();
        this.workflowInstance = workflowExecuteContext.getWorkflowInstance();
        this.workflowExecutionGraph = workflowExecuteContext.getWorkflowExecutionGraph();
        this.workflowEventBus = workflowExecuteContext.getWorkflowEventBus();
        this.workflowInstanceLifecycleListeners = workflowExecuteContext.getWorkflowInstanceLifecycleListeners();
    }

    @Override
    public int getId() {
        return workflowInstance.getId();
    }

    @Override
    public String getName() {
        return workflowInstance.getName();
    }

    @Override
    public boolean isWorkflowReadyPause() {
        final WorkflowExecutionStatus workflowExecutionStatus = workflowInstance.getState();
        return workflowExecutionStatus == WorkflowExecutionStatus.READY_PAUSE;
    }

    @Override
    public boolean isWorkflowReadyStop() {
        final WorkflowExecutionStatus workflowExecutionStatus = workflowInstance.getState();
        return workflowExecutionStatus == WorkflowExecutionStatus.READY_STOP;
    }

    @Override
    public ProcessInstance getWorkflowInstance() {
        return workflowExecuteContext.getWorkflowInstance();
    }

    @Override
    public WorkflowExecutionStatus getState() {
        return workflowInstance.getState();
    }

    @Override
    public List<IWorkflowLifecycleListener> getWorkflowLifecycleListeners() {
        return workflowInstanceLifecycleListeners;
    }

    @Override
    public void registerWorkflowInstanceLifecycleListener(IWorkflowLifecycleListener listener) {
        workflowInstanceLifecycleListeners.add(listener);
    }

}
