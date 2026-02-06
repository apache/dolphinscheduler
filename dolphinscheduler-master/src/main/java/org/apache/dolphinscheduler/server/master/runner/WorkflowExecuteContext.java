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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowGraph;
import org.apache.dolphinscheduler.server.master.engine.workflow.listener.IWorkflowLifecycleListener;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class WorkflowExecuteContext implements IWorkflowExecuteContext {

    private final Command command;

    private final WorkflowDefinition workflowDefinition;

    private final Project project;

    private final WorkflowInstance workflowInstance;

    private final IWorkflowGraph workflowGraph;

    private volatile IWorkflowExecutionGraph workflowExecutionGraph;

    private final WorkflowEventBus workflowEventBus;

    private final List<IWorkflowLifecycleListener> workflowInstanceLifecycleListeners;

    public WorkflowExecuteContext(Command command,
                                  WorkflowDefinition workflowDefinition,
                                  Project project,
                                  WorkflowInstance workflowInstance,
                                  IWorkflowGraph workflowGraph,
                                  IWorkflowExecutionGraph workflowExecutionGraph,
                                  WorkflowEventBus workflowEventBus,
                                  List<IWorkflowLifecycleListener> workflowInstanceLifecycleListeners) {
        this.command = command;
        this.workflowDefinition = workflowDefinition;
        this.project = project;
        this.workflowInstance = workflowInstance;
        this.workflowGraph = workflowGraph;
        this.workflowExecutionGraph = workflowExecutionGraph;
        this.workflowEventBus = workflowEventBus;
        this.workflowInstanceLifecycleListeners = workflowInstanceLifecycleListeners;
    }

    /**
     * Set the workflow execution graph.
     * This method should be called when the workflow is ready to start execution,
     * typically during the handling of WorkflowStartLifecycleEvent.
     *
     * @param workflowExecutionGraph the workflow execution graph to set
     */
    @Override
    public void setWorkflowExecutionGraph(final IWorkflowExecutionGraph workflowExecutionGraph) {
        this.workflowExecutionGraph = workflowExecutionGraph;
    }

    /**
     * Check if the workflow execution graph has been initialized.
     */
    @Override
    public boolean isWorkflowExecutionGraphInitialized() {
        return workflowExecutionGraph != null;
    }

    public static WorkflowExecuteContextBuilder builder() {
        return new WorkflowExecuteContextBuilder();
    }

    @Data
    @NoArgsConstructor
    public static class WorkflowExecuteContextBuilder {

        private Command command;

        private WorkflowDefinition workflowDefinition;

        private WorkflowInstance workflowInstance;

        private IWorkflowGraph workflowGraph;

        private IWorkflowExecutionGraph workflowExecutionGraph;

        private WorkflowEventBus workflowEventBus;

        private List<IWorkflowLifecycleListener> workflowInstanceLifecycleListeners;

        private Project project;

        public WorkflowExecuteContextBuilder withCommand(Command command) {
            this.command = command;
            return this;
        }

        public WorkflowExecuteContext build() {
            return new WorkflowExecuteContext(
                    command,
                    workflowDefinition,
                    project,
                    workflowInstance,
                    workflowGraph,
                    workflowExecutionGraph,
                    workflowEventBus,
                    Optional.ofNullable(workflowInstanceLifecycleListeners).orElse(Collections.emptyList()));
        }
    }

}
