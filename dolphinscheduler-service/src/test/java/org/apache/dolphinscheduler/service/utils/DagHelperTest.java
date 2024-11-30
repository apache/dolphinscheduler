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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ConditionsParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.plugin.task.api.task.ConditionsLogicTaskChannelFactory;
import org.apache.dolphinscheduler.plugin.task.api.task.DependentLogicTaskChannelFactory;
import org.apache.dolphinscheduler.plugin.task.api.task.SwitchLogicTaskChannelFactory;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.WorkflowDag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.truth.Truth;

public class DagHelperTest {

    @Test
    public void testHaveSubAfterNode() {
        Long parentNodeCode = 5293789969856L;
        List<TaskNodeRelation> taskNodeRelations = new ArrayList<>();
        TaskNodeRelation relation = new TaskNodeRelation();
        relation.setStartNode(5293789969856L);
        relation.setEndNode(5293789969857L);
        taskNodeRelations.add(relation);

        TaskNodeRelation relationNext = new TaskNodeRelation();
        relationNext.setStartNode(5293789969856L);
        relationNext.setEndNode(5293789969858L);
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

        WorkflowDag workflowDag = new WorkflowDag();
        workflowDag.setEdges(taskNodeRelations);
        workflowDag.setNodes(taskNodes);
        DAG<Long, TaskNode, TaskNodeRelation> dag = DagHelper.buildDagGraph(workflowDag);
        boolean canSubmit = DagHelper.haveAllNodeAfterNode(parentNodeCode, dag);
        Assertions.assertTrue(canSubmit);

        boolean haveConditions = DagHelper.haveConditionsAfterNode(parentNodeCode, dag);
        Assertions.assertTrue(haveConditions);

        boolean dependent = DagHelper.haveSubAfterNode(parentNodeCode, dag, DependentLogicTaskChannelFactory.NAME);
        Assertions.assertFalse(dependent);
    }

    @Test
    public void testTaskNodeCanSubmit() {
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

        List<Long> startNodes = new ArrayList<>();
        List<Long> recoveryNodes = new ArrayList<>();
        List<TaskNode> destTaskNodeList = DagHelper.generateFlowNodeListByStartNode(taskNodeList,
                startNodes, recoveryNodes, TaskDependType.TASK_POST);
        List<TaskNodeRelation> taskNodeRelations = DagHelper.generateRelationListByFlowNodes(destTaskNodeList);
        WorkflowDag workflowDag = new WorkflowDag();
        workflowDag.setEdges(taskNodeRelations);
        workflowDag.setNodes(destTaskNodeList);

        // 1->2->3->5->7
        // 4->3->6
        // 1->2->8->5->7
        DAG<Long, TaskNode, TaskNodeRelation> dag = DagHelper.buildDagGraph(workflowDag);
        TaskNode taskNode3 = dag.getNode(3L);
        Map<Long, TaskInstance> completeTaskList = new HashMap<>();
        Map<Long, TaskNode> skipNodeList = new HashMap<>();
        completeTaskList.putIfAbsent(1L, new TaskInstance());
        Boolean canSubmit = false;

        // 2/4 are forbidden submit 3
        node2 = dag.getNode(2L);
        node2.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        TaskNode nodex = dag.getNode(4L);
        nodex.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        canSubmit = DagHelper.allDependsForbiddenOrEnd(taskNode3, dag, skipNodeList, completeTaskList);
        Assertions.assertEquals(canSubmit, true);

        // 2forbidden, 3 cannot be submit
        completeTaskList.putIfAbsent(2L, new TaskInstance());
        TaskNode nodey = dag.getNode(4L);
        nodey.setRunFlag("");
        canSubmit = DagHelper.allDependsForbiddenOrEnd(taskNode3, dag, skipNodeList, completeTaskList);
        Assertions.assertEquals(canSubmit, false);

        // 2/3 forbidden submit 5
        node3 = dag.getNode(3L);
        node3.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        node8 = dag.getNode(8L);
        node8.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        node5 = dag.getNode(5L);
        canSubmit = DagHelper.allDependsForbiddenOrEnd(node5, dag, skipNodeList, completeTaskList);
        Assertions.assertEquals(canSubmit, true);
    }

    @Test
    public void testParsePostNodeList() {
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

        List<Long> startNodes = new ArrayList<>();
        List<Long> recoveryNodes = new ArrayList<>();
        List<TaskNode> destTaskNodeList = DagHelper.generateFlowNodeListByStartNode(taskNodeList,
                startNodes, recoveryNodes, TaskDependType.TASK_POST);
        List<TaskNodeRelation> taskNodeRelations = DagHelper.generateRelationListByFlowNodes(destTaskNodeList);
        WorkflowDag workflowDag = new WorkflowDag();
        workflowDag.setEdges(taskNodeRelations);
        workflowDag.setNodes(destTaskNodeList);

        // 1->2->3->5->7
        // 4->3->6
        // 1->2->8->5->7
        DAG<Long, TaskNode, TaskNodeRelation> dag = DagHelper.buildDagGraph(workflowDag);
        Map<Long, TaskInstance> completeTaskList = new HashMap<>();
        Map<Long, TaskNode> skipNodeList = new HashMap<>();

        Set<Long> postNodes = null;
        // complete : null
        // expect post: 1/4
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains(1L));
        Assertions.assertTrue(postNodes.contains(4L));

        // complete : 1
        // expect post: 2/4
        completeTaskList.put(1L, new TaskInstance());
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains(2L));
        Assertions.assertTrue(postNodes.contains(4L));

        // complete : 1/2
        // expect post: 4
        completeTaskList.put(2L, new TaskInstance());
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains(4L));
        Assertions.assertTrue(postNodes.contains(8L));

        // complete : 1/2/4
        // expect post: 3
        completeTaskList.put(4L, new TaskInstance());
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains(3L));
        Assertions.assertTrue(postNodes.contains(8L));

        // complete : 1/2/4/3
        // expect post: 8/6
        completeTaskList.put(3L, new TaskInstance());
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains(8L));
        Assertions.assertTrue(postNodes.contains(6L));

        // complete : 1/2/4/3/8
        // expect post: 6/5
        completeTaskList.put(8L, new TaskInstance());
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains(5L));
        Assertions.assertTrue(postNodes.contains(6L));
        // complete : 1/2/4/3/5/6/8
        // expect post: 7
        completeTaskList.put(6L, new TaskInstance());
        completeTaskList.put(5L, new TaskInstance());
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(1, postNodes.size());
        Assertions.assertTrue(postNodes.contains(7L));
    }

    @Test
    public void testForbiddenPostNode() throws IOException {
        DAG<Long, TaskNode, TaskNodeRelation> dag = generateDag();
        Map<Long, TaskInstance> completeTaskList = new HashMap<>();
        Map<Long, TaskNode> skipNodeList = new HashMap<>();
        Set<Long> postNodes = null;
        // dag: 1-2-3-5-7 4-3-6 2-8-5-7
        // forbid:2 complete:1 post:4/8
        completeTaskList.put(1L, new TaskInstance());
        TaskNode node2 = dag.getNode(2L);
        node2.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains(4L));
        Assertions.assertTrue(postNodes.contains(8L));

        // forbid:2/4 complete:1 post:3/8
        TaskNode node4 = dag.getNode(4L);
        node4.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(2, postNodes.size());
        Assertions.assertTrue(postNodes.contains(3L));
        Assertions.assertTrue(postNodes.contains(8L));

        // forbid:2/4/5 complete:1/8 post:3
        completeTaskList.put(8L, new TaskInstance());
        TaskNode node5 = dag.getNode(5L);
        node5.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(1, postNodes.size());
        Assertions.assertTrue(postNodes.contains(3L));
    }

    @Test
    public void testConditionPostNode() throws IOException {
        DAG<Long, TaskNode, TaskNodeRelation> dag = generateDag();
        Map<Long, TaskInstance> completeTaskList = new HashMap<>();
        Map<Long, TaskNode> skipNodeList = new HashMap<>();
        Set<Long> postNodes = null;
        // dag: 1-2-3-5-7 4-3-6 2-8-5-7
        // 3-if
        completeTaskList.put(1L, new TaskInstance());
        completeTaskList.put(2L, new TaskInstance());
        completeTaskList.put(4L, new TaskInstance());

        TaskInstance taskInstance3 = new TaskInstance();
        taskInstance3.setTaskType(ConditionsLogicTaskChannelFactory.NAME);
        ConditionsParameters.ConditionResult conditionResult = ConditionsParameters.ConditionResult.builder()
                .conditionSuccess(true)
                .successNode(Lists.newArrayList(5L))
                .failedNode(Lists.newArrayList(6L))
                .build();
        ConditionsParameters conditionsParameters = new ConditionsParameters();
        conditionsParameters.setConditionResult(conditionResult);
        taskInstance3.setTaskParams(JSONUtils.toJsonString(conditionsParameters));
        taskInstance3.setState(TaskExecutionStatus.SUCCESS);
        TaskNode node3 = dag.getNode(3L);
        node3.setType(ConditionsLogicTaskChannelFactory.NAME);
        // complete 1/2/3/4 expect:8
        completeTaskList.put(3L, taskInstance3);
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(1, postNodes.size());
        Assertions.assertTrue(postNodes.contains(8L));

        // 2.complete 1/2/3/4/8 expect:5 skip:6
        completeTaskList.put(8L, new TaskInstance());
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertTrue(postNodes.contains(5L));
        Assertions.assertEquals(1, skipNodeList.size());
        Assertions.assertTrue(skipNodeList.containsKey(6L));

        // 3.complete 1/2/3/4/5/8 expect post:7 skip:6
        skipNodeList.clear();
        TaskInstance taskInstance1 = new TaskInstance();
        completeTaskList.put(5L, taskInstance1);
        postNodes = DagHelper.parsePostNodes(null, skipNodeList, dag, completeTaskList);
        Assertions.assertEquals(1, postNodes.size());
        Assertions.assertTrue(postNodes.contains(7L));
        Assertions.assertEquals(1, skipNodeList.size());
        Assertions.assertTrue(skipNodeList.containsKey(6L));

    }

    @Test
    public void testSwitchPostNode() {
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
        node1.setType(SwitchLogicTaskChannelFactory.NAME);
        SwitchParameters switchParameters = new SwitchParameters();
        node1.setParams(JSONUtils.toJsonString(switchParameters));
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
        node5.setId("5");
        node5.setName("5");
        node5.setCode(5);
        node5.setType("SHELL");
        List<Long> dep5 = new ArrayList<>();
        dep5.add(1L);
        node5.setPreTasks(JSONUtils.toJsonString(dep5));
        taskNodeList.add(node5);

        TaskNode node6 = new TaskNode();
        node5.setId("6");
        node5.setName("6");
        node5.setCode(6);
        node5.setType("SHELL");
        List<Long> dep6 = new ArrayList<>();
        dep5.add(2L);
        dep5.add(4L);
        node5.setPreTasks(JSONUtils.toJsonString(dep6));
        taskNodeList.add(node6);

        List<Long> startNodes = new ArrayList<>();
        List<Long> recoveryNodes = new ArrayList<>();

        // 0
        // 1->2->6
        // 1->4->6
        // 1->5
        List<TaskNode> destTaskNodeList = DagHelper.generateFlowNodeListByStartNode(taskNodeList,
                startNodes, recoveryNodes, TaskDependType.TASK_POST);
        List<TaskNodeRelation> taskNodeRelations = DagHelper.generateRelationListByFlowNodes(destTaskNodeList);
        WorkflowDag workflowDag = new WorkflowDag();
        workflowDag.setEdges(taskNodeRelations);
        workflowDag.setNodes(destTaskNodeList);

        DAG<Long, TaskNode, TaskNodeRelation> dag = DagHelper.buildDagGraph(workflowDag);
        Map<Long, TaskNode> skipTaskNodeList = new HashMap<>();
        Map<Long, TaskInstance> completeTaskList = new HashMap<>();
        completeTaskList.put(0L, new TaskInstance());
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.SUCCESS);
        taskInstance.setTaskCode(1L);
        taskInstance.setTaskType(SwitchLogicTaskChannelFactory.NAME);
        switchParameters = SwitchParameters.builder()
                .nextBranch(5L)
                .switchResult(SwitchParameters.SwitchResult.builder()
                        .dependTaskList(Lists.newArrayList(
                                new SwitchResultVo("", 2L),
                                new SwitchResultVo("", 4L)))
                        .nextNode(5L)
                        .build())
                .build();
        taskInstance.setTaskParams(JSONUtils.toJsonString(switchParameters));
        completeTaskList.put(1l, taskInstance);
        List<Long> nextBranch = DagHelper.skipTaskNode4Switch(skipTaskNodeList, taskInstance, dag);
        Assertions.assertNotNull(skipTaskNodeList.get(2L));
        Assertions.assertNotNull(skipTaskNodeList.get(4L));
        Assertions.assertEquals(2, skipTaskNodeList.size());
        Truth.assertThat(nextBranch).containsExactly(5L);
    }

    /**
     * process:
     * 1->2->3->5->7
     * 4->3->6
     * 1->2->8->5->7
     * DAG graph:
     * 4 ->   -> 6
     * \ /
     * 1 -> 2 -> 3 -> 5 -> 7
     * \       /
     * -> 8 ->
     *
     * @return dag
     * @throws JsonProcessingException if error throws JsonProcessingException
     */
    private DAG<Long, TaskNode, TaskNodeRelation> generateDag() throws IOException {
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

        List<Long> startNodes = new ArrayList<>();
        List<Long> recoveryNodes = new ArrayList<>();
        List<TaskNode> destTaskNodeList = DagHelper.generateFlowNodeListByStartNode(taskNodeList,
                startNodes, recoveryNodes, TaskDependType.TASK_POST);
        List<TaskNodeRelation> taskNodeRelations = DagHelper.generateRelationListByFlowNodes(destTaskNodeList);
        WorkflowDag workflowDag = new WorkflowDag();
        workflowDag.setEdges(taskNodeRelations);
        workflowDag.setNodes(destTaskNodeList);
        return DagHelper.buildDagGraph(workflowDag);
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
        WorkflowDag workflowDag = DagHelper.getWorkflowDag(taskNodeList);
        DAG<Long, TaskNode, TaskNodeRelation> dag = DagHelper.buildDagGraph(workflowDag);
        Assertions.assertNotNull(dag);
    }

    @Data
    @NoArgsConstructor
    private static class ProcessData {

        @EqualsAndHashCode.Include
        private List<TaskNode> tasks;

        @EqualsAndHashCode.Include
        private List<Property> globalParams;

        private int timeout;

        private int tenantId;

        public ProcessData(List<TaskNode> tasks, List<Property> globalParams) {
            this.tasks = tasks;
            this.globalParams = globalParams;
        }
    }

}
