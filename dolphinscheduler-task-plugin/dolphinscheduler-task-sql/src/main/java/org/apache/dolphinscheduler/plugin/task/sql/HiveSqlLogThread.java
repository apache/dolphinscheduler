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

import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.hive.jdbc.HiveStatement;
import org.slf4j.Logger;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


/**
 * hive log listener thread
 */
public class HiveSqlLogThread extends Thread {
    /**
     * hive statement
     */
    private final HiveStatement statement;
    /**
     * logger
     */
    private final Logger threadLogger;

    private final TaskExecutionContext taskExecutionContext;

    public HiveSqlLogThread(Statement statement, Logger logger, TaskExecutionContext taskExecutionContext) {
        this.statement = (HiveStatement) statement;
        this.threadLogger = logger;
        this.taskExecutionContext = taskExecutionContext;
    }
    @Override
    public void run() {
        if (statement == null) {
            threadLogger.info("hive statement is null,end this log query!");
            return;
        }
        try {
            while (!statement.isClosed() && statement.hasMoreLogs()) {
                for (String log: statement.getQueryLog(true,500)) {

                    //hive mapreduce log
                    threadLogger.info(log);

                    List<String> appIds = LoggerUtils.getAppIds(log, threadLogger);


                    //get sql task yarn's application_id
                    if (!appIds.isEmpty()) {
                        taskExecutionContext.setAppIds(String.join(",", appIds));
                    }
                }
            }
        } catch (SQLException e) {
            threadLogger.error("Failed to view hive log,exception:[{}]",e.getMessage());
        }
    }
}
