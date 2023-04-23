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

package org.apache.dolphinscheduler.dao.upgrade;

import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonSplitDao {

    /**
     * executeJsonSplitProcessDefinition
     *
     * @param conn jdbc connection
     * @param processDefinitionLogs processDefinitionLogs
     */
    public void executeJsonSplitProcessDefinition(Connection conn, List<ProcessDefinitionLog> processDefinitionLogs) {
        String updateSql =
                "UPDATE t_ds_process_definition SET global_params=?,timeout=?,locations=?,update_time=? where id=?";
        String insertLogSql =
                "insert into t_ds_process_definition_log (code,name,version,description,project_code,release_state,user_id,"
                        + "global_params,flag,locations,timeout,operator,operate_time,create_time,update_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement processUpdate = conn.prepareStatement(updateSql);
            PreparedStatement insertLog = conn.prepareStatement(insertLogSql);
            int i = 0;
            for (ProcessDefinitionLog processDefinitionLog : processDefinitionLogs) {
                processUpdate.setString(1, processDefinitionLog.getGlobalParams());
                processUpdate.setInt(2, processDefinitionLog.getTimeout());
                processUpdate.setString(3, processDefinitionLog.getLocations());
                processUpdate.setDate(4, new Date(processDefinitionLog.getUpdateTime().getTime()));
                processUpdate.setInt(5, processDefinitionLog.getId());
                processUpdate.addBatch();

                insertLog.setLong(1, processDefinitionLog.getCode());
                insertLog.setString(2, processDefinitionLog.getName());
                insertLog.setInt(3, processDefinitionLog.getVersion());
                insertLog.setString(4, processDefinitionLog.getDescription());
                insertLog.setLong(5, processDefinitionLog.getProjectCode());
                insertLog.setInt(6, processDefinitionLog.getReleaseState().getCode());
                insertLog.setInt(7, processDefinitionLog.getUserId());
                insertLog.setString(8, processDefinitionLog.getGlobalParams());
                insertLog.setInt(9, processDefinitionLog.getFlag().getCode());
                insertLog.setString(10, processDefinitionLog.getLocations());
                insertLog.setInt(11, processDefinitionLog.getTimeout());
                insertLog.setInt(12, processDefinitionLog.getOperator());
                insertLog.setDate(13, new Date(processDefinitionLog.getOperateTime().getTime()));
                insertLog.setDate(14, new Date(processDefinitionLog.getCreateTime().getTime()));
                insertLog.setDate(15, new Date(processDefinitionLog.getUpdateTime().getTime()));
                insertLog.addBatch();

                i++;
                if (i % 1000 == 0) {
                    processUpdate.executeBatch();
                    processUpdate.clearBatch();
                    insertLog.executeBatch();
                    insertLog.clearBatch();
                }
            }
            processUpdate.executeBatch();
            insertLog.executeBatch();
            processUpdate.close();
            insertLog.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * executeJsonSplitProcessDefinition
     *
     * @param conn jdbc connection
     * @param processTaskRelationLogs processTaskRelationLogs
     */
    public void executeJsonSplitProcessTaskRelation(Connection conn,
                                                    List<ProcessTaskRelationLog> processTaskRelationLogs) {
        String insertSql =
                "insert into t_ds_process_task_relation (project_code,process_definition_code,process_definition_version,pre_task_code,pre_task_version,"
                        + "post_task_code,post_task_version,condition_type,condition_params,create_time,update_time) values (?,?,?,?,?,?,?,?,?,?,?)";
        String insertLogSql =
                "insert into t_ds_process_task_relation_log (project_code,process_definition_code,process_definition_version,pre_task_code,"
                        + "pre_task_version,post_task_code,post_task_version,condition_type,condition_params,operator,operate_time,create_time,update_time) "
                        + "values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement insert = conn.prepareStatement(insertSql);
            PreparedStatement insertLog = conn.prepareStatement(insertLogSql);
            int i = 0;
            for (ProcessTaskRelationLog processTaskRelationLog : processTaskRelationLogs) {
                insert.setLong(1, processTaskRelationLog.getProjectCode());
                insert.setLong(2, processTaskRelationLog.getProcessDefinitionCode());
                insert.setInt(3, processTaskRelationLog.getProcessDefinitionVersion());
                insert.setLong(4, processTaskRelationLog.getPreTaskCode());
                insert.setInt(5, processTaskRelationLog.getPreTaskVersion());
                insert.setLong(6, processTaskRelationLog.getPostTaskCode());
                insert.setInt(7, processTaskRelationLog.getPostTaskVersion());
                insert.setInt(8, processTaskRelationLog.getConditionType().getCode());
                insert.setString(9, processTaskRelationLog.getConditionParams());
                insert.setDate(10, new Date(processTaskRelationLog.getCreateTime().getTime()));
                insert.setDate(11, new Date(processTaskRelationLog.getUpdateTime().getTime()));
                insert.addBatch();

                insertLog.setLong(1, processTaskRelationLog.getProjectCode());
                insertLog.setLong(2, processTaskRelationLog.getProcessDefinitionCode());
                insertLog.setInt(3, processTaskRelationLog.getProcessDefinitionVersion());
                insertLog.setLong(4, processTaskRelationLog.getPreTaskCode());
                insertLog.setInt(5, processTaskRelationLog.getPreTaskVersion());
                insertLog.setLong(6, processTaskRelationLog.getPostTaskCode());
                insertLog.setInt(7, processTaskRelationLog.getPostTaskVersion());
                insertLog.setInt(8, processTaskRelationLog.getConditionType().getCode());
                insertLog.setString(9, processTaskRelationLog.getConditionParams());
                insertLog.setInt(10, processTaskRelationLog.getOperator());
                insertLog.setDate(11, new Date(processTaskRelationLog.getOperateTime().getTime()));
                insertLog.setDate(12, new Date(processTaskRelationLog.getCreateTime().getTime()));
                insertLog.setDate(13, new Date(processTaskRelationLog.getUpdateTime().getTime()));
                insertLog.addBatch();

                i++;
                if (i % 1000 == 0) {
                    insert.executeBatch();
                    insert.clearBatch();
                    insertLog.executeBatch();
                    insertLog.clearBatch();
                }
            }
            insert.executeBatch();
            insertLog.executeBatch();
            insert.close();
            insertLog.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * executeJsonSplitTaskDefinition
     *
     * @param conn jdbc connection
     * @param taskDefinitionLogs taskDefinitionLogs
     */
    public void executeJsonSplitTaskDefinition(Connection conn, List<TaskDefinitionLog> taskDefinitionLogs) {
        String insertSql =
                "insert into t_ds_task_definition (code,name,version,description,project_code,user_id,task_type,task_params,flag,task_priority,"
                        + "worker_group,environment_code,fail_retry_times,fail_retry_interval,timeout_flag,timeout_notify_strategy,timeout,delay_time,resource_ids,"
                        + "create_time,update_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        String insertLogSql =
                "insert into t_ds_task_definition_log (code,name,version,description,project_code,user_id,task_type,task_params,flag,task_priority,"
                        + "worker_group,environment_code,fail_retry_times,fail_retry_interval,timeout_flag,timeout_notify_strategy,timeout,delay_time,resource_ids,operator,"
                        + "operate_time,create_time,update_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement insert = conn.prepareStatement(insertSql);
            PreparedStatement insertLog = conn.prepareStatement(insertLogSql);
            int i = 0;
            for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
                insert.setLong(1, taskDefinitionLog.getCode());
                insert.setString(2, taskDefinitionLog.getName());
                insert.setInt(3, taskDefinitionLog.getVersion());
                insert.setString(4, taskDefinitionLog.getDescription());
                insert.setLong(5, taskDefinitionLog.getProjectCode());
                insert.setInt(6, taskDefinitionLog.getUserId());
                insert.setString(7, taskDefinitionLog.getTaskType());
                insert.setString(8, taskDefinitionLog.getTaskParams());
                insert.setInt(9, taskDefinitionLog.getFlag().getCode());
                insert.setInt(10, taskDefinitionLog.getTaskPriority().getCode());
                insert.setString(11, taskDefinitionLog.getWorkerGroup());
                insert.setLong(12, taskDefinitionLog.getEnvironmentCode());
                insert.setInt(13, taskDefinitionLog.getFailRetryTimes());
                insert.setInt(14, taskDefinitionLog.getFailRetryInterval());
                insert.setInt(15, taskDefinitionLog.getTimeoutFlag().getCode());
                insert.setInt(16, taskDefinitionLog.getTimeoutNotifyStrategy() == null ? 0
                        : taskDefinitionLog.getTimeoutNotifyStrategy().getCode());
                insert.setInt(17, taskDefinitionLog.getTimeout());
                insert.setInt(18, taskDefinitionLog.getDelayTime());
                insert.setString(19, taskDefinitionLog.getResourceIds());
                insert.setDate(20, new Date(taskDefinitionLog.getCreateTime().getTime()));
                insert.setDate(21, new Date(taskDefinitionLog.getUpdateTime().getTime()));
                insert.addBatch();

                insertLog.setLong(1, taskDefinitionLog.getCode());
                insertLog.setString(2, taskDefinitionLog.getName());
                insertLog.setInt(3, taskDefinitionLog.getVersion());
                insertLog.setString(4, taskDefinitionLog.getDescription());
                insertLog.setLong(5, taskDefinitionLog.getProjectCode());
                insertLog.setInt(6, taskDefinitionLog.getUserId());
                insertLog.setString(7, taskDefinitionLog.getTaskType());
                insertLog.setString(8, taskDefinitionLog.getTaskParams());
                insertLog.setInt(9, taskDefinitionLog.getFlag().getCode());
                insertLog.setInt(10, taskDefinitionLog.getTaskPriority().getCode());
                insertLog.setString(11, taskDefinitionLog.getWorkerGroup());
                insertLog.setLong(12, taskDefinitionLog.getEnvironmentCode());
                insertLog.setInt(13, taskDefinitionLog.getFailRetryTimes());
                insertLog.setInt(14, taskDefinitionLog.getFailRetryInterval());
                insertLog.setInt(15, taskDefinitionLog.getTimeoutFlag().getCode());
                insertLog.setInt(16, taskDefinitionLog.getTimeoutNotifyStrategy() == null ? 0
                        : taskDefinitionLog.getTimeoutNotifyStrategy().getCode());
                insertLog.setInt(17, taskDefinitionLog.getTimeout());
                insertLog.setInt(18, taskDefinitionLog.getDelayTime());
                insertLog.setString(19, taskDefinitionLog.getResourceIds());
                insertLog.setInt(20, taskDefinitionLog.getOperator());
                insertLog.setDate(21, new Date(taskDefinitionLog.getOperateTime().getTime()));
                insertLog.setDate(22, new Date(taskDefinitionLog.getCreateTime().getTime()));
                insertLog.setDate(23, new Date(taskDefinitionLog.getUpdateTime().getTime()));
                insertLog.addBatch();

                i++;
                if (i % 1000 == 0) {
                    insert.executeBatch();
                    insert.clearBatch();
                    insertLog.executeBatch();
                    insertLog.clearBatch();
                }
            }
            insert.executeBatch();
            insertLog.executeBatch();
            insert.close();
            insertLog.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
