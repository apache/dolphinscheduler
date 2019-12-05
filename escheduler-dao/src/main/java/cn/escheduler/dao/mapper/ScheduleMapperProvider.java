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

import cn.escheduler.common.enums.FailureStrategy;
import cn.escheduler.common.enums.Priority;
import cn.escheduler.common.enums.ReleaseState;
import cn.escheduler.common.enums.WarningType;
import cn.escheduler.common.utils.EnumFieldUtil;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * scheduler mapper provider
 */
public class ScheduleMapperProvider {

  public static final String DB_NAME = "t_escheduler_schedules";
  public static final String DEFINE_TABLE_NAME = "t_escheduler_process_definition";

  public String insert(Map<String, Object> parameter) {
    return new SQL() {{
      INSERT_INTO(DB_NAME);

      VALUES("`process_definition_id`", "#{schedule.processDefinitionId}");
      VALUES("`start_time`", "#{schedule.startTime}");
      VALUES("`end_time`", "#{schedule.endTime}");
      VALUES("`crontab`", "#{schedule.crontab}");
      VALUES("`failure_strategy`", EnumFieldUtil.genFieldStr("schedule.failureStrategy", FailureStrategy.class));
      VALUES("`warning_type`", EnumFieldUtil.genFieldStr("schedule.warningType", WarningType.class));
      VALUES("`create_time`", "#{schedule.createTime}");
      VALUES("`update_time`", "#{schedule.updateTime}");
      VALUES("`user_id`", "#{schedule.userId}");
      VALUES("`release_state`", EnumFieldUtil.genFieldStr("schedule.releaseState", ReleaseState.class));
      VALUES("`warning_group_id`", "#{schedule.warningGroupId}");
      VALUES("`worker_group_id`", "#{schedule.workerGroupId}");
      VALUES("`process_instance_priority`", EnumFieldUtil.genFieldStr("schedule.processInstancePriority", Priority.class));
    }}.toString();
  }

  public String update(Map<String, Object> parameter) {
    return new SQL() {
      {
        UPDATE(DB_NAME);

        SET("`start_time`=#{schedule.startTime}");
        SET("`end_time`=#{schedule.endTime}");
        SET("`crontab`=#{schedule.crontab}");
        SET("`failure_strategy`=" + EnumFieldUtil.genFieldStr("schedule.failureStrategy", FailureStrategy.class));
        SET("`warning_type`=" + EnumFieldUtil.genFieldStr("schedule.warningType", WarningType.class));
        SET("`create_time`=#{schedule.createTime}");
        SET("`update_time`=#{schedule.updateTime}");
        SET("`user_id`=#{schedule.userId}");
        SET("`release_state`=" + EnumFieldUtil.genFieldStr("schedule.releaseState", ReleaseState.class));
        SET("`warning_group_id`=#{schedule.warningGroupId}");
        SET("`worker_group_id`=#{schedule.workerGroupId}");
        SET("`process_instance_priority`="+ EnumFieldUtil.genFieldStr("schedule.processInstancePriority", Priority.class));

        WHERE("`id` = #{schedule.id}");
      }
    }.toString();
  }

  /**
   * query schedule by id
   * @param parameter
   * @return
   */
  public String queryById(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("*");
      FROM(DB_NAME);
      WHERE("`id` = #{id}");
    }}.toString();
  }

  /**
   * query schedule list by process define id
   * @param parameter
   * @return
   */
  public String queryByProcessDefineIdPaging(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("p_f.name as process_definition_name");
      SELECT("p.name as project_name");
      SELECT("u.user_name");
      SELECT("s.*");

      FROM(DB_NAME + " as s");

      JOIN(DEFINE_TABLE_NAME + " as p_f on s.process_definition_id = p_f.id");
      JOIN("t_escheduler_project as p on p_f.project_id = p.id");
      JOIN("t_escheduler_user as u on s.user_id = u.id");
      if(parameter.get("processDefinitionId") != null && (int)parameter.get("processDefinitionId") != 0) {
        WHERE("s.process_definition_id = #{processDefinitionId}");
      }
      ORDER_BY("s.update_time desc limit #{offset},#{pageSize}");
    }}.toString();
  }

  /**
   * count schedule number by process definition id and search value
   * @param parameter
   * @return
   */
  public String countByProcessDefineId(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("count(0)");

      FROM(DB_NAME + " as s");

      JOIN(DEFINE_TABLE_NAME + " as p_f on s.process_definition_id = p_f.id");
      JOIN("t_escheduler_project as p on p_f.project_id = p.id");
      JOIN("t_escheduler_user as u on s.user_id = u.id");
      if(parameter.get("processDefinitionId") != null && (int)parameter.get("processDefinitionId") != 0) {
        WHERE("s.process_definition_id = #{processDefinitionId}");
      }
    }}.toString();
  }

  /**
   * query schedule list by project id
   * @param parameter
   * @return
   */
  public String querySchedulerListByProjectName(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("p_f.name as process_definition_name");
      SELECT("p_f.desc as `desc`");
      SELECT("p.name as project_name");
      SELECT("u.user_name");
      SELECT("s.*");

      FROM(DB_NAME + " as s");

      JOIN( DEFINE_TABLE_NAME + " as p_f on s.process_definition_id = p_f.id");
      JOIN("t_escheduler_project as p on p_f.project_id = p.id");
      JOIN("t_escheduler_user as u on s.user_id = u.id");

      WHERE("p.name = #{projectName}");
    }}.toString();
  }

  /**
   * query schedule list by definition array
   * @param parameter
   * @return
   */
  public String selectAllByProcessDefineArray(Map<String, Object> parameter) {

    StringBuilder strIds = new StringBuilder();
    int[] idsArray = (int[]) parameter.get("processDefineIds");
    for(int i=0;i<idsArray.length;i++){
      strIds.append(idsArray[i]);
      if(i<idsArray.length-1){
        strIds.append(",");
      }
    }


    return new SQL() {{
      SELECT("*");
      FROM(DB_NAME);
      WHERE("`process_definition_id` in (" + String.join(",",strIds.toString()) +")");
      WHERE("release_state = 1");
    }}.toString();
  }

  /**
   * query schedule by process definition id
   * @param parameter
   * @return
   */
  public String queryByProcessDefinitionId(Map<String, Object> parameter) {

    return new SQL() {{
      SELECT("*");
      FROM(DB_NAME);
      WHERE("process_definition_id = #{processDefinitionId}");
    }}.toString();
  }

  /**
   * delete schedule by id
   *
   * @param parameter
   * @return
   */
  public String delete(Map<String, Object> parameter) {
    return new SQL() {
      {
        DELETE_FROM(DB_NAME);

        WHERE("`id`=#{scheduleId}");
      }
    }.toString();
  }

}
