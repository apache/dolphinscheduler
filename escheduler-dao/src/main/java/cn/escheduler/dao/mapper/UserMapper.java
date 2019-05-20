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
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.List;

public interface UserMapper {

    /**
     * insert user
     * @param user
     * @return
     */
    @InsertProvider(type = UserMapperProvider.class, method = "insert")
    @Options(useGeneratedKeys = true,keyProperty = "user.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "user.id", before = false, resultType = int.class)
    int insert(@Param("user") User user);


    /**
     * delete user
     * @param userId
     * @return
     */
    @DeleteProvider(type = UserMapperProvider.class, method = "delete")
    int delete(@Param("userId") int userId);


    /**
     * update user
     *
     * @param user
     * @return
     */
    @UpdateProvider(type = UserMapperProvider.class, method = "update")
    int update(@Param("user") User user);


    /**
     * query user by id
     * @param userId
     * @return
     */
    @Results(value = {
            @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userPassword", column = "user_password", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "email", column = "email", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "phone", column = "phone", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userType", column = "user_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UserType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "tenantId", column = "tenant_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = UserMapperProvider.class, method = "queryById")
    User queryById(@Param("userId") int userId);

    /**
     * query all general user list
     * @return
     */
    @Results(value = {
            @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userPassword", column = "user_password", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "email", column = "email", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "phone", column = "phone", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userType", column = "user_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UserType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "tenantId", column = "tenant_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = UserMapperProvider.class, method = "queryAllGeneralUsers")
    List<User> queryAllGeneralUsers();


    /**
     * query all user list
     * @return
     */
    @Results(value = {
            @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userPassword", column = "user_password", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "email", column = "email", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "phone", column = "phone", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userType", column = "user_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UserType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "tenantId", column = "tenant_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = UserMapperProvider.class, method = "queryAllUsers")
    List<User> queryAllUsers();


    /**
     * check user name and password
     * @param userName
     * @param userPassword
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
    @SelectProvider(type = UserMapperProvider.class, method = "queryForCheck")
    User queryForCheck(@Param("userName") String userName, @Param("userPassword") String userPassword);


    /**
     * query user by name
     * @param userName
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
    @SelectProvider(type = UserMapperProvider.class, method = "queryByUserName")
    User queryByUserName(@Param("userName") String userName);

    /**
     * count user by search value
     * @param searchVal
     * @return
     */
    @SelectProvider(type = UserMapperProvider.class, method = "countUserPaging")
    Integer countUserPaging(@Param("searchVal") String searchVal);


    /**
     * query user list paging
     * @param searchVal
     * @param offset
     * @param pageSize
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userPassword", column = "user_password", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "email", column = "email", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "phone", column = "phone", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userType", column = "user_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UserType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "tenantId", column = "tenant_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantName", column = "tenant_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "queueName", column = "queue_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = UserMapperProvider.class, method = "queryUserPaging")
    List<User> queryUserPaging(@Param("searchVal") String searchVal,
                               @Param("offset") Integer offset,
                               @Param("pageSize") Integer pageSize);

    /**
     * query detail by user id
     * @param id
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userPassword", column = "user_password", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "email", column = "email", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "phone", column = "phone", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userType", column = "user_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UserType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "tenantId", column = "tenant_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantName", column = "tenant_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "queueName", column = "queue_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = UserMapperProvider.class, method = "queryDetailsById")
    User queryDetailsById(@Param("id") int id);


    /**
     * 根据用户id查询已经授权的告警组
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
    @SelectProvider(type = UserMapperProvider.class, method = "queryUserListByAlertGroupId")
    List<User> queryUserListByAlertGroupId(@Param("alertgroupId") int alertgroupId);


    /**
     * query tenant code by user id
     * @param userId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userPassword", column = "user_password", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "email", column = "email", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "phone", column = "phone", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userType", column = "user_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UserType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "tenantId", column = "tenant_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantCode", column = "tenant_code", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = UserMapperProvider.class, method = "queryTenantCodeByUserId")
    User queryTenantCodeByUserId(@Param("userId") int userId);


    /**
     * query user queue by process instance id
     * @param processInstanceId
     * @return
     */
    @SelectProvider(type = UserMapperProvider.class, method = "queryQueueByProcessInstanceId")
    String queryQueueByProcessInstanceId(@Param("processInstanceId") int processInstanceId);


    /**
     * query user by token
     * @param token
     * @return
     */
    @Results(value = {
            @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userPassword", column = "user_password", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "email", column = "email", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "phone", column = "phone", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userType", column = "user_type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UserType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "tenantId", column = "tenant_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = UserMapperProvider.class, method = "queryUserByToken")
    User queryUserByToken(@Param("token") String token);
}
