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
 * process instance map mapper provider
 */
public class ProcessInstanceMapMapperProvider {

    private static final String TABLE_NAME = "t_escheduler_relation_process_instance";

    /**
     * insert process instance relation
     *
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {
            {
                INSERT_INTO(TABLE_NAME);
                VALUES("`parent_process_instance_id`", "#{processInstanceMap.parentProcessInstanceId}");
                VALUES("`parent_task_instance_id`", "#{processInstanceMap.parentTaskInstanceId}");
                VALUES("`process_instance_id`", "#{processInstanceMap.processInstanceId}");
            }
        }.toString();
    }

    /**
     * delete process instance relation
     *
     * @param parameter
     * @return
     */
    public String delete(Map<String, Object> parameter) {
        return new SQL() {
            {
                DELETE_FROM(TABLE_NAME);

                WHERE("`id`=#{processInstanceMapId}");
            }
        }.toString();
    }

    /**
     * delete by parent process id
     *
     * @param parameter
     * @return
     */
    public String deleteByParentProcessId(Map<String, Object> parameter) {
        return new SQL() {
            {
                DELETE_FROM(TABLE_NAME);

                WHERE("`parent_process_instance_id`=#{parentProcessId}");
            }
        }.toString();
    }

    /**
     * update process map
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);

                SET("`parent_process_instance_id`=#{processInstanceMap.parentProcessInstanceId}");
                SET("`parent_task_instance_id`=#{processInstanceMap.parentTaskInstanceId}");
                SET("`process_instance_id`=#{processInstanceMap.processInstanceId}");
                WHERE("`id`=#{processInstanceMap.id}");
            }
        }.toString();
    }


    /**
     * query by map id
     *
     * @param parameter
     * @return
     */
    public String queryById(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");

                FROM(TABLE_NAME);

                WHERE("`id` = #{processMapId}");
            }
        }.toString();
    }

    /**
     * query by parent process instance id and parent task id
     *
     * @param parameter
     * @return
     */
    public String queryByParentId(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("*");
                FROM(TABLE_NAME);
                WHERE("`parent_process_instance_id` = #{parentProcessId}");
                WHERE("`parent_task_instance_id` = #{parentTaskId}");
            }
        }.toString();
    }

    /**
     * query by sub process instance id
     *
     * @param parameter
     * @return
     */
    public String queryBySubProcessId(Map<String, Object> parameter) {
         return new SQL() {
            {
                SELECT("*");
                FROM(TABLE_NAME);
                WHERE("`process_instance_id` = #{subProcessId}");
            }
        }.toString();
    }


}
