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

import cn.escheduler.common.model.MasterServer;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

public interface MasterServerMapper {

    /**
     * query all masters
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
    @SelectProvider(type = MasterServerMapperProvider.class, method = "queryAllMaster")
    List<MasterServer> queryAllMaster();

    /**
     * insert one master
     *
     * @param masterServer
     * @return
     */
    @InsertProvider(type = MasterServerMapperProvider.class, method = "insert")
    @SelectKey(statement = "SELECT LAST_INSERT_ID() as id", resultType = Integer.class, keyProperty = "masterServer.id", before = false)
    int insert(@Param("masterServer") MasterServer masterServer);

    /**
     * update master
     *
     * @param masterServer
     * @return
     */
    @UpdateProvider(type = MasterServerMapperProvider.class, method = "update")
    int update(@Param("masterServer") MasterServer masterServer);

    /**
     * delete master
     */
    @DeleteProvider(type = MasterServerMapperProvider.class, method = "delete")
    void delete();

    /**
     * delete master by host
     *
     * @param host
     */
    @DeleteProvider(type = MasterServerMapperProvider.class, method = "deleteWorkerByHost")
    int deleteWorkerByHost(@Param("host") String host);




}
