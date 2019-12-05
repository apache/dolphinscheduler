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

public class MasterServerMapperProvider {

    private static final String TABLE_NAME = "t_escheduler_master_server";

    /**
     *
     * @return
     */
    public String queryAllMaster() {
        return new SQL() {{
            SELECT("*");

            FROM(TABLE_NAME);
        }}.toString();
    }

    /**
     *
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {{
            INSERT_INTO(TABLE_NAME);

            VALUES("host", "#{masterServer.host}");
            VALUES("port", "#{masterServer.port}");
            VALUES("zk_directory", "#{masterServer.zkDirectory}");
            VALUES("res_info", "#{masterServer.resInfo}");
            VALUES("create_time", "#{masterServer.createTime}");
            VALUES("last_heartbeat_time", "#{masterServer.lastHeartbeatTime}");
        }}.toString();
    }

    /**
     *
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {{
            UPDATE(TABLE_NAME);

            SET("last_heartbeat_time = #{masterServer.lastHeartbeatTime}");
            SET("port = #{masterServer.port}");
            SET("res_info = #{masterServer.resInfo}");

            WHERE("host = #{masterServer.host}");
        }}.toString();
    }

    /**
     *
     * @return
     */
    public String delete() {
        return new SQL() {{
            DELETE_FROM(TABLE_NAME);
        }}.toString();
    }

    /**
     *
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
