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

package org.apache.dolphinscheduler.plugin.task.sql;

import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.hive.param.HiveConnectionParam;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SqlParameters;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hive sql listener test
 */
public class HiveSqlLogThreadTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HiveSqlLogThreadTest.class);
    @Test
    public void testHiveSql() throws IOException {
        String taskJson="{\"type\":\"HIVE\",\"datasource\":1,\"sql\":\"select count(*) from tmp.test_doris\",\"udfs\":\"\",\"sqlType\":\"0\",\"sendEmail\":false,\"displayRows\":10,\"title\":\"\",\"groupId\":null,\"localParams\":[],\"connParams\":\"\",\"preStatements\":[],\"postStatements\":[],\"dependence\":{},\"conditionResult\":{\"successNode\":[],\"failedNode\":[]},\"waitStartTimeout\":{},\"switchResult\":{}}";
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskType("hive");
        taskExecutionContext.setTaskParams(taskJson);
        SqlParameters sqlParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SqlParameters.class);
        assert sqlParameters != null;
        String sql = sqlParameters.getSql();

        String krb5FilePath ="/etc/krb5.conf";
        String krb5KeyTabPath ="/etc/vulcan.keytab";
        System.setProperty("java.security.krb5.conf", krb5FilePath);
        System.setProperty("sun.security.krb5.debug", "true");
        org.apache.hadoop.conf.Configuration configuration = new org.apache.hadoop.conf.Configuration();
        configuration.set("hadoop.security.authentication", "Kerberos");
        configuration.set("keytab.file", krb5KeyTabPath);
        configuration.set("kerberos.principal", "vulcan@BJ.BAIDU.COM");

        UserGroupInformation.setConfiguration(configuration);
        UserGroupInformation.loginUserFromKeytab("vulcan", krb5KeyTabPath);

        // create hive connection
        HiveConnectionParam connectionParam = new HiveConnectionParam();
        connectionParam.setUser("hive");
        connectionParam.setDriverClassName("org.apache.hive.jdbc.HiveDriver");
        connectionParam.setAddress("jdbc:hive2://bjbd-test-bigdata-hive-001:10000");
        connectionParam.setJdbcUrl("jdbc:hive2://bjbd-test-bigdata-hive-001:10000/default;principal=hive/_HOST@BJ.BAIDU.COM");
        connectionParam.setPrincipal("hive/_HOST@BJ.BAIDU.COM");
        connectionParam.setDatabase("default");
        BaseConnectionParam baseConnectionParam = (BaseConnectionParam) DataSourceUtils.buildConnectionParams(
            DbType.valueOf(sqlParameters.getType()),JSONUtils.toJsonString(connectionParam)
            );



        ResultSet res = null;
        Connection con = null;
        Statement stmt = null;
        try{
            con = DataSourceClientProvider.getInstance().getConnection(DbType.valueOf(sqlParameters.getType()), baseConnectionParam);

            stmt = con.createStatement();
            //print process log
            HiveSqlLogThread queryThread = new HiveSqlLogThread(stmt,LOGGER,taskExecutionContext);
            queryThread.setName("sql log print");
            queryThread.start();
            res = stmt.executeQuery(sql);
            while (res.next()) {
                LOGGER.info("---------{}--------",res.getInt(1));
            }

        }catch (Exception e){
            LOGGER.error(sql+" query failed" , e);
        }

    }
}
