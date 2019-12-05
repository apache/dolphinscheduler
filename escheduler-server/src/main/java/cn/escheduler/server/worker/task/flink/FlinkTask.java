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
package cn.escheduler.server.worker.task.flink;

import cn.escheduler.common.process.Property;
import cn.escheduler.common.task.AbstractParameters;
import cn.escheduler.common.task.flink.FlinkParameters;
import cn.escheduler.common.utils.JSONUtils;
import cn.escheduler.common.utils.ParameterUtils;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.server.utils.FlinkArgsUtils;
import cn.escheduler.server.utils.ParamUtils;
import cn.escheduler.server.worker.task.AbstractYarnTask;
import cn.escheduler.server.worker.task.TaskProps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  flink task
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

  public FlinkTask(TaskProps props, Logger logger) {
    super(props, logger);
  }

  @Override
  public void init() {

    logger.info("flink task params {}", taskProps.getTaskParams());

    flinkParameters = JSONUtils.parseObject(taskProps.getTaskParams(), FlinkParameters.class);

    if (!flinkParameters.checkParameters()) {
      throw new RuntimeException("flink task params is not valid");
    }
    flinkParameters.setQueue(taskProps.getQueue());

    if (StringUtils.isNotEmpty(flinkParameters.getMainArgs())) {
      String args = flinkParameters.getMainArgs();
      // get process instance by task instance id
      ProcessInstance processInstance = processDao.findProcessInstanceByTaskId(taskProps.getTaskInstId());

      /**
       *  combining local and global parameters
       */
      Map<String, Property> paramsMap = ParamUtils.convert(taskProps.getUserDefParamsMap(),
              taskProps.getDefinedParams(),
              flinkParameters.getLocalParametersMap(),
              processInstance.getCmdTypeIfComplement(),
              processInstance.getScheduleTime());

      logger.info("param Map : {}", paramsMap);
      if (paramsMap != null ){

        args = ParameterUtils.convertParameterPlaceholders(args, ParamUtils.convert(paramsMap));
        logger.info("param args : {}", args);
      }
      flinkParameters.setMainArgs(args);
    }
  }

  /**
   *  create command
   * @return
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
            .convertParameterPlaceholders(String.join(" ", args), taskProps.getDefinedParams());

    logger.info("flink task command : {}", command);

    return command;
  }

  @Override
  public AbstractParameters getParameters() {
    return flinkParameters;
  }
}
