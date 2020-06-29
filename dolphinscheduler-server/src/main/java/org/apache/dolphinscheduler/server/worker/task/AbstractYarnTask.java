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

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.slf4j.Logger;

/**
 *  abstract yarn task
 */
public abstract class AbstractYarnTask extends AbstractTask {
  /**
   *  process task
   */
  private ShellCommandExecutor shellCommandExecutor;

  /**
   *  process database access
   */
  protected ProcessService processService;

  /**
   * Abstract Yarn Task
   * @param taskExecutionContext taskExecutionContext
   * @param logger    logger
   */
  public AbstractYarnTask(TaskExecutionContext taskExecutionContext, Logger logger) {
    super(taskExecutionContext, logger);
    this.processService = SpringApplicationContext.getBean(ProcessService.class);
    this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
            taskExecutionContext,
            logger);
  }

  @Override
  public void handle() throws Exception {
    try {
      // SHELL task exit code
      CommandExecuteResult commandExecuteResult = shellCommandExecutor.run(buildCommand());
      setExitStatusCode(commandExecuteResult.getExitStatusCode());
      setAppIds(commandExecuteResult.getAppIds());
      setProcessId(commandExecuteResult.getProcessId());
    } catch (Exception e) {
      logger.error("yarn process failure", e);
      exitStatusCode = -1;
      throw e;
    }
  }

  /**
   * cancel application
   * @param status status
   * @throws Exception exception
   */
  @Override
  public void cancelApplication(boolean status) throws Exception {
    cancel = true;
    // cancel process
    shellCommandExecutor.cancelApplication();
    TaskInstance taskInstance = processService.findTaskInstanceById(taskExecutionContext.getTaskInstanceId());
    if (status && taskInstance != null){
      ProcessUtils.killYarnJob(taskExecutionContext);
    }
  }

  /**
   * create command
   * @return String
   * @throws Exception exception
   */
  protected abstract String buildCommand() throws Exception;

  /**
   * set main jar name
   */
  protected abstract void setMainJarName();
}
