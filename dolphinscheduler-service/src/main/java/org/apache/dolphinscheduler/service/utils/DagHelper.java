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

import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ConditionsParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.plugin.task.api.task.ConditionsLogicTaskChannelFactory;
import org.apache.dolphinscheduler.plugin.task.api.task.SwitchLogicTaskChannelFactory;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.WorkflowDag;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;

/**
 * dag tools
 */
@Slf4j
public class DagHelper {

    /**
     * generate flow node relation list by task node list;
     * Edges that are not in the task Node List will not be added to the result
     *
     * @param taskNodeList taskNodeList
     * @return task node relation list
     */
    public static List<TaskNodeRelation> generateRelationListByFlowNodes(List<TaskNode> taskNodeList) {
        List<TaskNodeRelation> nodeRelationList = new ArrayList<>();
        for (TaskNode taskNode : taskNodeList) {
            String preTasks = taskNode.getPreTasks();
            List<Long> preTaskList = JSONUtils.toList(preTasks, Long.class);
            if (preTaskList != null) {
                for (Long depNodeCode : preTaskList) {
                    if (null != findNodeByCode(taskNodeList, depNodeCode)) {
                        nodeRelationList.add(new TaskNodeRelation(depNodeCode, taskNode.getCode()));
                    }
                }
            }
        }
        return nodeRelationList;
    }

    /**
     * generate task nodes needed by dag
     *
     * @param taskNodeList         taskNodeList
     * @param startNodeNameList    startNodeNameList
     * @param recoveryNodeCodeList recoveryNodeCodeList
     * @param taskDependType       taskDependType
     * @return task node list
     */
    public static List<TaskNode> generateFlowNodeListByStartNode(List<TaskNode> taskNodeList,
                                                                 List<Long> startNodeNameList,
                                                                 List<Long> recoveryNodeCodeList,
                                                                 TaskDependType taskDependType) {
        List<TaskNode> destFlowNodeList = new ArrayList<>();
        List<Long> startNodeList = startNodeNameList;

        if (taskDependType != TaskDependType.TASK_POST && CollectionUtils.isEmpty(startNodeList)) {
            log.error("start node list is empty! cannot continue run the workflow ");
            return destFlowNodeList;
        }

        List<TaskNode> destTaskNodeList = new ArrayList<>();
        List<TaskNode> tmpTaskNodeList = new ArrayList<>();

        if (taskDependType == TaskDependType.TASK_POST
                && CollectionUtils.isNotEmpty(recoveryNodeCodeList)) {
            startNodeList = recoveryNodeCodeList;
        }
        if (CollectionUtils.isEmpty(startNodeList)) {
            // no special designation start nodes
            tmpTaskNodeList = taskNodeList;
        } else {
            // specified start nodes or resume execution
            for (Long startNodeCode : startNodeList) {
                TaskNode startNode = findNodeByCode(taskNodeList, startNodeCode);
                List<TaskNode> childNodeList = new ArrayList<>();
                if (startNode == null) {
                    log.error("start node name [{}] is not in task node list [{}] ",
                            startNodeCode,
                            taskNodeList);
                    continue;
                } else if (TaskDependType.TASK_POST == taskDependType) {
                    List<Long> visitedNodeCodeList = new ArrayList<>();
                    childNodeList = getFlowNodeListPost(startNode, taskNodeList, visitedNodeCodeList);
                } else if (TaskDependType.TASK_PRE == taskDependType) {
                    List<Long> visitedNodeCodeList = new ArrayList<>();
                    childNodeList =
                            getFlowNodeListPre(startNode, recoveryNodeCodeList, taskNodeList, visitedNodeCodeList);
                } else {
                    childNodeList.add(startNode);
                }
                tmpTaskNodeList.addAll(childNodeList);
            }
        }

        for (TaskNode taskNode : tmpTaskNodeList) {
            if (null == findNodeByCode(destTaskNodeList, taskNode.getCode())) {
                destTaskNodeList.add(taskNode);
            }
        }
        return destTaskNodeList;
    }

    /**
     * find all the nodes that depended on the start node
     *
     * @param startNode    startNode
     * @param taskNodeList taskNodeList
     * @return task node list
     */
    private static List<TaskNode> getFlowNodeListPost(TaskNode startNode,
                                                      List<TaskNode> taskNodeList,
                                                      List<Long> visitedNodeCodeList) {
        List<TaskNode> resultList = new ArrayList<>();
        for (TaskNode taskNode : taskNodeList) {
            List<Long> depList = taskNode.getDepList();
            if (null != depList && null != startNode && depList.contains(startNode.getCode())
                    && !visitedNodeCodeList.contains(taskNode.getCode())) {
                resultList.addAll(getFlowNodeListPost(taskNode, taskNodeList, visitedNodeCodeList));
            }
        }
        // why add (startNode != null) condition? for SonarCloud Quality Gate passed
        if (null != startNode) {
            visitedNodeCodeList.add(startNode.getCode());
        }

        resultList.add(startNode);
        return resultList;
    }

    /**
     * find all nodes that start nodes depend on.
     *
     * @param startNode            startNode
     * @param recoveryNodeCodeList recoveryNodeCodeList
     * @param taskNodeList         taskNodeList
     * @return task node list
     */
    private static List<TaskNode> getFlowNodeListPre(TaskNode startNode,
                                                     List<Long> recoveryNodeCodeList,
                                                     List<TaskNode> taskNodeList,
                                                     List<Long> visitedNodeCodeList) {

        List<TaskNode> resultList = new ArrayList<>();

        List<Long> depList = new ArrayList<>();
        if (null != startNode) {
            depList = startNode.getDepList();
            resultList.add(startNode);
        }
        if (CollectionUtils.isEmpty(depList)) {
            return resultList;
        }
        for (Long depNodeCode : depList) {
            TaskNode start = findNodeByCode(taskNodeList, depNodeCode);
            if (recoveryNodeCodeList.contains(depNodeCode)) {
                resultList.add(start);
            } else if (!visitedNodeCodeList.contains(depNodeCode)) {
                resultList.addAll(getFlowNodeListPre(start, recoveryNodeCodeList, taskNodeList, visitedNodeCodeList));
            }
        }
        // why add (startNode != null) condition? for SonarCloud Quality Gate passed
        if (null != startNode) {
            visitedNodeCodeList.add(startNode.getCode());
        }
        return resultList;
    }

    /**
     * find node by node code
     *
     * @param nodeDetails nodeDetails
     * @param nodeCode    nodeCode
     * @return task node
     */
    public static TaskNode findNodeByCode(List<TaskNode> nodeDetails, Long nodeCode) {
        for (TaskNode taskNode : nodeDetails) {
            if (taskNode.getCode() == nodeCode) {
                return taskNode;
            }
        }
        return null;
    }

    /**
     * the task can be submit when  all the depends nodes are forbidden or complete
     *
     * @param taskNode         taskNode
     * @param dag              dag
     * @param completeTaskList completeTaskList
     * @return can submit
     */
    public static boolean allDependsForbiddenOrEnd(TaskNode taskNode,
                                                   DAG<Long, TaskNode, TaskNodeRelation> dag,
                                                   Map<Long, TaskNode> skipTaskNodeList,
                                                   Map<Long, TaskInstance> completeTaskList) {
        List<Long> dependList = taskNode.getDepList();
        if (dependList == null) {
            return true;
        }
        for (Long dependNodeCode : dependList) {
            TaskNode dependNode = dag.getNode(dependNodeCode);
            if (dependNode == null || completeTaskList.containsKey(dependNodeCode)
                    || dependNode.isForbidden()
                    || skipTaskNodeList.containsKey(dependNodeCode)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * parse the successor nodes of previous node.
     * this function parse the condition node to find the right branch.
     * also check all the depends nodes forbidden or complete
     *
     * @return successor nodes
     */
    public static Set<Long> parsePostNodes(Long preNodeCode,
                                           Map<Long, TaskNode> skipTaskNodeList,
                                           DAG<Long, TaskNode, TaskNodeRelation> dag,
                                           Map<Long, TaskInstance> completeTaskList) {
        Set<Long> postNodeList = new HashSet<>();
        Collection<Long> startVertexes = new ArrayList<>();

        if (preNodeCode == null) {
            startVertexes = dag.getBeginNode();
        } else if (TaskTypeUtils.isConditionTask(dag.getNode(preNodeCode).getType())) {
            List<Long> conditionTaskList = parseConditionTask(preNodeCode, skipTaskNodeList, dag, completeTaskList);
            startVertexes.addAll(conditionTaskList);
        } else if (TaskTypeUtils.isSwitchTask(dag.getNode(preNodeCode).getType())) {
            List<Long> conditionTaskList = parseSwitchTask(preNodeCode, skipTaskNodeList, dag, completeTaskList);
            startVertexes.addAll(conditionTaskList);
        } else {
            startVertexes = dag.getSubsequentNodes(preNodeCode);
        }
        for (Long subsequent : startVertexes) {
            TaskNode taskNode = dag.getNode(subsequent);
            if (taskNode == null) {
                log.error("taskNode {} is null, please check dag", subsequent);
                continue;
            }
            if (isTaskNodeNeedSkip(taskNode, skipTaskNodeList)) {
                setTaskNodeSkip(subsequent, dag, skipTaskNodeList);
                continue;
            }
            if (!DagHelper.allDependsForbiddenOrEnd(taskNode, dag, skipTaskNodeList, completeTaskList)) {
                continue;
            }
            if (taskNode.isForbidden() || completeTaskList.containsKey(subsequent)) {
                postNodeList.addAll(parsePostNodes(subsequent, skipTaskNodeList, dag, completeTaskList));
                continue;
            }
            postNodeList.add(subsequent);
        }
        return postNodeList;
    }

    /**
     * if all of the task dependence are skipped, skip it too.
     */
    private static boolean isTaskNodeNeedSkip(TaskNode taskNode,
                                              Map<Long, TaskNode> skipTaskNodeList) {
        if (CollectionUtils.isEmpty(taskNode.getDepList())) {
            return false;
        }
        for (Long depNode : taskNode.getDepList()) {
            if (!skipTaskNodeList.containsKey(depNode)) {
                return false;
            }
        }
        return true;
    }

    /**
     * parse condition task find the branch workflow
     * set skip flag for another one.
     */
    public static List<Long> parseConditionTask(Long nodeCode,
                                                Map<Long, TaskNode> skipTaskNodeList,
                                                DAG<Long, TaskNode, TaskNodeRelation> dag,
                                                Map<Long, TaskInstance> completeTaskList) {
        List<Long> conditionTaskList = new ArrayList<>();
        TaskNode taskNode = dag.getNode(nodeCode);
        if (!TaskTypeUtils.isConditionTask(taskNode.getType())) {
            return conditionTaskList;
        }
        if (!completeTaskList.containsKey(nodeCode)) {
            return conditionTaskList;
        }
        TaskInstance taskInstance = completeTaskList.get(nodeCode);
        ConditionsParameters conditionsParameters =
                JSONUtils.parseObject(taskInstance.getTaskParams(), new TypeReference<ConditionsParameters>() {
                });
        ConditionsParameters.ConditionResult conditionResult = conditionsParameters.getConditionResult();

        List<Long> skipNodeList = new ArrayList<>();
        if (conditionResult.isConditionSuccess()) {
            conditionTaskList = conditionResult.getSuccessNode();
            skipNodeList = conditionResult.getFailedNode();
        } else {
            conditionTaskList = conditionResult.getFailedNode();
            skipNodeList = conditionResult.getSuccessNode();
        }

        if (CollectionUtils.isNotEmpty(skipNodeList)) {
            skipNodeList.forEach(skipNode -> setTaskNodeSkip(skipNode, dag, skipTaskNodeList));
        }
        // the conditionTaskList maybe null if no next task
        conditionTaskList = Optional.ofNullable(conditionTaskList).orElse(new ArrayList<>());
        return conditionTaskList;
    }

    /**
     * parse condition task find the branch workflow
     * set skip flag for another one.
     *
     * @param nodeCode
     * @return
     */
    public static List<Long> parseSwitchTask(Long nodeCode,
                                             Map<Long, TaskNode> skipTaskNodeList,
                                             DAG<Long, TaskNode, TaskNodeRelation> dag,
                                             Map<Long, TaskInstance> completeTaskList) {
        List<Long> conditionTaskList = new ArrayList<>();
        TaskNode taskNode = dag.getNode(nodeCode);
        if (!SwitchLogicTaskChannelFactory.NAME.equals(taskNode.getType())) {
            return conditionTaskList;
        }
        if (!completeTaskList.containsKey(nodeCode)) {
            return conditionTaskList;
        }
        conditionTaskList = skipTaskNode4Switch(skipTaskNodeList, completeTaskList.get(nodeCode), dag);
        return conditionTaskList;
    }

    public static List<Long> skipTaskNode4Switch(Map<Long, TaskNode> skipTaskNodeList,
                                                 TaskInstance taskInstance,
                                                 DAG<Long, TaskNode, TaskNodeRelation> dag) {
        SwitchParameters switchParameters =
                JSONUtils.parseObject(taskInstance.getTaskParams(), new TypeReference<SwitchParameters>() {
                });

        SwitchParameters.SwitchResult switchResult = switchParameters.getSwitchResult();
        Long nextBranch = switchParameters.getNextBranch();
        if (switchResult == null) {
            log.error("switchResult is null, please check the switch task configuration");
            return Collections.emptyList();
        }
        if (nextBranch == null) {
            log.error("switchParameters.getNextBranch() is null, please check the switch task configuration");
            return Collections.emptyList();
        }

        Set<Long> allNextBranches = new HashSet<>();
        if (switchResult.getNextNode() != null) {
            allNextBranches.add(switchResult.getNextNode());
        }
        if (CollectionUtils.isNotEmpty(switchResult.getDependTaskList())) {
            for (SwitchResultVo switchResultVo : switchResult.getDependTaskList()) {
                allNextBranches.add(switchResultVo.getNextNode());
            }
        }

        allNextBranches.remove(nextBranch);

        for (Long branch : allNextBranches) {
            setTaskNodeSkip(branch, dag, skipTaskNodeList);
        }
        return Lists.newArrayList(nextBranch);
    }

    /**
     * set task node and the post nodes skip flag
     */
    private static void setTaskNodeSkip(Long skipNodeCode,
                                        DAG<Long, TaskNode, TaskNodeRelation> dag,
                                        Map<Long, TaskNode> skipTaskNodeList) {
        if (!dag.containsNode(skipNodeCode)) {
            return;
        }
        skipTaskNodeList.putIfAbsent(skipNodeCode, dag.getNode(skipNodeCode));
        Collection<Long> postNodeList = dag.getSubsequentNodes(skipNodeCode);
        for (Long post : postNodeList) {
            TaskNode postNode = dag.getNode(post);
            if (isTaskNodeNeedSkip(postNode, skipTaskNodeList)) {
                setTaskNodeSkip(post, dag, skipTaskNodeList);
            }
        }
    }

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
    public static WorkflowDag getWorkflowDag(List<TaskNode> taskNodeList) {
        List<TaskNodeRelation> taskNodeRelations = new ArrayList<>();

        // Traverse node information and build relationships
        for (TaskNode taskNode : taskNodeList) {
            String preTasks = taskNode.getPreTasks();
            List<Long> preTasksList = JSONUtils.toList(preTasks, Long.class);

            // If the dependency is not empty
            if (preTasksList != null) {
                for (Long depNode : preTasksList) {
                    taskNodeRelations.add(new TaskNodeRelation(depNode, taskNode.getCode()));
                }
            }
        }

        WorkflowDag workflowDag = new WorkflowDag();
        workflowDag.setEdges(taskNodeRelations);
        workflowDag.setNodes(taskNodeList);
        return workflowDag;
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

    /**
     * is there have conditions after the parent node
     */
    public static boolean haveConditionsAfterNode(Long parentNodeCode,
                                                  DAG<Long, TaskNode, TaskNodeRelation> dag) {
        return haveSubAfterNode(parentNodeCode, dag, ConditionsLogicTaskChannelFactory.NAME);
    }

    /**
     * is there have all node after the parent node
     */
    public static boolean haveAllNodeAfterNode(Long parentNodeCode,
                                               DAG<Long, TaskNode, TaskNodeRelation> dag) {
        return haveSubAfterNode(parentNodeCode, dag, null);
    }

    /**
     * Whether there is a specified type of child node after the parent node
     */
    public static boolean haveSubAfterNode(Long parentNodeCode,
                                           DAG<Long, TaskNode, TaskNodeRelation> dag, String filterNodeType) {
        Set<Long> subsequentNodes = dag.getSubsequentNodes(parentNodeCode);
        if (CollectionUtils.isEmpty(subsequentNodes)) {
            return false;
        }
        if (StringUtils.isBlank(filterNodeType)) {
            return true;
        }
        for (Long nodeName : subsequentNodes) {
            TaskNode taskNode = dag.getNode(nodeName);
            if (taskNode.getType().equalsIgnoreCase(filterNodeType)) {
                return true;
            }
        }
        return false;
    }
}
