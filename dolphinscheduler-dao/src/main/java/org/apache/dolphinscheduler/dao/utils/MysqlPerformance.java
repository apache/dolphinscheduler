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
package org.apache.dolphinscheduler.dao.utils;


import static org.apache.dolphinscheduler.dao.MonitorDBDao.VARIABLE_NAME;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.entity.MonitorRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * mysql performance
 */
public class MysqlPerformance extends BaseDBPerformance{

    private static Logger logger = LoggerFactory.getLogger(MysqlPerformance.class);


    /**
     * get monitor record
     * @param conn connection
     * @return MonitorRecord
     */
    @Override
    public MonitorRecord getMonitorRecord(Connection conn) {
        MonitorRecord monitorRecord = new MonitorRecord();
        monitorRecord.setDate(new Date());
        monitorRecord.setDbType(DbType.MYSQL);
        monitorRecord.setState(Flag.YES);
        Statement pstmt= null;
        try{
            pstmt = conn.createStatement();

            try (ResultSet rs1 = pstmt.executeQuery("show global variables")) {
                while(rs1.next()){
                    if(rs1.getString(VARIABLE_NAME).equalsIgnoreCase("MAX_CONNECTIONS")){
                        monitorRecord.setMaxConnections( Long.parseLong(rs1.getString("value")));
                    }
                }
            }

            try (ResultSet rs2 = pstmt.executeQuery("show global status")) {
                while(rs2.next()){
                    if(rs2.getString(VARIABLE_NAME).equalsIgnoreCase("MAX_USED_CONNECTIONS")){
                        monitorRecord.setMaxUsedConnections(Long.parseLong(rs2.getString("value")));
                    }else if(rs2.getString(VARIABLE_NAME).equalsIgnoreCase("THREADS_CONNECTED")){
                        monitorRecord.setThreadsConnections(Long.parseLong(rs2.getString("value")));
                    }else if(rs2.getString(VARIABLE_NAME).equalsIgnoreCase("THREADS_RUNNING")){
                        monitorRecord.setThreadsRunningConnections(Long.parseLong(rs2.getString("value")));
                    }
                }
            }
        }catch (Exception e) {
            monitorRecord.setState(Flag.NO);
            logger.error("SQLException ", e);
        }finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            }catch (SQLException e) {
                logger.error("SQLException ", e);
            }
        }
        return monitorRecord;
    }


}
