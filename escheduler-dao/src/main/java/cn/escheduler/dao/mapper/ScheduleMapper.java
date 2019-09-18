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
import cn.escheduler.dao.model.Schedule;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

/**
 * scheduler mapper
 */
public interface ScheduleMapper {

  /**
   * insert scheduler
   * @param schedule
   * @return
   */
  @InsertProvider(type = ScheduleMapperProvider.class, method = "insert")
  int insert(@Param("schedule") Schedule schedule);

  /**
   * update schedule info
   * @param schedule
   * @return
   */
  @UpdateProvider(type = ScheduleMapperProvider.class, method = "update")
  int update(@Param("schedule") Schedule schedule);

  /**
   * query schedule list by process define id
   *
   * @param processDefinitionId
   * @param searchVal
   * @param offset
   * @param pageSize
   * @return
   */
  @Results(value = {
          @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "processDefinitionId", column = "process_definition_id",javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "processDefinitionName", column = "process_definition_name", javaType =String.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "projectName", column = "project_name", javaType = String.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "startTime", column = "start_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "endTime", column = "end_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "crontab", column = "crontab", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "warningType", column = "warning_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = WarningType.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "createTime", column = "create_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "updateTime", column = "update_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "releaseState", column = "release_state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ReleaseState.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "warningGroupId", column = "warning_group_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "workerGroupId", column = "worker_group_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
  })
  @SelectProvider(type = ScheduleMapperProvider.class, method = "queryByProcessDefineIdPaging")
  List<Schedule> queryByProcessDefineIdPaging(@Param("processDefinitionId") int processDefinitionId,
                                              @Param("searchVal") String searchVal,
                                              @Param("offset") int offset,
                                              @Param("pageSize") int pageSize);

  /**
   * count schedule number by process definition id and search value
   * @param processDefinitionId
   * @param searchVal
   * @return
   */
    @SelectProvider(type = ScheduleMapperProvider.class, method = "countByProcessDefineId")
    Integer countByProcessDefineId(@Param("processDefinitionId") Integer processDefinitionId,
                                   @Param("searchVal") String searchVal
    );

  /**
   * query schedule list by project id
   *
   * @param projectName
   * @return
   */
  @Results(value = {
          @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "projectName", column = "project_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "processDefinitionName", column = "process_definition_name", id = true, javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "desc", column = "desc", id = true, javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "processDefinitionId", column = "process_definition_id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "startTime", column = "start_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "endTime", column = "end_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "crontab", column = "crontab", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "warningType", column = "warning_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = WarningType.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "createTime", column = "create_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "updateTime", column = "update_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "releaseState", column = "release_state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ReleaseState.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "warningGroupId", column = "warning_group_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "workerGroupId", column = "worker_group_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
  })
  @SelectProvider(type = ScheduleMapperProvider.class, method = "querySchedulerListByProjectName")
  List<Schedule> querySchedulerListByProjectName(@Param("projectName") String projectName);


  /**
   * query schedule by id
   * @param id
   * @return
   */
    @Results(value = {
            @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processDefinitionId", column = "process_definition_id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "startTime", column = "start_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "endTime", column = "end_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "crontab", column = "crontab", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningType", column = "warning_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = WarningType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "createTime", column = "create_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "updateTime", column = "update_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "releaseState", column = "release_state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ReleaseState.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningGroupId", column = "warning_group_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "workerGroupId", column = "worker_group_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
    })
    @SelectProvider(type = ScheduleMapperProvider.class, method = "queryById")
    Schedule queryById(@Param("id") int id);

  /**
   * query schedule list by definition array
   * @param processDefineIds
   * @return
   */
  @Results(value = {
          @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "processDefinitionId", column = "process_definition_id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "startTime", column = "start_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "endTime", column = "end_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "crontab", column = "crontab", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "warningType", column = "warning_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = WarningType.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "createTime", column = "create_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "updateTime", column = "update_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "releaseState", column = "release_state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ReleaseState.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "warningGroupId", column = "warning_group_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "workerGroupId", column = "worker_group_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
  })
  @SelectProvider(type = ScheduleMapperProvider.class, method = "selectAllByProcessDefineArray")
  List<Schedule> selectAllByProcessDefineArray(@Param("processDefineIds") int[] processDefineIds);

  /**
   * query schedule list by definition id
   * @param processDefinitionId
   * @return
   */
  @Results(value = {
          @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "processDefinitionId", column = "process_definition_id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "startTime", column = "start_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "endTime", column = "end_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "crontab", column = "crontab", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "warningType", column = "warning_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = WarningType.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "createTime", column = "create_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "updateTime", column = "update_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "releaseState", column = "release_state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ReleaseState.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "warningGroupId", column = "warning_group_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "workerGroupId", column = "worker_group_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
  })
  @SelectProvider(type = ScheduleMapperProvider.class, method = "queryByProcessDefinitionId")
  List<Schedule> queryByProcessDefinitionId(@Param("processDefinitionId") int processDefinitionId);

  /**
   * delete schedule by id
   * @param scheduleId
   * @return
   */
  @DeleteProvider(type = ScheduleMapperProvider.class, method = "delete")
  int delete(@Param("scheduleId") int scheduleId);

}
