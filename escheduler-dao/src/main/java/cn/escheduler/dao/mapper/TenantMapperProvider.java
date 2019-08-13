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
 * tenant mapper provider
 */
public class TenantMapperProvider {

    private static final String TABLE_NAME = "t_escheduler_tenant";

    /**
     * insert tenant
     *
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {
            {
                INSERT_INTO(TABLE_NAME);
                VALUES("`tenant_code`", "#{tenant.tenantCode}");
                VALUES("`tenant_name`", "#{tenant.tenantName}");
                VALUES("`queue_id`", "#{tenant.queueId}");
                VALUES("`desc`", "#{tenant.desc}");
                VALUES("`create_time`", "#{tenant.createTime}");
                VALUES("`update_time`", "#{tenant.updateTime}");

            }
        }.toString();
    }

    /**
     * delete tenant
     *
     * @param parameter
     * @return
     */
    public String deleteById(Map<String, Object> parameter) {
        return new SQL() {
            {
                DELETE_FROM(TABLE_NAME);

                WHERE("`id`=#{id}");
            }
        }.toString();
    }

    /**
     * update tenant
     *
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);

                SET("`tenant_name`=#{tenant.tenantName}");
                SET("`tenant_code`=#{tenant.tenantCode}");
                SET("`desc`=#{tenant.desc}");
                SET("`queue_id`=#{tenant.queueId}");
                SET("`update_time`=#{tenant.updateTime}");

                WHERE("`id`=#{tenant.id}");
            }
        }.toString();
    }


    /**
     * query tenant by id
     *
     * @param parameter
     * @return
     */
    public String queryById(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("t.*,q.queue_name,q.queue");
                FROM(TABLE_NAME + " t,t_escheduler_queue q");
                WHERE(" t.queue_id = q.id");
                WHERE(" t.id = #{tenantId}");
            }
        }.toString();
    }

    /**
     * query tenant by code
     *
     * @param parameter
     * @return
     */
    public String queryByTenantCode(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);

                WHERE("`tenant_code` = #{tenantCode}");
            }
        }.toString();
    }


    /**
     * count tenant by search value
     * @param parameter
     * @return
     */
    public String countTenantPaging(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("count(0)");
            FROM(TABLE_NAME +" t,t_escheduler_queue q");
            WHERE( " t.queue_id = q.id");
            Object searchVal = parameter.get("searchVal");
            if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                WHERE( " tenant_name like concat('%', #{searchVal}, '%') ");
            }
        }}.toString();
    }

    /**
     * query tenant list paging
     * @param parameter
     * @return
     */
    public String queryTenantPaging(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("t.*,q.queue_name");
                FROM(TABLE_NAME +" t,t_escheduler_queue q");
                WHERE( " t.queue_id = q.id");
                Object searchVal = parameter.get("searchVal");
                if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                    WHERE( " t.tenant_name like concat('%', #{searchVal}, '%') ");
                }
                ORDER_BY(" t.update_time desc limit #{offset},#{pageSize} ");
            }
        }.toString();

    }

    /**
     * query all tenant list
     * @param parameter
     * @return
     */
    public String queryAllTenant(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");
                FROM(TABLE_NAME);
            }
        }.toString();

    }


}
