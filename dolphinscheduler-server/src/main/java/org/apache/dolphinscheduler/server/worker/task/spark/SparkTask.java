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
package org.apache.dolphinscheduler.server.worker.task.spark;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.SparkVersion;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.spark.SparkParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.utils.SparkArgsUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractYarnTask;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * spark task
 */
public class SparkTask extends AbstractYarnTask {

  /**
   * spark1 command
   */
  private static final String SPARK1_COMMAND = "${SPARK_HOME1}/bin/spark-submit";

  /**
   * spark2 command
   */
  private static final String SPARK2_COMMAND = "${SPARK_HOME2}/bin/spark-submit";

  /**
   *  spark parameters
   */
  private SparkParameters sparkParameters;

  /**
   * taskExecutionContext
   */
  private TaskExecutionContext taskExecutionContext;

  public SparkTask(TaskExecutionContext taskExecutionContext, Logger logger) {
    super(taskExecutionContext, logger);
    this.taskExecutionContext = taskExecutionContext;
  }

  @Override
  public void init() {

    logger.info("spark task params {}", taskExecutionContext.getTaskParams());

    sparkParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SparkParameters.class);

    if (!sparkParameters.checkParameters()) {
      throw new RuntimeException("spark task params is not valid");
    }
    sparkParameters.setQueue(taskExecutionContext.getQueue());

    setMainJarName();

    if (StringUtils.isNotEmpty(sparkParameters.getMainArgs())) {
      String args = sparkParameters.getMainArgs();

      // replace placeholder
      Map<String, Property> paramsMap = ParamUtils.convert(ParamUtils.getUserDefParamsMap(taskExecutionContext.getDefinedParams()),
              taskExecutionContext.getDefinedParams(),
              sparkParameters.getLocalParametersMap(),
              CommandType.of(taskExecutionContext.getCmdTypeIfComplement()),
              taskExecutionContext.getScheduleTime());

      if (paramsMap != null ){
        args = ParameterUtils.convertParameterPlaceholders(args, ParamUtils.convert(paramsMap));
      }
      sparkParameters.setMainArgs(args);
    }
  }

  /**
   * create command
   * @return command
   */
  @Override
  protected String buildCommand() {
    List<String> args = new ArrayList<>();

    //spark version
    String sparkCommand = SPARK2_COMMAND;

    if (SparkVersion.SPARK1.name().equals(sparkParameters.getSparkVersion())) {
      sparkCommand = SPARK1_COMMAND;
    }

    args.add(sparkCommand);

    // other parameters
    args.addAll(SparkArgsUtils.buildArgs(sparkParameters));

    String command = ParameterUtils
            .convertParameterPlaceholders(String.join(" ", args), taskExecutionContext.getDefinedParams());

    logger.info("spark task command : {}", command);

    return command;
  }

  @Override
  protected void setMainJarName() {
    // main jar
    ResourceInfo mainJar = sparkParameters.getMainJar();
    if (mainJar != null) {
      int resourceId = mainJar.getId();
      String resourceName;
      if (resourceId == 0) {
        resourceName = mainJar.getRes();
      } else {
        Resource resource = processService.getResourceById(sparkParameters.getMainJar().getId());
        if (resource == null) {
          logger.error("resource id: {} not exist", resourceId);
          throw new RuntimeException(String.format("resource id: %d not exist", resourceId));
        }
        resourceName = resource.getFullName().replaceFirst("/", "");
      }
      mainJar.setRes(resourceName);
      sparkParameters.setMainJar(mainJar);
    }
  }

  @Override
  public AbstractParameters getParameters() {
    return sparkParameters;
  }
}
