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
package cn.escheduler.server.worker.task.python;


import cn.escheduler.common.process.Property;
import cn.escheduler.common.task.AbstractParameters;
import cn.escheduler.common.task.python.PythonParameters;
import cn.escheduler.common.utils.CommonUtils;
import cn.escheduler.common.utils.JSONUtils;
import cn.escheduler.common.utils.ParameterUtils;
import cn.escheduler.dao.DaoFactory;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.server.utils.ParamUtils;
import cn.escheduler.server.worker.task.AbstractTask;
import cn.escheduler.server.worker.task.PythonCommandExecutor;
import cn.escheduler.server.worker.task.TaskProps;
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
 *  python task
 */
public class PythonTask extends AbstractTask {

  /**
   *  python parameters
   */
  private PythonParameters pythonParameters;

  /**
   *  task dir
   */
  private String taskDir;

  private PythonCommandExecutor pythonProcessTask;

  /**
   * process database access
   */
  private ProcessDao processDao;


  public PythonTask(TaskProps taskProps, Logger logger) {
    super(taskProps, logger);

    this.taskDir = taskProps.getTaskDir();

    this.pythonProcessTask = new PythonCommandExecutor(this::logHandle,
            taskProps.getTaskDir(), taskProps.getTaskAppId(),
            taskProps.getTenantCode(), null, taskProps.getTaskStartTime(),
            taskProps.getTaskTimeout(), logger);
    this.processDao = DaoFactory.getDaoInstance(ProcessDao.class);
  }

  @Override
  public void init() {
    logger.info("python task params {}", taskProps.getTaskParams());

    pythonParameters = JSONUtils.parseObject(taskProps.getTaskParams(), PythonParameters.class);

    if (!pythonParameters.checkParameters()) {
      throw new RuntimeException("python task params is not valid");
    }
  }

  @Override
  public void handle() throws Exception {
    try {
      //  construct process
      exitStatusCode = pythonProcessTask.run(buildCommand(), processDao);
    } catch (Exception e) {
      logger.error("python process exception", e);
      exitStatusCode = -1;
    }
  }

  @Override
  public void cancelApplication(boolean cancelApplication) throws Exception {
    // cancel process
    pythonProcessTask.cancelApplication();
  }

  /**
   *  build command
   * @return
   * @throws Exception
   */
  private String buildCommand() throws Exception {
    // generate scripts
//    String fileName = String.format("%s/py_%s_node.py", taskDir, taskProps.getTaskAppId());
//    Path path = new File(fileName).toPath();



//    if (Files.exists(path)) {
//      return fileName;
//    }

    String rawScript = pythonParameters.getRawScript().replaceAll("\\r\\n", "\n");


    // find process instance by task id
    ProcessInstance processInstance = processDao.findProcessInstanceByTaskId(taskProps.getTaskInstId());

    /**
     *  combining local and global parameters
     */
    Map<String, Property> paramsMap = ParamUtils.convert(taskProps.getUserDefParamsMap(),
            taskProps.getDefinedParams(),
            pythonParameters.getLocalParametersMap(),
            processInstance.getCmdTypeIfComplement(),
            processInstance.getScheduleTime());
    if (paramsMap != null){
      rawScript = ParameterUtils.convertParameterPlaceholders(rawScript, ParamUtils.convert(paramsMap));
    }


//    pythonParameters.setRawScript(rawScript);

    logger.info("raw script : {}", pythonParameters.getRawScript());
    logger.info("task dir : {}", taskDir);

//    Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-xr-x");
//    FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
//
//    Files.createFile(path, attr);
//
//    Files.write(path, pythonParameters.getRawScript().getBytes(), StandardOpenOption.APPEND);
//
//    return fileName;
    return rawScript;
  }

  @Override
  public AbstractParameters getParameters() {
    return pythonParameters;
  }



}
