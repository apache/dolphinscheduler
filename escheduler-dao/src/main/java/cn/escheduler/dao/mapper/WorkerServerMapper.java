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

import cn.escheduler.dao.model.WorkerServer;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

public interface WorkerServerMapper {

    /**
     * query worker list
     *
     * @return
     */
    @Results(value = {
            @Result(property = "id", column = "id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "port", column = "port", javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "zkDirectory", column = "zk_directory", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "resInfo", column = "res_info", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "lastHeartbeatTime", column = "last_heartbeat_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP)
    })
    @SelectProvider(type = WorkerServerMapperProvider.class, method = "queryAllWorker")
    List<WorkerServer> queryAllWorker();

    /**
     * query worker list
     *
     * @return
     */
    @Results(value = {
            @Result(property = "id", column = "id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "host", column = "host", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "port", column = "port", javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "zkDirectory", column = "zk_directory", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "resInfo", column = "res_info", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "lastHeartbeatTime", column = "last_heartbeat_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP)
    })
    @SelectProvider(type = WorkerServerMapperProvider.class, method = "queryWorkerByHost")
    List<WorkerServer> queryWorkerByHost(@Param("host") String host);

    /**
     * insert worker server
     *
     * @param workerServer
     * @return
     */
    @InsertProvider(type = WorkerServerMapperProvider.class, method = "insert")
    int insert(@Param("workerServer") WorkerServer workerServer);

    /**
     * update worker
     *
     * @param workerServer
     * @return
     */
    @UpdateProvider(type = WorkerServerMapperProvider.class, method = "update")
    int update(@Param("workerServer") WorkerServer workerServer);

    /**
     * delete work by host
     * @param host
     * @return
     */
    @DeleteProvider(type = WorkerServerMapperProvider.class, method = "deleteWorkerByHost")
    int deleteWorkerByHost(@Param("host") String host);




}
