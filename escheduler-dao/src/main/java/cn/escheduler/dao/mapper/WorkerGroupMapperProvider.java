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
 * worker group mapper provider
 */
public class WorkerGroupMapperProvider {

    private static final String TABLE_NAME = "t_escheduler_worker_group";

    /**
     * query worker list
     * @return
     */
    public String queryAllWorkerGroup() {
        return new SQL() {{
            SELECT("*");

            FROM(TABLE_NAME);

            ORDER_BY("update_time desc");
        }}.toString();
    }

    /**
     * insert worker server
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {{
            INSERT_INTO(TABLE_NAME);

            VALUES("id", "#{workerGroup.id}");
            VALUES("name", "#{workerGroup.name}");
            VALUES("ip_list", "#{workerGroup.ipList}");
            VALUES("create_time", "#{workerGroup.createTime}");
            VALUES("update_time", "#{workerGroup.updateTime}");
        }}.toString();
    }

    /**
     * update worker group
     *
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {{
            UPDATE(TABLE_NAME);

            SET("name = #{workerGroup.name}");
            SET("ip_list = #{workerGroup.ipList}");
            SET("create_time = #{workerGroup.createTime}");
            SET("update_time = #{workerGroup.updateTime}");

            WHERE("id = #{workerGroup.id}");
        }}.toString();
    }

    /**
     * delete worker group by id
     * @param parameter
     * @return
     */
    public String deleteById(Map<String, Object> parameter) {
        return new SQL() {{
            DELETE_FROM(TABLE_NAME);

            WHERE("id = #{id}");
        }}.toString();
    }

     /**
     * query worker group by name
     * @param parameter
     * @return
     */
    public String queryWorkerGroupByName(Map<String, Object> parameter) {
        return new SQL() {{

            SELECT("*");
            FROM(TABLE_NAME);

            WHERE("name = #{name}");
        }}.toString();
    }

    /**
     * query worker group by id
     * @param parameter
     * @return
     */
    public String queryById(Map<String, Object> parameter) {
        return new SQL() {{

            SELECT("*");
            FROM(TABLE_NAME);

            WHERE("id = #{id}");
        }}.toString();
    }


/**
     * query worker group by id
     * @param parameter
     * @return
     */
    public String queryListPaging(Map<String, Object> parameter) {
        return new SQL() {{

            SELECT("*");
            FROM(TABLE_NAME);

            Object searchVal = parameter.get("searchVal");
            if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                WHERE( " name like concat('%', #{searchVal}, '%') ");
            }
            ORDER_BY(" update_time desc limit #{offset},#{pageSize} ");
        }}.toString();
    }

    /**
     * count worker group number by search value
     * @param parameter
     * @return
     */
    public String countPaging(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("count(0)");
            FROM(TABLE_NAME);
            Object searchVal = parameter.get("searchVal");
            if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                WHERE( " name like concat('%', #{searchVal}, '%') ");
            }
        }}.toString();
    }
}
