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
package cn.escheduler.dao;

import cn.escheduler.common.Constants;
import cn.escheduler.dao.model.MonitorRecord;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * database state dao
 */
public class MonitorDBDao {

    private static Logger logger = LoggerFactory.getLogger(MonitorDBDao.class);
    public static final String VARIABLE_NAME = "variable_name";

    /**
     * 加载配置文件
     */
    private static Configuration conf;

    static {
        try {
            conf = new PropertiesConfiguration(Constants.DATA_SOURCE_PROPERTIES);
        }catch (ConfigurationException e){
            logger.error("load configuration excetpion",e);
            System.exit(1);
        }
    }

    /**
     * create connection
     * @return
     */
    private static Connection getConn() {
        String url = conf.getString(Constants.SPRING_DATASOURCE_URL);
        String username = conf.getString(Constants.SPRING_DATASOURCE_USERNAME);
        String password = conf.getString(Constants.SPRING_DATASOURCE_PASSWORD);
        Connection conn = null;
        try {
            //classloader,load driver
            Class.forName(Constants.JDBC_MYSQL_CLASS_NAME);
            conn = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            logger.error("ClassNotFoundException ", e);
        } catch (SQLException e) {
            logger.error("SQLException ", e);
        }
        return conn;
    }


    /**
     * query database state
     * @return
     */
    public static List<MonitorRecord> queryDatabaseState() {
        List<MonitorRecord> list = new ArrayList<>(1);

        Connection conn = null;
        long maxConnections = 0;
        long maxUsedConnections = 0;
        long threadsConnections = 0;
        long threadsRunningConnections = 0;
        //mysql running state
        int state = 1;


        MonitorRecord monitorRecord = new MonitorRecord();
        try {
            conn = getConn();
            if(conn == null){
                return list;
            }

            Statement pstmt = conn.createStatement();

            ResultSet rs1 = pstmt.executeQuery("show global variables");
            while(rs1.next()){
                if(rs1.getString(VARIABLE_NAME).toUpperCase().equals("MAX_CONNECTIONS")){
                    maxConnections= Long.parseLong(rs1.getString("value"));
                }
            }

            ResultSet rs2 = pstmt.executeQuery("show global status");
            while(rs2.next()){
                if(rs2.getString(VARIABLE_NAME).toUpperCase().equals("MAX_USED_CONNECTIONS")){
                    maxUsedConnections = Long.parseLong(rs2.getString("value"));
                }else if(rs2.getString(VARIABLE_NAME).toUpperCase().equals("THREADS_CONNECTED")){
                    threadsConnections = Long.parseLong(rs2.getString("value"));
                }else if(rs2.getString(VARIABLE_NAME).toUpperCase().equals("THREADS_RUNNING")){
                    threadsRunningConnections= Long.parseLong(rs2.getString("value"));
                }
            }


        } catch (SQLException e) {
            logger.error("SQLException ", e);
            state = 0;
        }finally {
            try {
                if(conn != null){
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("SQLException ", e);
            }
        }

        monitorRecord.setDate(new Date());
        monitorRecord.setMaxConnections(maxConnections);
        monitorRecord.setMaxUsedConnections(maxUsedConnections);
        monitorRecord.setThreadsConnections(threadsConnections);
        monitorRecord.setThreadsRunningConnections(threadsRunningConnections);
        monitorRecord.setState(state);

        list.add(monitorRecord);

        return list;
    }
}
