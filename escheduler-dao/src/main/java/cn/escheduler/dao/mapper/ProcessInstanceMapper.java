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
import cn.escheduler.dao.model.ExecuteStatusCount;
import cn.escheduler.dao.model.ProcessInstance;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * process instance mapper
 */
public interface ProcessInstanceMapper {

    /**
     * insert process instance
     * @param processInstance
     * @return
     */
    @InsertProvider(type = ProcessInstanceMapperProvider.class, method = "insert")
    @Options(useGeneratedKeys = true,keyProperty = "processInstance.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "processInstance.id", before = false, resultType = int.class)
    int insert(@Param("processInstance") ProcessInstance processInstance);


    /**
     * delete process instance
     * @param processId
     * @return
     */
    @DeleteProvider(type = ProcessInstanceMapperProvider.class, method = "delete")
    int delete(@Param("processId") int processId);

    /**
     * update process instance
     *
     * @param processInstance
     * @return
     */
    @UpdateProvider(type = ProcessInstanceMapperProvider.class, method = "update")
    int update(@Param("processInstance") ProcessInstance processInstance);


    /**
     * query instance detail by id
     * @param processId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processDefinitionId", column = "process_definition_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "recovery", column = "recovery",  typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "startTime", column = "start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "runTimes", column = "run_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "commandType", column = "command_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = CommandType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "commandParam", column = "command_param",  javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskDependType", column = "task_depend_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = TaskDependType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "maxTryTimes", column = "max_try_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningType", column = "warning_type", javaType = WarningType.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningGroupId", column = "warning_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "scheduleTime", column = "schedule_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "commandStartTime", column = "command_start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "globalParams", column = "global_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executorId", column = "executor_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstanceJson", column = "process_instance_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "isSubProcess", column = "is_sub_process", javaType = Flag.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "locations", column = "locations", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "connects", column = "connects", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "historyCmd", column = "history_cmd", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "dependenceScheduleTimes", column = "dependence_schedule_times", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "duration", column = "duration", javaType = Long.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "tenantCode", column = "tenant_code", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "queue", column = "queue", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "workerGroupId", column = "worker_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "timeout", column = "timeout",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
    })
    @SelectProvider(type = ProcessInstanceMapperProvider.class, method = "queryDetailById")
    ProcessInstance queryDetailById(@Param("processId") int processId);


    /**
     * query instance by id
     * @param processId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processDefinitionId", column = "process_definition_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "recovery", column = "recovery",  typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "startTime", column = "start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "runTimes", column = "run_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "commandType", column = "command_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = CommandType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "commandParam", column = "command_param",  javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskDependType", column = "task_depend_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = TaskDependType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "maxTryTimes", column = "max_try_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningType", column = "warning_type", javaType = WarningType.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningGroupId", column = "warning_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "scheduleTime", column = "schedule_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "commandStartTime", column = "command_start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "globalParams", column = "global_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executorId", column = "executor_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstanceJson", column = "process_instance_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "isSubProcess", column = "is_sub_process", javaType = Flag.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "locations", column = "locations", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "connects", column = "connects", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "historyCmd", column = "history_cmd", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "dependenceScheduleTimes", column = "dependence_schedule_times", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "workerGroupId", column = "worker_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "timeout", column = "timeout",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
    })
    @SelectProvider(type = ProcessInstanceMapperProvider.class, method = "queryById")
    ProcessInstance queryById(@Param("processId") int processId);

    /**
     * query instance list by host and state array
     * @param host
     * @param stateArray
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processDefinitionId", column = "process_definition_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "recovery", column = "recovery",  typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "startTime", column = "start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "runTimes", column = "run_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "commandType", column = "command_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = CommandType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "commandParam", column = "command_param",  javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskDependType", column = "task_depend_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = TaskDependType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "maxTryTimes", column = "max_try_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningType", column = "warning_type", javaType = WarningType.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningGroupId", column = "warning_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "scheduleTime", column = "schedule_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "commandStartTime", column = "command_start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "globalParams", column = "global_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executorId", column = "executor_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "isSubProcess", column = "is_sub_process", javaType = Flag.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "locations", column = "locations", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "connects", column = "connects", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "historyCmd", column = "history_cmd", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "dependenceScheduleTimes", column = "dependence_schedule_times", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "processInstanceJson", column = "process_instance_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "workerGroupId", column = "worker_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "timeout", column = "timeout",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)

    })
    @SelectProvider(type = ProcessInstanceMapperProvider.class, method = "queryByHostAndStatus")
    List<ProcessInstance> queryByHostAndStatus(@Param("host") String host, @Param("states")int[] stateArray);

    /**
     * query instance list by state array
     * @param stateArray
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processDefinitionId", column = "process_definition_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "recovery", column = "recovery",  typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "startTime", column = "start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "runTimes", column = "run_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "commandType", column = "command_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = CommandType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "commandParam", column = "command_param",  javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskDependType", column = "task_depend_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = TaskDependType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "maxTryTimes", column = "max_try_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningType", column = "warning_type", javaType = WarningType.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningGroupId", column = "warning_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "scheduleTime", column = "schedule_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "commandStartTime", column = "command_start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "globalParams", column = "global_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executorId", column = "executor_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "isSubProcess", column = "is_sub_process", javaType = Flag.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "locations", column = "locations", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "connects", column = "connects", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "historyCmd", column = "history_cmd", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "dependenceScheduleTimes", column = "dependence_schedule_times", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "processInstanceJson", column = "process_instance_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "workerGroupId", column = "worker_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "timeout", column = "timeout",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)

    })
    @SelectProvider(type = ProcessInstanceMapperProvider.class, method = "listByStatus")
    List<ProcessInstance> listByStatus(@Param("states")int[] stateArray);

    /**
     * query list paging
     *
     * @param projectId
     * @param processDefinitionId
     * @param searchVal
     * @param statusArray
     * @param startTime
     * @param endTime
     * @param offset
     * @param pageSize
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processDefinitionId", column = "process_definition_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "recovery", column = "recovery",  typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "startTime", column = "start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "runTimes", column = "run_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "commandType", column = "command_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = CommandType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "commandParam", column = "command_param",  javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskDependType", column = "task_depend_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = TaskDependType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "maxTryTimes", column = "max_try_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningType", column = "warning_type", javaType = WarningType.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningGroupId", column = "warning_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "scheduleTime", column = "schedule_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "commandStartTime", column = "command_start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "globalParams", column = "global_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executorId", column = "executor_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "isSubProcess", column = "is_sub_process", javaType = Flag.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "locations", column = "locations", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "connects", column = "connects", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "historyCmd", column = "history_cmd", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "dependenceScheduleTimes", column = "dependence_schedule_times", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "duration", column = "duration", javaType = Long.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "processInstanceJson", column = "process_instance_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "workerGroupId", column = "worker_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "timeout", column = "timeout",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)

    })
    @SelectProvider(type = ProcessInstanceMapperProvider.class, method = "queryProcessInstanceListPaging")
    List<ProcessInstance> queryProcessInstanceListPaging(@Param("projectId") int projectId,
                                                         @Param("processDefinitionId") Integer processDefinitionId,
                                                         @Param("searchVal") String searchVal,
                                                         @Param("states")String statusArray,
                                                         @Param("host")String host,
                                                         @Param("startTime") Date startTime,
                                                         @Param("endTime") Date endTime,
                                                         @Param("offset") int offset,
                                                         @Param("pageSize") int pageSize);

    /**
     * count process numbers
     * @param projectId
     * @param processDefinitionId
     * @param statusArray
     * @param startTime
     * @param endTime
     * @param searchVal
     * @return
     */
    @SelectProvider(type = ProcessInstanceMapperProvider.class, method = "countProcessInstance")
    Integer countProcessInstance(@Param("projectId") int projectId,
                                 @Param("processDefinitionId") Integer processDefinitionId,
                                 @Param("states")String statusArray,
                                 @Param("host")String host,
                                 @Param("startTime") Date startTime,
                                 @Param("endTime") Date endTime,
                                 @Param("searchVal") String searchVal
    );

    /**
     * update process instance by host and status
     * @param host
     * @param stateArray
     * @return
     */
    @UpdateProvider(type = ProcessInstanceMapperProvider.class, method = "setFailoverByHostAndStateArray")
    int setFailoverByHostAndStateArray(@Param("host") String host,@Param("states")int[] stateArray);

    /**
     * update process instance by state
     * @param originState
     * @param destState
     * @return
     */
    @UpdateProvider(type = ProcessInstanceMapperProvider.class, method = "updateProcessInstanceByState")
    int updateProcessInstanceByState(@Param("originState")ExecutionStatus originState, @Param("destState")ExecutionStatus destState);

    /**
     * update state
     * @param  processId
     * @param executionStatus
     * @return
     */
    @UpdateProvider(type = ProcessInstanceMapperProvider.class, method = "updateState")
    int updateState(@Param("processId")Integer processId, @Param("executionStatus")ExecutionStatus executionStatus);


    /**
     * query process instance by task id
     * @param taskId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processDefinitionId", column = "process_definition_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "recovery", column = "recovery",  typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "startTime", column = "start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "runTimes", column = "run_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "commandType", column = "command_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = CommandType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "commandParam", column = "command_param",  javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskDependType", column = "task_depend_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = TaskDependType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "maxTryTimes", column = "max_try_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningType", column = "warning_type", javaType = WarningType.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningGroupId", column = "warning_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "scheduleTime", column = "schedule_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "commandStartTime", column = "command_start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "globalParams", column = "global_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executorId", column = "executor_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "isSubProcess", column = "is_sub_process", javaType = Flag.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "locations", column = "locations", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "connects", column = "connects", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "historyCmd", column = "history_cmd", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "duration", column = "duration", javaType = Long.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "dependenceScheduleTimes", column = "dependence_schedule_times", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "duration", column = "duration", javaType = Long.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "processInstanceJson", column = "process_instance_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "workerGroupId", column = "worker_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "timeout", column = "timeout",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)

    })
    @SelectProvider(type = ProcessInstanceMapperProvider.class, method = "queryByTaskId")
    ProcessInstance queryByTaskId(@Param("taskId") int taskId);


    /**
     * update process instance
     * @param processId
     * @param processJson
     * @param globalParams
     * @param scheduleTime
     * @param locations
     * @param connects
     * @param flag
     * @return
     */
    @UpdateProvider(type = ProcessInstanceMapperProvider.class, method = "updateProcessInstance")
    int updateProcessInstance(@Param("processId") Integer processId,
                              @Param("processJson") String processJson,
                              @Param("globalParams") String globalParams,
                              @Param("scheduleTime") Date scheduleTime,
                              @Param("locations") String locations,
                              @Param("connects") String connects,
                              @Param("flag") Flag flag);

    /**
     * count process number group by state and user
     * @param userId
     * @param userType
     * @param startTime
     * @param endTime
     * @param projectId
     * @return
     */
    @Results(value = {
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "count", column = "count", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
    })
    @SelectProvider(type = ProcessInstanceMapperProvider.class, method = "countInstanceStateByUser")
    List<ExecuteStatusCount> countInstanceStateByUser(
                        @Param("userId") int userId,
                        @Param("userType") UserType userType,
                        @Param("startTime") Date startTime,
                        @Param("endTime") Date endTime,
                        @Param("projectId") int projectId);

    /**
     * query sub process id list by father process instance id
     *
     * @param parentInstanceId
     * @return
     */
    @SelectProvider(type = ProcessInstanceMapperProvider.class, method = "querySubIdListByParentId")
    List<Integer> querySubIdListByParentId(@Param("parentInstanceId") int parentInstanceId);

    /**
     * query instance by definition id
     * @param processDefinitionId
     * @param size
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processDefinitionId", column = "process_definition_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "recovery", column = "recovery",  typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "startTime", column = "start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "runTimes", column = "run_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "commandType", column = "command_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = CommandType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "commandParam", column = "command_param",  javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskDependType", column = "task_depend_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = TaskDependType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "maxTryTimes", column = "max_try_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningType", column = "warning_type", javaType = WarningType.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningGroupId", column = "warning_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "scheduleTime", column = "schedule_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "commandStartTime", column = "command_start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "globalParams", column = "global_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executorId", column = "executor_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "isSubProcess", column = "is_sub_process", javaType = Flag.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "locations", column = "locations", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "connects", column = "connects", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "historyCmd", column = "history_cmd", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "duration", column = "duration", javaType = Long.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "dependenceScheduleTimes", column = "dependence_schedule_times", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "duration", column = "duration", javaType = Long.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "processInstanceJson", column = "process_instance_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "workerGroupId", column = "worker_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "timeout", column = "timeout",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)

    })
    @SelectProvider(type = ProcessInstanceMapperProvider.class, method = "queryByProcessDefineId")
    List<ProcessInstance> queryByProcessDefineId(@Param("processDefinitionId") int processDefinitionId,@Param("size") int size);


    /**
     * query process instance by definition and scheduler time
     * @param processDefinitionId
     * @param scheduleTime
     * @param excludeId
     * @param startTime
     * @param endTime
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processDefinitionId", column = "process_definition_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "recovery", column = "recovery",  typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "startTime", column = "start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "runTimes", column = "run_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "commandType", column = "command_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = CommandType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "commandParam", column = "command_param",  javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskDependType", column = "task_depend_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = TaskDependType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "maxTryTimes", column = "max_try_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningType", column = "warning_type", javaType = WarningType.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningGroupId", column = "warning_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "scheduleTime", column = "schedule_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "commandStartTime", column = "command_start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "globalParams", column = "global_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executorId", column = "executor_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "isSubProcess", column = "is_sub_process", javaType = Flag.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "locations", column = "locations", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "connects", column = "connects", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "historyCmd", column = "history_cmd", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "dependenceScheduleTimes", column = "dependence_schedule_times", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "duration", column = "duration", javaType = Long.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "processInstanceJson", column = "process_instance_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "workerGroupId", column = "worker_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "timeout", column = "timeout",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)

    })
    @SelectProvider(type = ProcessInstanceMapperProvider.class, method = "queryByScheduleTime")
    ProcessInstance queryByScheduleTime(@Param("processDefinitionId") int processDefinitionId,
                                        @Param("scheduleTime") String scheduleTime,
                                        @Param("excludeId") int excludeId,
                                        @Param("startTime") String startTime,
                                        @Param("endTime") String endTime);

    /**
     *  get last scheduler process intance between start time and end time
     * @param definitionId
     * @param startTime
     * @param endTime
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processDefinitionId", column = "process_definition_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "recovery", column = "recovery",  typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "startTime", column = "start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "runTimes", column = "run_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "commandType", column = "command_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = CommandType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "commandParam", column = "command_param",  javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskDependType", column = "task_depend_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = TaskDependType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "maxTryTimes", column = "max_try_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningType", column = "warning_type", javaType = WarningType.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningGroupId", column = "warning_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "scheduleTime", column = "schedule_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "commandStartTime", column = "command_start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "globalParams", column = "global_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executorId", column = "executor_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "isSubProcess", column = "is_sub_process", javaType = Flag.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "locations", column = "locations", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "connects", column = "connects", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "historyCmd", column = "history_cmd", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "dependenceScheduleTimes", column = "dependence_schedule_times", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "processInstanceJson", column = "process_instance_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "workerGroupId", column = "worker_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "timeout", column = "timeout",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)

    })
    @SelectProvider(type = ProcessInstanceMapperProvider.class, method = "queryLastSchedulerProcess")
    ProcessInstance queryLastSchedulerProcess(@Param("processDefinitionId") int definitionId,
                                              @Param("startTime") String startTime,
                                              @Param("endTime") String endTime);

    /**
     *  get last running process instance between start time and end time
     * @param definitionId
     * @param startTime
     * @param endTime
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processDefinitionId", column = "process_definition_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "recovery", column = "recovery",  typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "startTime", column = "start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "runTimes", column = "run_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "commandType", column = "command_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = CommandType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "commandParam", column = "command_param",  javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskDependType", column = "task_depend_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = TaskDependType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "maxTryTimes", column = "max_try_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningType", column = "warning_type", javaType = WarningType.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningGroupId", column = "warning_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "scheduleTime", column = "schedule_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "commandStartTime", column = "command_start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "globalParams", column = "global_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executorId", column = "executor_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "isSubProcess", column = "is_sub_process", javaType = Flag.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "locations", column = "locations", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "connects", column = "connects", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "historyCmd", column = "history_cmd", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "dependenceScheduleTimes", column = "dependence_schedule_times", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "processInstanceJson", column = "process_instance_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "workerGroupId", column = "worker_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "timeout", column = "timeout",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
    })
    @SelectProvider(type = ProcessInstanceMapperProvider.class, method = "queryLastRunningProcess")
    ProcessInstance queryLastRunningProcess(@Param("processDefinitionId") int definitionId,
                                            @Param("startTime") String startTime,
                                            @Param("endTime") String endTime,
                                            @Param("states")int[] stateArray);

    /**
     *  get last manual process instance between start time and end time
     * @param definitionId
     * @param startTime
     * @param endTime
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processDefinitionId", column = "process_definition_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "recovery", column = "recovery",  typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "startTime", column = "start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "runTimes", column = "run_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "commandType", column = "command_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = CommandType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "commandParam", column = "command_param",  javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskDependType", column = "task_depend_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = TaskDependType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "maxTryTimes", column = "max_try_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "failureStrategy", column = "failure_strategy", typeHandler = EnumOrdinalTypeHandler.class, javaType = FailureStrategy.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningType", column = "warning_type", javaType = WarningType.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningGroupId", column = "warning_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "scheduleTime", column = "schedule_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "commandStartTime", column = "command_start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "globalParams", column = "global_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executorId", column = "executor_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "isSubProcess", column = "is_sub_process", javaType = Flag.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "locations", column = "locations", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "connects", column = "connects", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "historyCmd", column = "history_cmd", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "dependenceScheduleTimes", column = "dependence_schedule_times", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "processInstanceJson", column = "process_instance_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "workerGroupId", column = "worker_group_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "timeout", column = "timeout",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantId", column = "tenant_id",  javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
    })
    @SelectProvider(type = ProcessInstanceMapperProvider.class, method = "queryLastManualProcess")
    ProcessInstance queryLastManualProcess(@Param("processDefinitionId") int definitionId,
                                           @Param("startTime") String startTime,
                                           @Param("endTime") String endTime);
}
