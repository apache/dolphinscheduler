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
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.ssh.SSHParameters;
import org.apache.dolphinscheduler.common.utils.*;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.dao.datasource.RemoteServerSource;
import org.apache.dolphinscheduler.server.entity.SSHTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.slf4j.Logger;

import java.io.InputStream;

/**
 * SSH task
 */
public class SSHTask extends AbstractTask {

    /**
     * shell parameters
     */
    private SSHParameters sshParameters;

    /**
     * jsch session
     */
    private Session session;

    /**
     * jsch channel
     */
    private Channel channel;

    /**
     * shell command executor
     */

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     * @param logger               logger
     */
    public SSHTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);

        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        logger.info("shell task params {}", taskExecutionContext.getTaskParams());

        sshParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SSHParameters.class);

        if (!sshParameters.checkParameters()) {
            throw new RuntimeException("shell task params is not valid");
        }
    }

    /**
     * Step 1: Get username,password,host of remote server.
     * Step 2: Connect remote server.
     * Step 3: Execute command on remote server.
     * Step 4: Get result of remote server.
     *
     * @throws Exception
     */
    @Override
    public void handle() throws Exception {
        String command = null;
        try {
            SSHTaskExecutionContext sshTaskExecutionContext = taskExecutionContext.getSshTaskExecutionContext();
            // get datasource
            RemoteServerSource remote = (RemoteServerSource) DataSourceFactory.getDatasource(DbType.REMOTESERVER,
                sshTaskExecutionContext.getConnectionParams());
            // construct process
            openSession(remote);
            command = buildCommand();
            Object[] result = execute(command);
            if ((int) result[0] == 0) {
                setExitStatusCode(Constants.EXIT_CODE_SUCCESS);
                logger.info("Execute ssh task success, the command is {{}} and result is {{}}", command, result[1]);
            } else {
                setExitStatusCode(Constants.EXIT_CODE_FAILURE);
                logger.error("Execute ssh task failed, the command is {{}} and result is {{}}", command, result[1]);
            }
            session.disconnect();
        } catch (Exception e) {
            logger.error("Execute ssh task exception, the command is {{}} and exception is {{}}", command, e);
            setExitStatusCode(Constants.EXIT_CODE_FAILURE);
            throw e;
        }
    }

    @Override
    public void cancelApplication(boolean cancelApplication) throws Exception {
        // cancel ssh task
        if (!session.isConnected()){
            return;
        }
        if (channel.isClosed()) {
            return;
        }
        channel.disconnect();
        session.disconnect();
    }

    /**
     * create command
     *
     * @return shell or cmd command
     * @throws Exception exception
     */
    private String buildCommand() throws Exception {
        String script = sshParameters.getRawScript();
        return script;
    }

    @Override
    public AbstractParameters getParameters() {
        return sshParameters;
    }


    private void openSession(RemoteServerSource remote) throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(remote.getUser(), remote.getHost(), remote.getPort());
        session.setPassword(remote.getPassword());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(Constants.REMOTESERVER_TIME_OUT);
    }

    private Object[] execute(String command) throws Exception {
        StringBuilder builder = new StringBuilder();
        channel = session.openChannel("exec");
        ChannelExec channelExec = ((ChannelExec) channel);
        channelExec.setCommand(command);
        InputStream inputStream = channelExec.getInputStream();
        InputStream errStream = channelExec.getErrStream();
        channel.connect();
        byte[] tmp = new byte[1024];
        int exitStatus;
        //process success message
        while (true) {
            while (inputStream.available() > 0) {
                int i = inputStream.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                builder.append(new String(tmp, 0, i));
            }
            while (errStream.available() > 0) {
                int i = errStream.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                builder.append(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                if (inputStream.available() > 0) {
                    continue;
                }
                if (errStream.available() > 0) {
                    continue;
                }
                exitStatus = channel.getExitStatus();
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
        }
        channel.disconnect();
        return new Object[]{exitStatus, builder.toString()};
    }

}
