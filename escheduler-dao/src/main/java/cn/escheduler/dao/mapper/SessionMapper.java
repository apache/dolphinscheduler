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

import cn.escheduler.dao.model.Session;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * session mapper
 */
public interface SessionMapper {

    /**
     * insert session
     * @param session
     * @return
     */
    @InsertProvider(type = SessionMapperProvider.class, method = "insert")
    int insert(@Param("session") Session session);


    /**
     * delete session
     * @param sessionId
     * @return
     */
    @DeleteProvider(type = SessionMapperProvider.class, method = "delete")
    int deleteById(@Param("sessionId") String sessionId);


    /**
     * update session
     *
     * @param sessionId
     * @param loginTime
     * @return
     */
    @UpdateProvider(type = SessionMapperProvider.class, method = "update")
    int update(@Param("sessionId") String sessionId, @Param("loginTime") Date loginTime);


    /**
     * query by session id and ip
     *
     * @param sessionId
     * @return
     */
    @Results(value = {
            @Result(property = "id", column = "id", id = true, javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "ip", column = "ip", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "lastLoginTime", column = "last_login_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = SessionMapperProvider.class, method = "queryBySessionId")
    Session queryBySessionId(@Param("sessionId") String sessionId);


    /**
     * query by user id and ip
     * @param userId
     * @return
     */
    @Results(value = {
            @Result(property = "id", column = "id", id = true, javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "ip", column = "ip", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "lastLoginTime", column = "last_login_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = SessionMapperProvider.class, method = "queryByUserId")
    List<Session> queryByUserId(@Param("userId") int userId);

}
