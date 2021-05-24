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

package org.apache.dolphinscheduler.server.worker.task;

import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.datax.DataxTask;
import org.apache.dolphinscheduler.server.worker.task.flink.FlinkTask;
import org.apache.dolphinscheduler.server.worker.task.http.HttpTask;
import org.apache.dolphinscheduler.server.worker.task.mr.MapReduceTask;
import org.apache.dolphinscheduler.server.worker.task.procedure.ProcedureTask;
import org.apache.dolphinscheduler.server.worker.task.python.PythonTask;
import org.apache.dolphinscheduler.server.worker.task.shell.ShellTask;
import org.apache.dolphinscheduler.server.worker.task.spark.SparkTask;
import org.apache.dolphinscheduler.server.worker.task.sql.SqlTask;
import org.apache.dolphinscheduler.server.worker.task.sqoop.SqoopTask;
import org.apache.dolphinscheduler.service.alert.AlertClientService;

import org.slf4j.Logger;

/**
 * task manager
 */
public class TaskManager {

    /**
     * create new task
     * @param taskExecutionContext  taskExecutionContext
     * @param logger    logger
     * @return AbstractTask
     * @throws IllegalArgumentException illegal argument exception
     */
    public static AbstractTask newTask(TaskExecutionContext taskExecutionContext, Logger logger, AlertClientService alertClientService) throws IllegalArgumentException {
        String taskType = taskExecutionContext.getTaskType();
        if (taskType == null) {
            logger.error("task type is null");
            throw new IllegalArgumentException("task type is null");
        }
        switch (taskType) {
            case "SHELL":
            case "WATERDROP":
                return new ShellTask(taskExecutionContext, logger);
            case "PROCEDURE":
                return new ProcedureTask(taskExecutionContext, logger);
            case "SQL":
                return new SqlTask(taskExecutionContext, logger, alertClientService);
            case "MR":
                return new MapReduceTask(taskExecutionContext, logger);
            case "SPARK":
                return new SparkTask(taskExecutionContext, logger);
            case "FLINK":
                return new FlinkTask(taskExecutionContext, logger);
            case "PYTHON":
                return new PythonTask(taskExecutionContext, logger);
            case "HTTP":
                return new HttpTask(taskExecutionContext, logger);
            case "DATAX":
                return new DataxTask(taskExecutionContext, logger);
            case "SQOOP":
                return new SqoopTask(taskExecutionContext, logger);
            default:
                logger.error("not support task type: {}", taskType);
                throw new IllegalArgumentException("not support task type");
        }
    }
}
