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
package org.apache.dolphinscheduler.server.worker.task.shell;


import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.shell.ShellParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.SpringApplicationContext;
import org.apache.dolphinscheduler.dao.ProcessDao;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.dolphinscheduler.server.worker.task.ShellCommandExecutor;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import java.util.Set;

/**
 * shell task
 */
public class ShellTask extends AbstractTask {

  /**
   * shell parameters
   */
  private ShellParameters shellParameters;

  /**
   * task dir
   */
  private String taskDir;

  /**
   * shell command executor
   */
  private ShellCommandExecutor shellCommandExecutor;

  /**
   * process database access
   */
  private ProcessDao processDao;

  /**
   * constructor
   * @param taskProps task props
   * @param logger    logger
   */
  public ShellTask(TaskProps taskProps, Logger logger) {
    super(taskProps, logger);

    this.taskDir = taskProps.getTaskDir();

    this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle, taskProps.getTaskDir(),
            taskProps.getTaskAppId(),
            taskProps.getTaskInstId(),
            taskProps.getTenantCode(),
            taskProps.getEnvFile(),
            taskProps.getTaskStartTime(),
            taskProps.getTaskTimeout(),
            logger);
    this.processDao = SpringApplicationContext.getBean(ProcessDao.class);
  }

  @Override
  public void init() {
    logger.info("shell task params {}", taskProps.getTaskParams());

    shellParameters = JSONUtils.parseObject(taskProps.getTaskParams(), ShellParameters.class);

    if (!shellParameters.checkParameters()) {
      throw new RuntimeException("shell task params is not valid");
    }
  }

  @Override
  public void handle() throws Exception {
    try {
      // construct process
      exitStatusCode = shellCommandExecutor.run(buildCommand(), processDao);
    } catch (Exception e) {
      logger.error("shell task failure", e);
      exitStatusCode = -1;
      throw e;
    }
  }

  @Override
  public void cancelApplication(boolean cancelApplication) throws Exception {
    // cancel process
    shellCommandExecutor.cancelApplication();
  }

  /**
   * create command
   * @return file name
   * @throws Exception exception
   */
  private String buildCommand() throws Exception {
    // generate scripts
    String fileName = String.format("%s/%s_node.sh", taskDir, taskProps.getTaskAppId());
    Path path = new File(fileName).toPath();

    if (Files.exists(path)) {
      return fileName;
    }

    String script = shellParameters.getRawScript().replaceAll("\\r\\n", "\n");


    /**
     *  combining local and global parameters
     */
    Map<String, Property> paramsMap = ParamUtils.convert(taskProps.getUserDefParamsMap(),
            taskProps.getDefinedParams(),
            shellParameters.getLocalParametersMap(),
            taskProps.getCmdTypeIfComplement(),
            taskProps.getScheduleTime());
    if (paramsMap != null){
      script = ParameterUtils.convertParameterPlaceholders(script, ParamUtils.convert(paramsMap));
    }


    shellParameters.setRawScript(script);

    logger.info("raw script : {}", shellParameters.getRawScript());
    logger.info("task dir : {}", taskDir);

    Set<PosixFilePermission> perms = PosixFilePermissions.fromString(Constants.RWXR_XR_X);
    FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

    Files.createFile(path, attr);

    Files.write(path, shellParameters.getRawScript().getBytes(), StandardOpenOption.APPEND);

    return fileName;
  }

  @Override
  public AbstractParameters getParameters() {
    return shellParameters;
  }



}
