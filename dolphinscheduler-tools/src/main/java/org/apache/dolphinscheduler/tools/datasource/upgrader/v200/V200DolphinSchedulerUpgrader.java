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

package org.apache.dolphinscheduler.tools.datasource.upgrader.v200;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ConditionType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.TaskTimeoutParameter;
import org.apache.dolphinscheduler.tools.datasource.dao.JsonSplitDao;
import org.apache.dolphinscheduler.tools.datasource.dao.ProcessDefinitionDao;
import org.apache.dolphinscheduler.tools.datasource.dao.ProjectDao;
import org.apache.dolphinscheduler.tools.datasource.dao.ScheduleDao;
import org.apache.dolphinscheduler.tools.datasource.upgrader.DolphinSchedulerUpgrader;
import org.apache.dolphinscheduler.tools.datasource.upgrader.DolphinSchedulerVersion;
import org.apache.dolphinscheduler.tools.datasource.upgrader.UpgradeDao;

import org.apache.commons.collections4.CollectionUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

@Slf4j
@Component
public class V200DolphinSchedulerUpgrader implements DolphinSchedulerUpgrader {

    @Autowired
    private DataSource dataSource;

    @Lazy()
    @Autowired
    private UpgradeDao upgradeDao;

    @Override
    public void doUpgrade() {
        processDefinitionJsonSplit();
        upgradeDao.upgradeDolphinSchedulerDDL("2.0.0_schema", "dolphinscheduler_ddl_post.sql");
    }

    private void processDefinitionJsonSplit() {
        ProjectDao projectDao = new ProjectDao();
        ProcessDefinitionDao processDefinitionDao = new ProcessDefinitionDao();
        ScheduleDao scheduleDao = new ScheduleDao();
        JsonSplitDao jsonSplitDao = new JsonSplitDao();
        try (Connection connection = dataSource.getConnection()) {
            // execute project
            Map<Integer, Long> projectIdCodeMap = projectDao.queryAllProject(connection);
            projectDao.updateProjectCode(connection, projectIdCodeMap);

            // execute process definition code
            List<WorkflowDefinition> workflowDefinitions =
                    processDefinitionDao.queryProcessDefinition(connection);
            processDefinitionDao.updateProcessDefinitionCode(connection, workflowDefinitions,
                    projectIdCodeMap);

            // execute schedule
            Map<Integer, Long> allSchedule = scheduleDao.queryAllSchedule(connection);
            Map<Integer, Long> processIdCodeMap = workflowDefinitions.stream()
                    .collect(Collectors.toMap(WorkflowDefinition::getId, WorkflowDefinition::getCode));
            scheduleDao.updateScheduleCode(connection, allSchedule, processIdCodeMap);

            // json split
            Map<Integer, String> processDefinitionJsonMap =
                    processDefinitionDao.queryAllProcessDefinition(connection);
            List<WorkflowDefinitionLog> processDefinitionLogs = new ArrayList<>();
            List<WorkflowTaskRelationLog> processTaskRelationLogs = new ArrayList<>();
            List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
            Map<Integer, Map<Long, Map<String, Long>>> processTaskMap = new HashMap<>();
            splitProcessDefinitionJson(workflowDefinitions, processDefinitionJsonMap, processDefinitionLogs,
                    processTaskRelationLogs, taskDefinitionLogs, processTaskMap);
            convertDependence(taskDefinitionLogs, projectIdCodeMap, processTaskMap);

            // execute json split
            jsonSplitDao.executeJsonSplitProcessDefinition(connection, processDefinitionLogs);
            jsonSplitDao.executeJsonSplitProcessTaskRelation(connection, processTaskRelationLogs);
            jsonSplitDao.executeJsonSplitTaskDefinition(connection, taskDefinitionLogs);
        } catch (Exception e) {
            log.error("json split error", e);
        }
    }

    private void splitProcessDefinitionJson(List<WorkflowDefinition> workflowDefinitions,
                                            Map<Integer, String> processDefinitionJsonMap,
                                            List<WorkflowDefinitionLog> processDefinitionLogs,
                                            List<WorkflowTaskRelationLog> processTaskRelationLogs,
                                            List<TaskDefinitionLog> taskDefinitionLogs,
                                            Map<Integer, Map<Long, Map<String, Long>>> processTaskMap) throws Exception {
        Map<Integer, WorkflowDefinition> processDefinitionMap = workflowDefinitions.stream()
                .collect(Collectors.toMap(WorkflowDefinition::getId, processDefinition -> processDefinition));
        Date now = new Date();
        for (Map.Entry<Integer, String> entry : processDefinitionJsonMap.entrySet()) {
            if (entry.getValue() == null) {
                throw new Exception("processDefinitionJson is null");
            }
            ObjectNode jsonObject = JSONUtils.parseObject(entry.getValue());
            WorkflowDefinition workflowDefinition = processDefinitionMap.get(entry.getKey());
            if (workflowDefinition != null) {
                workflowDefinition.setTimeout(jsonObject.get("timeout").asInt());
                workflowDefinition.setGlobalParams(jsonObject.get("globalParams").toString());
            } else {
                throw new Exception("It can't find processDefinition, please check !");
            }
            Map<String, Long> taskIdCodeMap = new HashMap<>();
            Map<String, List<String>> taskNamePreMap = new HashMap<>();
            Map<String, Long> taskNameCodeMap = new HashMap<>();
            Map<Long, Map<String, Long>> processCodeTaskNameCodeMap = new HashMap<>();
            List<TaskDefinitionLog> taskDefinitionLogList = new ArrayList<>();
            ArrayNode tasks = JSONUtils.parseArray(jsonObject.get("tasks").toString());
            for (int i = 0; i < tasks.size(); i++) {
                ObjectNode task = (ObjectNode) tasks.path(i);
                ObjectNode param = (ObjectNode) task.get("params");
                TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
                String taskType = task.get("type").asText();
                if (param != null) {
                    JsonNode resourceJsonNode = param.get("resourceList");
                    if (resourceJsonNode != null && !resourceJsonNode.isEmpty()) {
                        List<ResourceInfo> resourceList =
                                JSONUtils.toList(param.get("resourceList").toString(), ResourceInfo.class);
                        List<Integer> resourceIds =
                                resourceList.stream().map(ResourceInfo::getId).collect(Collectors.toList());
                        taskDefinitionLog.setResourceIds(Joiner.on(Constants.COMMA).join(resourceIds));
                    } else {
                        taskDefinitionLog.setResourceIds("");
                    }
                    if ("SUB_PROCESS".equals(taskType)) {
                        JsonNode jsonNodeDefinitionId = param.get("processDefinitionId");
                        if (jsonNodeDefinitionId != null) {
                            param.put("processDefinitionCode",
                                    processDefinitionMap.get(jsonNodeDefinitionId.asInt()).getCode());
                            param.remove("processDefinitionId");
                        }
                    }
                    param.put("conditionResult", task.get("conditionResult"));
                    param.put("dependence", task.get("dependence"));
                    taskDefinitionLog.setTaskParams(JSONUtils.toJsonString(param));
                }
                TaskTimeoutParameter timeout =
                        JSONUtils.parseObject(JSONUtils.toJsonString(task.get("timeout")), TaskTimeoutParameter.class);
                if (timeout != null) {
                    taskDefinitionLog.setTimeout(timeout.getInterval());
                    taskDefinitionLog.setTimeoutFlag(timeout.getEnable() ? TimeoutFlag.OPEN : TimeoutFlag.CLOSE);
                    taskDefinitionLog.setTimeoutNotifyStrategy(timeout.getStrategy());
                }
                String desc = task.get("description") != null ? task.get("description").asText()
                        : task.get("desc") != null ? task.get("desc").asText() : "";
                taskDefinitionLog.setDescription(desc);
                taskDefinitionLog.setFlag(
                        Constants.FLOWNODE_RUN_FLAG_NORMAL.equals(task.get("runFlag").asText()) ? Flag.YES : Flag.NO);
                taskDefinitionLog.setTaskType(taskType);
                taskDefinitionLog.setFailRetryInterval(
                        "SUB_PROCESS".equals(taskType) ? 1 : task.get("retryInterval").asInt());
                taskDefinitionLog.setFailRetryTimes(
                        "SUB_PROCESS".equals(taskType) ? 0 : task.get("maxRetryTimes").asInt());
                taskDefinitionLog.setTaskPriority(JSONUtils
                        .parseObject(JSONUtils.toJsonString(task.get("taskInstancePriority")), Priority.class));
                String name = task.get("name").asText();
                taskDefinitionLog.setName(name);
                taskDefinitionLog
                        .setWorkerGroup(task.get("workerGroup") == null ? "default" : task.get("workerGroup").asText());
                long taskCode = CodeGenerateUtils.genCode();
                taskDefinitionLog.setCode(taskCode);
                taskDefinitionLog.setVersion(Constants.VERSION_FIRST);
                taskDefinitionLog.setProjectCode(workflowDefinition.getProjectCode());
                taskDefinitionLog.setUserId(workflowDefinition.getUserId());
                taskDefinitionLog.setEnvironmentCode(-1);
                taskDefinitionLog.setDelayTime(0);
                taskDefinitionLog.setOperator(1);
                taskDefinitionLog.setOperateTime(now);
                taskDefinitionLog.setCreateTime(now);
                taskDefinitionLog.setUpdateTime(now);
                taskDefinitionLogList.add(taskDefinitionLog);
                taskIdCodeMap.put(task.get("id").asText(), taskCode);
                List<String> preTasks = JSONUtils.toList(task.get("preTasks").toString(), String.class);
                taskNamePreMap.put(name, preTasks);
                taskNameCodeMap.put(name, taskCode);
            }
            convertConditions(taskDefinitionLogList, taskNameCodeMap);
            taskDefinitionLogs.addAll(taskDefinitionLogList);
            workflowDefinition.setLocations(convertLocations(workflowDefinition.getLocations(), taskIdCodeMap));
            WorkflowDefinitionLog processDefinitionLog = new WorkflowDefinitionLog(workflowDefinition);
            processDefinitionLog.setOperator(1);
            processDefinitionLog.setOperateTime(now);
            processDefinitionLog.setUpdateTime(now);
            processDefinitionLogs.add(processDefinitionLog);
            handleProcessTaskRelation(taskNamePreMap, taskNameCodeMap, workflowDefinition, processTaskRelationLogs);
            processCodeTaskNameCodeMap.put(workflowDefinition.getCode(), taskNameCodeMap);
            processTaskMap.put(entry.getKey(), processCodeTaskNameCodeMap);
        }
    }

    private void convertDependence(List<TaskDefinitionLog> taskDefinitionLogs,
                                   Map<Integer, Long> projectIdCodeMap,
                                   Map<Integer, Map<Long, Map<String, Long>>> processTaskMap) {
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
            if ("DEPENDENT".equals(taskDefinitionLog.getTaskType())) {
                ObjectNode taskParams = JSONUtils.parseObject(taskDefinitionLog.getTaskParams());
                ObjectNode dependence = (ObjectNode) taskParams.get("dependence");
                ArrayNode dependTaskList =
                        JSONUtils.parseArray(JSONUtils.toJsonString(dependence.get("dependTaskList")));
                for (int i = 0; i < dependTaskList.size(); i++) {
                    ObjectNode dependTask = (ObjectNode) dependTaskList.path(i);
                    ArrayNode dependItemList =
                            JSONUtils.parseArray(JSONUtils.toJsonString(dependTask.get("dependItemList")));
                    for (int j = 0; j < dependItemList.size(); j++) {
                        ObjectNode dependItem = (ObjectNode) dependItemList.path(j);
                        dependItem.put("projectCode", projectIdCodeMap.get(dependItem.get("projectId").asInt()));
                        int definitionId = dependItem.get("definitionId").asInt();
                        Map<Long, Map<String, Long>> processCodeTaskNameCodeMap = processTaskMap.get(definitionId);
                        if (processCodeTaskNameCodeMap == null) {
                            log.warn(
                                    "We can't find processDefinition [{}], please check it is not exist, remove this dependence",
                                    definitionId);
                            dependItemList.remove(j);
                            continue;
                        }
                        Optional<Map.Entry<Long, Map<String, Long>>> mapEntry =
                                processCodeTaskNameCodeMap.entrySet().stream().findFirst();
                        if (mapEntry.isPresent()) {
                            Map.Entry<Long, Map<String, Long>> processCodeTaskNameCodeEntry = mapEntry.get();
                            dependItem.put("definitionCode", processCodeTaskNameCodeEntry.getKey());
                            String depTasks = dependItem.get("depTasks").asText();
                            long taskCode =
                                    "ALL".equals(depTasks) || processCodeTaskNameCodeEntry.getValue() == null ? 0L
                                            : processCodeTaskNameCodeEntry.getValue().get(depTasks);
                            dependItem.put("depTaskCode", taskCode);
                        }
                        dependItem.remove("projectId");
                        dependItem.remove("definitionId");
                        dependItem.remove("depTasks");
                        dependItemList.set(j, dependItem);
                    }
                    dependTask.put("dependItemList", dependItemList);
                    dependTaskList.set(i, dependTask);
                }
                dependence.put("dependTaskList", dependTaskList);
                taskDefinitionLog.setTaskParams(JSONUtils.toJsonString(taskParams));
            }
        }
    }

    private void convertConditions(List<TaskDefinitionLog> taskDefinitionLogList,
                                   Map<String, Long> taskNameCodeMap) throws Exception {
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogList) {
            if ("CONDITIONS".equals(taskDefinitionLog.getTaskType())) {
                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode taskParams = JSONUtils.parseObject(taskDefinitionLog.getTaskParams());
                // reset conditionResult
                ObjectNode conditionResult = (ObjectNode) taskParams.get("conditionResult");
                List<String> successNode =
                        JSONUtils.toList(conditionResult.get("successNode").toString(), String.class);
                List<Long> nodeCode = new ArrayList<>();
                successNode.forEach(node -> nodeCode.add(taskNameCodeMap.get(node)));
                conditionResult.set("successNode", objectMapper.readTree(objectMapper.writeValueAsString(nodeCode)));
                List<String> failedNode = JSONUtils.toList(conditionResult.get("failedNode").toString(), String.class);
                nodeCode.clear();
                failedNode.forEach(node -> nodeCode.add(taskNameCodeMap.get(node)));
                conditionResult.set("failedNode", objectMapper.readTree(objectMapper.writeValueAsString(nodeCode)));
                // reset dependItemList
                ObjectNode dependence = (ObjectNode) taskParams.get("dependence");
                ArrayNode dependTaskList =
                        JSONUtils.parseArray(JSONUtils.toJsonString(dependence.get("dependTaskList")));
                for (int i = 0; i < dependTaskList.size(); i++) {
                    ObjectNode dependTask = (ObjectNode) dependTaskList.path(i);
                    ArrayNode dependItemList =
                            JSONUtils.parseArray(JSONUtils.toJsonString(dependTask.get("dependItemList")));
                    for (int j = 0; j < dependItemList.size(); j++) {
                        ObjectNode dependItem = (ObjectNode) dependItemList.path(j);
                        JsonNode depTasks = dependItem.get("depTasks");
                        dependItem.put("depTaskCode", taskNameCodeMap.get(depTasks.asText()));
                        dependItem.remove("depTasks");
                        dependItemList.set(j, dependItem);
                    }
                    dependTask.put("dependItemList", dependItemList);
                    dependTaskList.set(i, dependTask);
                }
                dependence.put("dependTaskList", dependTaskList);
                taskDefinitionLog.setTaskParams(JSONUtils.toJsonString(taskParams));
            }
        }
    }

    private String convertLocations(String locations, Map<String, Long> taskIdCodeMap) {
        if (Strings.isNullOrEmpty(locations)) {
            return locations;
        }
        Map<String, ObjectNode> locationsMap =
                JSONUtils.parseObject(locations, new TypeReference<Map<String, ObjectNode>>() {
                });
        if (locationsMap == null) {
            return locations;
        }
        ArrayNode jsonNodes = JSONUtils.createArrayNode();
        for (Map.Entry<String, ObjectNode> entry : locationsMap.entrySet()) {
            ObjectNode nodes = JSONUtils.createObjectNode();
            nodes.put("taskCode", taskIdCodeMap.get(entry.getKey()));
            ObjectNode oldNodes = entry.getValue();
            nodes.put("x", oldNodes.get("x").asInt());
            nodes.put("y", oldNodes.get("y").asInt());
            jsonNodes.add(nodes);
        }
        return jsonNodes.toString();
    }

    private void handleProcessTaskRelation(Map<String, List<String>> taskNamePreMap,
                                           Map<String, Long> taskNameCodeMap,
                                           WorkflowDefinition workflowDefinition,
                                           List<WorkflowTaskRelationLog> processTaskRelationLogs) {
        Date now = new Date();
        for (Map.Entry<String, List<String>> entry : taskNamePreMap.entrySet()) {
            List<String> entryValue = entry.getValue();
            if (CollectionUtils.isNotEmpty(entryValue)) {
                for (String preTaskName : entryValue) {
                    WorkflowTaskRelationLog processTaskRelationLog = setProcessTaskRelationLog(workflowDefinition, now);
                    processTaskRelationLog.setPreTaskCode(taskNameCodeMap.get(preTaskName));
                    processTaskRelationLog.setPreTaskVersion(Constants.VERSION_FIRST);
                    processTaskRelationLog.setPostTaskCode(taskNameCodeMap.get(entry.getKey()));
                    processTaskRelationLog.setPostTaskVersion(Constants.VERSION_FIRST);
                    processTaskRelationLogs.add(processTaskRelationLog);
                }
            } else {
                WorkflowTaskRelationLog processTaskRelationLog = setProcessTaskRelationLog(workflowDefinition, now);
                processTaskRelationLog.setPreTaskCode(0);
                processTaskRelationLog.setPreTaskVersion(0);
                processTaskRelationLog.setPostTaskCode(taskNameCodeMap.get(entry.getKey()));
                processTaskRelationLog.setPostTaskVersion(Constants.VERSION_FIRST);
                processTaskRelationLogs.add(processTaskRelationLog);
            }
        }
    }

    private WorkflowTaskRelationLog setProcessTaskRelationLog(WorkflowDefinition workflowDefinition, Date now) {
        WorkflowTaskRelationLog processTaskRelationLog = new WorkflowTaskRelationLog();
        processTaskRelationLog.setProjectCode(workflowDefinition.getProjectCode());
        processTaskRelationLog.setProcessDefinitionCode(workflowDefinition.getCode());
        processTaskRelationLog.setProcessDefinitionVersion(workflowDefinition.getVersion());
        processTaskRelationLog.setConditionType(ConditionType.NONE);
        processTaskRelationLog.setConditionParams("{}");
        processTaskRelationLog.setOperator(1);
        processTaskRelationLog.setOperateTime(now);
        processTaskRelationLog.setCreateTime(now);
        processTaskRelationLog.setUpdateTime(now);
        return processTaskRelationLog;
    }

    @Override
    public DolphinSchedulerVersion getCurrentVersion() {
        return DolphinSchedulerVersion.V2_0_0;
    }
}
