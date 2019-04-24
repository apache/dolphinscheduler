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
import cn.escheduler.dao.model.Command;
import cn.escheduler.dao.model.ExecuteStatusCount;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * command mapper
 */
public interface CommandMapper {

    /**
     * inert command
     * @param command
     * @return
     */
    @InsertProvider(type = CommandMapperProvider.class, method = "insert")
    @Options(useGeneratedKeys = true,keyProperty = "command.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "command.id", before = false, resultType = int.class)
    int insert(@Param("command") Command command);


    /**
     * delete command
     * @param cmdId
     * @return
     */
    @DeleteProvider(type = CommandMapperProvider.class, method = "delete")
    int delete(@Param("cmdId") int cmdId);


    /**
     * update command
     *
     * @param command
     * @return
     */
    @UpdateProvider(type = CommandMapperProvider.class, method = "update")
    int update(@Param("command") Command command);


    /**
     * query a command that can run normally
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "commandType", column = "command_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = CommandType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "processDefinitionId", column = "process_definition_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "executorId", column = "executor_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "commandParam", column = "command_param", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskDependType", column = "task_depend_type", javaType = TaskDependType.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "failureStrategy", column = "failure_strategy", javaType = FailureStrategy.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningType", column = "warning_type", javaType = WarningType.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningGroupId", column = "warning_group_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "scheduleTime", column = "schedule_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "startTime", column = "start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "workerGroupId", column = "worker_group_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
    })
    @SelectProvider(type = CommandMapperProvider.class, method = "queryOneCommand")
    Command queryOneCommand();


    /**
     * query all commands
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "commandType", column = "command_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = CommandType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "processDefinitionId", column = "process_definition_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "executorId", column = "executor_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "commandParam", column = "command_param", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "taskDependType", column = "task_depend_type", javaType = TaskDependType.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "failureStrategy", column = "failure_strategy", javaType = FailureStrategy.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningType", column = "warning_type", javaType = WarningType.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "warningGroupId", column = "warning_group_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "scheduleTime", column = "schedule_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "startTime", column = "start_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "workerGroupId", column = "worker_group_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "processInstancePriority", column = "process_instance_priority", javaType = Priority.class, typeHandler = EnumOrdinalTypeHandler.class, jdbcType = JdbcType.TINYINT)
    })
    @SelectProvider(type = CommandMapperProvider.class, method = "queryAllCommand")
    List<Command> queryAllCommand();



    @Results(value = {
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "count", column = "count", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
    })
    @SelectProvider(type = CommandMapperProvider.class, method = "countCommandState")
    List<ExecuteStatusCount> countCommandState(
            @Param("userId") int userId,
            @Param("userType") UserType userType,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("projectId") int projectId);
}
