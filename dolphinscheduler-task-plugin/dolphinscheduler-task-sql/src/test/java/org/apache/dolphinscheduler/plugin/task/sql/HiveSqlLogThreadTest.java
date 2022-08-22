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
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.apache.hive.jdbc.HiveStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hive sql listener test
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.*"})
public class HiveSqlLogThreadTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HiveSqlLogThreadTest.class);

    @Test
    public void testHiveSql() throws SQLException {
        TaskExecutionContext taskExecutionContext = PowerMockito.mock(TaskExecutionContext.class);
        PowerMockito.when(taskExecutionContext.getTaskType()).thenReturn("hive");

        String sql = "select count(*) from test.table";

        List<String> mockLog = new ArrayList<>();
        mockLog.add("start hive sql log\napplication_1231_2323");
        HiveStatement statement = PowerMockito.mock(HiveStatement.class);
        PowerMockito.when(statement.isClosed()).thenReturn(false);
        PowerMockito.when(statement.hasMoreLogs()).thenReturn(true);
        PowerMockito.when(statement.getQueryLog(true,500)).thenReturn(mockLog);
        try {
            //print process log
            HiveSqlLogThread queryThread = PowerMockito.spy(new HiveSqlLogThread(statement,LOGGER,taskExecutionContext));
            queryThread.start();

            Assert.assertEquals(taskExecutionContext.getTaskType(), "hive");

        } catch (Exception e) {
            LOGGER.error("query failed,sql is [{}]" ,sql);
        }

    }
}
