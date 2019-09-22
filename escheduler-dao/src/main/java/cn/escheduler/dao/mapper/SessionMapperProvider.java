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

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * session mapper provider
 */
public class SessionMapperProvider {

    private static final String TABLE_NAME = "t_escheduler_session";

    /**
     * insert session
     *
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {
            {
                INSERT_INTO(TABLE_NAME);
                VALUES("`id`", "#{session.id}");
                VALUES("`user_id`", "#{session.userId}");
                VALUES("`ip`", "#{session.ip}");
                VALUES("`last_login_time`", "#{session.lastLoginTime}");

            }
        }.toString();
    }

    /**
     * delete session
     *
     * @param parameter
     * @return
     */
    public String delete(Map<String, Object> parameter) {
        return new SQL() {
            {
                DELETE_FROM(TABLE_NAME);

                WHERE("`id`=#{sessionId}");
            }
        }.toString();
    }

    /**
     * update session
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);

                SET("`last_login_time`=#{loginTime}");

                WHERE("`id` = #{sessionId}");
            }
        }.toString();
    }






    /**
     * query by  session id
     *
     * @param parameter
     * @return
     */
    public String queryById(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);

                WHERE("`id` = #{sessionId}");
            }
        }.toString();
    }

    /**
     * query by session id
     * @param parameter
     * @return
     */
    public String queryBySessionId(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("*");

            FROM(TABLE_NAME);

            WHERE("`id` = #{sessionId}");
        }}.toString();
    }

    /**
     * query by user id
     * @param parameter
     * @return
     */
    public String queryByUserId(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("*");

            FROM(TABLE_NAME);

            WHERE("`user_id` = #{userId}");
        }}.toString();
    }
}
