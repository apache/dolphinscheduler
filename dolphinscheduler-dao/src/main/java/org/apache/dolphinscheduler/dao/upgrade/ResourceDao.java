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
import java.util.HashMap;
import java.util.Map;

/**
 * resource dao
 */
public class ResourceDao {
    public static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionDao.class);

    /**
     * list all resources
     * @param conn connection
     * @return map that key is full_name and value is id
     */
    Map<String,Integer> listAllResources(Connection conn){
        Map<String,Integer> resourceMap = new HashMap<>();

        String sql = String.format("SELECT id,full_name FROM t_ds_resources");
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()){
                Integer id = rs.getInt(1);
                String fullName = rs.getString(2);
                resourceMap.put(fullName,id);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException("sql: " + sql, e);
        } finally {
            ConnectionUtils.releaseResource(rs, pstmt, conn);
        }

        return resourceMap;
    }

}
