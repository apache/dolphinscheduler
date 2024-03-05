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

package org.apache.dolphinscheduler.server.master.dag;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;

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

    private final Map<Long, DAGNode> taskCodeMap;

    private WorkflowDAGBuilder() {
        this.taskCodeMap = new HashMap<>();
        this.taskNameMap = new HashMap<>();
    }

    public static WorkflowDAGBuilder newBuilder() {
        return new WorkflowDAGBuilder();
    }

    public WorkflowDAGBuilder addTaskNodes(List<TaskDefinitionLog> taskDefinitionList) {
        taskDefinitionList.forEach(this::addTaskNode);
        return this;
    }

    public WorkflowDAGBuilder addTaskNode(TaskDefinitionLog taskDefinition) {
        String taskName = taskDefinition.getName();
        long taskCode = taskDefinition.getCode();
        if (taskCodeMap.containsKey(taskCode)) {
            throw new IllegalArgumentException("TaskNode with code " + taskCode + " already exists");
        }
        if (taskNameMap.containsKey(taskName)) {
            throw new IllegalArgumentException("TaskNode with name " + taskName + " already exists");
        }

        DAGNode taskNode = DAGNode.builder()
                .nodeName(taskName)
                .inDegrees(new ArrayList<>())
                .outDegrees(new ArrayList<>())
                .skip(Flag.NO.equals(taskDefinition.getFlag()))
                .build();
        taskNameMap.put(taskName, taskNode);
        taskCodeMap.put(taskCode, taskNode);
        return this;
    }

    public WorkflowDAGBuilder addTaskEdges(List<ProcessTaskRelationLog> processTaskRelations) {
        processTaskRelations.forEach(this::addTaskEdge);
        return this;
    }

    public WorkflowDAGBuilder addTaskEdge(ProcessTaskRelationLog processTaskRelation) {
        long preTaskCode = processTaskRelation.getPreTaskCode();
        long postTaskCode = processTaskRelation.getPostTaskCode();

        if (taskCodeMap.containsKey(preTaskCode)) {
            DAGNode fromTask = taskCodeMap.get(preTaskCode);
            if (taskCodeMap.containsKey(postTaskCode)) {
                DAGNode toTask = taskCodeMap.get(postTaskCode);
                DAGEdge edge = DAGEdge.builder()
                        .fromNodeName(fromTask.getNodeName())
                        .toNodeName(toTask.getNodeName())
                        .build();
                if (fromTask.getOutDegrees().contains(edge)) {
                    throw new IllegalArgumentException(
                            "Edge from " + fromTask.getNodeName() + " to " + toTask.getNodeName() + " already exists");
                }
                fromTask.getOutDegrees().add(edge);
                if (toTask.getInDegrees().contains(edge)) {
                    throw new IllegalArgumentException(
                            "Edge from " + fromTask.getNodeName() + " to " + toTask.getNodeName() + " already exists");
                }
                toTask.getInDegrees().add(edge);
            }
        }
        return this;
    }

    public WorkflowDAG build() {
        return WorkflowDAG.builder()
                .dagNodeMap(taskNameMap)
                .build();
    }

}
