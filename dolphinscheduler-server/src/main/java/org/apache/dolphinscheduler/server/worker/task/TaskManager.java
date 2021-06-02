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

<<<<<<< HEAD
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.utils.EnumUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
=======
>>>>>>> cc9e5d5d34fcf2279b267cca7df37a9e80eeba07
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
<<<<<<< HEAD
import org.apache.dolphinscheduler.server.worker.task.ssh.SSHTask;
=======
import org.apache.dolphinscheduler.service.alert.AlertClientService;

>>>>>>> cc9e5d5d34fcf2279b267cca7df37a9e80eeba07
import org.slf4j.Logger;

/**
 * task manager
 */
public class TaskManager {

    /**
     * create new task
<<<<<<< HEAD
     *
     * @param taskExecutionContext taskExecutionContext
     * @param logger               logger
     * @return AbstractTask
     * @throws IllegalArgumentException illegal argument exception
     */
    public static AbstractTask newTask(TaskExecutionContext taskExecutionContext, Logger logger)
        throws IllegalArgumentException {
        switch (EnumUtils.getEnum(TaskType.class, taskExecutionContext.getTaskType())) {
            case SHELL:
                Boolean remote = false;
                if (JSONUtils.parseObject(taskExecutionContext.getTaskParams()).get("remote") != null) {
                    remote = JSONUtils.parseObject(taskExecutionContext.getTaskParams()).get("remote").asBoolean();
                }
                if (remote != null && remote) {
                    return new SSHTask(taskExecutionContext, logger);
                } else {
                    return new ShellTask(taskExecutionContext, logger);
                }
            case PROCEDURE:
                return new ProcedureTask(taskExecutionContext, logger);
            case SQL:
                return new SqlTask(taskExecutionContext, logger);
            case MR:
                return new MapReduceTask(taskExecutionContext, logger);
            case SPARK:
                return new SparkTask(taskExecutionContext, logger);
            case FLINK:
                return new FlinkTask(taskExecutionContext, logger);
            case PYTHON:
                return new PythonTask(taskExecutionContext, logger);
            case HTTP:
                return new HttpTask(taskExecutionContext, logger);
            case DATAX:
                return new DataxTask(taskExecutionContext, logger);
            case SQOOP:
                return new SqoopTask(taskExecutionContext, logger);
            default:
                logger.error("unsupport task type: {}", taskExecutionContext.getTaskType());
=======
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
>>>>>>> cc9e5d5d34fcf2279b267cca7df37a9e80eeba07
                throw new IllegalArgumentException("not support task type");
        }
    }
}
