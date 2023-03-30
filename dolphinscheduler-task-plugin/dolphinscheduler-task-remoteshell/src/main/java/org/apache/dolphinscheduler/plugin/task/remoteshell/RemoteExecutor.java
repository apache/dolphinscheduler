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

package org.apache.dolphinscheduler.plugin.task.remoteshell;

import org.apache.dolphinscheduler.plugin.datasource.ssh.SSHUtils;
import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHConnectionParam;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.apache.sshd.sftp.client.fs.SftpFileSystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteExecutor {

    protected final Logger logger =
            LoggerFactory.getLogger(String.format(TaskConstants.TASK_LOGGER_THREAD_NAME, getClass()));

    protected static final Pattern SETVALUE_REGEX = Pattern.compile(TaskConstants.SETVALUE_REGEX);

    static final String REMOTE_SHELL_HOME = "/tmp/dolphinscheduler-remote-shell-%s/";
    static final String STATUS_TAG_MESSAGE = "DOLPHINSCHEDULER-REMOTE-SHELL-TASK-STATUS-";
    static final int TRACK_INTERVAL = 5000;

    protected StringBuilder varPool = new StringBuilder();

    SshClient sshClient;
    ClientSession session;
    SSHConnectionParam sshConnectionParam;

    public RemoteExecutor(SSHConnectionParam sshConnectionParam) {

        this.sshConnectionParam = sshConnectionParam;
        initClient();
    }

    private void initClient() {
        sshClient = SshClient.setUpDefaultClient();
        sshClient.start();
    }

    private ClientSession getSession() {
        if (session != null && session.isOpen()) {
            return session;
        }
        try {
            session = SSHUtils.getSession(sshClient, sshConnectionParam);
            if (session == null || !session.auth().verify().isSuccess()) {
                throw new TaskException("SSH connection failed");
            }
        } catch (Exception e) {
            throw new TaskException("SSH connection failed", e);
        }
        return session;
    }

    public int run(String taskId, String localFile) throws IOException {
        try {
            // only run task if no exist same task
            String pid = getTaskPid(taskId);
            if (StringUtils.isEmpty(pid)) {
                saveCommand(taskId, localFile);
                String runCommand = String.format(COMMAND.RUN_COMMAND, getRemoteShellHome(), taskId,
                        getRemoteShellHome(), taskId);
                runRemote(runCommand);
            }
            track(taskId);
            return getTaskExitCode(taskId);
        } catch (Exception e) {
            throw new TaskException("Remote shell task error", e);
        }
    }

    public void track(String taskId) throws Exception {
        int logN = 0;
        String pid;
        logger.info("Remote shell task log:");
        do {
            pid = getTaskPid(taskId);
            String trackCommand = String.format(COMMAND.TRACK_COMMAND, logN + 1, getRemoteShellHome(), taskId);
            String log = runRemote(trackCommand);
            if (StringUtils.isEmpty(log)) {
                Thread.sleep(TRACK_INTERVAL);
            } else {
                logN += log.split("\n").length;
                setVarPool(log);
                logger.info(log);
            }
        } while (StringUtils.isNotEmpty(pid));
    }

    public String getVarPool() {
        return varPool.toString();
    }

    private void setVarPool(String log) {
        String[] lines = log.split("\n");
        for (String line : lines) {
            if (line.startsWith("${setValue(") || line.startsWith("#{setValue(")) {
                varPool.append(findVarPool(line));
                varPool.append("$VarPool$");
            }
        }
    }

    private String findVarPool(String line) {
        Matcher matcher = SETVALUE_REGEX.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public Integer getTaskExitCode(String taskId) throws IOException {
        String trackCommand = String.format(COMMAND.LOG_TAIL_COMMAND, getRemoteShellHome(), taskId);
        String log = runRemote(trackCommand);
        int exitCode = -1;
        logger.info("Remote shell task run status: {}", log);
        if (log.contains(STATUS_TAG_MESSAGE)) {
            String status = log.replace(STATUS_TAG_MESSAGE, "").trim();
            if (status.equals("0")) {
                logger.info("Remote shell task success");
                exitCode = 0;
            } else {
                logger.error("Remote shell task failed");
                exitCode = Integer.parseInt(status);
            }
        }
        cleanData(taskId);
        logger.error("Remote shell task failed");
        return exitCode;
    }

    public void cleanData(String taskId) {
        String cleanCommand =
                String.format(COMMAND.CLEAN_COMMAND, getRemoteShellHome(), taskId, getRemoteShellHome(), taskId);
        try {
            runRemote(cleanCommand);
        } catch (Exception e) {
            logger.error("Remote shell task clean data failed, but will not affect the task execution", e);
        }
    }

    public void kill(String taskId) throws IOException {
        String pid = getTaskPid(taskId);
        String killCommand = String.format(COMMAND.KILL_COMMAND, pid);
        runRemote(killCommand);
        cleanData(taskId);
    }

    public String getTaskPid(String taskId) throws IOException {
        String pidCommand = String.format(COMMAND.GET_PID_COMMAND, taskId);
        return runRemote(pidCommand).trim();
    }

    public void saveCommand(String taskId, String localFile) throws IOException {
        String checkDirCommand = String.format(COMMAND.CHECK_DIR, getRemoteShellHome(), getRemoteShellHome());
        runRemote(checkDirCommand);
        uploadScript(taskId, localFile);

        logger.info("The final script is: \n{}",
                runRemote(String.format(COMMAND.CAT_FINAL_SCRIPT, getRemoteShellHome(), taskId)));
    }

    public void uploadScript(String taskId, String localFile) throws IOException {

        String remotePath = getRemoteShellHome() + taskId + ".sh";
        logger.info("upload script from local:{} to remote: {}", localFile, remotePath);
        try (SftpFileSystem fs = SftpClientFactory.instance().createSftpFileSystem(getSession())) {
            Path path = fs.getPath(remotePath);
            Files.copy(Paths.get(localFile), path);
        }
    }

    public String runRemote(String command) throws IOException {
        try (
                ChannelExec channel = getSession().createExecChannel(command);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ByteArrayOutputStream err = new ByteArrayOutputStream()) {

            channel.setOut(System.out);
            channel.setOut(out);
            channel.setErr(err);
            channel.open();
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0);
            channel.close();
            if (channel.getExitStatus() != 0) {
                throw new TaskException("Remote shell task error, error message: " + err.toString());
            }
            return out.toString();
        }
    }

    private String getRemoteShellHome() {
        return String.format(REMOTE_SHELL_HOME, sshConnectionParam.getUser());
    }

    static class COMMAND {

        private COMMAND() {
            throw new IllegalStateException("Utility class");
        }

        static final String CHECK_DIR = "if [ ! -d %s ]; then mkdir -p %s; fi";
        static final String RUN_COMMAND = "nohup /bin/bash %s%s.sh >%s%s.log 2>&1 &";
        static final String TRACK_COMMAND = "tail -n +%s %s%s.log";

        static final String LOG_TAIL_COMMAND = "tail -n 1 %s%s.log";
        static final String GET_PID_COMMAND = "ps -ef | grep \"%s.sh\" | grep -v grep | awk '{print $2}'";
        static final String KILL_COMMAND = "kill -9 %s";
        static final String CLEAN_COMMAND = "rm %s%s.sh %s%s.log";

        static final String HEADER = "#!/bin/bash\n";

        static final String ADD_STATUS_COMMAND = "\necho %s$?";

        static final String CAT_FINAL_SCRIPT = "cat %s%s.sh";
    }

}
