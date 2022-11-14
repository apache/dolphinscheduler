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

package org.apache.dolphinscheduler.plugin.task.api.ssh;

import org.apache.dolphinscheduler.plugin.task.api.model.SSHSessionHost;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * ACP pooled object: JSch Session Wrapper
 */
public class SSHSessionHolder {

    private static final Logger logger = LoggerFactory.getLogger(SSHSessionHolder.class);

    private static final String EXEC_TYPE = "exec";

    private static final String SFTP_TYPE = "sftp";

    private static final String SHELL_TYPE = "shell";

    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    private static final int KB_UNIT = 1024;

    public static final String SINGLE_SLASH = "/";

    private Session session;

    private final SSHSessionHost sessionHost;

    private final String id;

    private SftpConfig sftpConfig;

    public SSHSessionHolder(SSHSessionHost sessionHost) {
        this.sessionHost = sessionHost;
        this.id = UUID.randomUUID().toString();
        this.session = null;
    }

    public void connect() throws JSchException {
        this.connect(DEFAULT_CONNECT_TIMEOUT);
    }

    public void connect(int timeoutMills) throws JSchException {
        JSch jSch = new JSch();
        this.session = jSch.getSession(sessionHost.getAccount(), sessionHost.getIp(), sessionHost.getPort());
        this.session.setTimeout(timeoutMills);
        this.session.setConfig("StrictHostKeyChecking", "no");
        // other authorization methods can be considered in the future
        this.session.setPassword(sessionHost.getPassword());
        this.session.connect();
        logger.info("Connected to ssh session: {}, session id: {}", sessionHost, id);
    }

    public void disconnect() {
        if (session != null) {
            session.disconnect();
        }
    }

    public boolean isConnected() {
        return session.isConnected();
    }

    public void keepAlive() throws Exception {
        if (session != null) {
            session.sendKeepAliveMsg();
        }
    }

    public SSHResponse execCommand(String command) {
        return this.execCommand(command, -1, logger);
    }

    public SSHResponse execCommand(String command, long timeout, Logger logger) {
        return this.execCommand(createChannelExec(), command, timeout, logger);
    }

    public SSHResponse execCommand(ChannelExec channelExec, String command, long timeout, Logger customLogger) {
        customLogger.info("Executing command {} on session:{}", command, this);
        channelExec.setCommand(command);
        channelExec.setInputStream(null);
        channelExec.setErrStream(System.err);
        // it will kill process when channel disconnect, default true
        channelExec.setPty(true);

        SSHResponse response = new SSHResponse();
        try (
                InputStream in = channelExec.getInputStream();
                InputStream err = channelExec.getErrStream();) {
            channelExec.connect(DEFAULT_CONNECT_TIMEOUT);

            long timeoutSave = timeout > 0 ? timeout : Integer.MAX_VALUE;
            List<String> out = new ArrayList<String>();
            byte[] outSave = new byte[1024];
            while (true) {
                timeoutSave--;
                while (in.available() > 0) {
                    int i = in.read(outSave, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    String line = new String(outSave, 0, i, StandardCharsets.UTF_8);
                    out.add(line);
                    customLogger.info(line);
                }
                while (err.available() > 0) {
                    int i = err.read(outSave, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    String line = new String(outSave, 0, i, StandardCharsets.UTF_8);
                    out.add(line);
                    customLogger.error(line);
                }
                if (channelExec.isClosed() || channelExec.isEOF()) {
                    response.setExitCode(channelExec.getExitStatus());
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(1000);
                if (timeoutSave < 0) {
                    customLogger.error("Exec command {} on session {} timed out", command, this);
                    throw new SSHException("Exec command " + command + " on session " + this + " timed out");
                }
            }
            response.setOut(out);
            logger.info("Exec command {} on session {} finished, exit code: {}", command, this, response.getExitCode());
            return response;
        } catch (InterruptedException e) {
            logger.error("Exec command {} on session {} interrupted", command, this);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new SSHException("Exec command " + command + " on session " + this + " failed", e);
        } finally {
            close(channelExec);
        }
        return response;
    }

    public boolean sftpDir(String localDirPath, String remoteDirPath) {
        return sftpDir(localDirPath, remoteDirPath, logger);
    }

    public boolean sftpDir(String localDirPath, String remoteDirPath, Logger customLogger) {
        return sftpDir(createChannelSftp(), localDirPath, remoteDirPath, sftpConfig.isEnableUploadMonitor(),
                sftpConfig.getMaxUploadRate(), sftpConfig.getMaxFileSize(), customLogger);
    }

    /**
     * Sftp local directory to remote host,
     * @param channelSftp SSH sftp channel
     * @param localDirPath local directory path
     * @param remoteDirPath remote target directory path
     * @param enableUploadMonitor enable upload monitor thread
     * @param maxUploadRate max upload rate, if negative, will not limit
     * @param maxFileSize max file size, if negative, will not limit
     * @param customLogger custom logger, default local
     * @return sftp result
     */
    public boolean sftpDir(ChannelSftp channelSftp, String localDirPath, String remoteDirPath,
                           boolean enableUploadMonitor, int maxUploadRate, int maxFileSize, Logger customLogger) {
        customLogger.info("Start to sftp local dir: {} to {}:{}", localDirPath, sessionHost, remoteDirPath);

        File file = new File(localDirPath);
        if (!file.exists()) {
            customLogger.error("{} not exists.", localDirPath);
            return false;
        }

        try {
            channelSftp.connect(DEFAULT_CONNECT_TIMEOUT);
            try {
                channelSftp.cd(remoteDirPath);
            } catch (SftpException e) {
                if (!createDirOnRemote(remoteDirPath)) {
                    customLogger.error("Create directory:{} on remote:{} failed, so exit.", remoteDirPath,
                            sessionHost);
                    return false;
                }
            }

            long totalSize = file.length();

            if (maxFileSize >= 0) {
                if (totalSize > (long) maxFileSize * KB_UNIT * KB_UNIT) {
                    customLogger.error("The size of :{} has exceeded the maximum size:{}, size: {}", totalSize,
                            maxFileSize, localDirPath);
                    return false;
                }
            }

            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files == null || files.length == 0) {
                    customLogger.error("{} is a empty directory.", localDirPath);
                    return false;
                }
                for (File subFile : files) {
                    String sf = subFile.getCanonicalPath();
                    if (subFile.isDirectory()) {
                        String mkdirPath = remoteDirPath + "/" + subFile.getName();
                        try {
                            channelSftp.cd(mkdirPath);
                        } catch (SftpException e) {
                            if (!createDirOnRemote(mkdirPath)) {
                                customLogger.error("Could not create directory {} on remote session:{}", mkdirPath,
                                        sessionHost);
                                return false;
                            }
                        }
                        if (!sftpDir(createChannelSftp(), sf, mkdirPath, enableUploadMonitor, maxUploadRate, -1,
                                customLogger)) {
                            customLogger.error("sftp {} to {}:{} failed.", sf, sessionHost, mkdirPath);
                            return false;
                        }
                    } else {
                        upload(channelSftp, sf, remoteDirPath, enableUploadMonitor, maxUploadRate);
                    }
                }
            } else {
                upload(channelSftp, file.getCanonicalPath(), remoteDirPath, enableUploadMonitor, maxUploadRate);
            }
            return true;
        } catch (Exception e) {
            throw new SSHException(
                    "sftp " + localDirPath + " to " + sessionHost.toString() + ":" + remoteDirPath + " failed.", e);
        }
    }

    /**
     * Create a directory on the remote server
     * Because JSch does not support one-time creation of multi-layer directories, so just use `mkdir -p` instead
     * @param remoteDirPath remote directory path
     * @return result
     */
    public boolean createDirOnRemote(String remoteDirPath) {
        logger.info("create directory:{} on remote:{}", remoteDirPath, sessionHost);
        SSHResponse response = execCommand("mkdir -p " + remoteDirPath);
        return response.getExitCode() == 0;
    }

    public void clearPath(String path) {
        if (SINGLE_SLASH.equals(path)) {
            return;
        }
        execCommand("rm -rf " + path);
    }

    private void upload(ChannelSftp channelSftp, String src, String dst, boolean enableUploadMonitor,
                        int maxUploadRate) throws Exception {
        if (maxUploadRate != -1) {
            String fileName = new File(src).getName();
            dst += "/" + fileName;
            try (
                    OutputStream os = enableUploadMonitor
                            ? channelSftp.put(dst, new DSSftpProgressMonitor(new File(src).length(), src),
                                    ChannelSftp.OVERWRITE)
                            : channelSftp.put(dst, ChannelSftp.OVERWRITE)) {
                byte[] buffer = new byte[KB_UNIT * maxUploadRate];
                int read;
                if (os != null) {
                    try (FileInputStream fis = new FileInputStream(src)) {
                        do {
                            read = fis.read(buffer, 0, buffer.length);
                            if (read > 0) {
                                os.write(buffer, 0, read);
                            }
                            os.flush();
                        } while (read >= 0);
                    }
                }
            } ;
        } else {
            if (enableUploadMonitor) {
                channelSftp.put(src, dst, new DSSftpProgressMonitor(new File(src).length(), src),
                        ChannelSftp.OVERWRITE);
            } else {
                channelSftp.put(src, dst, ChannelSftp.OVERWRITE);
            }
        }
    }

    public ChannelExec createChannelExec() {
        try {
            return (ChannelExec) session.openChannel(EXEC_TYPE);
        } catch (JSchException e) {
            throw new SSHException("Create Jsch Session ChannelExec failed.", e);
        }
    }

    public ChannelSftp createChannelSftp() {
        try {
            return (ChannelSftp) session.openChannel(SFTP_TYPE);
        } catch (JSchException e) {
            throw new SSHException("Create Sftp Session ChannelExec failed.", e);
        }
    }

    public void close(Channel channel) {
        if (channel != null) {
            channel.disconnect();
        }
    }

    public void setSftpConfig(SftpConfig sftpConfig) {
        this.sftpConfig = sftpConfig;
    }

    @Override
    public String toString() {
        return "SSHSessionHolder{" +
                "sessionHost=" + sessionHost +
                ", id='" + id + '\'' +
                '}';
    }
}
