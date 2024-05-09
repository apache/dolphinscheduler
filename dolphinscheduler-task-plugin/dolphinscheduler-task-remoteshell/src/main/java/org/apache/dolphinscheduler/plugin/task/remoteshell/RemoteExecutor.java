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

import static org.apache.dolphinscheduler.plugin.task.remoteshell.RemoteExecutor.COMMAND.PSTREE_COMMAND;

import org.apache.dolphinscheduler.plugin.datasource.ssh.SSHUtils;
import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHConnectionParam;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.parser.TaskOutputParameterParser;
import org.apache.dolphinscheduler.plugin.task.api.utils.ProcessUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteExecutor implements AutoCloseable {

    static final String REMOTE_SHELL_HOME = "/tmp/dolphinscheduler-remote-shell-%s/";
    static final String STATUS_TAG_MESSAGE = "DOLPHINSCHEDULER-REMOTE-SHELL-TASK-STATUS-";
    static final int TRACK_INTERVAL = 5000;

    protected Map<String, String> taskOutputParams = new HashMap<>();
    private SshClient sshClient;
    private ClientSession session;
    private SSHConnectionParam sshConnectionParam;

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
        log.info("Remote shell task log:");
        TaskOutputParameterParser taskOutputParameterParser = new TaskOutputParameterParser();
        do {
            pid = getTaskPid(taskId);
            String trackCommand = String.format(COMMAND.TRACK_COMMAND, logN + 1, getRemoteShellHome(), taskId);
            String logLine = runRemote(trackCommand);
            if (StringUtils.isEmpty(logLine)) {
                Thread.sleep(TRACK_INTERVAL);
            } else {
                logN += logLine.split("\n").length;
                log.info(logLine);
                taskOutputParameterParser.appendParseLog(logLine);
            }
        } while (StringUtils.isNotEmpty(pid));
        taskOutputParams.putAll(taskOutputParameterParser.getTaskOutputParams());
    }

    public Map<String, String> getTaskOutputParams() {
        return taskOutputParams;
    }

    public Integer getTaskExitCode(String taskId) throws IOException {
        String trackCommand = String.format(COMMAND.LOG_TAIL_COMMAND, getRemoteShellHome(), taskId);
        String logLine = runRemote(trackCommand);
        int exitCode = -1;
        log.info("Remote shell task run status: {}", logLine);
        if (logLine.contains(STATUS_TAG_MESSAGE)) {
            String status = StringUtils.substringAfter(logLine, STATUS_TAG_MESSAGE);
            if (status.equals("0")) {
                log.info("Remote shell task success");
                exitCode = 0;
            } else {
                log.error("Remote shell task failed");
                exitCode = Integer.parseInt(status);
            }
        }
        cleanData(taskId);
        return exitCode;
    }

    public void cleanData(String taskId) {
        String cleanCommand =
                String.format(COMMAND.CLEAN_COMMAND, getRemoteShellHome(), taskId, getRemoteShellHome(), taskId);
        try {
            runRemote(cleanCommand);
        } catch (Exception e) {
            log.error("Remote shell task clean data failed, but will not affect the task execution", e);
        }
    }

    public void kill(String taskId) throws IOException {
        String pid = getTaskPid(taskId);

        if (StringUtils.isEmpty(pid)) {
            log.warn("query remote-shell task remote process id with empty");
            return;
        }
        if (!NumberUtils.isParsable(pid)) {
            log.error("query remote-shell task remote process id error, pid {} can not parse to number", pid);
            return;
        }

        // query all pid
        String remotePidStr = getAllRemotePidStr(pid);
        String killCommand = String.format(COMMAND.KILL_COMMAND, remotePidStr);
        log.info("prepare to execute kill command in host: {}, kill cmd: {}", sshConnectionParam.getHost(),
                killCommand);
        runRemote(killCommand);
        cleanData(taskId);
    }

    protected String getAllRemotePidStr(String pid) {

        String remoteProcessIdStr = "";
        String cmd = String.format(PSTREE_COMMAND, pid);
        log.info("query all process id cmd: {}", cmd);

        try {
            String rawPidStr = runRemote(cmd);
            remoteProcessIdStr = ProcessUtils.parsePidStr(rawPidStr);
            if (!remoteProcessIdStr.startsWith(pid)) {
                log.error("query remote process id error, [{}] first pid not equal [{}]", remoteProcessIdStr, pid);
                remoteProcessIdStr = pid;
            }
        } catch (Exception e) {
            log.error("query remote all process id error", e);
            remoteProcessIdStr = pid;
        }
        return remoteProcessIdStr;
    }

    public String getTaskPid(String taskId) throws IOException {
        String pidCommand = String.format(COMMAND.GET_PID_COMMAND, taskId);
        return runRemote(pidCommand).trim();
    }

    public void saveCommand(String taskId, String localFile) throws IOException {
        String checkDirCommand = String.format(COMMAND.CHECK_DIR, getRemoteShellHome(), getRemoteShellHome());
        runRemote(checkDirCommand);
        uploadScript(taskId, localFile);

        log.info("The final script is: \n{}",
                runRemote(String.format(COMMAND.CAT_FINAL_SCRIPT, getRemoteShellHome(), taskId)));
    }

    public void uploadScript(String taskId, String localFile) throws IOException {

        String remotePath = getRemoteShellHome() + taskId + ".sh";
        log.info("upload script from local:{} to remote: {}", localFile, remotePath);
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
            Integer exitStatus = channel.getExitStatus();
            if (exitStatus == null || exitStatus != 0) {
                throw new TaskException(
                        "Remote shell task error, exitStatus: " + exitStatus + " error message: " + err);
            }
            return out.toString();
        }
    }

    private String getRemoteShellHome() {
        return String.format(REMOTE_SHELL_HOME, sshConnectionParam.getUser());
    }

    @SneakyThrows
    @Override
    public void close() {
        if (session != null && session.isOpen()) {
            session.close();
        }
        if (sshClient != null && sshClient.isStarted()) {
            sshClient.close();
        }

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

        static final String PSTREE_COMMAND = "pstree -p %s";

    }

}
