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

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.master.graph.IWorkflowGraph;

import lombok.Getter;

public class WorkflowExecuteContext implements IWorkflowExecuteContext {

    @Getter
    private final ProcessDefinition workflowDefinition;

    @Getter
    private final ProcessInstance workflowInstance;

    // This is the task definition graph
    // todo: we need to add a task instance graph, then move the task instance from WorkflowExecuteRunnable to
    // WorkflowExecuteContext
    @Getter
    private final IWorkflowGraph workflowGraph;

    public WorkflowExecuteContext(ProcessDefinition workflowDefinition,
                                  ProcessInstance workflowInstance,
                                  IWorkflowGraph workflowGraph) {
        checkNotNull(workflowDefinition, "workflowDefinition is null");
        checkNotNull(workflowInstance, "workflowInstance is null");
        checkNotNull(workflowGraph, "workflowGraph is null");

        this.workflowDefinition = workflowDefinition;
        this.workflowInstance = workflowInstance;
        this.workflowGraph = workflowGraph;
    }

}
