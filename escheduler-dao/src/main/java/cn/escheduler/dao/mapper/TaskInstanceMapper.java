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
import cn.escheduler.common.enums.UserType;
import cn.escheduler.dao.model.ExecuteStatusCount;
import cn.escheduler.dao.model.TaskInstance;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * task intance mapper
 */
public interface TaskInstanceMapper {

    /**
     * insert task instance
     * @param taskInstance
     * @return
     */
    @InsertProvider(type = TaskInstanceMapperProvider.class, method = "insert")
    @Options(useGeneratedKeys = true,keyProperty = "taskInstance.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "taskInstance.id", before = false, resultType = int.class)
    int insert(@Param("taskInstance") TaskInstance taskInstance);

    /**
     * delete task instance
     * @param taskInstanceId
     * @return
     */
    @DeleteProvider(type = TaskInstanceMapperProvider.class, method = "delete")
    int delete(@Param("taskInstanceId") int taskInstanceId);

    /**
     * update task instance
     *
     * @param taskInstance
     * @return
     */
    @UpdateProvider(type = TaskInstanceMapperProvider.class, method = "update")
    int update(@Param("taskInstance") TaskInstance taskInstance);

    /**
     * query task by id
     * @param taskInstanceId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskType", column = "task_type",javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "processDefinitionId", column = "process_definition_id",javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstanceId", column = "process_instance_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "taskJson", column = "task_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "submitTime", column = "submit_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "startTime", column = "start_time",  javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time",  javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executePath", column = "execute_path", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "logPath", column = "log_path", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "alertFlag", column = "alert_flag", typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "retryTimes", column = "retry_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "pid", column = "pid", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "maxRetryTimes", column = "max_retry_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "retryInterval", column = "retry_interval", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "appLink", column = "app_link", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "duration", column = "duration", javaType = Long.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "flag", column = "flag", typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "workerGroupId", column = "worker_group_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "taskInstancePriority", column = "task_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
    })
    @SelectProvider(type = TaskInstanceMapperProvider.class, method = "queryById")
    TaskInstance queryById(@Param("taskInstanceId") int taskInstanceId);

    /**
     * query task id list by process instance id and state
     * @param processInstanceId
     * @param state
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", javaType = Integer.class, jdbcType = JdbcType.INTEGER)})
    @SelectProvider(type = TaskInstanceMapperProvider.class, method = "queryTaskByProcessIdAndState")
    List<Integer> queryTaskByProcessIdAndState(@Param("processInstanceId") Integer processInstanceId,
                                               @Param("state") Integer state);

    /**
     * query valid task instance list by process id
     * @param processInstanceId
     * @param flag
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskType", column = "task_type",javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "processDefinitionId", column = "process_definition_id",javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstanceId", column = "process_instance_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "taskJson", column = "task_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "submitTime", column = "submit_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "startTime", column = "start_time",  javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time",  javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executePath", column = "execute_path", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "logPath", column = "log_path", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "alertFlag", column = "alert_flag", typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "retryTimes", column = "retry_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "maxRetryTimes", column = "max_retry_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "retryInterval", column = "retry_interval", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "pid", column = "pid", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "appLink", column = "app_link", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "duration", column = "duration", javaType = Long.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "flag", column = "flag", typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "workerGroupId", column = "worker_group_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "taskInstancePriority", column = "task_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
    })
    @SelectProvider(type = TaskInstanceMapperProvider.class, method = "findValidTaskListByProcessId")
    List<TaskInstance> findValidTaskListByProcessId(@Param("processInstanceId") Integer processInstanceId,
                                                    @Param("flag") Flag flag);

    /**
     * query task list by host and state
     * @param host
     * @param stateArray
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskType", column = "task_type",javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "processDefinitionId", column = "process_definition_id",javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstanceId", column = "process_instance_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "taskJson", column = "task_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "submitTime", column = "submit_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "startTime", column = "start_time",  javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time",  javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executePath", column = "execute_path", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "logPath", column = "log_path", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "alertFlag", column = "alert_flag", typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "retryTimes", column = "retry_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "maxRetryTimes", column = "max_retry_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "retryInterval", column = "retry_interval", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "pid", column = "pid", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "appLink", column = "app_link", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "duration", column = "duration", javaType = Long.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "flag", column = "flag", typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "workerGroupId", column = "worker_group_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "taskInstancePriority", column = "task_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
    })
    @SelectProvider(type = TaskInstanceMapperProvider.class, method = "queryByHostAndStatus")
    List<TaskInstance> queryByHostAndStatus(@Param("host") String host,@Param("states") int[] stateArray);

    /**
     * set task state to need failover when worker down
     * @param host
     * @param stateArray
     * @return
     */
    @UpdateProvider(type = TaskInstanceMapperProvider.class, method = "setFailoverByHostAndStateArray")
    int setFailoverByHostAndStateArray(@Param("host") String host, @Param("states")int[] stateArray);

    /**
     * count task number group by state and user
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
    @SelectProvider(type = TaskInstanceMapperProvider.class, method = "countTaskInstanceStateByUser")
    List<ExecuteStatusCount> countTaskInstanceStateByUser(@Param("userId") int userId,
                                                          @Param("userType") UserType userType,
                                                          @Param("startTime") Date startTime,
                                                          @Param("endTime") Date endTime,
                                                          @Param("projectId") int projectId);

    /**
     * count task number by search fields
     * @param projectId
     * @param processInstanceId
     * @param taskName
     * @param statusArray
     * @param startTime
     * @param endTime
     * @param searchVal
     * @return
     */
    @SelectProvider(type = TaskInstanceMapperProvider.class, method = "countTaskInstance")
    Integer countTaskInstance(@Param("projectId") int projectId,
                              @Param("processInstanceId") Integer processInstanceId,
                              @Param("taskName") String taskName,
                              @Param("states") String statusArray,
                              @Param("host") String host,
                              @Param("startTime") Date startTime,
                              @Param("endTime") Date endTime,
                              @Param("searchVal") String searchVal
    );

    /**
     * query task list paging by search fields
     * @param projectId
     * @param processInstanceId
     * @param searchVal
     * @param taskName
     * @param statusArray
     * @param startTime
     * @param endTime
     * @param offset
     * @param pageSize
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskType", column = "task_type",javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "processDefinitionId", column = "process_definition_id",javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstanceId", column = "process_instance_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstanceName", column = "process_instance_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskJson", column = "task_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "submitTime", column = "submit_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "startTime", column = "start_time",  javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time",  javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executePath", column = "execute_path", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "logPath", column = "log_path", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "alertFlag", column = "alert_flag", typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "retryTimes", column = "retry_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "maxRetryTimes", column = "max_retry_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "retryInterval", column = "retry_interval", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "pid", column = "pid", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "appLink", column = "app_link", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "duration", column = "duration", javaType = Long.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "flag", column = "flag", typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "workerGroupId", column = "worker_group_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "taskInstancePriority", column = "task_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
    })
    @SelectProvider(type = TaskInstanceMapperProvider.class, method = "queryTaskInstanceListPaging")
    List<TaskInstance> queryTaskInstanceListPaging(
            @Param("projectId") int projectId,
            @Param("processInstanceId") Integer processInstanceId,
            @Param("searchVal") String searchVal,
            @Param("taskName") String taskName,
            @Param("states") String statusArray,
            @Param("host") String host,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    /**
     * query task list by process id and task name
     * @param processInstanceId
     * @param name
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskType", column = "task_type",javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "processDefinitionId", column = "process_definition_id",javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstanceId", column = "process_instance_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstanceName", column = "process_instance_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskJson", column = "task_json", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "submitTime", column = "submit_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "startTime", column = "start_time",  javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "endTime", column = "end_time",  javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "executePath", column = "execute_path", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "logPath", column = "log_path", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "alertFlag", column = "alert_flag", typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "retryTimes", column = "retry_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "maxRetryTimes", column = "max_retry_times", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "retryInterval", column = "retry_interval", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "pid", column = "pid", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "appLink", column = "app_link", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "duration", column = "duration", javaType = Long.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "flag", column = "flag", typeHandler = EnumOrdinalTypeHandler.class, javaType = Flag.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "workerGroupId", column = "worker_group_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "taskInstancePriority", column = "task_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
    })
    @SelectProvider(type = TaskInstanceMapperProvider.class, method = "queryByInstanceIdAndName")
    TaskInstance queryByInstanceIdAndName(@Param("processInstanceId") int processInstanceId,
                                          @Param("name") String name);


    /**
     * count task
     * @param userId
     * @param userType
     * @param projectId
     * @return
     */
    @SelectProvider(type = TaskInstanceMapperProvider.class, method = "countTask")
    Integer countTask(@Param("userId") int userId,
                        @Param("userType") UserType userType,
                        @Param("projectId") int projectId,
                        @Param("taskIds") int[] taskIds);
}
