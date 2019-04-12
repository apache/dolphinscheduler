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
 * queue mapper provider
 */
public class QueueMapperProvider {

    private static final String TABLE_NAME = "t_escheduler_queue";

    /**
     * insert queue
     *
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {
            {
                INSERT_INTO(TABLE_NAME);
                VALUES("`queue_name`", "#{queue.queueName}");
                VALUES("`queue`", "#{queue.queue}");
                VALUES("`create_time`", "#{queue.createTime}");
                VALUES("`update_time`", "#{queue.updateTime}");
            }
        }.toString();
    }

    /**
     * delete queue
     *
     * @param parameter
     * @return
     */
    public String delete(Map<String, Object> parameter) {
        return new SQL() {
            {
                DELETE_FROM(TABLE_NAME);

                WHERE("`id`=#{queueId}");
            }
        }.toString();
    }

    /**
     * update queue
     *
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);

                SET("`queue_name`=#{queue.queueName}");
                SET("`queue`=#{queue.queue}");
                SET("`update_time`=#{queue.updateTime}");

                WHERE("`id`=#{queue.id}");
            }
        }.toString();
    }


    /**
     * query queue by id
     *
     * @param parameter
     * @return
     */
    public String queryById(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);

                WHERE("`id` = #{queueId}");
            }
        }.toString();
    }

    /**
     * query all queue list
     * @param parameter
     * @return
     */
    public String queryAllQueue(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);
            }
        }.toString();
    }

    /**
     * count queue by search value
     * @param parameter
     * @return
     */
    public String countQueuePaging(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("count(0)");
            FROM(TABLE_NAME);
            Object searchVal = parameter.get("searchVal");
            if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                WHERE( " queue_name like concat('%', #{searchVal}, '%') ");
            }
        }}.toString();
    }

    /**
     * query tenant list paging
     * @param parameter
     * @return
     */
    public String queryQueuePaging(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");
                FROM(TABLE_NAME);
                Object searchVal = parameter.get("searchVal");
                if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                    WHERE( " queue_name like concat('%', #{searchVal}, '%') ");
                }
                ORDER_BY(" update_time desc limit #{offset},#{pageSize} ");
            }
        }.toString();

    }

    /**
     * query by queue
     *
     * @param parameter
     * @return
     */
    public String queryByQueue(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);

                WHERE("`queue` = #{queue}");
            }
        }.toString();
    }

    /**
     * query by queue name
     *
     * @param parameter
     * @return
     */
    public String queryByQueueName(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);

                WHERE("`queue_name` = #{queueName}");
            }
        }.toString();
    }

}
