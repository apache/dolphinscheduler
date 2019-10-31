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
package org.apache.dolphinscheduler.dao;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.dao.datasource.ConnectionFactory;
import org.apache.dolphinscheduler.dao.entity.MonitorRecord;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.dolphinscheduler.dao.utils.MysqlPerformance;
import org.apache.dolphinscheduler.dao.utils.PostgrePerformance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
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
            conf = new PropertiesConfiguration(Constants.APPLICATION_PROPERTIES);
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
        String url =  conf.getString(Constants.SPRING_DATASOURCE_URL);
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

    public static MonitorRecord getCurrentDbPerformance(){
        MonitorRecord monitorRecord = null;
        Connection conn = null;
        try{
            DruidDataSource dataSource = ConnectionFactory.getDataSource();
            dataSource.setInitialSize(2);
            dataSource.setMinIdle(2);
            dataSource.setMaxActive(2);
            conn = dataSource.getConnection();
            if(conn == null){
                return monitorRecord;
            }
            if(conf.getString(Constants.SPRING_DATASOURCE_DRIVER_CLASS_NAME).contains(DbType.MYSQL.toString().toLowerCase())) {
                return new MysqlPerformance().getMonitorRecord(conn);
            } else if(conf.getString(Constants.SPRING_DATASOURCE_DRIVER_CLASS_NAME).contains(DbType.POSTGRESQL.toString().toLowerCase())){
                return new PostgrePerformance().getMonitorRecord(conn);
            }
        }catch (Exception e) {
            logger.error("SQLException " + e);
        }finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("SQLException ", e);
            }
        }
        return monitorRecord;
    }

    /**
     * query database state
     * @return
     */
    public static List<MonitorRecord> queryDatabaseState() {
        List<MonitorRecord> list = new ArrayList<>(1);

        MonitorRecord monitorRecord = getCurrentDbPerformance();
        if(monitorRecord != null){
            list.add(monitorRecord);
        }
        return list;
    }
}
