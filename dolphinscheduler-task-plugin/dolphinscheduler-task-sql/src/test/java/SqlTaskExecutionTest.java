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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SqlParameters;
import org.apache.dolphinscheduler.plugin.task.sql.HiveSqlLogThread;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * sql task execution test
 */
public class SqlTaskExecutionTest {

    public static Connection con = null;
    public static Statement stmt = null;
    @Test
    public void hiveLogListener() throws IOException, ClassNotFoundException {
        String taskJson="{\"type\":\"HIVE\",\"datasource\":1,\"sql\":\"select * from tmp.test_doris limit 10\",\"udfs\":\"\",\"sqlType\":\"0\",\"sendEmail\":false,\"displayRows\":10,\"title\":\"\",\"groupId\":null,\"localParams\":[],\"connParams\":\"\",\"preStatements\":[],\"postStatements\":[],\"dependence\":{},\"conditionResult\":{\"successNode\":[],\"failedNode\":[]},\"waitStartTimeout\":{},\"switchResult\":{}}";
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskType("hive");
        taskExecutionContext.setTaskParams(taskJson);
        SqlParameters sqlParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SqlParameters.class);
        String type = sqlParameters.getType();
        String sql = sqlParameters.getSql();
        System.out.println(type);

        // hive connection

        String url= "jdbc:hive2://127.0.0.1:10000";
        Class.forName("org.apache.hive.jdbc.HiveDriver");
        ResultSet res = null;
        try{
            if(con == null) {
                con = DriverManager.getConnection(url);
            }
            if(stmt == null) {
                stmt = con.createStatement();
            }
            HiveSqlLogThread queryThread = new HiveSqlLogThread(stmt, LoggerFactory.getLogger("log test"), taskExecutionContext);
            queryThread.setName("sql log print");
            queryThread.start();
            res = stmt.executeQuery(sql);
        }catch (Exception e){
           e.printStackTrace();
        }

    }
}
