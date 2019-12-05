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
import cn.escheduler.common.utils.EnumFieldUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * und function mapper provider
 *
 */
public class UserMapperProvider {

    private static final String TABLE_NAME = "t_escheduler_user";

    /**
     * insert user
     *
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {
            {
                INSERT_INTO(TABLE_NAME);
                VALUES("`user_name`", "#{user.userName}");
                VALUES("`user_password`", "#{user.userPassword}");
                VALUES("`email`", "#{user.email}");
                VALUES("`phone`", "#{user.phone}");
                VALUES("`user_type`", EnumFieldUtil.genFieldStr("user.userType", UserType.class));
                VALUES("`tenant_id`", "#{user.tenantId}");
                VALUES("`queue`", "#{user.queue}");
                VALUES("`create_time`", "#{user.createTime}");
                VALUES("`update_time`", "#{user.updateTime}");
            }
        }.toString();
    }

    /**
     * delete user
     *
     * @param parameter
     * @return
     */
    public String delete(Map<String, Object> parameter) {
        return new SQL() {
            {
                DELETE_FROM(TABLE_NAME);

                WHERE("`id`=#{userId}");
            }
        }.toString();
    }

    /**
     * update user
     *
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);

                SET("`user_name`=#{user.userName}");
                SET("`user_password`=#{user.userPassword}");
                SET("`email`=#{user.email}");
                SET("`phone`=#{user.phone}");
                SET("`user_type`="+EnumFieldUtil.genFieldStr("user.userType", UserType.class));
                SET("`tenant_id`=#{user.tenantId}");
                SET("`queue`=#{user.queue}");
                SET("`create_time`=#{user.createTime}");
                SET("`update_time`=#{user.updateTime}");

                WHERE("`id`=#{user.id}");
            }
        }.toString();
    }

    /**
     * query user by id
     * @param parameter
     * @return
     */
    public String queryById(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);

                WHERE("`id` = #{userId}");
            }
        }.toString();
    }

    /**
     * query all user list
     *
     * @return
     */
    public String queryAllGeneralUsers() {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);

                WHERE("user_type = 1");
            }
        }.toString();
    }

    /**
     * query all user list
     *
     * @return
     */
    public String queryAllUsers() {
        return new SQL() {
            {
                SELECT("*");
                FROM(TABLE_NAME);
            }
        }.toString();
    }



    /**
     * check user name and password
     *
     * @param parameter
     * @return
     */
    public String queryForCheck(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");
                FROM(TABLE_NAME);
                WHERE("`user_name` = #{userName} AND `user_password` = #{userPassword}");
            }
        }.toString();
    }

    /**
     * query user by name
     *
     * @param parameter
     * @return
     */
    public String queryByUserName(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");
                FROM(TABLE_NAME);
                WHERE("`user_name` = #{userName}");
            }
        }.toString();
    }

    /**
     * count user number by search value
     * @param parameter
     * @return
     */
    public String countUserPaging(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("count(0)");
            FROM(TABLE_NAME);
            Object searchVal = parameter.get("searchVal");
            if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                WHERE( " user_name like concat('%', #{searchVal}, '%') ");
            }
        }}.toString();
    }

    /**
     * query user list paging
     * @param parameter
     * @return
     */
    public String queryUserPaging(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("u.id,u.user_name,u.user_password,u.user_type,u.email,u.phone,u.tenant_id,u.create_time,u.update_time,t.tenant_name," +
                        "case when u.queue <> '' then u.queue else q.queue_name end as queue," +
                        "q.queue_name");
                FROM(TABLE_NAME + " u ");
                LEFT_OUTER_JOIN("t_escheduler_tenant t on u.tenant_id = t.id");
                LEFT_OUTER_JOIN("t_escheduler_queue q on t.queue_id = q.id");
                Object searchVal = parameter.get("searchVal");
                if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                    WHERE( " u.user_name like concat('%', #{searchVal}, '%') ");
                }
                ORDER_BY(" u.update_time desc limit #{offset},#{pageSize} ");


            }
        }.toString();

    }

    /**
     * query detail by user id
     * @param parameter
     * @return
     */
    public String queryDetailsById(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("u.*, t.tenant_name," +
                        "case when u.queue <> '' then u.queue else q.queue_name end as queue_name");

                FROM(TABLE_NAME + " u,t_escheduler_tenant t,t_escheduler_queue q");

                WHERE("u.tenant_id = t.id and t.queue_id = q.id and u.id = #{id}");
            }
        }.toString();
    }


    /**
     * query user list by alert group id
     * @param parameter
     * @return
     */
    public String queryUserListByAlertGroupId(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("u.*");
                FROM(TABLE_NAME + " u,t_escheduler_relation_user_alertgroup rel");
                WHERE("u.id = rel.user_id AND u.user_type = 1 AND rel.alertgroup_id = #{alertgroupId}");
            }
        }.toString();
    }


    /**
     * query tenant code by user id
     * @param parameter
     * @return
     */
    public String queryTenantCodeByUserId(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("u.*,t.tenant_code");
                FROM(TABLE_NAME + " u,t_escheduler_tenant t");
                WHERE("u.tenant_id = t.id AND u.id = #{userId}");
            }
        }.toString();
    }


    /**
     * query tenant code by user id
     * @param parameter
     * @return
     */
    public String queryQueueByProcessInstanceId(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("queue");
                FROM(TABLE_NAME + " u,t_escheduler_process_instance p");
                WHERE("u.id = p.executor_id and p.id=#{processInstanceId}");
            }
        }.toString();
    }


    /**
     * query user by id
     * @param parameter
     * @return
     */
    public String queryUserByToken(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("u.*");
                FROM(TABLE_NAME + " u ,t_escheduler_access_token t");
                WHERE(" u.id = t.user_id and token=#{token} and t.expire_time > NOW()");
            }
        }.toString();
    }

}
