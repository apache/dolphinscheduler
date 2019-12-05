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

import cn.escheduler.common.enums.AlertType;
import cn.escheduler.common.utils.EnumFieldUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * alert group mapper provider
 */
public class AlertGroupMapperProvider {

    private static final String TABLE_NAME = "t_escheduler_alertgroup";

    /**
     * insert one alert group
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {
            {
                INSERT_INTO(TABLE_NAME);
                VALUES("`group_name`", "#{alertGroup.groupName}");
                VALUES("`group_type`", EnumFieldUtil.genFieldStr("alertGroup.groupType", AlertType.class));
                VALUES("`desc`", "#{alertGroup.desc}");
                VALUES("`create_time`", "#{alertGroup.createTime}");
                VALUES("`update_time`", "#{alertGroup.updateTime}");
            }
        }.toString();
    }

    /**
     * delete alert group by id
     * @param parameter
     * @return
     */
    public String delete(Map<String, Object> parameter) {
        return new SQL() {
            {
                DELETE_FROM(TABLE_NAME);

                WHERE("`id`=#{id}");
            }
        }.toString();
    }

    /**
     * update alert group
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);

                SET("`group_name`=#{alertGroup.groupName}");
                SET("`group_type`="+EnumFieldUtil.genFieldStr("alertGroup.groupType", AlertType.class));
                SET("`desc`=#{alertGroup.desc}");
                SET("`update_time`=#{alertGroup.updateTime}");

                WHERE("`id`=#{alertGroup.id}");
            }
        }.toString();
    }


    /**
     * query alert group by id
     * @param parameter
     * @return
     */
    public String queryById(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);

                WHERE("`id` = #{alertGroupId}");
            }
        }.toString();
    }

    /**
     * query all alert group list
     * @param parameter
     * @return
     */
    public String queryAllGroupList(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);

                ORDER_BY( "update_time desc");

            }
        }.toString();
    }

    /**
     * count alert group by search key
     * @param parameter
     * @return
     */
    public String countAlertGroupPaging(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("count(0)");

            FROM(TABLE_NAME);

            Object searchVal = parameter.get("searchVal");
            if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                WHERE( " group_name like concat('%', #{searchVal}, '%') ");
            }
        }}.toString();
    }

    /**
     * query alert group list paging by search key
     * @param parameter
     * @return
     */
    public String queryAlertGroupPaging(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");
                FROM(TABLE_NAME);
                Object searchVal = parameter.get("searchVal");
                if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                    WHERE( " group_name like concat('%', #{searchVal}, '%') ");
                }
                ORDER_BY(" update_time desc limit #{offset},#{pageSize} ");
            }
        }.toString();

    }


    /**
     * query alert group by user id
     * @param parameter
     * @return
     */
    public String queryByUserId(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("g.*");

                FROM(TABLE_NAME + " g,t_escheduler_relation_user_alertgroup rel");

                WHERE("rel.alertgroup_id = g.id and rel.user_id = #{userId}");
            }
        }.toString();
    }


    /**
     * query alert group by name
     * @param parameter
     * @return
     */
    public String queryByGroupName(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");
                FROM(TABLE_NAME);
                WHERE("group_name = #{groupName}");
            }
        }.toString();
    }

}
