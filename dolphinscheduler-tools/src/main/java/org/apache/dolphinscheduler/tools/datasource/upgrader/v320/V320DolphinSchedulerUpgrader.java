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

package org.apache.dolphinscheduler.tools.datasource.upgrader.v320;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.tools.datasource.upgrader.DolphinSchedulerUpgrader;
import org.apache.dolphinscheduler.tools.datasource.upgrader.DolphinSchedulerVersion;
import org.apache.dolphinscheduler.tools.datasource.upgrader.UpgradeDao;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class V320DolphinSchedulerUpgrader implements DolphinSchedulerUpgrader {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UpgradeDao upgradeDao;

    @SneakyThrows
    @Override
    public void doUpgrade() {
        upgradeWorkflowInstance();
        upgradeTaskInstance();
        upgradeDao.upgradeDolphinSchedulerDDL(getCurrentVersion().getVersionName() + "_schema",
                "dolphinscheduler_ddl_post.sql");
    }

    private void upgradeWorkflowInstance() {
        Map<Integer, String> userMap = getUserMap();
        while (true) {
            List<Map<String, Object>> needUpdateWorkflowInstances = getProcessInstanceWhichProjectCodeIsNull();
            if (CollectionUtils.isEmpty(needUpdateWorkflowInstances)) {
                return;
            }
            needUpdateWorkflowInstances.parallelStream()
                    .forEach(processInstance -> {
                        Integer id = (Integer) processInstance.get("id");
                        Long processDefinitionCode = (Long) processInstance.get("process_definition_code");
                        Integer processDefinitionVersion = (Integer) processInstance.get("process_definition_version");

                        Map<String, Object> processDefinitionLog =
                                getProcessDefinitionLogByCode(processDefinitionCode, processDefinitionVersion);

                        Long projectCode = -1L;
                        String tenantCode = null;
                        String executorName = null;
                        if (MapUtils.isNotEmpty(processDefinitionLog)) {
                            Map<String, Object> scheduler = getSchedulerByProcessDefinitionCode(processDefinitionCode);
                            projectCode = processDefinitionLog.get("project_code") == null ? -1L
                                    : (Long) processDefinitionLog.get("project_code");
                            tenantCode = scheduler.get("tenant_code") == null ? Constants.DEFAULT
                                    : (String) scheduler.get("tenant_code");
                            executorName = userMap.get((Integer) processInstance.get("executor_id"));
                        }
                        updateProjectCodeInProcessInstance(id, projectCode, tenantCode, executorName);
                    });
            log.info("Success upgrade workflow instance, current batch size: {}", needUpdateWorkflowInstances.size());
        }
    }

    private void upgradeTaskInstance() {
        while (true) {
            List<Map<String, Object>> taskInstances = getTaskInstanceWhichProjectCodeIsNull();
            if (CollectionUtils.isEmpty(taskInstances)) {
                return;
            }

            taskInstances.parallelStream()
                    .forEach(taskInstance -> {
                        Integer id = (Integer) taskInstance.get("id");
                        Integer processInstanceId = (Integer) taskInstance.get("process_instance_id");
                        Map<String, Object> processInstance = getProcessInstanceById(processInstanceId);

                        Long projectCode = -1L;
                        String processInstanceName = null;
                        String executorName = null;

                        if (MapUtils.isNotEmpty(processInstance)) {
                            projectCode = processInstance.get("project_code") == null ? -1L
                                    : (Long) processInstance.get("project_code");
                            processInstanceName = (String) processInstance.get("name");
                            executorName = (String) processInstance.get("executor_name");
                        }
                        updateProjectCodeInTaskInstance(id, projectCode, processInstanceName, executorName);
                    });
            log.info("Success upgrade task instance, current batch size: {}", taskInstances.size());
        }
    }

    private List<Map<String, Object>> getTaskInstanceWhichProjectCodeIsNull() {
        List<Map<String, Object>> processInstanceList = new ArrayList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select id, process_instance_id from t_ds_task_instance where project_code is null limit 1000");
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", resultSet.getInt("id"));
                row.put("process_instance_id", resultSet.getInt("process_instance_id"));
                processInstanceList.add(row);
            }
            return processInstanceList;
        } catch (Exception ex) {
            throw new RuntimeException("Query t_ds_process_instance error", ex);
        }

    }

    private Map<Integer, String> getUserMap() {
        Map<Integer, String> userMap = new HashMap<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement("select id, user_name from t_ds_user");
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                userMap.put(resultSet.getInt("id"), resultSet.getString("user_name"));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Query t_ds_user error", ex);
        }
        return userMap;
    }

    private List<Map<String, Object>> getProcessInstanceWhichProjectCodeIsNull() {
        List<Map<String, Object>> processInstanceList = new ArrayList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select id, process_definition_code, process_definition_version, executor_id from t_ds_process_instance where project_code is null limit 1000");
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", resultSet.getInt("id"));
                row.put("process_definition_code", resultSet.getLong("process_definition_code"));
                row.put("process_definition_version", resultSet.getInt("process_definition_version"));
                row.put("executor_id", resultSet.getInt("executor_id"));
                processInstanceList.add(row);
            }
            return processInstanceList;
        } catch (Exception ex) {
            throw new RuntimeException("Query t_ds_process_instance error", ex);
        }
    }

    private Map<String, Object> getProcessInstanceById(Integer processInstanceId) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select project_code, name, executor_name from t_ds_process_instance where id = ?");) {
            preparedStatement.setInt(1, processInstanceId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("project_code", resultSet.getLong("project_code"));
                    row.put("name", resultSet.getString("name"));
                    row.put("executor_name", resultSet.getString("executor_name"));
                    return row;
                }
            }
            return Collections.emptyMap();
        } catch (Exception ex) {
            throw new RuntimeException("Query t_ds_process_instance error", ex);
        }
    }

    private Map<String, Object> getProcessDefinitionLogByCode(Long processDefinitionCode,
                                                              Integer processDefinitionVersion) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select  project_code from t_ds_process_definition_log where code = ? and version = ?")) {
            preparedStatement.setLong(1, processDefinitionCode);
            preparedStatement.setInt(2, processDefinitionVersion);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("project_code", resultSet.getLong("project_code"));
                    return row;
                }
            }
            return Collections.emptyMap();
        } catch (Exception ex) {
            throw new RuntimeException("Query t_ds_process_definition_log error", ex);
        }
    }

    private Map<String, Object> getSchedulerByProcessDefinitionCode(Long processDefinitionCode) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection
                        .prepareStatement("select  * from t_ds_schedules where process_definition_code = ?")) {
            preparedStatement.setLong(1, processDefinitionCode);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("tenant_code", resultSet.getString("tenant_code"));
                    return row;
                }
            }
            return Collections.emptyMap();
        } catch (Exception ex) {
            throw new RuntimeException("Query t_ds_schedules error", ex);
        }
    }

    private void updateProjectCodeInProcessInstance(Integer processInstanceId, Long projectCode, String tenantCode,
                                                    String executorName) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "update t_ds_process_instance set project_code = ?, tenant_code = ?, executor_name = ? where id = ?")) {
            preparedStatement.setLong(1, projectCode);
            preparedStatement.setString(2, tenantCode);
            preparedStatement.setString(3, executorName);
            preparedStatement.setInt(4, processInstanceId);
            preparedStatement.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Update t_ds_process_instance error", ex);
        }
    }

    private void updateProjectCodeInTaskInstance(Integer id, Long projectCode, String processInstanceName,
                                                 String executorName) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "update t_ds_task_instance set project_code = ?, process_instance_name = ?, executor_name = ? where id = ?")) {
            preparedStatement.setLong(1, projectCode);
            preparedStatement.setString(2, processInstanceName);
            preparedStatement.setString(3, executorName);
            preparedStatement.setInt(4, id);
            preparedStatement.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Update t_ds_process_instance error", ex);
        }
    }

    @Override
    public DolphinSchedulerVersion getCurrentVersion() {
        return DolphinSchedulerVersion.V3_2_0;
    }
}
