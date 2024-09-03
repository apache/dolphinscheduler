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

package org.apache.dolphinscheduler.server.master.engine.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.common.enums.TaskDependType;

import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class WorkflowGraphBfsVisitor {

    private IWorkflowGraph workflowGraph;

    private TaskDependType taskDependType;

    private Set<String> startNodes;

    private BiConsumer<String, Set<String>> visitFunction;

    private WorkflowGraphBfsVisitor(WorkflowGraphBfsVisitorBuilder workflowGraphBfsVisitorBuilder) {
        this.taskDependType = workflowGraphBfsVisitorBuilder.taskDependType;
        this.workflowGraph = checkNotNull(workflowGraphBfsVisitorBuilder.workflowGraph);
        this.visitFunction = checkNotNull(workflowGraphBfsVisitorBuilder.visitFunction);
        if (CollectionUtils.isEmpty(workflowGraphBfsVisitorBuilder.startNodes)) {
            this.startNodes = new HashSet<>(workflowGraph.getStartNodes());
        } else {
            this.startNodes = new HashSet<>(checkNotNull(workflowGraphBfsVisitorBuilder.startNodes));
        }
    }

    public static WorkflowGraphBfsVisitorBuilder builder() {
        return new WorkflowGraphBfsVisitorBuilder();
    }

    public void visit() {
        switch (taskDependType) {
            case TASK_ONLY:
                visitStartNodesOnly();
                break;
            case TASK_PRE:
                visitToStartNodes();
                break;
            case TASK_POST:
                visitFromStartNodes();
                break;
            default:
                throw new IllegalArgumentException("Unsupported task depend type: " + taskDependType);
        }
    }

    /**
     * visit start nodes only
     */
    private void visitStartNodesOnly() {
        startNodes.forEach(startNode -> {
            final Set<String> realSuccessors = workflowGraph.getSuccessors(startNode)
                    .stream()
                    .filter(startNode::contains)
                    .collect(Collectors.toSet());
            visitFunction.accept(startNode, realSuccessors);
        });
    }

    /**
     * Find the graph nodes that can be reached to the start nodes
     */
    private void visitToStartNodes() {
        final LinkedList<String> bootstrapTaskCodes = new LinkedList<>(startNodes);
        final Set<String> visited = new HashSet<>();
        while (!bootstrapTaskCodes.isEmpty()) {
            String taskName = bootstrapTaskCodes.removeFirst();
            if (visited.contains(taskName)) {
                continue;
            }
            visited.add(taskName);
            final Set<String> successors = workflowGraph.getPredecessors(taskName);
            bootstrapTaskCodes.addAll(successors);
        }
        visited.forEach(taskName -> {
            Set<String> realSuccessors = workflowGraph.getSuccessors(taskName)
                    .stream()
                    .filter(visited::contains)
                    .collect(Collectors.toSet());
            visitFunction.accept(taskName, realSuccessors);
        });
    }

    /**
     * Find the graph nodes that can be reached from the start nodes
     */
    private void visitFromStartNodes() {
        final LinkedList<String> bootstrapTaskCodes = new LinkedList<>(startNodes);
        final Set<String> visited = new HashSet<>();

        while (!bootstrapTaskCodes.isEmpty()) {
            String taskName = bootstrapTaskCodes.removeFirst();
            if (visited.contains(taskName)) {
                continue;
            }
            visited.add(taskName);
            final Set<String> successors = workflowGraph.getSuccessors(taskName);
            visitFunction.accept(taskName, successors);
            bootstrapTaskCodes.addAll(successors);
        }

    }

    public static class WorkflowGraphBfsVisitorBuilder {

        private IWorkflowGraph workflowGraph;

        private List<String> startNodes;

        private TaskDependType taskDependType = TaskDependType.TASK_POST;

        private BiConsumer<String, Set<String>> visitFunction;

        public WorkflowGraphBfsVisitorBuilder onWorkflowGraph(IWorkflowGraph workflowGraph) {
            this.workflowGraph = workflowGraph;
            return this;
        }

        public WorkflowGraphBfsVisitorBuilder taskDependType(TaskDependType taskDependType) {
            this.taskDependType = taskDependType;
            return this;
        }

        public WorkflowGraphBfsVisitorBuilder fromTask(List<String> startNodes) {
            this.startNodes = startNodes;
            return this;
        }

        public WorkflowGraphBfsVisitorBuilder doVisitFunction(BiConsumer<String, Set<String>> visitFunction) {
            this.visitFunction = visitFunction;
            return this;
        }

        public WorkflowGraphBfsVisitor build() {
            return new WorkflowGraphBfsVisitor(this);
        }
    }
}
