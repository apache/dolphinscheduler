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
package org.apache.dolphinscheduler.server.worker.task.ssh;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.shell.ShellParameters;
import org.apache.dolphinscheduler.common.utils.*;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.slf4j.Logger;

import java.io.InputStream;

/**
 * SSH task
 */
public class SSHTask extends AbstractTask {

  private String user;
  private String password;
  private String host;
  private int port = 22;
  private int timeout = 3000000;

  /**
   * shell parameters
   */
  private ShellParameters shellParameters;

  /**
   * shell command executor
   */

  /**
   *  taskExecutionContext
   */
  private TaskExecutionContext taskExecutionContext;

  /**
   * constructor
   * @param taskExecutionContext taskExecutionContext
   * @param logger    logger
   */
  public SSHTask(TaskExecutionContext taskExecutionContext, Logger logger) {
    super(taskExecutionContext, logger);

    this.taskExecutionContext = taskExecutionContext;
  }

  @Override
  public void init() {
    logger.info("shell task params {}", taskExecutionContext.getTaskParams());

    shellParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), ShellParameters.class);

    user = shellParameters.getLocalParametersMap().get("user").getValue();
    password = shellParameters.getLocalParametersMap().get("password").getValue();
    host = shellParameters.getLocalParametersMap().get("host").getValue();

    if (!shellParameters.checkParameters()) {
      throw new RuntimeException("shell task params is not valid");
    }
  }

  /**
   * Step 1: Get username,password,host of remote server.
   * Step 2: Connect remote server.
   * Step 3: Execute command on remote server.
   * Step 4: Get result of remote server.
   * @throws Exception
   */
  @Override
  public void handle() throws Exception {
    String command = null;
    try {
      // construct process
      SSHClient client = openSession();
      command = buildCommand();
      String result = execute(client.getSession(), command);
      client.setIdle(true);
      setExitStatusCode(Constants.EXIT_CODE_SUCCESS);
      setAppIds("");
      setProcessId(1111111);
      logger.info("Execute ssh success : ", result);
    } catch (Exception e) {
      logger.error("shell task error", e);
      logger.error("Command: ", command);
      setExitStatusCode(Constants.EXIT_CODE_FAILURE);
      throw e;
    }
  }

  @Override
  public void cancelApplication(boolean cancelApplication) throws Exception {
    // cancel process
  }

  /**
   * create command
   * @return shell or cmd command
   * @throws Exception exception
   */
  private String buildCommand() throws Exception {
    String script = shellParameters.getRawScript();
    return script;
  }

  @Override
  public AbstractParameters getParameters() {
    return shellParameters;
  }


  private SSHClient openSession() throws Exception {
    return SSHPool.getSSHClient(host, user, password, port, timeout);
  }

  private String execute(Session session, String command) throws Exception {
    StringBuilder outputBuffer = new StringBuilder();
    Channel channel = session.openChannel("exec");
    ((ChannelExec) channel).setCommand(command);
    InputStream commandOutput = channel.getInputStream();
    channel.connect();
    int readByte = commandOutput.read();
    while (readByte != 0xffffffff) {
      outputBuffer.append((char) readByte);
      readByte = commandOutput.read();
    }
    channel.disconnect();
    return outputBuffer.toString();
  }

}
