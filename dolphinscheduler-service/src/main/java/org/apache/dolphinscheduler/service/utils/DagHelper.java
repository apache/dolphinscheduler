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

package org.apache.dolphinscheduler.service.utils;

import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.WorkflowDag;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * dag tools
 */
@Slf4j
public class DagHelper {

    /***
     * build dag graph
     * @param workflowDag workflowDag
     * @return dag
     */
    public static DAG<Long, TaskNode, TaskNodeRelation> buildDagGraph(WorkflowDag workflowDag) {

        DAG<Long, TaskNode, TaskNodeRelation> dag = new DAG<>();

        // add vertex
        if (CollectionUtils.isNotEmpty(workflowDag.getNodes())) {
            for (TaskNode node : workflowDag.getNodes()) {
                dag.addNode(node.getCode(), node);
            }
        }

        // add edge
        if (CollectionUtils.isNotEmpty(workflowDag.getEdges())) {
            for (TaskNodeRelation edge : workflowDag.getEdges()) {
                dag.addEdge(edge.getStartNode(), edge.getEndNode());
            }
        }
        return dag;
    }

    /**
     * get workflow dag
     *
     * @param taskNodeList task node list
     * @return workflow dag
     */
    public static WorkflowDag getWorkflowDag(List<TaskNode> taskNodeList,
                                             List<WorkflowTaskRelation> workflowTaskRelations) {
        Map<Long, TaskNode> taskNodeMap = new HashMap<>();

        taskNodeList.forEach(taskNode -> {
            taskNodeMap.putIfAbsent(taskNode.getCode(), taskNode);
        });

        List<TaskNodeRelation> taskNodeRelations = new ArrayList<>();
        for (WorkflowTaskRelation workflowTaskRelation : workflowTaskRelations) {
            long preTaskCode = workflowTaskRelation.getPreTaskCode();
            long postTaskCode = workflowTaskRelation.getPostTaskCode();

            if (workflowTaskRelation.getPreTaskCode() != 0
                    && taskNodeMap.containsKey(preTaskCode) && taskNodeMap.containsKey(postTaskCode)) {
                TaskNode preNode = taskNodeMap.get(preTaskCode);
                TaskNode postNode = taskNodeMap.get(postTaskCode);
                taskNodeRelations
                        .add(new TaskNodeRelation(preNode.getCode(), postNode.getCode()));
            }
        }
        WorkflowDag workflowDag = new WorkflowDag();
        workflowDag.setEdges(taskNodeRelations);
        workflowDag.setNodes(taskNodeList);
        return workflowDag;
    }

}
