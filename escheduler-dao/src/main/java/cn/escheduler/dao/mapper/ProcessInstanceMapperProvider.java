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
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * process instance mapper provider
 */
public class ProcessInstanceMapperProvider {

    private static final String TABLE_NAME = "t_escheduler_process_instance";
    private static final String TABLE_NAME_MAP = "t_escheduler_relation_process_instance";
    private static final String DEFINE_TABLE_NAME = "t_escheduler_process_definition";

    /**
     * insert process instance
     *
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {
            {
                INSERT_INTO(TABLE_NAME);
                VALUES("`process_definition_id`", "#{processInstance.processDefinitionId}");
                VALUES("`state`", EnumFieldUtil.genFieldStr("processInstance.state", ExecutionStatus.class));
                VALUES("`recovery`", EnumFieldUtil.genFieldStr("processInstance.recovery", Flag.class));
                VALUES("`start_time`", "#{processInstance.startTime}");
                VALUES("`end_time`", "#{processInstance.endTime}");
                VALUES("`run_times`", "#{processInstance.runTimes}");
                VALUES("`name`", "#{processInstance.name}");
                VALUES("`host`", "#{processInstance.host}");
                VALUES("`command_type`", EnumFieldUtil.genFieldStr("processInstance.commandType", CommandType.class));
                VALUES("`command_param`", "#{processInstance.commandParam}");
                VALUES("`task_depend_type`", EnumFieldUtil.genFieldStr("processInstance.taskDependType", TaskDependType.class));
                VALUES("`max_try_times`", "#{processInstance.maxTryTimes}");
                VALUES("`failure_strategy`", EnumFieldUtil.genFieldStr("processInstance.failureStrategy", FailureStrategy.class));
                VALUES("`warning_type`", EnumFieldUtil.genFieldStr("processInstance.warningType", WarningType.class));
                VALUES("`warning_group_id`", "#{processInstance.warningGroupId}");
                VALUES("`schedule_time`", "#{processInstance.scheduleTime}");
                VALUES("`command_start_time`", "#{processInstance.commandStartTime}");
                VALUES("`global_params`", "#{processInstance.globalParams}");
                VALUES("`process_instance_json`", "#{processInstance.processInstanceJson}");
                VALUES("`locations`", "#{processInstance.locations}");
                VALUES("`connects`", "#{processInstance.connects}");
                VALUES("`history_cmd`", "#{processInstance.historyCmd}");
                VALUES("`dependence_schedule_times`", "#{processInstance.dependenceScheduleTimes}");
                VALUES("`is_sub_process`", EnumFieldUtil.genFieldStr("processInstance.isSubProcess", Flag.class));
                VALUES("`executor_id`", "#{processInstance.executorId}");
                VALUES("`worker_group_id`", "#{processInstance.workerGroupId}");
                VALUES("`timeout`", "#{processInstance.timeout}");
                VALUES("`tenant_id`", "#{processInstance.tenantId}");
                VALUES("`process_instance_priority`", EnumFieldUtil.genFieldStr("processInstance.processInstancePriority", Priority.class));
            }
        }.toString();
    }

    /**
     * delete process instance
     *
     * @param parameter
     * @return
     */
    public String delete(Map<String, Object> parameter) {
        return new SQL() {
            {
                DELETE_FROM(TABLE_NAME);
                WHERE("id=#{processId}");
            }
        }.toString();
    }


    /**
     * 根据父工作流id查询子工作流list
     * @param parameter
     * @return
     */
    public String querySubIdListByParentId(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT( "process_instance_id");
                FROM(TABLE_NAME_MAP);
                WHERE("parent_process_instance_id = #{parentInstanceId}" );
            }
        }.toString();
    }

    /**
     * 更新流程实例
     *
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);

                SET("`process_definition_id`=#{processInstance.processDefinitionId}");
                SET("`state`="+EnumFieldUtil.genFieldStr("processInstance.state", ExecutionStatus.class));
                SET("`recovery`="+EnumFieldUtil.genFieldStr("processInstance.recovery", Flag.class));
                SET("`start_time`=#{processInstance.startTime}");
                SET("`end_time`=#{processInstance.endTime}");
                SET("`run_times`=#{processInstance.runTimes}");
                SET("`name`=#{processInstance.name}");
                SET("`host`=#{processInstance.host}");
                SET("`command_type`="+EnumFieldUtil.genFieldStr("processInstance.commandType", CommandType.class));
                SET("`command_param`=#{processInstance.commandParam}");
                SET("`task_depend_type`="+EnumFieldUtil.genFieldStr("processInstance.taskDependType", TaskDependType.class));
                SET("`max_try_times`=#{processInstance.maxTryTimes}");
                SET("`failure_strategy`="+EnumFieldUtil.genFieldStr("processInstance.failureStrategy", FailureStrategy.class));
                SET("`warning_type`="+ EnumFieldUtil.genFieldStr("processInstance.warningType", WarningType.class));
                SET("`warning_group_id`=#{processInstance.warningGroupId}");
                SET("`schedule_time`=#{processInstance.scheduleTime}");
                SET("`command_start_time`=#{processInstance.commandStartTime}");
                SET("`process_instance_json`=#{processInstance.processInstanceJson}");
                SET("`global_params`=#{processInstance.globalParams}");
                SET("`locations`=#{processInstance.locations}");
                SET("`connects`=#{processInstance.connects}");
                SET("`history_cmd`=#{processInstance.historyCmd}");
                SET("`dependence_schedule_times`=#{processInstance.dependenceScheduleTimes}");
                SET("`is_sub_process`="+EnumFieldUtil.genFieldStr("processInstance.isSubProcess", Flag.class));
                SET("`executor_id`=#{processInstance.executorId}");
                SET("`tenant_id`=#{processInstance.tenantId}");
                SET("`worker_group_id`=#{processInstance.workerGroupId}");
                SET("`timeout`=#{processInstance.timeout}");

                WHERE("`id`=#{processInstance.id}");

            }
        }.toString();
    }

    public String updateProcessInstance(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);
                if(parameter.get("flag") != null){
                    SET("`flag`="+ EnumFieldUtil.genFieldStr("flag", Flag.class));
                }

                if(parameter.get("scheduleTime") != null){
                    SET("`schedule_time`=#{scheduleTime}");
                }

                if(parameter.get("processJson") != null){
                    SET("`process_instance_json`=#{processJson}");
                    SET("`global_params`=#{globalParams}");
                }
                if(parameter.get("locations") != null){
                    SET("`locations`=#{locations}");
                }
                if(parameter.get("connects") != null){
                    SET("`connects`=#{connects}");
                }
                WHERE("`id`=#{processId}");

            }
        }.toString();
    }

    /**
     * update process instance by state
     * @param parameter
     * @return
     */
    public String updateProcessInstanceByState(Map<String, Object> parameter) {

        return new SQL() {
            {
                UPDATE(TABLE_NAME);

                SET("`state`=" + EnumFieldUtil.genFieldStr("destState", ExecutionStatus.class));
                WHERE("`state`=" + EnumFieldUtil.genFieldStr("originState", ExecutionStatus.class));
            }
        }.toString();
    }

    /**
     * update state
     * @param parameter
     * @return
     */
    public String updateState(Map<String, Object> parameter) {

        return new SQL() {
            {
                UPDATE(TABLE_NAME);

                SET("`state`=" + EnumFieldUtil.genFieldStr("executionStatus", ExecutionStatus.class));
                WHERE("`id`=#{processId}");
            }
        }.toString();
    }

    /**
     * query detail by id
     * @param parameter
     * @return
     */
    public String queryDetailById(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("inst.*,UNIX_TIMESTAMP(inst.end_time)-UNIX_TIMESTAMP(inst.start_time) as duration");

                FROM(TABLE_NAME + "  inst");

                WHERE("inst.id = #{processId}");
            }
        }.toString();
    }

    /**
     * query by id
     * @param parameter
     * @return
     */
    public String queryById(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME );

                WHERE("`id` = #{processId}");
            }
        }.toString();
    }

    /**
     * query list paging
     * @param parameter
     * @return
     */
    public String queryProcessInstanceListPaging(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("instance.*,  (UNIX_TIMESTAMP(instance.end_time) - UNIX_TIMESTAMP(instance.start_time)) as duration");

                FROM(TABLE_NAME  + " instance");

                JOIN(DEFINE_TABLE_NAME + " define ON instance.process_definition_id = define.id");

                if(parameter.get("processDefinitionId") != null && (int)parameter.get("processDefinitionId") != 0){
                    WHERE( "instance.process_definition_id = #{processDefinitionId} ");
                }

                WHERE("instance.is_sub_process=0 and define.project_id = #{projectId}");

                Object start = parameter.get("startTime");
                if(start != null && StringUtils.isNotEmpty(start.toString())){
                    WHERE("instance.start_time > #{startTime} and instance.start_time <= #{endTime}");
                }

                Object searchVal = parameter.get("searchVal");
                if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                    WHERE( " instance.name like concat('%', #{searchVal}, '%') ");
                }
                Object states = parameter.get("states");
                if(states != null && StringUtils.isNotEmpty(states.toString())){
                    String stateStr = states.toString();
                    WHERE("instance.state in ( "+ stateStr + " )");
                }
                Object host = parameter.get("host");
                if(host != null && StringUtils.isNotEmpty(host.toString())){
                    WHERE( "instance.host like concat('%', #{host}, '%') ");
                }
                ORDER_BY("instance.start_time desc limit #{offset},#{pageSize} ");
            }
        }.toString();

    }

    public String countProcessInstance(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("count(1)");
                FROM(TABLE_NAME + " instance");
                JOIN(DEFINE_TABLE_NAME + " define ON instance.process_definition_id = define.id");
                WHERE(" define.project_id = #{projectId}");
                if(parameter.get("processDefinitionId") != null && (int)parameter.get("processDefinitionId") != 0){
                    WHERE( "instance.process_definition_id = #{processDefinitionId} ");
                }
                WHERE(" instance.is_sub_process=0");
                Object startTime = parameter.get("startTime");
                if(startTime != null && StringUtils.isNotEmpty(startTime.toString())) {
                    WHERE("instance.start_time > #{startTime} and instance.start_time <= #{endTime}");
                }
                Object searchVal = parameter.get("searchVal");
                if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                    WHERE( " instance.name like concat('%', #{searchVal}, '%') ");
                }
                Object states = parameter.get("states");
                if(states != null && StringUtils.isNotEmpty(states.toString())){
                    String stateStr = states.toString();
                    WHERE("instance.state in ( "+ stateStr + " )");
                }
                Object host = parameter.get("host");
                if(host != null && StringUtils.isNotEmpty(host.toString())){
                    WHERE( "instance.host like concat('%', #{host}, '%') ");
                }
            }
        }.toString();
    }

    public String countInstanceStateByUser(Map<String, Object> parameter){
        return new SQL(){
            {
                SELECT ("state, count(0) as count");
                FROM(TABLE_NAME + " t");
                JOIN(DEFINE_TABLE_NAME + " d on d.id=t.process_definition_id");
                JOIN("t_escheduler_project p on p.id=d.project_id");
                WHERE("t.flag = 1 and t.is_sub_process = 0");
                WHERE("t.start_time > #{startTime} and t.start_time <= #{endTime}");
                if(parameter.get("projectId") != null && (int)parameter.get("projectId") != 0){
                    WHERE( "p.id = #{projectId} ");
                }else{
                    if(parameter.get("userType") != null && String.valueOf(parameter.get("userType")) == "GENERAL_USER") {
                        AND();
                        WHERE(" p.id in (select project_id as id from `t_escheduler_relation_project_user` where user_id=#{userId} \n" +
                                "union select id as id from `t_escheduler_project` where user_id =#{userId})");
                    }
                }
                GROUP_BY("t.state");
            }
        }.toString();
    }

    /**
     * list all processes by status
     *
     * @param parameter
     * @return
     */
    public String listByStatus(Map<String, Object> parameter) {
        StringBuilder strStates = new StringBuilder();
        int[] stateArray = (int[]) parameter.get("states");

        for(int i=0;i<stateArray.length;i++){
            strStates.append(stateArray[i]);
            if(i<stateArray.length-1){
                strStates.append(",");
            }
        }

        return new SQL() {
            {

                SELECT("*");

                FROM(TABLE_NAME);

                WHERE("`state` in (" + strStates.toString() +")");
                ORDER_BY("`id` asc");


            }
        }.toString();
    }


    /**
     * query all processes by host and status
     *
     * @param parameter
     * @return
     */
    public String queryByHostAndStatus(Map<String, Object> parameter) {
        StringBuilder strStates = new StringBuilder();
        int[] stateArray = (int[]) parameter.get("states");

        for(int i=0;i<stateArray.length;i++){
            strStates.append(stateArray[i]);
            if(i<stateArray.length-1){
                strStates.append(",");
            }
        }

        return new SQL() {
            {

                SELECT("*");

                FROM(TABLE_NAME);

                Object host = parameter.get("host");
                if(host != null && StringUtils.isNotEmpty(host.toString())){

                    WHERE("`host` = #{host} ");
                }
                WHERE("`state` in (" + strStates.toString() +")");
                ORDER_BY("`id` asc");


            }
        }.toString();
    }


    /**
     * update host to null by host and status
     *
     * @param parameter
     * @return
     */
    public String setFailoverByHostAndStateArray(Map<String, Object> parameter) {
        StringBuilder strStates = new StringBuilder();
        int[] stateArray = (int[]) parameter.get("states");

        for(int i=0;i<stateArray.length;i++){
            strStates.append(stateArray[i]);
            if(i<stateArray.length-1){
                strStates.append(",");
            }
        }



        String strResult = new SQL() {
            {

                UPDATE(TABLE_NAME);

                SET("`host`=null");

                WHERE("`host` = #{host} and `state` in (" + strStates.toString() + ")");
            }
        }.toString();


        return new SQL() {
            {

                UPDATE(TABLE_NAME);

                SET("`host`=null");

                WHERE("`host` = #{host} and `state` in (" + strStates.toString() + ")");
            }
        }.toString();
    }

    /**
     *
     * query by task id
     * @param parameter
     * @return
     */
    public String queryByTaskId(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("process.*, UNIX_TIMESTAMP(process.end_time)-UNIX_TIMESTAMP(process.start_time) as duration");

                FROM(TABLE_NAME + " process");

                JOIN("t_escheduler_task_instance task");

                WHERE("task.process_instance_id = process.id");

                WHERE("task.id=#{taskId}");
            }
        }.toString();
    }

    /**
     * query instance by definition id
     * @param parameter
     * @return
     */
    public String queryByProcessDefineId(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*,UNIX_TIMESTAMP(end_time)-UNIX_TIMESTAMP(start_time) as duration");

                FROM(TABLE_NAME);

                WHERE("process_definition_id=#{processDefinitionId}");
                ORDER_BY("start_time desc limit  #{size}");
            }
        }.toString();
    }

    /**
     * query process instance by definition and scheduler time
     * @param parameter
     * @return
     */
    public String queryByScheduleTime(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*,UNIX_TIMESTAMP(end_time)-UNIX_TIMESTAMP(start_time) as duration");

                FROM(TABLE_NAME);

                WHERE("process_definition_id=#{processDefinitionId} ");
                if(parameter.get("scheduleTime") != null){
                    WHERE("schedule_time=#{scheduleTime}");
                }
                if(parameter.get("startTime") != null && parameter.get("endTime")!= null){
                    WHERE("command_start_time between #{startTime} and #{endTime}");
                }
                if(parameter.get("excludeId") != null && Integer.parseInt(parameter.get("excludeId").toString())!= 0){
                    WHERE(" id not in ( #{excludeId}) ");
                }
                ORDER_BY("start_time desc limit 1");
            }
        }.toString();
    }

    public String queryLastSchedulerProcess(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);

                WHERE("process_definition_id=#{processDefinitionId} ");
                if(parameter.get("startTime") != null && parameter.get("endTime") != null){
                    WHERE("schedule_time between #{startTime} and #{endTime}");
                }

                ORDER_BY("end_time desc limit 1");
            }
        }.toString();
    }

    public String queryLastManualProcess(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);

                WHERE("process_definition_id=#{processDefinitionId} ");
                if(parameter.get("startTime") != null && parameter.get("endTime") != null){
                    WHERE("start_time between #{startTime} and #{endTime}");
                    WHERE("`schedule_time` is null");
                }
                ORDER_BY("end_time desc limit 1");
            }
        }.toString();
    }

    public String queryLastRunningProcess(Map<String, Object> parameter) {
        StringBuilder strStates = new StringBuilder();
        int[] stateArray = (int[]) parameter.get("states");

        for(int i=0;i<stateArray.length;i++){
            strStates.append(stateArray[i]);
            if(i<stateArray.length-1){
                strStates.append(",");
            }
        }

        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);

                if(parameter.get("startTime") != null && parameter.get("endTime") != null
                        ){
                    WHERE("process_definition_id=#{processDefinitionId} and (schedule_time between #{startTime} and #{endTime} " +
                            "or start_time between #{startTime} and #{endTime})");
                }
                WHERE("`state` in (" + strStates.toString() + ")");
                ORDER_BY("start_time desc limit 1");
            }
        }.toString();
    }
}
