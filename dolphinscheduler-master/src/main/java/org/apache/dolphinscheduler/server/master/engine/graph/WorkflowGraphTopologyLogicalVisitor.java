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
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;

import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

public class WorkflowGraphTopologyLogicalVisitor {

    private final IWorkflowGraph workflowGraph;

    private final TaskDependType taskDependType;

    private final Set<String> startNodes;

    private final BiConsumer<String, Set<String>> visitFunction;

    private WorkflowGraphTopologyLogicalVisitor(WorkflowGraphBfsVisitorBuilder workflowGraphBfsVisitorBuilder) {
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
     * Visit start nodes only.
     */
    private void visitStartNodesOnly() {
        doVisitationInSubGraph(Sets.newHashSet(startNodes));
    }

    /**
     * Find the graph nodes that can be reached to the start nodes, and then do visitation with topology logical.
     */
    private void visitToStartNodes() {
        final LinkedList<String> bootstrapTaskCodes = new LinkedList<>(startNodes);
        final Set<String> subGraphNodes = new HashSet<>();
        while (!bootstrapTaskCodes.isEmpty()) {
            String taskName = bootstrapTaskCodes.removeFirst();
            if (subGraphNodes.contains(taskName)) {
                continue;
            }
            subGraphNodes.add(taskName);
            final Set<String> successors = workflowGraph.getPredecessors(taskName);
            bootstrapTaskCodes.addAll(successors);
        }
        doVisitationInSubGraph(subGraphNodes);
    }

    /**
     * Find the graph nodes that can be reached from the start nodes, and then do visitation with topology logical.
     */
    private void visitFromStartNodes() {
        final LinkedList<String> bootstrapTaskCodes = new LinkedList<>(startNodes);
        final Set<String> subGraphNodes = new HashSet<>();
        while (!bootstrapTaskCodes.isEmpty()) {
            String taskName = bootstrapTaskCodes.removeFirst();
            if (subGraphNodes.contains(taskName)) {
                continue;
            }
            subGraphNodes.add(taskName);
            final Set<String> successors = workflowGraph.getSuccessors(taskName);
            bootstrapTaskCodes.addAll(successors);
        }
        doVisitationInSubGraph(subGraphNodes);
    }

    private void doVisitationInSubGraph(Set<String> subGraphNodes) {
        // visit from the workflow graph by topology
        // If the node is not in the subGraph, then skip it.
        Map<String, Integer> inDegreeMap = workflowGraph.getAllTaskNodes()
                .stream()
                .collect(Collectors.toMap(TaskDefinition::getName,
                        taskDefinition -> workflowGraph.getPredecessors(taskDefinition.getName()).size()));
        final LinkedList<String> bootstrapTaskCodes = inDegreeMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!bootstrapTaskCodes.isEmpty()) {
            String taskName = bootstrapTaskCodes.removeFirst();
            if (inDegreeMap.get(taskName) > 0) {
                continue;
            }
            final Set<String> successors = workflowGraph.getSuccessors(taskName);
            if (subGraphNodes.contains(taskName)) {
                visitFunction.accept(taskName, successors);
            }
            for (String successor : successors) {
                inDegreeMap.put(successor, inDegreeMap.get(successor) - 1);
            }
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

        public WorkflowGraphTopologyLogicalVisitor build() {
            return new WorkflowGraphTopologyLogicalVisitor(this);
        }
    }
}
