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
import cn.escheduler.dao.model.ErrorCommand;
import cn.escheduler.dao.model.ExecuteStatusCount;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * command mapper
 */
public interface ErrorCommandMapper {

    /**
     * inert error command
     * @param errorCommand
     * @return
     */
    @InsertProvider(type = ErrorCommandMapperProvider.class, method = "insert")
    @Options(useGeneratedKeys = true,keyProperty = "errorCommand.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "errorCommand.id", before = false, resultType = int.class)
    int insert(@Param("errorCommand") ErrorCommand errorCommand);

    @Results(value = {
            @Result(property = "state", column = "state", typeHandler = EnumOrdinalTypeHandler.class, javaType = ExecutionStatus.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "count", column = "count", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
    })
    @SelectProvider(type = ErrorCommandMapperProvider.class, method = "countCommandState")
    List<ExecuteStatusCount> countCommandState(
            @Param("userId") int userId,
            @Param("userType") UserType userType,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("projectId") int projectId);


}
