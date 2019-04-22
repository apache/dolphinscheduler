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

import cn.escheduler.dao.model.WorkerGroup;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

/**
 * worker group mapper
 */
public interface WorkerGroupMapper {

    /**
     * query all worker group list
     *
     * @return
     */
    @Results(value = {
            @Result(property = "id", column = "id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "ipList", column = "ip_list", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "updateTime", column = "update_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
    })
    @SelectProvider(type = WorkerGroupMapperProvider.class, method = "queryAllWorkerGroup")
    List<WorkerGroup> queryAllWorkerGroup();

    /**
     * query worker group by name
     *
     * @return
     */
    @Results(value = {
            @Result(property = "id", column = "id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "ipList", column = "ip_list", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "updateTime", column = "update_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
    })
    @SelectProvider(type = WorkerGroupMapperProvider.class, method = "queryWorkerGroupByName")
    List<WorkerGroup> queryWorkerGroupByName(@Param("name") String name);

     /**
     * query worker group paging by search value
     *
     * @return
     */
    @Results(value = {
            @Result(property = "id", column = "id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "ipList", column = "ip_list", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "updateTime", column = "update_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
    })
    @SelectProvider(type = WorkerGroupMapperProvider.class, method = "queryListPaging")
    List<WorkerGroup> queryListPaging(@Param("offset") int offset,
                                      @Param("pageSize") int pageSize,
                                      @Param("searchVal") String searchVal);

    /**
     * count worker group by search value
     * @param searchVal
     * @return
     */
    @SelectProvider(type = WorkerGroupMapperProvider.class, method = "countPaging")
    int countPaging(@Param("searchVal") String searchVal);

    /**
     * insert worker server
     *
     * @param workerGroup
     * @return
     */
    @InsertProvider(type = WorkerGroupMapperProvider.class, method = "insert")
    @Options(useGeneratedKeys = true,keyProperty = "workerGroup.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "workerGroup.id", before = false, resultType = int.class)
    int insert(@Param("workerGroup") WorkerGroup workerGroup);

    /**
     * update worker
     *
     * @param workerGroup
     * @return
     */
    @UpdateProvider(type = WorkerGroupMapperProvider.class, method = "update")
    int update(@Param("workerGroup") WorkerGroup workerGroup);

    /**
     * delete work group by id
     * @param id
     * @return
     */
    @DeleteProvider(type = WorkerGroupMapperProvider.class, method = "deleteById")
    int deleteById(@Param("id") int id);

    /**
     * query work group by id
     * @param id
     * @return
     */
    @Results(value = {
            @Result(property = "id", column = "id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "ipList", column = "ip_list", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "updateTime", column = "update_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
    })
    @SelectProvider(type = WorkerGroupMapperProvider.class, method = "queryById")
    WorkerGroup queryById(@Param("id") int id);



}
