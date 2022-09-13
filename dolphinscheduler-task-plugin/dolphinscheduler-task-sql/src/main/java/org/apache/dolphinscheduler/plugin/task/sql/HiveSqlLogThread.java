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
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.hive.jdbc.HivePreparedStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;


public class HiveSqlLogThread extends Thread {

    private final HivePreparedStatement statement;

    private final Logger hiveMapReduceLogger;

    private final TaskExecutionContext taskExecutionContext;

    public HiveSqlLogThread(PreparedStatement statement, Logger logger, TaskExecutionContext taskExecutionContext) throws SQLException {
        super.setDaemon(true);
        this.statement = statement.unwrap(HivePreparedStatement.class);
        this.hiveMapReduceLogger = logger;
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void run() {

        Thread.currentThread().setName(taskExecutionContext.getTaskLogName());

        if (statement == null) {
            hiveMapReduceLogger.info("hive statement is null, end this log query!");
            return;
        }
        try {
            while (!statement.isClosed() && statement.hasMoreLogs()) {
                for (String log : statement.getQueryLog(true, 500)) {

                    hiveMapReduceLogger.info(log);

                    List<String> appIds = LogUtils.getAppIds(log, hiveMapReduceLogger);
                    //get sql task yarn's application_id
                    if (!appIds.isEmpty() && StringUtils.isBlank(taskExecutionContext.getAppIds())) {
                        hiveMapReduceLogger.info("yarn application_id is {}",appIds);
                        taskExecutionContext.setAppIds(String.join(",", appIds));
                    }
                }
            }
        } catch (SQLException e) {
            hiveMapReduceLogger.error("Failed to view hive log");
        }

    }
}
