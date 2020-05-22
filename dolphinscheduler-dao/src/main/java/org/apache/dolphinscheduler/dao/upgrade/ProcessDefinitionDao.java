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

package org.apache.dolphinscheduler.dao.upgrade;

import org.apache.dolphinscheduler.common.utils.ConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ProcessDefinitionDao {


    public static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionDao.class);

    /**
     * queryAllProcessDefinition
     * @param conn jdbc connection
     * @return ProcessDefinition Json List
     */
    public Map<Integer,String> queryAllProcessDefinition(Connection conn){

        Map<Integer,String> processDefinitionJsonMap = new HashMap<>();

        String sql = String.format("SELECT id,process_definition_json FROM t_ds_process_definition");
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()){
                Integer id = rs.getInt(1);
                String processDefinitionJson = rs.getString(2);
                processDefinitionJsonMap.put(id,processDefinitionJson);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException("sql: " + sql, e);
        } finally {
            ConnectionUtils.releaseResource(rs, pstmt, conn);
        }

        return processDefinitionJsonMap;
    }


    /**
     * updateProcessDefinitionJson
     * @param conn jdbc connection
     * @param processDefinitionJsonMap processDefinitionJsonMap
     */
    public void updateProcessDefinitionJson(Connection conn,Map<Integer,String> processDefinitionJsonMap){
        String sql = "UPDATE t_ds_process_definition SET process_definition_json=? where id=?";
        try {
            for (Map.Entry<Integer, String> entry : processDefinitionJsonMap.entrySet()){
                try(PreparedStatement pstmt= conn.prepareStatement(sql)) {
                    pstmt.setString(1,entry.getValue());
                    pstmt.setInt(2,entry.getKey());
                    pstmt.executeUpdate();
                }

            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException("sql: " + sql, e);
        } finally {
            ConnectionUtils.releaseResource(conn);
        }
    }
}
