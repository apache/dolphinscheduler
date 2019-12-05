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
package cn.escheduler.dao.mapper;

import cn.escheduler.common.enums.*;
import cn.escheduler.common.utils.EnumFieldUtil;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;


/**
 * command mapper provider
 */
public class CommandMapperProvider {
    private static final String TABLE_NAME = "t_escheduler_command";
    private static final String DEFINE_TABLE_NAME = "t_escheduler_process_definition";

    /**
     * inert command
     *
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {
            {
                INSERT_INTO(TABLE_NAME);
                VALUES("`command_type`", EnumFieldUtil.genFieldStr("command.commandType", CommandType.class));
                VALUES("`process_definition_id`", "#{command.processDefinitionId}");
                VALUES("`executor_id`", "#{command.executorId}");
                VALUES("`command_param`", "#{command.commandParam}");
                VALUES("`task_depend_type`", EnumFieldUtil.genFieldStr("command.taskDependType", TaskDependType.class));
                VALUES("`failure_strategy`", EnumFieldUtil.genFieldStr("command.failureStrategy", FailureStrategy.class));
                VALUES("`warning_type`", EnumFieldUtil.genFieldStr("command.warningType", WarningType.class));
                VALUES("`process_instance_priority`", EnumFieldUtil.genFieldStr("command.processInstancePriority", Priority.class));
                VALUES("`warning_group_id`", "#{command.warningGroupId}");
                VALUES("`schedule_time`", "#{command.scheduleTime}");
                VALUES("`update_time`", "#{command.updateTime}");
                VALUES("`worker_group_id`", "#{command.workerGroupId}");
                VALUES("`start_time`", "#{command.startTime}");

            }
        }.toString();
    }

    /**
     * delete command
     *
     * @param parameter
     * @return
     */
    public String delete(Map<String, Object> parameter) {
        return new SQL() {
            {
                DELETE_FROM(TABLE_NAME);

                WHERE("`id`=#{cmdId}");
            }
        }.toString();
    }

    /**
     * update command
     *
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);

                SET("`command_type`=" + EnumFieldUtil.genFieldStr("command.commandType", CommandType.class));
                SET("`process_definition_id`=#{command.processDefinitionId}");
                SET("`executor_id`=#{command.executorId}");
                SET("`command_param`=#{command.commandParam}");
                SET("`task_depend_type`="+ EnumFieldUtil.genFieldStr("command.taskDependType", TaskDependType.class));
                SET("`failure_strategy`="+ EnumFieldUtil.genFieldStr("command.failureStrategy", FailureStrategy.class));
                SET("`warning_type`="+ EnumFieldUtil.genFieldStr("command.warningType", WarningType.class));
                SET("`process_instance_priority`="+ EnumFieldUtil.genFieldStr("command.processInstancePriority", Priority.class));
                SET("`warning_group_id`=#{command.warningGroupId}");
                SET("`schedule_time`=#{command.scheduleTime}");
                SET("`update_time`=#{command.updateTime}");
                SET("`worker_group_id`=#{command.workerGroupId}");
                SET("`start_time`=#{command.startTime}");

                WHERE("`id`=#{command.id}");
            }
        }.toString();
    }

    /**
     * query a command that can run normally
     * command must be release on line, usable.
     * @param parameter
     * @return
     */
    public String queryOneCommand(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("cmd.*,process_define.id as process_definition_id");

                FROM(TABLE_NAME + " cmd");

                JOIN( DEFINE_TABLE_NAME + " process_define ON cmd.process_definition_id = process_define.id");
                WHERE("process_define.release_state =1 AND process_define.flag = 1");
                ORDER_BY("update_time asc");


            }
        }.toString() + " limit 1";
    }

    /**
     * query all commands
     * @param parameter
     * @return
     */
    public String queryAllCommand(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("cmd.*");

                FROM(TABLE_NAME + " cmd");
            }
        }.toString();
    }

    /**
     *
     * count command type
     * @param parameter
     * @return
     */
    public String countCommandState(Map<String, Object> parameter){
        return new SQL(){
            {
                SELECT ("command_type as state,COUNT(*) AS count");
                FROM(TABLE_NAME + " cmd,t_escheduler_process_definition process");
                WHERE("cmd.process_definition_id = process.id");
                if(parameter.get("projectId") != null && (int)parameter.get("projectId") != 0){
                    WHERE( "process.project_id = #{projectId} ");
                }else{
                    if(parameter.get("userType") != null && String.valueOf(parameter.get("userType")) == "GENERAL_USER") {
                        AND();
                        WHERE("process.project_id in (select id as project_id from t_escheduler_project tp where tp.user_id= #{userId} " +
                                "union select project_id from t_escheduler_relation_project_user tr where tr.user_id= #{userId} )");

                    }
                }
                WHERE("cmd.start_time >= #{startTime} and cmd.update_time <= #{endTime}");
                GROUP_BY("cmd.command_type");
            }
        }.toString();
    }
}
