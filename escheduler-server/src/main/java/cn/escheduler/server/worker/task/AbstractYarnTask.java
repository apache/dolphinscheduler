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
package cn.escheduler.server.worker.task;

import cn.escheduler.dao.DaoFactory;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.dao.model.TaskInstance;
import cn.escheduler.server.utils.ProcessUtils;
import org.slf4j.Logger;

import java.io.IOException;

/**
 *  abstract yarn task
 */
public abstract class AbstractYarnTask extends AbstractTask {

  /**
   *  process instance
   */
  protected ProcessInstance processInstance;

  /**
   *  process task
   */
  private ShellCommandExecutor shellCommandExecutor;

  /**
   *  process database access
   */
  protected ProcessDao processDao;

  /**
   * @param taskProps
   * @param logger
   * @throws IOException
   */
  public AbstractYarnTask(TaskProps taskProps, Logger logger) {
    super(taskProps, logger);
    this.processDao = DaoFactory.getDaoInstance(ProcessDao.class);
    this.processInstance = processDao.findProcessInstanceByTaskId(taskProps.getTaskInstId());
    this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
            taskProps.getTaskDir(),
            taskProps.getTaskAppId(),
            taskProps.getTaskInstId(),
            taskProps.getTenantCode(),
            taskProps.getEnvFile(),
            taskProps.getTaskStartTime(),
            taskProps.getTaskTimeout(),
            logger);
  }

  @Override
  public void handle() throws Exception {
    try {
      // construct process
      exitStatusCode = shellCommandExecutor.run(buildCommand(), processDao);
    } catch (Exception e) {
      logger.error("yarn process failure", e);
      exitStatusCode = -1;
    }
  }

  @Override
  public void cancelApplication(boolean status) throws Exception {
    cancel = true;
    // cancel process
    shellCommandExecutor.cancelApplication();
    TaskInstance taskInstance = processDao.findTaskInstanceById(taskProps.getTaskInstId());
    if (status && taskInstance != null){
      ProcessUtils.killYarnJob(taskInstance);
    }
  }

  /**
   *  create command
   */
  protected abstract String buildCommand() throws Exception;
}
