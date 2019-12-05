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

import cn.escheduler.dao.model.Tenant;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.List;

/**
 * tenant mapper
 */
public interface TenantMapper {

    /**
     * insert tenant
     * @param tenant
     * @return
     */
    @InsertProvider(type = TenantMapperProvider.class, method = "insert")
    @Options(useGeneratedKeys = true,keyProperty = "tenant.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "tenant.id", before = false, resultType = int.class)
    int insert(@Param("tenant") Tenant tenant);


    /**
     * delete tenant
     * @param id
     * @return
     */
    @DeleteProvider(type = TenantMapperProvider.class, method = "deleteById")
    int deleteById(@Param("id") int id);


    /**
     * update tenant
     *
     * @param tenant
     * @return
     */
    @UpdateProvider(type = TenantMapperProvider.class, method = "update")
    int update(@Param("tenant") Tenant tenant);


    /**
     * query tenant by id
     * @param tenantId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantCode", column = "tenant_code", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "tenantName", column = "tenant_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "queueId", column = "queue_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "queueName", column = "queue_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "queue", column = "queue", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
    })
    @SelectProvider(type = TenantMapperProvider.class, method = "queryById")
    Tenant queryById(@Param("tenantId") int tenantId);

    /**
     * query tenant by code
     * @param tenantCode
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantCode", column = "tenant_code", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "tenantName", column = "tenant_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "queueId", column = "queue_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
    })
    @SelectProvider(type = TenantMapperProvider.class, method = "queryByTenantCode")
    Tenant queryByTenantCode(@Param("tenantCode") String tenantCode);

    /**
     * count tenant by search value
     * @param searchVal
     * @return
     */
    @SelectProvider(type = TenantMapperProvider.class, method = "countTenantPaging")
    Integer countTenantPaging(@Param("searchVal") String searchVal);


    /**
     * query tenant list paging
     * @param searchVal
     * @param offset
     * @param pageSize
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantCode", column = "tenant_code", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "tenantName", column = "tenant_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "queueId", column = "queue_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "queueName", column = "queue_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = TenantMapperProvider.class, method = "queryTenantPaging")
    List<Tenant> queryTenantPaging(@Param("searchVal") String searchVal,
                                   @Param("offset") Integer offset,
                                   @Param("pageSize") Integer pageSize);

    /**
     * query all tenant list
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "tenantCode", column = "tenant_code", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "tenantName", column = "tenant_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "queueId", column = "queue_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = TenantMapperProvider.class, method = "queryAllTenant")
    List<Tenant> queryAllTenant();
}
