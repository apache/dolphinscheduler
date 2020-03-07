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

package org.apache.dolphinscheduler.dao.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.process.ProcessDag;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * dag helper test
 */
public class DagHelperTest {
    /**
     * test task node can submit
     * @throws JsonProcessingException if error throws JsonProcessingException
     */
    @Test
    public void testTaskNodeCanSubmit() throws JsonProcessingException {
        //1->2->3->5
        //4->3
        DAG<String, TaskNode, TaskNodeRelation> dag = generateDag();
        TaskNode taskNode3 = dag.getNode("3");
        Map<String, TaskInstance > completeTaskList = new HashMap<>();
        completeTaskList.putIfAbsent("1", new TaskInstance());
        Boolean canSubmit = false;

        // 2/4 are forbidden submit 3
        TaskNode node2 = dag.getNode("2");
        node2.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        TaskNode nodex = dag.getNode("4");
        nodex.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        canSubmit = DagHelper.taskNodeCanSubmit(taskNode3, dag, completeTaskList);
        Assert.assertEquals(canSubmit, true);

        // 2forbidden, 3 cannot be submit
        completeTaskList.putIfAbsent("2", new TaskInstance());
        TaskNode nodey = dag.getNode("4");
        nodey.setRunFlag("");
        canSubmit = DagHelper.taskNodeCanSubmit(taskNode3, dag, completeTaskList);
        Assert.assertEquals(canSubmit, false);

        // 2/3 forbidden submit 5
        TaskNode node3 = dag.getNode("3");
        node3.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        TaskNode node5 = dag.getNode("5");
        canSubmit = DagHelper.taskNodeCanSubmit(node5, dag, completeTaskList);
        Assert.assertEquals(canSubmit, true);
   }

    /**
     * 1->2->3->5
     * 4->3
     * @return dag
     * @throws JsonProcessingException if error throws JsonProcessingException
     */
    private DAG<String, TaskNode, TaskNodeRelation> generateDag() throws JsonProcessingException {
        List<TaskNode> taskNodeList = new ArrayList<>();
        TaskNode node1 = new TaskNode();
        node1.setId("1");
        node1.setName("1");
        taskNodeList.add(node1);

        TaskNode node2 = new TaskNode();
        node2.setId("2");
        node2.setName("2");
        List<String> dep2 = new ArrayList<>();
        dep2.add("1");
        node2.setDepList(dep2);
        taskNodeList.add(node2);


        TaskNode node4 = new TaskNode();
        node4.setId("4");
        node4.setName("4");
        taskNodeList.add(node4);

        TaskNode node3 = new TaskNode();
        node3.setId("3");
        node3.setName("3");
        List<String> dep3 = new ArrayList<>();
        dep3.add("2");
        dep3.add("4");
        node3.setDepList(dep3);
        taskNodeList.add(node3);

        TaskNode node5 = new TaskNode();
        node5.setId("5");
        node5.setName("5");
        List<String> dep5 = new ArrayList<>();
        dep5.add("3");
        node5.setDepList(dep5);
        taskNodeList.add(node5);

        List<String> startNodes = new ArrayList<>();
        List<String> recoveryNodes  = new ArrayList<>();
        List<TaskNode> destTaskNodeList = DagHelper.generateFlowNodeListByStartNode(taskNodeList,
                startNodes, recoveryNodes, TaskDependType.TASK_POST);
        List<TaskNodeRelation> taskNodeRelations =DagHelper.generateRelationListByFlowNodes(destTaskNodeList);
        ProcessDag processDag = new ProcessDag();
        processDag.setEdges(taskNodeRelations);
        processDag.setNodes(destTaskNodeList);

        return DagHelper.buildDagGraph(processDag);
    }

    @Test
    public void testBuildDagGraph() {
        String shellJson = "{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-9527\",\"name\":\"shell-1\"," +
                "\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"#!/bin/bash\\necho \\\"shell-1\\\"\"}," +
                "\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\"," +
                "\"timeout\":{\"strategy\":\"\",\"interval\":1,\"enable\":false},\"taskInstancePriority\":\"MEDIUM\"," +
                "\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":1,\"timeout\":0}";

        ProcessData processData = JSONUtils.parseObject(shellJson, ProcessData.class);
        assert processData != null;
        List<TaskNode> taskNodeList = processData.getTasks();
        ProcessDag processDag = DagHelper.getProcessDag(taskNodeList);
        DAG<String, TaskNode, TaskNodeRelation> dag = DagHelper.buildDagGraph(processDag);
        Assert.assertNotNull(dag);
    }

}
