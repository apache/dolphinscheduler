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

public class WorkerServerMapperProvider {

    private static final String TABLE_NAME = "t_escheduler_worker_server";

    /**
     * query worker list
     * @return
     */
    public String queryAllWorker() {
        return new SQL() {{
            SELECT("*");

            FROM(TABLE_NAME);
        }}.toString();
    }

    /**
     * query worker list
     * @return
     */
    public String queryWorkerByHost(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("*");

            FROM(TABLE_NAME);

            WHERE("host = #{host}");
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

            VALUES("host", "#{workerServer.host}");
            VALUES("port", "#{workerServer.port}");
            VALUES("zk_directory", "#{workerServer.zkDirectory}");
            VALUES("res_info", "#{workerServer.resInfo}");
            VALUES("create_time", "#{workerServer.createTime}");
            VALUES("last_heartbeat_time", "#{workerServer.lastHeartbeatTime}");
        }}.toString();
    }

    /**
     * update worker
     *
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {{
            UPDATE(TABLE_NAME);

            SET("last_heartbeat_time = #{workerServer.lastHeartbeatTime}");
            SET("port = #{workerServer.port}");
            SET("res_info = #{workerServer.resInfo}");

            WHERE("host = #{workerServer.host}");
        }}.toString();
    }

    /**
     * delete work by host
     * @param parameter
     * @return
     */
    public String deleteWorkerByHost(Map<String, Object> parameter) {
        return new SQL() {{
            DELETE_FROM(TABLE_NAME);

            WHERE("host = #{host}");
        }}.toString();
    }


}
