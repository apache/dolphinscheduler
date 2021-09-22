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
import java.util.HashMap;
import java.util.Map;

public class WorkerGroupDao {

    public static final Logger logger = LoggerFactory.getLogger(WorkerGroupDao.class);

    /**
     * query all old worker group
     * @param conn jdbc connection
     * @return old worker group Map
     */
    public Map<Integer,String> queryAllOldWorkerGroup(Connection conn){
        Map<Integer,String> workerGroupMap = new HashMap<>();

        String sql = String.format("select id,name from t_ds_worker_group");
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()){
                int id = rs.getInt(1);
                String name = rs.getString(2);
                workerGroupMap.put(id,name);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException("sql: " + sql, e);
        } finally {
            ConnectionUtils.releaseResource(rs, pstmt, conn);
        }

        return workerGroupMap;
    }
}
