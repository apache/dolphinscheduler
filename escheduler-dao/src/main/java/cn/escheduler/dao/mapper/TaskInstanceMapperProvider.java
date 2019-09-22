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

import cn.escheduler.common.enums.ExecutionStatus;
import cn.escheduler.common.enums.Flag;
import cn.escheduler.common.enums.Priority;
import cn.escheduler.common.utils.EnumFieldUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * task instance mapper provider
 */
public class TaskInstanceMapperProvider {

    private static final String TABLE_NAME = "t_escheduler_task_instance";
    private static final String DEFINE_TABLE_NAME = "t_escheduler_process_definition";
    private static final String INSTANCE_TABLE_NAME = "t_escheduler_process_instance";

    /**
     * insert task instance
     *
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {
            {
                INSERT_INTO(TABLE_NAME);
                VALUES("`name`", "#{taskInstance.name}");
                VALUES("`task_type`", "#{taskInstance.taskType}");
                VALUES("`process_definition_id`","#{taskInstance.processDefinitionId}");
                VALUES("`process_instance_id`", "#{taskInstance.processInstanceId}");
                VALUES("`task_json`", "#{taskInstance.taskJson}");
                VALUES("`state`", EnumFieldUtil.genFieldStr("taskInstance.state", ExecutionStatus.class));
                VALUES("`submit_time`", "#{taskInstance.submitTime}");
                VALUES("`start_time`", "#{taskInstance.startTime}");
                VALUES("`end_time`", "#{taskInstance.endTime}");
                VALUES("`host`", "#{taskInstance.host}");
                VALUES("`execute_path`", "#{taskInstance.executePath}");
                VALUES("`log_path`", "#{taskInstance.logPath}");
                VALUES("`alert_flag`", EnumFieldUtil.genFieldStr("taskInstance.alertFlag", Flag.class));
                VALUES("`retry_times`", "#{taskInstance.retryTimes}");
                VALUES("`pid`", "#{taskInstance.pid}");
                VALUES("`max_retry_times`", "#{taskInstance.maxRetryTimes}");
                VALUES("`retry_interval`", "#{taskInstance.retryInterval}");
                VALUES("`app_link`", "#{taskInstance.appLink}");
                VALUES("`worker_group_id`", "#{taskInstance.workerGroupId}");
                VALUES("`flag`", EnumFieldUtil.genFieldStr("taskInstance.flag", Flag.class));
                VALUES("`task_instance_priority`", EnumFieldUtil.genFieldStr("taskInstance.taskInstancePriority", Priority.class));

            }
        }.toString();
    }

    /**
     * delete task instance
     *
     * @param parameter
     * @return
     */
    public String delete(Map<String, Object> parameter) {
        return new SQL() {
            {
                DELETE_FROM(TABLE_NAME);

                WHERE("`id`=#{taskInstanceId}");
            }
        }.toString();
    }

    /**
     * update task instance
     *
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);

                SET("`name`=#{taskInstance.name}");
                SET("`task_type`=#{taskInstance.taskType}");
                SET("`process_definition_id`=#{taskInstance.processDefinitionId}");
                SET("`process_instance_id`=#{taskInstance.processInstanceId}");
                SET("`task_json`=#{taskInstance.taskJson}");
                SET("`state`="+ EnumFieldUtil.genFieldStr("taskInstance.state", ExecutionStatus.class));
                SET("`submit_time`=#{taskInstance.submitTime}");
                SET("`start_time`=#{taskInstance.startTime}");
                SET("`end_time`=#{taskInstance.endTime}");
                SET("`host`=#{taskInstance.host}");
                SET("`execute_path`=#{taskInstance.executePath}");
                SET("`log_path`=#{taskInstance.logPath}");
                SET("`alert_flag`="+ EnumFieldUtil.genFieldStr("taskInstance.alertFlag", Flag.class));
                SET("`retry_times`=#{taskInstance.retryTimes}");
                SET("`pid`=#{taskInstance.pid}");
                SET("`max_retry_times`=#{taskInstance.maxRetryTimes}");
                SET("`retry_interval`=#{taskInstance.retryInterval}");
                SET("`app_link`=#{taskInstance.appLink}");
                SET("`worker_group_id`=#{taskInstance.workerGroupId}");
                SET("`flag`="+ EnumFieldUtil.genFieldStr("taskInstance.flag", Flag.class));
                SET("`task_instance_priority`="+ EnumFieldUtil.genFieldStr("taskInstance.taskInstancePriority", Priority.class));

                WHERE("`id`=#{taskInstance.id}");

            }
        }.toString();
    }

    /**
     * query task by id
     * @param parameter
     * @return
     */
    public String queryById(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*, UNIX_TIMESTAMP(end_time)-UNIX_TIMESTAMP(start_time) as duration");
                FROM(TABLE_NAME);
                WHERE("id = #{taskInstanceId}");
            }
        }.toString();
    }


    /**
     * query task id list by process instance id and state
     * @param parameter
     * @return
     */
    public String queryTaskByProcessIdAndState(Map<String, Object> parameter){
        return new SQL(){
            {
                SELECT("id");
                FROM(TABLE_NAME);
                WHERE("`process_instance_id` = #{processInstanceId}");
                WHERE("`state` = #{state}");
                WHERE("`flag` = 1 ");
            }
        }.toString();
    }

    /**
     * query valid task instance list by process id
     * @param parameter
     * @return
     */
    public String findValidTaskListByProcessId(Map<String, Object> parameter) {
        return new SQL()
        {
            {
                SELECT("*, UNIX_TIMESTAMP(end_time)-UNIX_TIMESTAMP(start_time) as duration");
                FROM(TABLE_NAME );
                WHERE("`process_instance_id` = #{processInstanceId} ");
                WHERE("`flag` = " + EnumFieldUtil.genFieldStr("flag", Flag.class));
                ORDER_BY("start_time desc");
            }
        }.toString();
    }

    /**
     *
     * count task number group by state and user
     * @param parameter
     * @return
     */
    public String countTaskInstanceStateByUser(Map<String, Object> parameter){
        return new SQL(){
            {
                SELECT ("state, count(0) as count");
                FROM(TABLE_NAME + " t");
                LEFT_OUTER_JOIN(DEFINE_TABLE_NAME + " d on d.id=t.process_definition_id");
                LEFT_OUTER_JOIN("t_escheduler_project p on p.id=d.project_id");
                if(parameter.get("projectId") != null && (int)parameter.get("projectId") != 0){
                    WHERE( "p.id = #{projectId} ");
                }else{
                    if(parameter.get("userType") != null && String.valueOf(parameter.get("userType")) == "GENERAL_USER") {
                        AND();
                        WHERE("d.project_id in (select id as project_id from t_escheduler_project tp where tp.user_id= #{userId} " +
                                "union select project_id from t_escheduler_relation_project_user tr where tr.user_id= #{userId} )");

                    }
                }
                WHERE("t.flag = 1 and t.start_time > #{startTime} and t.start_time <= #{endTime}");
                GROUP_BY("t.state");
            }
        }.toString();
    }


    /**
     * query task list by host and state
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
                SELECT("*, UNIX_TIMESTAMP(end_time)-UNIX_TIMESTAMP(start_time) as duration");
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
     * query TaskInstance by host and status
     *
     * @param parameter
     * @return
     */
    public String queryLimitNumByHostAndStatus(Map<String, Object> parameter) {
        StringBuilder strStates = new StringBuilder();
        int[] stateArray = (int[]) parameter.get("states");
        for(int i=0;i<stateArray.length;i++){
            strStates.append(stateArray[i]);
            if(i<stateArray.length-1){
                strStates.append(",");
            }
        }
        int limitNum = (int) parameter.get("limit_num");


        return new SQL() {
            {
                SELECT("*, UNIX_TIMESTAMP(end_time)-UNIX_TIMESTAMP(start_time) as duration");

                FROM(TABLE_NAME);

                WHERE("`host` = #{host} and `state` in (" + strStates.toString() +")");
                ORDER_BY("`id` asc limit "+ " " + limitNum);

            }
        }.toString();
    }


    /**
     * set task state to need failover when worker down
     *
     * @param parameter
     * @return
     */
    public String setFailoverByHostAndStateArray(Map<String, Object> parameter) {
        StringBuilder strStates = new StringBuilder();
        int[] stateArray = (int[]) parameter.get("states");
        int state = ExecutionStatus.NEED_FAULT_TOLERANCE.ordinal();
        for(int i=0;i<stateArray.length;i++){
            strStates.append(stateArray[i]);
            if(i<stateArray.length-1){
                strStates.append(",");
            }
        }

        return new SQL() {
            {

                UPDATE(TABLE_NAME);

                SET("`state`=" + state);

                WHERE("`host` = #{host} and `state` in (" + strStates.toString() + ")");
            }
        }.toString();
    }

    /**
     * query task list paging by search fields
     * @param parameter
     * @return
     */
    public String queryTaskInstanceListPaging(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("instance.*,process.name as process_instance_name, UNIX_TIMESTAMP(instance.end_time)-UNIX_TIMESTAMP(instance.start_time) as duration");

                FROM(TABLE_NAME  + " instance");

                JOIN(DEFINE_TABLE_NAME + " define ON instance.process_definition_id = define.id");
                JOIN(INSTANCE_TABLE_NAME + " process on process.id=instance.process_instance_id");
                WHERE("define.project_id = #{projectId}");

                Object start = parameter.get("startTime");
                if(start != null && StringUtils.isNotEmpty(start.toString())){
                    WHERE("instance.start_time > #{startTime} and instance.start_time <= #{endTime}");
                }

                if(parameter.get("processInstanceId") != null && (int)parameter.get("processInstanceId") != 0){
                    WHERE( "instance.process_instance_id = #{processInstanceId} ");
                }

                Object searchVal = parameter.get("searchVal");
                if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                    WHERE( " instance.name like concat('%', #{searchVal}, '%') ");
                }
                Object taskName = parameter.get("taskName");
                if(taskName != null && StringUtils.isNotEmpty(taskName.toString())){
                    WHERE( " instance.name=#{taskName}");
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


    /**
     * count task number by search fields
     * @param parameter
     * @return
     */
    public String countTaskInstance(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("count(1)");


                FROM(TABLE_NAME + " instance");

                JOIN(DEFINE_TABLE_NAME + " define ON instance.process_definition_id = define.id");
                WHERE("define.project_id = #{projectId}");

                if(parameter.get("processInstanceId") != null && (int)parameter.get("processInstanceId") != 0){
                    WHERE( "instance.process_instance_id = #{processInstanceId} ");
                }
                Object startTime = parameter.get("startTime");
                if(startTime != null && StringUtils.isNotEmpty(startTime.toString())) {
                    WHERE("instance.start_time > #{startTime} and instance.start_time <= #{endTime}");
                }
                Object searchVal = parameter.get("searchVal");
                if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                    WHERE( " instance.name like concat('%', #{searchVal}, '%') ");
                }
                Object taskName = parameter.get("taskName");
                if(taskName != null && StringUtils.isNotEmpty(taskName.toString())){
                    WHERE( " instance.name=#{taskName}");
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

    /**
     * query task list by process id and task name
     * @param parameter
     * @return
     */
    public String queryByInstanceIdAndName(Map<String, Object> parameter){
        return new SQL(){
            {
                SELECT("*,UNIX_TIMESTAMP(end_time)-UNIX_TIMESTAMP(start_time) as duration");
                FROM(TABLE_NAME);
                WHERE("`process_instance_id` = #{processInstanceId}");
                WHERE("`name` = #{name}");
                WHERE("`flag` = 1 ");
            }
        }.toString();
    }


    /**
     *
     * count task
     * @param parameter
     * @return
     */
    public String countTask(Map<String, Object> parameter){

        StringBuilder taskIdsStr = new StringBuilder();
        int[] stateArray = (int[]) parameter.get("taskIds");
        for(int i=0;i<stateArray.length;i++){
            taskIdsStr.append(stateArray[i]);
            if(i<stateArray.length-1){
                taskIdsStr.append(",");
            }
        }

        return new SQL(){
            {
                SELECT("count(1) as count");
                FROM(TABLE_NAME + " task,t_escheduler_process_definition process");
                WHERE("task.process_definition_id=process.id");
                if(parameter.get("projectId") != null && (int)parameter.get("projectId") != 0){
                    WHERE( "process.project_id = #{projectId} ");
                }else{
                    if(parameter.get("userType") != null && String.valueOf(parameter.get("userType")) == "GENERAL_USER") {
                        AND();
                        WHERE("process.project_id in (select id as project_id from t_escheduler_project tp where tp.user_id= #{userId} " +
                                "union select project_id from t_escheduler_relation_project_user tr where tr.user_id= #{userId} )");

                    }
                }
                WHERE("task.id in (" + taskIdsStr.toString() + ")");
            }
        }.toString();
    }


}
