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
package org.apache.dolphinscheduler.server.worker.task.flink;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.flink.FlinkParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.server.utils.FlinkArgsUtils;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractYarnTask;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * flink task
 */
public class FlinkTask extends AbstractYarnTask {

  /**
   *  flink command
   */
  private static final String FLINK_COMMAND = "flink";
  private static final String FLINK_RUN = "run";

  /**
   *  flink parameters
   */
  private FlinkParameters flinkParameters;

  /**
   * taskExecutionContext
   */
  private TaskExecutionContext taskExecutionContext;

  public FlinkTask(TaskExecutionContext taskExecutionContext, Logger logger) {
    super(taskExecutionContext, logger);
    this.taskExecutionContext = taskExecutionContext;
  }

  @Override
  public void init() {

    logger.info("flink task params {}", taskExecutionContext.getTaskParams());

    flinkParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), FlinkParameters.class);

    if (!flinkParameters.checkParameters()) {
      throw new RuntimeException("flink task params is not valid");
    }
    flinkParameters.setQueue(taskExecutionContext.getQueue());
    setMainJarName();


    if (StringUtils.isNotEmpty(flinkParameters.getMainArgs())) {
      String args = flinkParameters.getMainArgs();


      // replace placeholder
      Map<String, Property> paramsMap = ParamUtils.convert(ParamUtils.getUserDefParamsMap(taskExecutionContext.getDefinedParams()),
              taskExecutionContext.getDefinedParams(),
              flinkParameters.getLocalParametersMap(),
              CommandType.of(taskExecutionContext.getCmdTypeIfComplement()),
              taskExecutionContext.getScheduleTime());

      logger.info("param Map : {}", paramsMap);
      if (paramsMap != null ){

        args = ParameterUtils.convertParameterPlaceholders(args, ParamUtils.convert(paramsMap));
        logger.info("param args : {}", args);
      }
      flinkParameters.setMainArgs(args);
    }
  }

  /**
   * create command
   * @return command
   */
  @Override
  protected String buildCommand() {
    List<String> args = new ArrayList<>();

    args.add(FLINK_COMMAND);
    args.add(FLINK_RUN);
    logger.info("flink task args : {}", args);
    // other parameters
    args.addAll(FlinkArgsUtils.buildArgs(flinkParameters));

    String command = ParameterUtils
            .convertParameterPlaceholders(String.join(" ", args), taskExecutionContext.getDefinedParams());

    logger.info("flink task command : {}", command);

    return command;
  }

  @Override
  protected void setMainJarName() {
    // main jar
    ResourceInfo mainJar = flinkParameters.getMainJar();
    if (mainJar != null) {
      int resourceId = mainJar.getId();
      String resourceName;
      if (resourceId == 0) {
        resourceName = mainJar.getRes();
      } else {
        Resource resource = processService.getResourceById(flinkParameters.getMainJar().getId());
        if (resource == null) {
          logger.error("resource id: {} not exist", resourceId);
          throw new RuntimeException(String.format("resource id: %d not exist", resourceId));
        }
        resourceName = resource.getFullName().replaceFirst("/", "");
      }
      mainJar.setRes(resourceName);
      flinkParameters.setMainJar(mainJar);
    }
  }

  @Override
  public AbstractParameters getParameters() {
    return flinkParameters;
  }
}
