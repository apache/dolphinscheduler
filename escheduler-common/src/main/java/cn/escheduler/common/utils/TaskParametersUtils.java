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
package cn.escheduler.common.utils;

import cn.escheduler.common.enums.TaskType;
import cn.escheduler.common.task.AbstractParameters;
import cn.escheduler.common.task.dependent.DependentParameters;
import cn.escheduler.common.task.flink.FlinkParameters;
import cn.escheduler.common.task.http.HttpParameters;
import cn.escheduler.common.task.mr.MapreduceParameters;
import cn.escheduler.common.task.procedure.ProcedureParameters;
import cn.escheduler.common.task.python.PythonParameters;
import cn.escheduler.common.task.shell.ShellParameters;
import cn.escheduler.common.task.spark.SparkParameters;
import cn.escheduler.common.task.sql.SqlParameters;
import cn.escheduler.common.task.subprocess.SubProcessParameters;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * task parameters utils
 */
public class TaskParametersUtils {

  private static Logger logger = LoggerFactory.getLogger(TaskParametersUtils.class);

  /**
   * get task parameters
   * @param taskType
   * @param parameter
   * @return task parameters
   */
  public static AbstractParameters getParameters(String taskType, String parameter) {
    try {
      switch (EnumUtils.getEnum(TaskType.class,taskType)) {
        case SUB_PROCESS:
          return JSONUtils.parseObject(parameter, SubProcessParameters.class);
        case SHELL:
          return JSONUtils.parseObject(parameter, ShellParameters.class);
        case PROCEDURE:
          return JSONUtils.parseObject(parameter, ProcedureParameters.class);
        case SQL:
          return JSONUtils.parseObject(parameter, SqlParameters.class);
        case MR:
          return JSONUtils.parseObject(parameter, MapreduceParameters.class);
        case SPARK:
          return JSONUtils.parseObject(parameter, SparkParameters.class);
        case PYTHON:
          return JSONUtils.parseObject(parameter, PythonParameters.class);
        case DEPENDENT:
          return JSONUtils.parseObject(parameter, DependentParameters.class);
        case FLINK:
          return JSONUtils.parseObject(parameter, FlinkParameters.class);
        case HTTP:
          return JSONUtils.parseObject(parameter, HttpParameters.class);
        default:
          return null;
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return null;
  }
}
