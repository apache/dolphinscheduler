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


import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.dao.ProcessDao;
import org.apache.dolphinscheduler.server.worker.task.dependent.DependentTask;
import org.apache.dolphinscheduler.server.worker.task.flink.FlinkTask;
import org.apache.dolphinscheduler.server.worker.task.http.HttpTask;
import org.apache.dolphinscheduler.server.worker.task.mr.MapReduceTask;
import org.apache.dolphinscheduler.server.worker.task.processdure.ProcedureTask;
import org.apache.dolphinscheduler.server.worker.task.python.PythonTask;
import org.apache.dolphinscheduler.server.worker.task.shell.ShellTask;
import org.apache.dolphinscheduler.server.worker.task.spark.SparkTask;
import org.apache.dolphinscheduler.server.worker.task.sql.SqlTask;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;

/**
 *  task manaster
 */
public class TaskManager {


  /**
   *  create new task
   * @param taskType
   * @param props
   * @param logger
   * @return
   * @throws IllegalArgumentException
   */
  public static AbstractTask newTask(String taskType, TaskProps props, Logger logger,ProcessDao processDao)
      throws IllegalArgumentException {
    switch (EnumUtils.getEnum(TaskType.class,taskType)) {
        case SHELL:
        return new ShellTask(props, logger,processDao);
      case PROCEDURE:
        return new ProcedureTask(props, logger,processDao);
      case SQL:
        return new SqlTask(props, logger,processDao);
      case MR:
        return new MapReduceTask(props, logger,processDao);
      case SPARK:
        return new SparkTask(props, logger,processDao);
      case FLINK:
        return new FlinkTask(props, logger,processDao);
      case PYTHON:
        return new PythonTask(props, logger,processDao);
      case DEPENDENT:
        return new DependentTask(props, logger,processDao);
      case HTTP:
        return new HttpTask(props, logger,processDao);
      default:
        logger.error("unsupport task type: {}", taskType);
        throw new IllegalArgumentException("not support task type");
    }
  }
}
