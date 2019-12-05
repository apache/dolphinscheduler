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

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * access token mapper provider
 *
 */
public class AccessTokenMapperProvider {

    private static final String TABLE_NAME = "t_escheduler_access_token";

    /**
     * insert accessToken
     *
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {
            {
                INSERT_INTO(TABLE_NAME);
                VALUES("`user_id`", "#{accessToken.userId}");
                VALUES("`token`", "#{accessToken.token}");
                VALUES("`expire_time`", "#{accessToken.expireTime}");;
                VALUES("`create_time`", "#{accessToken.createTime}");
                VALUES("`update_time`", "#{accessToken.updateTime}");
            }
        }.toString();
    }

    /**
     * delete accessToken
     *
     * @param parameter
     * @return
     */
    public String delete(Map<String, Object> parameter) {
        return new SQL() {
            {
                DELETE_FROM(TABLE_NAME);

                WHERE("`id`=#{accessTokenId}");
            }
        }.toString();
    }

    /**
     * update accessToken
     *
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);

                SET("`user_id`=#{accessToken.userId}");
                SET("`token`=#{accessToken.token}");
                SET("`expire_time`=#{accessToken.expireTime}");
                SET("`update_time`=#{accessToken.updateTime}");

                WHERE("`id`=#{accessToken.id}");
            }
        }.toString();
    }


    /**
     * count user number by search value
     * @param parameter
     * @return
     */
    public String countAccessTokenPaging(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("count(0)");
            FROM(TABLE_NAME + " t,t_escheduler_user u");
            Object searchVal = parameter.get("searchVal");
            WHERE("u.id = t.user_id");
            if(parameter.get("userId") != null && (int)parameter.get("userId") != 0){
                WHERE(" u.id = #{userId}");
            }
            if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                WHERE(" u.user_name like concat('%', #{searchVal}, '%')");
            }
        }}.toString();
    }

    /**
     * query user list paging
     * @param parameter
     * @return
     */
    public String queryAccessTokenPaging(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("t.*,u.user_name");
                FROM(TABLE_NAME + " t,t_escheduler_user u");
                Object searchVal = parameter.get("searchVal");
                WHERE("u.id = t.user_id");
                if(parameter.get("userId") != null && (int)parameter.get("userId") != 0){
                    WHERE(" u.id = #{userId}");
                }
                if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                    WHERE(" u.user_name like concat('%', #{searchVal}, '%') ");
                }
                ORDER_BY(" t.update_time desc limit #{offset},#{pageSize} ");
            }
        }.toString();

    }




}
