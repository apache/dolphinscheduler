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
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowGraph;
import org.apache.dolphinscheduler.server.master.engine.workflow.listener.IWorkflowLifecycleListener;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class WorkflowExecuteContext implements IWorkflowExecuteContext {

    private final Command command;

    private final ProcessDefinition workflowDefinition;

    private final ProcessInstance workflowInstance;

    private final IWorkflowGraph workflowGraph;

    private final IWorkflowExecutionGraph workflowExecutionGraph;

    private final WorkflowEventBus workflowEventBus;

    private final List<IWorkflowLifecycleListener> workflowInstanceLifecycleListeners;

    public static WorkflowExecuteContextBuilder builder() {
        return new WorkflowExecuteContextBuilder();
    }

    @Data
    @NoArgsConstructor
    public static class WorkflowExecuteContextBuilder {

        private Command command;

        private ProcessDefinition workflowDefinition;

        private ProcessInstance workflowInstance;

        private IWorkflowGraph workflowGraph;

        private IWorkflowExecutionGraph workflowExecutionGraph;

        private WorkflowEventBus workflowEventBus;

        private List<IWorkflowLifecycleListener> workflowInstanceLifecycleListeners;

        public WorkflowExecuteContextBuilder withCommand(Command command) {
            this.command = command;
            return this;
        }

        public WorkflowExecuteContextBuilder withWorkflowInstanceLifecycleListeners(List<IWorkflowLifecycleListener> workflowLifecycleListeners) {
            this.workflowInstanceLifecycleListeners = workflowLifecycleListeners;
            return this;
        }

        public WorkflowExecuteContext build() {
            return new WorkflowExecuteContext(
                    command,
                    workflowDefinition,
                    workflowInstance,
                    workflowGraph,
                    workflowExecutionGraph,
                    workflowEventBus,
                    workflowInstanceLifecycleListeners);
        }
    }

}
