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

package org.apache.dolphinscheduler.tools.datasource.dao;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_CONDITIONS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_DEPENDENT;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SUB_PROCESS;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ConditionType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.ConnectionUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ScriptRunner;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.upgrade.JsonSplitDao;
import org.apache.dolphinscheduler.dao.upgrade.ProcessDefinitionDao;
import org.apache.dolphinscheduler.dao.upgrade.ProjectDao;
import org.apache.dolphinscheduler.dao.upgrade.ScheduleDao;
import org.apache.dolphinscheduler.dao.upgrade.SchemaUtils;
import org.apache.dolphinscheduler.dao.upgrade.WorkerGroupDao;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.TaskTimeoutParameter;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections.CollectionUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

public abstract class UpgradeDao {

    public static final Logger logger = LoggerFactory.getLogger(UpgradeDao.class);
    private static final String T_VERSION_NAME = "t_escheduler_version";
    private static final String T_NEW_VERSION_NAME = "t_ds_version";

    protected final DataSource dataSource;

    protected UpgradeDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected abstract String initSqlPath();

    public abstract DbType getDbType();

    public void initSchema() {
        // Execute the dolphinscheduler full sql
        runInitSql(getDbType());
    }

    /**
     * run init sql to init db schema
     * @param dbType db type
     */
    private void runInitSql(DbType dbType) {
        String sqlFile = String.format("dolphinscheduler_%s.sql", dbType.getDescp());
        Resource mysqlSQLFilePath = new ClassPathResource("sql/" + sqlFile);
        try (Connection conn = dataSource.getConnection()) {
            // Execute the dolphinscheduler_ddl.sql script to create the table structure of dolphinscheduler
            ScriptRunner initScriptRunner = new ScriptRunner(conn, true, true);
            try (Reader initSqlReader = new InputStreamReader(mysqlSQLFilePath.getInputStream())) {
                initScriptRunner.runScript(initSqlReader);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public abstract boolean isExistsTable(String tableName);

    public abstract boolean isExistsColumn(String tableName, String columnName);

    public String getCurrentVersion(String versionName) {
        String sql = String.format("select version from %s", versionName);
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String version = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                version = rs.getString(1);
            }

            return version;

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + sql, e);
        } finally {
            ConnectionUtils.releaseResource(rs, pstmt, conn);
        }
    }

    public void upgradeDolphinScheduler(String schemaDir) {
        upgradeDolphinSchedulerDDL(schemaDir, "dolphinscheduler_ddl.sql");
        upgradeDolphinSchedulerDML(schemaDir);
    }

    /**
     * upgrade DolphinScheduler worker group
     * ds-1.3.0 modify the worker group for process definition json
     */
    public void upgradeDolphinSchedulerWorkerGroup() {
        updateProcessDefinitionJsonWorkerGroup();
    }

    /**
     * upgrade DolphinScheduler resource list
     * ds-1.3.2 modify the resource list for process definition json
     */
    public void upgradeDolphinSchedulerResourceList() {
        updateProcessDefinitionJsonResourceList();
    }

    /**
     * upgrade DolphinScheduler to 2.0.0
     */
    public void upgradeDolphinSchedulerTo200(String schemaDir) {
        processDefinitionJsonSplit();
        upgradeDolphinSchedulerDDL(schemaDir, "dolphinscheduler_ddl_post.sql");
    }

    /**
     * upgrade DolphinScheduler to 2.0.6
     */
    public void upgradeDolphinSchedulerResourceFileSize() {
        ResourceDao resourceDao = new ResourceDao();
        try {
            // update the size of the folder that is the type of file.
            resourceDao.updateResourceFolderSizeByFileType(dataSource.getConnection(), 0);
            // update the size of the folder that is the type of udf.
            resourceDao.updateResourceFolderSizeByFileType(dataSource.getConnection(), 1);
        } catch (Exception ex) {
            logger.error("Failed to upgrade because of failing to update the folder's size of resource files.");
        }
    }

    /**
     * updateProcessDefinitionJsonWorkerGroup
     */
    protected void updateProcessDefinitionJsonWorkerGroup() {
        WorkerGroupDao workerGroupDao = new WorkerGroupDao();
        ProcessDefinitionDao processDefinitionDao = new ProcessDefinitionDao();
        Map<Integer, String> replaceProcessDefinitionMap = new HashMap<>();
        try {
            Map<Integer, String> oldWorkerGroupMap = workerGroupDao.queryAllOldWorkerGroup(dataSource.getConnection());
            Map<Integer, String> processDefinitionJsonMap =
                    processDefinitionDao.queryAllProcessDefinition(dataSource.getConnection());

            for (Map.Entry<Integer, String> entry : processDefinitionJsonMap.entrySet()) {
                ObjectNode jsonObject = JSONUtils.parseObject(entry.getValue());
                ArrayNode tasks = JSONUtils.parseArray(jsonObject.get("tasks").toString());

                for (int i = 0; i < tasks.size(); i++) {
                    ObjectNode task = (ObjectNode) tasks.path(i);
                    ObjectNode workerGroupNode = (ObjectNode) task.path("workerGroupId");
                    int workerGroupId = -1;
                    if (workerGroupNode != null && workerGroupNode.canConvertToInt()) {
                        workerGroupId = workerGroupNode.asInt(-1);
                    }
                    if (workerGroupId == -1) {
                        task.put("workerGroup", "default");
                    } else {
                        task.put("workerGroup", oldWorkerGroupMap.get(workerGroupId));
                    }
                }

                jsonObject.remove("task");

                jsonObject.put("tasks", tasks);

                replaceProcessDefinitionMap.put(entry.getKey(), jsonObject.toString());
            }
            if (replaceProcessDefinitionMap.size() > 0) {
                processDefinitionDao.updateProcessDefinitionJson(dataSource.getConnection(),
                        replaceProcessDefinitionMap);
            }
        } catch (Exception e) {
            logger.error("update process definition json workergroup error", e);
        }
    }

    protected void updateProcessDefinitionJsonResourceList() {
        ResourceDao resourceDao = new ResourceDao();
        ProcessDefinitionDao processDefinitionDao = new ProcessDefinitionDao();
        Map<Integer, String> replaceProcessDefinitionMap = new HashMap<>();
        try {
            Map<String, Integer> resourcesMap = resourceDao.listAllResources(dataSource.getConnection());
            Map<Integer, String> processDefinitionJsonMap =
                    processDefinitionDao.queryAllProcessDefinition(dataSource.getConnection());

            for (Map.Entry<Integer, String> entry : processDefinitionJsonMap.entrySet()) {
                ObjectNode jsonObject = JSONUtils.parseObject(entry.getValue());
                ArrayNode tasks = JSONUtils.parseArray(jsonObject.get("tasks").toString());

                for (int i = 0; i < tasks.size(); i++) {
                    ObjectNode task = (ObjectNode) tasks.get(i);
                    ObjectNode param = (ObjectNode) task.get("params");
                    if (param != null) {

                        List<ResourceInfo> resourceList =
                                JSONUtils.toList(param.get("resourceList").toString(), ResourceInfo.class);
                        ResourceInfo mainJar =
                                JSONUtils.parseObject(param.get("mainJar").toString(), ResourceInfo.class);
                        if (mainJar != null && mainJar.getId() == null) {
                            String fullName = mainJar.getRes().startsWith("/") ? mainJar.getRes()
                                    : String.format("/%s", mainJar.getRes());
                            if (resourcesMap.containsKey(fullName)) {
                                mainJar.setId(resourcesMap.get(fullName));
                                param.put("mainJar", JSONUtils.parseObject(JSONUtils.toJsonString(mainJar)));
                            }
                        }

                        if (CollectionUtils.isNotEmpty(resourceList)) {
                            List<ResourceInfo> newResourceList = resourceList.stream().map(resInfo -> {
                                String fullName = resInfo.getRes().startsWith("/") ? resInfo.getRes()
                                        : String.format("/%s", resInfo.getRes());
                                if (resInfo.getId() == null && resourcesMap.containsKey(fullName)) {
                                    resInfo.setId(resourcesMap.get(fullName));
                                }
                                return resInfo;
                            }).collect(Collectors.toList());
                            param.put("resourceList", JSONUtils.parseObject(JSONUtils.toJsonString(newResourceList)));
                        }
                    }
                    task.put("params", param);

                }

                jsonObject.remove("tasks");

                jsonObject.put("tasks", tasks);

                replaceProcessDefinitionMap.put(entry.getKey(), jsonObject.toString());
            }
            if (replaceProcessDefinitionMap.size() > 0) {
                processDefinitionDao.updateProcessDefinitionJson(dataSource.getConnection(),
                        replaceProcessDefinitionMap);
            }
        } catch (Exception e) {
            logger.error("update process definition json resource list error", e);
        }

    }

    private void upgradeDolphinSchedulerDML(String schemaDir) {
        String schemaVersion = schemaDir.split("_")[0];
        Resource sqlFilePath = new ClassPathResource(String.format("sql/upgrade/%s/%s/dolphinscheduler_dml.sql",
                schemaDir, getDbType().name().toLowerCase()));
        logger.info("sqlSQLFilePath: {}", sqlFilePath);
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            // Execute the upgraded dolphinscheduler dml
            ScriptRunner scriptRunner = new ScriptRunner(conn, false, true);
            try (Reader sqlReader = new InputStreamReader(sqlFilePath.getInputStream())) {
                scriptRunner.runScript(sqlReader);
                if (isExistsTable(T_VERSION_NAME)) {
                    // Change version in the version table to the new version
                    String upgradeSQL = String.format("update %s set version = ?", T_VERSION_NAME);
                    pstmt = conn.prepareStatement(upgradeSQL);
                    pstmt.setString(1, schemaVersion);
                    pstmt.executeUpdate();
                } else if (isExistsTable(T_NEW_VERSION_NAME)) {
                    // Change version in the version table to the new version
                    String upgradeSQL = String.format("update %s set version = ?", T_NEW_VERSION_NAME);
                    pstmt = conn.prepareStatement(upgradeSQL);
                    pstmt.setString(1, schemaVersion);
                    pstmt.executeUpdate();
                }
                conn.commit();
            }
        } catch (FileNotFoundException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                logger.error(e1.getMessage(), e1);
            }
            logger.error(e.getMessage(), e);
            throw new RuntimeException("sql file not found ", e);
        } catch (IOException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                logger.error(e1.getMessage(), e1);
            }
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            try {
                if (null != conn) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                logger.error(e1.getMessage(), e1);
            }
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            ConnectionUtils.releaseResource(pstmt, conn);
        }

    }

    /**
     * upgradeDolphinScheduler DDL
     *
     * @param schemaDir schemaDir
     */
    private void upgradeDolphinSchedulerDDL(String schemaDir, String scriptFile) {
        Resource sqlFilePath = new ClassPathResource(
                String.format("sql/upgrade/%s/%s/%s", schemaDir, getDbType().name().toLowerCase(), scriptFile));
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            String dbName = conn.getCatalog();
            logger.info(dbName);
            conn.setAutoCommit(true);
            // Execute the dolphinscheduler ddl.sql for the upgrade
            ScriptRunner scriptRunner = new ScriptRunner(conn, true, true);
            try (Reader sqlReader = new InputStreamReader(sqlFilePath.getInputStream())) {
                scriptRunner.runScript(sqlReader);
            }
        } catch (FileNotFoundException e) {

            logger.error(e.getMessage(), e);
            throw new RuntimeException("sql file not found ", e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            ConnectionUtils.releaseResource(pstmt, conn);
        }
    }

    /**
     * update version
     *
     * @param version version
     */
    public void updateVersion(String version) {
        // Change version in the version table to the new version
        String versionName = T_VERSION_NAME;
        if (!SchemaUtils.isAGreatVersion("1.2.0", version)) {
            versionName = "t_ds_version";
        }
        String upgradeSQL = String.format("update %s set version = ?", versionName);
        PreparedStatement pstmt = null;
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(upgradeSQL);
            pstmt.setString(1, version);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + upgradeSQL, e);
        } finally {
            ConnectionUtils.releaseResource(pstmt, conn);
        }

    }

    /**
     * upgrade DolphinScheduler to 2.0.0, json split
     */
    private void processDefinitionJsonSplit() {
        ProjectDao projectDao = new ProjectDao();
        ProcessDefinitionDao processDefinitionDao = new ProcessDefinitionDao();
        ScheduleDao scheduleDao = new ScheduleDao();
        JsonSplitDao jsonSplitDao = new JsonSplitDao();
        try {
            // execute project
            Map<Integer, Long> projectIdCodeMap = projectDao.queryAllProject(dataSource.getConnection());
            projectDao.updateProjectCode(dataSource.getConnection(), projectIdCodeMap);

            // execute process definition code
            List<ProcessDefinition> processDefinitions =
                    processDefinitionDao.queryProcessDefinition(dataSource.getConnection());
            processDefinitionDao.updateProcessDefinitionCode(dataSource.getConnection(), processDefinitions,
                    projectIdCodeMap);

            // execute schedule
            Map<Integer, Long> allSchedule = scheduleDao.queryAllSchedule(dataSource.getConnection());
            Map<Integer, Long> processIdCodeMap = processDefinitions.stream()
                    .collect(Collectors.toMap(ProcessDefinition::getId, ProcessDefinition::getCode));
            scheduleDao.updateScheduleCode(dataSource.getConnection(), allSchedule, processIdCodeMap);

            // json split
            Map<Integer, String> processDefinitionJsonMap =
                    processDefinitionDao.queryAllProcessDefinition(dataSource.getConnection());
            List<ProcessDefinitionLog> processDefinitionLogs = new ArrayList<>();
            List<ProcessTaskRelationLog> processTaskRelationLogs = new ArrayList<>();
            List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
            Map<Integer, Map<Long, Map<String, Long>>> processTaskMap = new HashMap<>();
            splitProcessDefinitionJson(processDefinitions, processDefinitionJsonMap, processDefinitionLogs,
                    processTaskRelationLogs, taskDefinitionLogs, processTaskMap);
            convertDependence(taskDefinitionLogs, projectIdCodeMap, processTaskMap);

            // execute json split
            jsonSplitDao.executeJsonSplitProcessDefinition(dataSource.getConnection(), processDefinitionLogs);
            jsonSplitDao.executeJsonSplitProcessTaskRelation(dataSource.getConnection(), processTaskRelationLogs);
            jsonSplitDao.executeJsonSplitTaskDefinition(dataSource.getConnection(), taskDefinitionLogs);
        } catch (Exception e) {
            logger.error("json split error", e);
        }
    }

    private void splitProcessDefinitionJson(List<ProcessDefinition> processDefinitions,
                                            Map<Integer, String> processDefinitionJsonMap,
                                            List<ProcessDefinitionLog> processDefinitionLogs,
                                            List<ProcessTaskRelationLog> processTaskRelationLogs,
                                            List<TaskDefinitionLog> taskDefinitionLogs,
                                            Map<Integer, Map<Long, Map<String, Long>>> processTaskMap) throws Exception {
        Map<Integer, ProcessDefinition> processDefinitionMap = processDefinitions.stream()
                .collect(Collectors.toMap(ProcessDefinition::getId, processDefinition -> processDefinition));
        Date now = new Date();
        for (Map.Entry<Integer, String> entry : processDefinitionJsonMap.entrySet()) {
            if (entry.getValue() == null) {
                throw new Exception("processDefinitionJson is null");
            }
            ObjectNode jsonObject = JSONUtils.parseObject(entry.getValue());
            ProcessDefinition processDefinition = processDefinitionMap.get(entry.getKey());
            if (processDefinition != null) {
                processDefinition
                        .setTenantId(jsonObject.get("tenantId") == null ? -1 : jsonObject.get("tenantId").asInt());
                processDefinition.setTimeout(jsonObject.get("timeout").asInt());
                processDefinition.setGlobalParams(jsonObject.get("globalParams").toString());
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
                    if (TASK_TYPE_SUB_PROCESS.equals(taskType)) {
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
                        TASK_TYPE_SUB_PROCESS.equals(taskType) ? 1 : task.get("retryInterval").asInt());
                taskDefinitionLog.setFailRetryTimes(
                        TASK_TYPE_SUB_PROCESS.equals(taskType) ? 0 : task.get("maxRetryTimes").asInt());
                taskDefinitionLog.setTaskPriority(JSONUtils
                        .parseObject(JSONUtils.toJsonString(task.get("taskInstancePriority")), Priority.class));
                String name = task.get("name").asText();
                taskDefinitionLog.setName(name);
                taskDefinitionLog
                        .setWorkerGroup(task.get("workerGroup") == null ? "default" : task.get("workerGroup").asText());
                long taskCode = CodeGenerateUtils.getInstance().genCode();
                taskDefinitionLog.setCode(taskCode);
                taskDefinitionLog.setVersion(Constants.VERSION_FIRST);
                taskDefinitionLog.setProjectCode(processDefinition.getProjectCode());
                taskDefinitionLog.setUserId(processDefinition.getUserId());
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
            processDefinition.setLocations(convertLocations(processDefinition.getLocations(), taskIdCodeMap));
            ProcessDefinitionLog processDefinitionLog = new ProcessDefinitionLog(processDefinition);
            processDefinitionLog.setOperator(1);
            processDefinitionLog.setOperateTime(now);
            processDefinitionLog.setUpdateTime(now);
            processDefinitionLogs.add(processDefinitionLog);
            handleProcessTaskRelation(taskNamePreMap, taskNameCodeMap, processDefinition, processTaskRelationLogs);
            processCodeTaskNameCodeMap.put(processDefinition.getCode(), taskNameCodeMap);
            processTaskMap.put(entry.getKey(), processCodeTaskNameCodeMap);
        }
    }

    public void convertConditions(List<TaskDefinitionLog> taskDefinitionLogList,
                                  Map<String, Long> taskNameCodeMap) throws Exception {
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogList) {
            if (TASK_TYPE_CONDITIONS.equals(taskDefinitionLog.getTaskType())) {
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

    public void convertDependence(List<TaskDefinitionLog> taskDefinitionLogs,
                                  Map<Integer, Long> projectIdCodeMap,
                                  Map<Integer, Map<Long, Map<String, Long>>> processTaskMap) {
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
            if (TASK_TYPE_DEPENDENT.equals(taskDefinitionLog.getTaskType())) {
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
                            logger.warn(
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

    private void handleProcessTaskRelation(Map<String, List<String>> taskNamePreMap,
                                           Map<String, Long> taskNameCodeMap,
                                           ProcessDefinition processDefinition,
                                           List<ProcessTaskRelationLog> processTaskRelationLogs) {
        Date now = new Date();
        for (Map.Entry<String, List<String>> entry : taskNamePreMap.entrySet()) {
            List<String> entryValue = entry.getValue();
            if (CollectionUtils.isNotEmpty(entryValue)) {
                for (String preTaskName : entryValue) {
                    ProcessTaskRelationLog processTaskRelationLog = setProcessTaskRelationLog(processDefinition, now);
                    processTaskRelationLog.setPreTaskCode(taskNameCodeMap.get(preTaskName));
                    processTaskRelationLog.setPreTaskVersion(Constants.VERSION_FIRST);
                    processTaskRelationLog.setPostTaskCode(taskNameCodeMap.get(entry.getKey()));
                    processTaskRelationLog.setPostTaskVersion(Constants.VERSION_FIRST);
                    processTaskRelationLogs.add(processTaskRelationLog);
                }
            } else {
                ProcessTaskRelationLog processTaskRelationLog = setProcessTaskRelationLog(processDefinition, now);
                processTaskRelationLog.setPreTaskCode(0);
                processTaskRelationLog.setPreTaskVersion(0);
                processTaskRelationLog.setPostTaskCode(taskNameCodeMap.get(entry.getKey()));
                processTaskRelationLog.setPostTaskVersion(Constants.VERSION_FIRST);
                processTaskRelationLogs.add(processTaskRelationLog);
            }
        }
    }

    private ProcessTaskRelationLog setProcessTaskRelationLog(ProcessDefinition processDefinition, Date now) {
        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
        processTaskRelationLog.setProjectCode(processDefinition.getProjectCode());
        processTaskRelationLog.setProcessDefinitionCode(processDefinition.getCode());
        processTaskRelationLog.setProcessDefinitionVersion(processDefinition.getVersion());
        processTaskRelationLog.setConditionType(ConditionType.NONE);
        processTaskRelationLog.setConditionParams("{}");
        processTaskRelationLog.setOperator(1);
        processTaskRelationLog.setOperateTime(now);
        processTaskRelationLog.setCreateTime(now);
        processTaskRelationLog.setUpdateTime(now);
        return processTaskRelationLog;
    }
}
