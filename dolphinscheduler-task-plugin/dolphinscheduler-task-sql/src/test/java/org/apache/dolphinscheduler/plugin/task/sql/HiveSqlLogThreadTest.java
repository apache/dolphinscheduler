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
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.hive.jdbc.HivePreparedStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HiveSqlLogThreadTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HiveSqlLogThreadTest.class);

    private static volatile TaskExecutionContext taskExecutionContext;

    @Test
    public void testHiveSql() throws Exception {
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskType("hive");
        taskExecutionContext.setTaskLogName("1-1-1-1-1");

        String sql = "select count(*) from test.table";

        List<String> mockLog = new ArrayList<>();
        mockLog.add("1start hive sql log\napplication_1231_2323");
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        HivePreparedStatement hivePreparedStatement = Mockito.mock(HivePreparedStatement.class);
        Mockito.when(statement.unwrap(HivePreparedStatement.class)).thenReturn(hivePreparedStatement);
        Mockito.when(hivePreparedStatement.isClosed()).thenReturn(false);
        Mockito.when(hivePreparedStatement.hasMoreLogs()).thenReturn(true);
        Mockito.when(hivePreparedStatement.getQueryLog(true, 500)).thenReturn(mockLog);
        LOGGER.info("log thread starting");
        HiveSqlLogThread queryThread = Mockito.spy(new HiveSqlLogThread(statement, LOGGER, taskExecutionContext));
        queryThread.start();


        Assert.assertTrue(queryThread.isAlive());

    }
}
