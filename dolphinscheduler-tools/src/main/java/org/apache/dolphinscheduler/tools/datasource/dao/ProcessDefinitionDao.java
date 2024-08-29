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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Deprecated
@Slf4j
public class ProcessDefinitionDao {

    /**
     * queryAllProcessDefinition
     *
     * @param conn jdbc connection
     * @return ProcessDefinition Json List
     */
    public Map<Integer, String> queryAllProcessDefinition(Connection conn) {

        Map<Integer, String> processDefinitionJsonMap = new HashMap<>();

        String sql = "SELECT id,process_definition_json FROM t_ds_process_definition";
        try (
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Integer id = rs.getInt(1);
                String processDefinitionJson = rs.getString(2);
                processDefinitionJsonMap.put(id, processDefinitionJson);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + sql, e);
        }

        return processDefinitionJsonMap;
    }

    /**
     * updateProcessDefinitionJson
     *
     * @param conn jdbc connection
     * @param processDefinitionJsonMap processDefinitionJsonMap
     */
    public void updateProcessDefinitionJson(Connection conn, Map<Integer, String> processDefinitionJsonMap) {
        String sql = "UPDATE t_ds_process_definition SET process_definition_json=? where id=?";
        try {
            for (Map.Entry<Integer, String> entry : processDefinitionJsonMap.entrySet()) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, entry.getValue());
                    pstmt.setInt(2, entry.getKey());
                    pstmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + sql, e);
        }
    }

    public List<WorkflowDefinition> queryProcessDefinition(Connection conn) {
        List<WorkflowDefinition> workflowDefinitions = new ArrayList<>();
        String sql =
                "SELECT id,code,project_code,user_id,locations,name,description,release_state,flag,create_time FROM t_ds_process_definition";
        try (
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                WorkflowDefinition workflowDefinition = new WorkflowDefinition();
                workflowDefinition.setId(rs.getInt(1));
                long code = rs.getLong(2);
                if (code == 0L) {
                    code = CodeGenerateUtils.genCode();
                }
                workflowDefinition.setCode(code);
                workflowDefinition.setVersion(Constants.VERSION_FIRST);
                workflowDefinition.setProjectCode(rs.getLong(3));
                workflowDefinition.setUserId(rs.getInt(4));
                workflowDefinition.setLocations(rs.getString(5));
                workflowDefinition.setName(rs.getString(6));
                workflowDefinition.setDescription(rs.getString(7));
                workflowDefinition.setReleaseState(ReleaseState.getEnum(rs.getInt(8)));
                workflowDefinition.setFlag(rs.getInt(9) == 1 ? Flag.YES : Flag.NO);
                workflowDefinition.setCreateTime(rs.getDate(10));
                workflowDefinitions.add(workflowDefinition);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + sql, e);
        }
        return workflowDefinitions;
    }

    /**
     * updateProcessDefinitionCode
     *
     * @param conn jdbc connection
     * @param workflowDefinitions processDefinitions
     * @param projectIdCodeMap projectIdCodeMap
     */
    public void updateProcessDefinitionCode(Connection conn, List<WorkflowDefinition> workflowDefinitions,
                                            Map<Integer, Long> projectIdCodeMap) {
        String sql = "UPDATE t_ds_process_definition SET code=?, project_code=?, version=? where id=?";
        try {
            for (WorkflowDefinition workflowDefinition : workflowDefinitions) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setLong(1, workflowDefinition.getCode());
                    long projectCode = workflowDefinition.getProjectCode();
                    if (String.valueOf(projectCode).length() <= 10) {
                        Integer projectId = Integer.parseInt(String.valueOf(projectCode));
                        if (projectIdCodeMap.containsKey(projectId)) {
                            projectCode = projectIdCodeMap.get(projectId);
                            workflowDefinition.setProjectCode(projectCode);
                        }
                    }
                    pstmt.setLong(2, projectCode);
                    pstmt.setInt(3, workflowDefinition.getVersion());
                    pstmt.setInt(4, workflowDefinition.getId());
                    pstmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + sql, e);
        }
    }
}
