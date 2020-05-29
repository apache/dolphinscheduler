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
package org.apache.dolphinscheduler.server.worker.task.dummy;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.slf4j.Logger;

/**
 * Dummy task
 */
public class DummyTask extends AbstractTask {

  /**
   *  taskExecutionContext
   */
  private TaskExecutionContext taskExecutionContext;

  /**
   * constructor
   * @param taskExecutionContext taskExecutionContext
   * @param logger    logger
   */
  public DummyTask(TaskExecutionContext taskExecutionContext, Logger logger) {
    super(taskExecutionContext, logger);

    this.taskExecutionContext = taskExecutionContext;
  }

  @Override
  public void init() {
    logger.info("shell task params {}", taskExecutionContext.getTaskParams());
  }

  @Override
  public void handle() throws Exception {
    try {
      logger.info("Execute dummy task success");
      setExitStatusCode(Constants.EXIT_CODE_SUCCESS);
      setAppIds("");
      setProcessId(222222);
    } catch (Exception e) {
      logger.error("shell task error", e);
      setExitStatusCode(Constants.EXIT_CODE_FAILURE);
      throw e;
    }
  }

  @Override
  public void cancelApplication(boolean cancelApplication) throws Exception {
    // cancel process
  }

  @Override
  public AbstractParameters getParameters() {
    return null;
  }

}
