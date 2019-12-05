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

import cn.escheduler.common.enums.UdfType;
import cn.escheduler.common.utils.EnumFieldUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * und function mapper
 */
public class UdfFuncMapperProvider {
    private final String TABLE_NAME = "t_escheduler_udfs";
    public static final String USER_UDFS_RELATION_TABLE_NAME = "t_escheduler_relation_udfs_user";

    /**
     * insert udf function
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {
            {
                INSERT_INTO(TABLE_NAME);
                VALUES("`user_id`", "#{udf.userId}");
                VALUES("`func_name`", "#{udf.funcName,jdbcType=VARCHAR}");
                VALUES("`class_name`", "#{udf.className}");
                VALUES("`arg_types`", "#{udf.argTypes}");
                VALUES("`database`", "#{udf.database}");
                VALUES("`desc`", "#{udf.desc}");
                VALUES("`resource_id`", "#{udf.resourceId}");
                VALUES("`resource_name`", "#{udf.resourceName}");
                VALUES("`type`", EnumFieldUtil.genFieldStr("udf.type", UdfType.class));
                VALUES("`create_time`", "#{udf.createTime}");
                VALUES("`update_time`", "#{udf.updateTime}");
            }
        }.toString();
    }

    /**
     * update udf function
     *
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);

                SET("`user_id`=#{udf.userId}");
                SET("`func_name`=#{udf.funcName}");
                SET("`class_name`=#{udf.className}");
                SET("`arg_types`=#{udf.argTypes}");
                SET("`database`=#{udf.database}");
                SET("`desc`=#{udf.desc}");
                SET("`resource_id`=#{udf.resourceId}");
                SET("`resource_name`=#{udf.resourceName}");
                SET("`type`="+EnumFieldUtil.genFieldStr("udf.type", UdfType.class));
                SET("`update_time`=#{udf.updateTime}");

                WHERE("`id`=#{udf.id}");
            }
        }.toString();
    }

    /**
     * query udf function by id
     *
     * @param parameter
     * @return
     */
    public String queryUdfById(Map<String, Object> parameter) {

        return new SQL() {{
            SELECT("r.*");

            FROM(TABLE_NAME + " r");
            WHERE("r.id = #{id}");
        }}.toString();
    }

    /**
     * query udf list by id string
     *
     * @param parameter
     * @return
     */
    public String queryUdfByIdStr(Map<String, Object> parameter) {

        String ids = (String) parameter.get("ids");

        return new SQL() {{
            SELECT("*");

            FROM(TABLE_NAME);
            WHERE("`id` in (" + ids +")");
            ORDER_BY("`id` asc");
        }}.toString();
    }

    /**
     * query all udf function paging
     * @param parameter
     * @return
     */
    public String queryUdfFuncPaging(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("*");
            FROM(TABLE_NAME );
            WHERE("id in (select udf_id from "+USER_UDFS_RELATION_TABLE_NAME+" where user_id=#{userId} union select id as udf_id  from "+TABLE_NAME+" where user_id=#{userId})");
            Object searchVal = parameter.get("searchVal");
            if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                WHERE( " name like concat('%', #{searchVal}, '%') ");
            }
            ORDER_BY("create_time desc limit #{offset},#{pageSize} ");

        }}.toString();
    }

    /**
     * query all udf function paging
     * @param parameter
     * @return
     */
    public String queryAllUdfFuncPaging(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("*");
            FROM(TABLE_NAME );
            Object searchVal = parameter.get("searchVal");
            if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                WHERE( " name like concat('%', #{searchVal}, '%') ");
            }
            ORDER_BY("create_time desc limit #{offset},#{pageSize} ");

        }}.toString();
    }

    /**
     * count udf number by user id
     *
     * @param parameter
     * @return
     */
    public String countUserUdfFunc(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("count(0)");
            FROM(TABLE_NAME);
            WHERE("id in (select udf_id from "+USER_UDFS_RELATION_TABLE_NAME+" where user_id=#{userId} union select id as udf_id  from "+TABLE_NAME+" where user_id=#{userId})");
        }}.toString();
    }

    /**
     * count udf number
     *
     * @param parameter
     * @return
     */
    public String countAllUdfFunc(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("count(0)");
            FROM(TABLE_NAME);
        }}.toString();
    }

    /**
     * query udf function by type
     * @param parameter
     * @return
     */
    public String getUdfFuncByType(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("*");
            FROM(TABLE_NAME );
            WHERE("type = #{type}");
            WHERE("id in (select udf_id from "+USER_UDFS_RELATION_TABLE_NAME+" where user_id=#{userId} union select id as udf_id  from "+TABLE_NAME+" where user_id=#{userId})");
        }}.toString();
    }

    /**
     * query udf function by name
     * @param parameter
     * @return
     */
    public String queryUdfFuncByName(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("*");
            FROM(TABLE_NAME );
            WHERE("func_name = #{func_name}");
        }}.toString();
    }

    /**
     * delete udf function
     *
     * @param parameter
     * @return
     */
    public String delete(Map<String, Object> parameter) {
        return new SQL() {
            {
                DELETE_FROM(TABLE_NAME);

                WHERE("`id` = #{udfFuncId}");
            }
        }.toString();
    }

    /**
     *
     * query udf function authorized to user
     * @param parameter
     * @return
     */
    public String authedUdfFunc(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("u.*");
            FROM(TABLE_NAME + " u,t_escheduler_relation_udfs_user rel");
            WHERE(" u.id = rel.udf_id AND rel.user_id = #{userId}");
        }}.toString();
    }

    /**
     * query udf function except user
     *
     * @param parameter
     * @return
     */
    public String queryUdfFuncExceptUserId(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("*");
            FROM(TABLE_NAME);
            WHERE("user_id <> #{userId}");
        }}.toString();
    }
}
