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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_CONDITIONS;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.ProcessDag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * dag helper test
 */
public class DagHelperTest {

    @Test
    public void testHaveSubAfterNode() {
        String parentNodeCode = "5293789969856";
        List<TaskNodeRelation> taskNodeRelations = new ArrayList<>();
        TaskNodeRelation relation = new TaskNodeRelation();
        relation.setStartNode("5293789969856");
        relation.setEndNode("5293789969857");
        taskNodeRelations.add(relation);

        TaskNodeRelation relationNext = new TaskNodeRelation();
        relationNext.setStartNode("5293789969856");
        relationNext.setEndNode("5293789969858");
        taskNodeRelations.add(relationNext);

        List<TaskNode> taskNodes = new ArrayList<>();
        TaskNode node = new TaskNode();
        node.setCode(5293789969856L);
        node.setType("SHELL");

        TaskNode subNode = new TaskNode();
        subNode.setCode(5293789969857L);
        subNode.setType("BLOCKING");
        subNode.setPreTasks("[5293789969856]");

        TaskNode subNextNode = new TaskNode();
        subNextNode.setCode(5293789969858L);
        subNextNode.setType("CONDITIONS");
        subNextNode.setPreTasks("[5293789969856]");

        taskNodes.add(node);
        taskNodes.add(subNode);
        taskNodes.add(subNextNode);

        ProcessDag processDag = new ProcessDag();
        processDag.setEdges(taskNodeRelations);
        processDag.setNodes(taskNodes);
        DAG<String, TaskNode, TaskNodeRelation> dag = DagHelper.buildDagGraph(processDag);
        boolean canSubmit = DagHelper.haveAllNodeAfterNode(parentNodeCode, dag);
        Assertions.assertTrue(canSubmit);

        boolean haveBlocking = DagHelper.haveBlockingAfterNode(parentNodeCode, dag);
        Assertions.assertTrue(haveBlocking);

        boolean haveConditions = DagHelper.haveConditionsAfterNode(parentNodeCode, dag);
        Assertions.assertTrue(haveConditions);

        boolean dependent = DagHelper.haveSubAfterNode(parentNodeCode, dag, TaskConstants.TASK_TYPE_DEPENDENT);
        Assertions.assertFalse(dependent);
    }

    /**
     * test task node can submit
     *
     * @throws JsonProcessingException if error throws JsonProcessingException
     */
    @Test
    public void testTaskNodeCanSubmit() throws IOException {
        // 1->2->3->5->7
        // 4->3->6
        DAG<String, TaskNode, TaskNodeRelation> dag = generateDag();
        TaskNode taskNode3 = dag.getNode("3");
        Map<String, TaskInstance> completeTaskList = new HashMap<>();
        Map<String, TaskNode> skipNodeList = new HashMap<>();
        completeTaskList.putIfAbsent("1", new TaskInstance());
        Boolean canSubmit = false;

        // 2/4 are forbidden submit 3
        TaskNode node2 = dag.getNode("2");
        node2.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        TaskNode nodex = dag.getNode("4");
        nodex.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        canSubmit = DagHelper.allDependsForbiddenOrEnd(taskNode3, dag, skipNodeList, completeTaskList);
        Assertions.assertEquals(canSubmit, true);

        // 2forbidden, 3 cannot be submit
        completeTaskList.putIfAbsent("2", new TaskInstance());
        TaskNode nodey = dag.getNode("4");
        nodey.setRunFlag("");
        canSubmit = DagHelper.allDependsForbiddenOrEnd(taskNode3, dag, skipNodeList, completeTaskList);
        Assertions.assertEquals(canSubmit, false);

        // 2/3 forbidden submit 5
        TaskNode node3 = dag.getNode("3");
        node3.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        TaskNode node8 = dag.getNode("8");
        node8.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        TaskNode node5 = dag.getNode("5");
        canSubmit = DagHelper.allDependsForbiddenOrEnd(node5, dag, skipNodeList, completeTaskList);
        Assertions.assertEquals(canSubmit, true);
    }

    /**
     * test parse post node list
     */
    @Test
    public void testParsePostNodeList() throws IOException {
        DAG<String, TaskNode, TaskNodeRelation> dag = generateDag();
        Map<String, TaskInstance> completeTaskList = new HashMap<>();
        Map<String, TaskNode> skipNodeList = new HashMap<>();

        Set<String> postNodes = null;
        // complete : null
        // expect post: 1/4
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains("1"));
        Assertions.assertTrue(postNodes.contains("4"));

        // complete : 1
        // expect post: 2/4
        completeTaskList.put("1", new TaskInstance());
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains("2"));
        Assertions.assertTrue(postNodes.contains("4"));

        // complete : 1/2
        // expect post: 4
        completeTaskList.put("2", new TaskInstance());
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains("4"));
        Assertions.assertTrue(postNodes.contains("8"));

        // complete : 1/2/4
        // expect post: 3
        completeTaskList.put("4", new TaskInstance());
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains("3"));
        Assertions.assertTrue(postNodes.contains("8"));

        // complete : 1/2/4/3
        // expect post: 8/6
        completeTaskList.put("3", new TaskInstance());
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains("8"));
        Assertions.assertTrue(postNodes.contains("6"));

        // complete : 1/2/4/3/8
        // expect post: 6/5
        completeTaskList.put("8", new TaskInstance());
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains("5"));
        Assertions.assertTrue(postNodes.contains("6"));
        // complete : 1/2/4/3/5/6/8
        // expect post: 7
        completeTaskList.put("6", new TaskInstance());
        completeTaskList.put("5", new TaskInstance());
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(1, postNodes.size());
        Assertions.assertTrue(postNodes.contains("7"));
    }

    /**
     * test forbidden post node
     *
     * @throws JsonProcessingException
     */
    @Test
    public void testForbiddenPostNode() throws IOException {
        DAG<String, TaskNode, TaskNodeRelation> dag = generateDag();
        Map<String, TaskInstance> completeTaskList = new HashMap<>();
        Map<String, TaskNode> skipNodeList = new HashMap<>();
        Set<String> postNodes = null;
        // dag: 1-2-3-5-7 4-3-6 2-8-5-7
        // forbid:2 complete:1 post:4/8
        completeTaskList.put("1", new TaskInstance());
        TaskNode node2 = dag.getNode("2");
        node2.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains("4"));
        Assertions.assertTrue(postNodes.contains("8"));

        // forbid:2/4 complete:1 post:3/8
        TaskNode node4 = dag.getNode("4");
        node4.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains("3"));
        Assertions.assertTrue(postNodes.contains("8"));

        // forbid:2/4/5 complete:1/8 post:3
        completeTaskList.put("8", new TaskInstance());
        TaskNode node5 = dag.getNode("5");
        node5.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(1, postNodes.size());
        Assertions.assertTrue(postNodes.contains("3"));
    }

    /**
     * test condition post node
     *
     * @throws JsonProcessingException
     */
    @Test
    public void testConditionPostNode() throws IOException {
        DAG<String, TaskNode, TaskNodeRelation> dag = generateDag();
        Map<String, TaskInstance> completeTaskList = new HashMap<>();
        Map<String, TaskNode> skipNodeList = new HashMap<>();
        Set<String> postNodes = null;
        // dag: 1-2-3-5-7 4-3-6 2-8-5-7
        // 3-if
        completeTaskList.put("1", new TaskInstance());
        completeTaskList.put("2", new TaskInstance());
        completeTaskList.put("4", new TaskInstance());
        TaskNode node3 = dag.getNode("3");
        node3.setType(TASK_TYPE_CONDITIONS);
        node3.setConditionResult("{\n"
                +
                "                \"successNode\": [5\n"
                +
                "                ],\n"
                +
                "                \"failedNode\": [6\n"
                +
                "                ]\n"
                +
                "            }");
        completeTaskList.remove("3");
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.SUCCESS);
        // complete 1/2/3/4 expect:8
        completeTaskList.put("3", taskInstance);
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(1, postNodes.size());
        Assertions.assertTrue(postNodes.contains("8"));

        // 2.complete 1/2/3/4/8 expect:5 skip:6
        completeTaskList.put("8", new TaskInstance());
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertTrue(postNodes.contains("5"));
        Assertions.assertEquals(1, skipNodeList.size());
        Assertions.assertTrue(skipNodeList.containsKey("6"));

        // 3.complete 1/2/3/4/5/8 expect post:7 skip:6
        skipNodeList.clear();
        TaskInstance taskInstance1 = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.SUCCESS);
        completeTaskList.put("5", taskInstance1);
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(1, postNodes.size());
        Assertions.assertTrue(postNodes.contains("7"));
        Assertions.assertEquals(1, skipNodeList.size());
        Assertions.assertTrue(skipNodeList.containsKey("6"));

        // dag: 1-2-3-5-7 4-3-6
        // 3-if , complete:1/2/3/4
        // 1.failure:3 expect post:6 skip:5/7
        skipNodeList.clear();
        completeTaskList.remove("3");
        taskInstance = new TaskInstance();

        Map<String, Object> taskParamsMap = new HashMap<>();
        taskParamsMap.put(Constants.SWITCH_RESULT, "");
        taskInstance.setTaskParams(JSONUtils.toJsonString(taskParamsMap));
        taskInstance.setState(TaskExecutionStatus.FAILURE);
        completeTaskList.put("3", taskInstance);
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(1, postNodes.size());
        Assertions.assertTrue(postNodes.contains("6"));
        Assertions.assertEquals(2, skipNodeList.size());
        Assertions.assertTrue(skipNodeList.containsKey("5"));
        Assertions.assertTrue(skipNodeList.containsKey("7"));

        // dag: 1-2-3-5-7 4-3-6
        // 3-if , complete:1/2/3/4
        // 1.failure:3 expect post:6 skip:5/7
        dag = generateDag2();
        skipNodeList.clear();
        completeTaskList.clear();
        taskInstance.setSwitchDependency(getSwitchNode());
        completeTaskList.put("1", taskInstance);
        postNodes = DagHelper.parsePostNodes("1", skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(1, postNodes.size());
    }

    /**
     * process:
     * 1->2->3->5->7
     * 4->3->6
     * 1->2->8->5->7
     * DAG graph:
     *      4 ->   -> 6
     *          \ /
     * 1 -> 2 -> 3 -> 5 -> 7
     *       \       /
     *        -> 8 ->
     *
     * @return dag
     * @throws JsonProcessingException if error throws JsonProcessingException
     */
    private DAG<String, TaskNode, TaskNodeRelation> generateDag() throws IOException {
        List<TaskNode> taskNodeList = new ArrayList<>();
        TaskNode node1 = new TaskNode();
        node1.setId("1");
        node1.setName("1");
        node1.setCode(1);
        node1.setType("SHELL");
        taskNodeList.add(node1);

        TaskNode node2 = new TaskNode();
        node2.setId("2");
        node2.setName("2");
        node2.setCode(2);
        node2.setType("SHELL");
        List<String> dep2 = new ArrayList<>();
        dep2.add("1");
        node2.setPreTasks(JSONUtils.toJsonString(dep2));
        taskNodeList.add(node2);

        TaskNode node4 = new TaskNode();
        node4.setId("4");
        node4.setName("4");
        node4.setCode(4);
        node4.setType("SHELL");
        taskNodeList.add(node4);

        TaskNode node3 = new TaskNode();
        node3.setId("3");
        node3.setName("3");
        node3.setCode(3);
        node3.setType("SHELL");
        List<String> dep3 = new ArrayList<>();
        dep3.add("2");
        dep3.add("4");
        node3.setPreTasks(JSONUtils.toJsonString(dep3));
        taskNodeList.add(node3);

        TaskNode node5 = new TaskNode();
        node5.setId("5");
        node5.setName("5");
        node5.setCode(5);
        node5.setType("SHELL");
        List<String> dep5 = new ArrayList<>();
        dep5.add("3");
        dep5.add("8");
        node5.setPreTasks(JSONUtils.toJsonString(dep5));
        taskNodeList.add(node5);

        TaskNode node6 = new TaskNode();
        node6.setId("6");
        node6.setName("6");
        node6.setCode(6);
        node6.setType("SHELL");
        List<String> dep6 = new ArrayList<>();
        dep6.add("3");
        node6.setPreTasks(JSONUtils.toJsonString(dep6));
        taskNodeList.add(node6);

        TaskNode node7 = new TaskNode();
        node7.setId("7");
        node7.setName("7");
        node7.setCode(7);
        node7.setType("SHELL");
        List<String> dep7 = new ArrayList<>();
        dep7.add("5");
        node7.setPreTasks(JSONUtils.toJsonString(dep7));
        taskNodeList.add(node7);

        TaskNode node8 = new TaskNode();
        node8.setId("8");
        node8.setName("8");
        node8.setCode(8);
        node8.setType("SHELL");
        List<String> dep8 = new ArrayList<>();
        dep8.add("2");
        node8.setPreTasks(JSONUtils.toJsonString(dep8));
        taskNodeList.add(node8);

        List<String> startNodes = new ArrayList<>();
        List<String> recoveryNodes = new ArrayList<>();
        List<TaskNode> destTaskNodeList = DagHelper.generateFlowNodeListByStartNode(taskNodeList,
                startNodes, recoveryNodes, TaskDependType.TASK_POST);
        List<TaskNodeRelation> taskNodeRelations = DagHelper.generateRelationListByFlowNodes(destTaskNodeList);
        ProcessDag processDag = new ProcessDag();
        processDag.setEdges(taskNodeRelations);
        processDag.setNodes(destTaskNodeList);
        return DagHelper.buildDagGraph(processDag);
    }

    /**
     * DAG graph:
     *    2
     *    ↑
     * 0->1(switch)
     *    ↓
     *    4
     *
     * @return dag
     * @throws JsonProcessingException if error throws JsonProcessingException
     */
    private DAG<String, TaskNode, TaskNodeRelation> generateDag2() throws IOException {
        List<TaskNode> taskNodeList = new ArrayList<>();

        TaskNode node = new TaskNode();
        node.setId("0");
        node.setName("0");
        node.setCode(0);
        node.setType("SHELL");
        taskNodeList.add(node);

        TaskNode node1 = new TaskNode();
        node1.setId("1");
        node1.setName("1");
        node1.setCode(1);
        node1.setType("switch");
        node1.setDependence(JSONUtils.toJsonString(getSwitchNode()));
        taskNodeList.add(node1);

        TaskNode node2 = new TaskNode();
        node2.setId("2");
        node2.setName("2");
        node2.setCode(2);
        node2.setType("SHELL");
        List<String> dep2 = new ArrayList<>();
        dep2.add("1");
        node2.setPreTasks(JSONUtils.toJsonString(dep2));
        taskNodeList.add(node2);

        TaskNode node4 = new TaskNode();
        node4.setId("4");
        node4.setName("4");
        node4.setCode(4);
        node4.setType("SHELL");
        List<String> dep4 = new ArrayList<>();
        dep4.add("1");
        node4.setPreTasks(JSONUtils.toJsonString(dep4));
        taskNodeList.add(node4);

        TaskNode node5 = new TaskNode();
        node5.setId("4");
        node5.setName("4");
        node5.setCode(4);
        node5.setType("SHELL");
        List<String> dep5 = new ArrayList<>();
        dep5.add("1");
        node5.setPreTasks(JSONUtils.toJsonString(dep5));
        taskNodeList.add(node5);

        List<String> startNodes = new ArrayList<>();
        List<String> recoveryNodes = new ArrayList<>();
        List<TaskNode> destTaskNodeList = DagHelper.generateFlowNodeListByStartNode(taskNodeList,
                startNodes, recoveryNodes, TaskDependType.TASK_POST);
        List<TaskNodeRelation> taskNodeRelations = DagHelper.generateRelationListByFlowNodes(destTaskNodeList);
        ProcessDag processDag = new ProcessDag();
        processDag.setEdges(taskNodeRelations);
        processDag.setNodes(destTaskNodeList);
        return DagHelper.buildDagGraph(processDag);
    }

    private SwitchParameters getSwitchNode() {
        SwitchParameters conditionsParameters = new SwitchParameters();
        SwitchResultVo switchResultVo1 = new SwitchResultVo();
        switchResultVo1.setCondition(" 2 == 1");
        switchResultVo1.setNextNode("2");
        SwitchResultVo switchResultVo2 = new SwitchResultVo();
        switchResultVo2.setCondition(" 2 == 2");
        switchResultVo2.setNextNode("4");
        List<SwitchResultVo> list = new ArrayList<>();
        list.add(switchResultVo1);
        list.add(switchResultVo2);
        conditionsParameters.setDependTaskList(list);
        conditionsParameters.setNextNode("5");
        conditionsParameters.setRelation("AND");

        // in: AND(AND(1 is SUCCESS))
        return conditionsParameters;
    }

    @Test
    public void testBuildDagGraph() {
        String shellJson =
                "{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-9527\",\"name\":\"shell-1\","
                        +
                        "\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"#!/bin/bash\\necho \\\"shell-1\\\"\"},"
                        +
                        "\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\","
                        +
                        "\"timeout\":{\"strategy\":\"\",\"interval\":1,\"enable\":false},\"taskInstancePriority\":\"MEDIUM\","
                        +
                        "\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":1,\"timeout\":0}";

        ProcessData processData = JSONUtils.parseObject(shellJson, ProcessData.class);
        assert processData != null;
        List<TaskNode> taskNodeList = processData.getTasks();
        ProcessDag processDag = DagHelper.getProcessDag(taskNodeList);
        DAG<String, TaskNode, TaskNodeRelation> dag = DagHelper.buildDagGraph(processDag);
        Assertions.assertNotNull(dag);
    }

}
