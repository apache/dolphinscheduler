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

package org.apache.dolphinscheduler.workflow.engine.dag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to build WorkflowDAG, you need to add TaskNode first, then add TaskEdge.
 * After adding all the TaskNodes and TaskEdges, you can call the build method to get the WorkflowDAG.
 * <p>
 * Example:
 * <pre>
 *     {@code
 *          WorkflowDAG workflowDAG = WorkflowDAGBuilder.newBuilder()
 *                 .addTaskNode(taskNodeA)
 *                 .addTaskNode(taskNodeB)
 *                 .addTaskNode(taskNodeC)
 *                 .addTaskEdge(edgeAB)
 *                 .addTaskEdge(edgeBC)
 *                 .build();
 *     }
 * </pre>
 */
public class WorkflowDAGBuilder {

    private final Map<String, DAGNode> taskNameMap;

    private WorkflowDAGBuilder() {
        this.taskNameMap = new HashMap<>();
    }

    public static WorkflowDAGBuilder newBuilder() {
        return new WorkflowDAGBuilder();
    }

    public WorkflowDAGBuilder addTaskNodes(List<DAGNodeDefinition> dagNodes) {
        dagNodes.forEach(this::addTaskNode);
        return this;
    }

    public WorkflowDAGBuilder addTaskNode(DAGNodeDefinition dagNodeDefinition) {
        String nodeName = dagNodeDefinition.getNodeName();
        if (taskNameMap.containsKey(nodeName)) {
            throw new IllegalArgumentException("TaskNode with name " + nodeName + " already exists");
        }

        DAGNode taskNode = DAGNode.builder()
                .nodeName(nodeName)
                .inDegrees(new ArrayList<>())
                .outDegrees(new ArrayList<>())
                .skip(dagNodeDefinition.isSkip())
                .build();
        taskNameMap.put(nodeName, taskNode);
        return this;
    }

    public WorkflowDAGBuilder addTaskEdges(List<DAGEdge> processTaskRelations) {
        processTaskRelations.forEach(this::addTaskEdge);
        return this;
    }

    public WorkflowDAGBuilder addTaskEdge(DAGEdge dagEdge) {
        String fromNodeName = dagEdge.getFromNodeName();
        String toNodeName = dagEdge.getToNodeName();

        if (taskNameMap.containsKey(fromNodeName)) {
            DAGNode fromTask = taskNameMap.get(fromNodeName);
            if (fromTask.getOutDegrees().contains(dagEdge)) {
                throw new IllegalArgumentException(
                        "Edge from " + fromNodeName + " to " + toNodeName + " already exists");
            }
            fromTask.getOutDegrees().add(dagEdge);
        }
        if (taskNameMap.containsKey(toNodeName)) {
            DAGNode toTask = taskNameMap.get(toNodeName);
            if (toTask.getInDegrees().contains(dagEdge)) {
                throw new IllegalArgumentException(
                        "Edge from " + fromNodeName + " to " + toNodeName + " already exists");
            }
            toTask.getInDegrees().add(dagEdge);
        }
        return this;
    }

    public WorkflowDAG build() {
        return new WorkflowDAG(new ArrayList<>(taskNameMap.values()));
    }

}
