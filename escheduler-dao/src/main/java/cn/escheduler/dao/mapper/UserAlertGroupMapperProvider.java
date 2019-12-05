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
 * user alert group mapper provider
 */
public class UserAlertGroupMapperProvider {

    private static final String TABLE_NAME = "t_escheduler_relation_user_alertgroup";

    /**
     * insert user alert grouExecutorService.p
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {
            {
                INSERT_INTO(TABLE_NAME);
                VALUES("`alertgroup_id`", "#{userAlertGroup.alertgroupId}");
                VALUES("`user_id`", "#{userAlertGroup.userId}");
                VALUES("`create_time`", "#{userAlertGroup.createTime}");
                VALUES("`update_time`", "#{userAlertGroup.updateTime}");

            }
        }.toString();
    }

    /**
     * query user list by alert group id
     *
     * @param parameter
     * @return
     */
    public String queryForUser(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("u.*");

            FROM(TABLE_NAME + " g_u");

            JOIN("t_escheduler_user u on g_u.user_id = u.id");

            WHERE("g_u.alertgroup_id = #{alertgroupId}");
        }}.toString();
    }

    /**
     * delete by alert group id
     * @param parameter
     * @return
     */
    public String deleteByAlertgroupId(Map<String, Object> parameter) {
        return new SQL() {{
            DELETE_FROM(TABLE_NAME);

            WHERE("`alertgroup_id` = #{alertgroupId}");
        }}.toString();
    }

    /**
     * list user information by alert group id
     *
     * @param parameter
     * @return
     */
    public String listUserByAlertgroupId(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("u.*");

            FROM(TABLE_NAME + " g_u");

            JOIN("t_escheduler_user u on g_u.user_id = u.id");

            WHERE("g_u.alertgroup_id = #{alertgroupId}");
        }}.toString();
    }

}
