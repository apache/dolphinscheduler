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

import cn.escheduler.common.enums.UserType;
import cn.escheduler.dao.model.User;
import cn.escheduler.dao.model.UserAlertGroup;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.List;

/**
 * user alert group mapper
 */
public interface UserAlertGroupMapper {

    /**
     * insert user alert group
     * @param userAlertGroup
     * @return
     */
    @InsertProvider(type = UserAlertGroupMapperProvider.class, method = "insert")
    @Options(useGeneratedKeys = true,keyProperty = "userAlertGroup.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "userAlertGroup.id", before = false, resultType = int.class)
    int insert(@Param("userAlertGroup") UserAlertGroup userAlertGroup);


    /**
     * query user list by alert group id
     *
     * @param alertgroupId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userPassword", column = "user_password", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "email", column = "email", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "phone", column = "phone", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userType", column = "user_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UserType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "tenantId", column = "tenant_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = UserAlertGroupMapperProvider.class, method = "queryForUser")
    List<User> queryForUser(@Param("alertgroupId") int alertgroupId);


    /**
     * delete by alert group id
     * @param alertgroupId
     * @return
     */
    @DeleteProvider(type = UserAlertGroupMapperProvider.class, method = "deleteByAlertgroupId")
    int deleteByAlertgroupId(@Param("alertgroupId") int alertgroupId);

    /**
     * list user information by alert group id
     *
     * @param alertgroupId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userPassword", column = "user_password", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "email", column = "email", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "phone", column = "phone", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userType", column = "user_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UserType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "tenantId", column = "tenant_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
    })
    @SelectProvider(type = UserAlertGroupMapperProvider.class, method = "listUserByAlertgroupId")
    List<User> listUserByAlertgroupId(@Param("alertgroupId") int alertgroupId);

}
