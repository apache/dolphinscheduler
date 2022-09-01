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

import org.apache.hive.jdbc.HivePreparedStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HiveSqlLogThreadTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HiveSqlLogThreadTest.class);

    private static TaskExecutionContext taskExecutionContext;

    @Test
    public void testHiveSql() throws InterruptedException, SQLException {
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskType("hive");
        taskExecutionContext.setFirstSubmitTime(new Date());
        taskExecutionContext.setProcessDefineCode(1L);
        taskExecutionContext.setProcessDefineVersion(1);
        taskExecutionContext.setProcessInstanceId(1);
        taskExecutionContext.setTaskInstanceId(1);

        List<String> mockLog = new ArrayList<>();
        mockLog.add("1start hive sql log\napplication_1231_2323");
        PreparedStatement statement = PowerMockito.mock(PreparedStatement.class);
        HivePreparedStatement hivePreparedStatement = PowerMockito.mock(HivePreparedStatement.class);
        PowerMockito.when(statement.unwrap(HivePreparedStatement.class)).thenReturn(hivePreparedStatement);
        PowerMockito.when(hivePreparedStatement.isClosed()).thenReturn(false);
        PowerMockito.when(hivePreparedStatement.hasMoreLogs()).thenReturn(true);
        PowerMockito.when(hivePreparedStatement.getQueryLog(true, 500)).thenReturn(mockLog);

        HiveSqlLogThread queryThread = PowerMockito.spy(new HiveSqlLogThread(statement, LOGGER, taskExecutionContext));
        queryThread.start();

        Thread.sleep(5000);
        Assert.assertEquals(taskExecutionContext.getAppIds(), "application_1231_2323");

    }
}
