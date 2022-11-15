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

package org.apache.dolphinscheduler.plugin.task.api;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_KILL;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.SSHSessionHost;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.ssh.SSHException;
import org.apache.dolphinscheduler.plugin.task.api.ssh.SSHResponse;
import org.apache.dolphinscheduler.plugin.task.api.ssh.SSHSessionHolder;
import org.apache.dolphinscheduler.plugin.task.api.ssh.SSHSessionPool;
import org.apache.dolphinscheduler.plugin.task.api.utils.AbstractCommandExecutorConstants;
import org.apache.dolphinscheduler.plugin.task.api.utils.OSUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jcraft.jsch.ChannelExec;

/**
 * abstract command executor
 */
public abstract class AbstractCommandExecutor {

    /**
     * rules for extracting Var Pool
     */
    protected static final Pattern SETVALUE_REGEX = Pattern.compile(TaskConstants.SETVALUE_REGEX);

    protected StringBuilder varPool = new StringBuilder();
    /**
     * process
     */
    private Process process;

    /**
     * log handler
     */
    protected Consumer<LinkedBlockingQueue<String>> logHandler;

    /**
     * logger
     */
    protected Logger logger;

    /**
     * log list
     */
    protected LinkedBlockingQueue<String> logBuffer;

    protected boolean logOutputIsSuccess = false;

    /*
     * SHELL result string
     */
    protected String taskResultString;

    /**
     * taskRequest
     */
    protected TaskExecutionContext taskRequest;

    /**
     * running on ssh host flag
     */
    protected boolean runningOnSSH = false;

    /**
     * SSH session channel
     */
    protected ChannelExec channelExec;

    /**
     * SSH session host
     */
    protected SSHSessionHost sessionHost;

    /**
     * bash execution context
     */
    protected BashTaskExecutionContext bashTaskExecutionContext;

    public AbstractCommandExecutor(Consumer<LinkedBlockingQueue<String>> logHandler,
                                   TaskExecutionContext taskRequest,
                                   Logger logger) {
        this.logHandler = logHandler;
        this.taskRequest = taskRequest;
        if (taskRequest != null) {
            this.bashTaskExecutionContext = taskRequest.getBashTaskExecutionContext();
        }
        this.logger = logger;
        this.logBuffer = new LinkedBlockingQueue<>();
    }

    public AbstractCommandExecutor(LinkedBlockingQueue<String> logBuffer) {
        this.logBuffer = logBuffer;
    }

    /**
     * build process
     *
     * @param commandFile command file
     * @throws IOException IO Exception
     */
    private void buildProcess(String commandFile) throws IOException {
        // setting up user to run commands
        List<String> command = new LinkedList<>();

        // init process builder
        ProcessBuilder processBuilder = new ProcessBuilder();
        // setting up a working directory
        processBuilder.directory(new File(taskRequest.getExecutePath()));
        // merge error information to standard output stream
        processBuilder.redirectErrorStream(true);

        // if sudo.enable=true,setting up user to run commands
        if (OSUtils.isSudoEnable()) {
            if (SystemUtils.IS_OS_LINUX
                    && PropertyUtils.getBoolean(AbstractCommandExecutorConstants.TASK_RESOURCE_LIMIT_STATE)) {
                generateCgroupCommand(command);
            } else {
                command.add("sudo");
                command.add("-u");
                command.add(taskRequest.getTenantCode());
                command.add("-E");
            }
        }
        command.add(commandInterpreter());
        command.add(commandFile);

        // setting commands
        processBuilder.command(command);
        process = processBuilder.start();

        printCommand(command);
    }

    /**
     * generate systemd command.
     * eg: sudo systemd-run -q --scope -p CPUQuota=100% -p MemoryMax=200M --uid=root
     * @param command command
     */
    private void generateCgroupCommand(List<String> command) {
        Integer cpuQuota = taskRequest.getCpuQuota();
        Integer memoryMax = taskRequest.getMemoryMax();

        command.add("sudo");
        command.add("systemd-run");
        command.add("-q");
        command.add("--scope");

        if (cpuQuota == -1) {
            command.add("-p");
            command.add("CPUQuota=");
        } else {
            command.add("-p");
            command.add(String.format("CPUQuota=%s%%", taskRequest.getCpuQuota()));
        }

        if (memoryMax == -1) {
            command.add("-p");
            command.add(String.format("MemoryMax=%s", "infinity"));
        } else {
            command.add("-p");
            command.add(String.format("MemoryMax=%sM", taskRequest.getMemoryMax()));
        }

        command.add(String.format("--uid=%s", taskRequest.getTenantCode()));
    }

    public TaskResponse run(String execCommand) throws IOException, InterruptedException {
        TaskResponse result = new TaskResponse();
        int taskInstanceId = taskRequest.getTaskInstanceId();
        if (null == TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId)) {
            result.setExitStatusCode(EXIT_CODE_KILL);
            return result;
        }
        if (StringUtils.isEmpty(execCommand)) {
            TaskExecutionContextCacheManager.removeByTaskInstanceId(taskInstanceId);
            return result;
        }

        if (bashTaskExecutionContext.getSessionHost() != null) {
            runningOnSSH = true;
        }

        if (runningOnSSH && SystemUtils.IS_OS_WINDOWS) {
            logger.error("SSH does not support Windows systems");
            TaskExecutionContextCacheManager.removeByTaskInstanceId(taskInstanceId);
            return result;
        }

        String commandFilePath = buildCommandFilePath();

        // create command file if not exists
        createCommandFileIfNotExists(execCommand, commandFilePath);

        result = runCommandFile(commandFilePath);

        return result;
    }

    private TaskResponse runCommandFile(String commandFilePath) throws IOException, InterruptedException {
        return runningOnSSH ? runOnSSH(commandFilePath) : runOnLocalProcess(commandFilePath);
    }

    private TaskResponse runOnSSH(String commandFilePath) {
        TaskResponse result = new TaskResponse();
        SSHSessionHolder sessionHolder = null;
        this.sessionHost = bashTaskExecutionContext.getSessionHost();
        try {
            sessionHolder = SSHSessionPool.getSessionHolder(sessionHost);
            logger.info("borrow session:{} success", sessionHolder);

            boolean uploadRes =
                    sessionHolder.sftpDir(taskRequest.getExecutePath(), taskRequest.getExecutePath(), logger);
            if (!uploadRes) {
                logger.error("upload task {} execute path to remote session {} failed", taskRequest.getExecutePath(),
                        sessionHost);
                result.setExitStatusCode(EXIT_CODE_FAILURE);
                return result;
            }

            if (taskRequest.getEnvFile() != null) {
                boolean uploadEnvRes =
                        sessionHolder.sftpDir(taskRequest.getEnvFile(), taskRequest.getExecutePath(), logger);
                if (!uploadEnvRes) {
                    logger.error("upload task {} execute path to remote session {} failed", taskRequest.getEnvFile(),
                            sessionHost);
                    result.setExitStatusCode(EXIT_CODE_FAILURE);
                    return result;
                }
            }

            // because JSch .chmod is not stable, so use the 'chmod -R' instead
            logger.info("update remote path's permission:{} on session:{} to 755", taskRequest.getExecutePath(),
                    sessionHost);
            String chmodCommand = "chmod -R 700 " + taskRequest.getExecutePath();
            sessionHolder.execCommand(chmodCommand, getRemainTime(), logger);

            // all ssh task process id equals -1
            taskRequest.setProcessId(-1);
            channelExec = sessionHolder.createChannelExec();
            String command = "sh " + commandFilePath;
            SSHResponse response = sessionHolder.execCommand(channelExec, command, getRemainTime(), logger);
            result.setExitStatusCode(response.getExitCode());

            boolean updateTaskExecutionContextStatus =
                    TaskExecutionContextCacheManager.updateTaskExecutionContext(taskRequest);
            if (Boolean.FALSE.equals(updateTaskExecutionContextStatus)) {
                // it will exit process after we disconnect ssh channel, so no need use any kill command
                channelExec.disconnect();
                result.setExitStatusCode(EXIT_CODE_KILL);
                return result;
            }

            parseSSHResponseOut(response.getOut());

        } catch (SSHException e) {
            logger.error("Running task command on remote session:{} failed.",
                    sessionHost, e);
            result.setExitStatusCode(EXIT_CODE_FAILURE);
            return result;
        } catch (Exception e) {
            logger.error("Cannot get ssh session {} from SSHSession ACP pool",
                    sessionHost, e);
            result.setExitStatusCode(EXIT_CODE_FAILURE);
            return result;
        } finally {
            SSHSessionPool.returnSSHSessionHolder(sessionHost, sessionHolder);
            SSHSessionPool.printPoolStatus();
            if (sessionHolder != null) {
                sessionHolder.clearPath(taskRequest.getExecutePath());
            }
        }
        return result;
    }

    private TaskResponse runOnLocalProcess(String commandFilePath) throws InterruptedException, IOException {
        TaskResponse result = new TaskResponse();
        // build process
        buildProcess(commandFilePath);

        // parse process output
        parseProcessOutput(process);

        int processId = getProcessId(process);

        result.setProcessId(processId);

        // cache processId
        taskRequest.setProcessId(processId);
        boolean updateTaskExecutionContextStatus =
                TaskExecutionContextCacheManager.updateTaskExecutionContext(taskRequest);
        if (Boolean.FALSE.equals(updateTaskExecutionContextStatus)) {
            ProcessUtils.kill(taskRequest);
            result.setExitStatusCode(EXIT_CODE_KILL);
            return result;
        }
        // print process id
        logger.info("process start, process id is: {}", processId);

        // if timeout occurs, exit directly
        long remainTime = getRemainTime();

        // waiting for the run to finish
        boolean status = process.waitFor(remainTime, TimeUnit.SECONDS);

        // if SHELL task exit
        if (status) {

            // SHELL task state
            result.setExitStatusCode(process.exitValue());

        } else {
            logger.error("process has failure, the task timeout configuration value is:{}, ready to kill ...",
                    taskRequest.getTaskTimeout());
            ProcessUtils.kill(taskRequest);
            result.setExitStatusCode(EXIT_CODE_FAILURE);
        }

        logger.info(
                "process has exited, execute path:{}, processId:{} ,exitStatusCode:{} ,processWaitForStatus:{} ,processExitValue:{}",
                taskRequest.getExecutePath(), processId, result.getExitStatusCode(), status, process.exitValue());
        return result;
    }

    private void parseSSHResponseOut(List<String> lines) {
        if (CollectionUtils.isEmpty(lines)) {
            return;
        }
        for (String line : lines) {
            if (line.startsWith("${setValue(") || line.startsWith("#{setValue(")) {
                varPool.append(findVarPool(line));
                varPool.append("$VarPool$");
            }
        }
    }

    public String getVarPool() {
        return varPool.toString();
    }

    /**
     * cancel application
     *
     * @throws Exception exception
     */
    public void cancelApplication() throws Exception {
        if (runningOnSSH) {
            killSSHProcess();
        } else {
            killLocalProcess();
        }
    }

    private void killLocalProcess() {
        if (process == null) {
            return;
        }

        // clear log
        clear();

        int processId = getProcessId(process);

        logger.info("cancel process: {}", processId);

        // kill , waiting for completion
        boolean killed = softKill(processId);

        if (!killed) {
            // hard kill
            hardKill(processId);

            // destory
            process.destroy();

            process = null;
        }
    }

    private void killSSHProcess() {
        if (channelExec != null && channelExec.isConnected() && !channelExec.isEOF()) {
            channelExec.disconnect();
        }
    }

    /**
     * soft kill
     *
     * @param processId process id
     * @return process is alive
     */
    private boolean softKill(int processId) {

        if (processId != 0 && process.isAlive()) {
            try {
                // sudo -u user command to run command
                String cmd = String.format("kill %d", processId);
                cmd = OSUtils.getSudoCmd(taskRequest.getTenantCode(), cmd);
                logger.info("soft kill task:{}, process id:{}, cmd:{}", taskRequest.getTaskAppId(), processId, cmd);

                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                logger.info("kill attempt failed", e);
            }
        }

        return process.isAlive();
    }

    /**
     * hard kill
     *
     * @param processId process id
     */
    private void hardKill(int processId) {
        if (processId != 0 && process.isAlive()) {
            try {
                String cmd = String.format("kill -9 %d", processId);
                cmd = OSUtils.getSudoCmd(taskRequest.getTenantCode(), cmd);
                logger.info("hard kill task:{}, process id:{}, cmd:{}", taskRequest.getTaskAppId(), processId, cmd);

                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                logger.error("kill attempt failed ", e);
            }
        }
    }

    private void printCommand(List<String> commands) {
        logger.info("task run command: {}", String.join(" ", commands));
    }

    /**
     * clear
     */
    private void clear() {

        LinkedBlockingQueue<String> markerLog = new LinkedBlockingQueue<>(1);
        markerLog.add(ch.qos.logback.classic.ClassicConstants.FINALIZE_SESSION_MARKER.toString());

        if (!logBuffer.isEmpty()) {
            // log handle
            logHandler.accept(logBuffer);
            logBuffer.clear();
        }
        logHandler.accept(markerLog);
    }

    /**
     * get the standard output of the process
     *
     * @param process process
     */
    private void parseProcessOutput(Process process) {
        String threadLoggerInfoName = taskRequest.getTaskLogName();
        ExecutorService getOutputLogService = newDaemonSingleThreadExecutor(threadLoggerInfoName);
        getOutputLogService.submit(() -> {
            try (BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = inReader.readLine()) != null) {
                    if (line.startsWith("${setValue(") || line.startsWith("#{setValue(")) {
                        varPool.append(findVarPool(line));
                        varPool.append("$VarPool$");
                    } else {
                        logBuffer.add(line);
                        taskResultString = line;
                    }
                }
                logOutputIsSuccess = true;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                logOutputIsSuccess = true;
            }
        });

        getOutputLogService.shutdown();

        ExecutorService parseProcessOutputExecutorService = newDaemonSingleThreadExecutor(threadLoggerInfoName);
        parseProcessOutputExecutorService.submit(() -> {
            try {
                long lastFlushTime = System.currentTimeMillis();
                while (logBuffer.size() > 0 || !logOutputIsSuccess) {
                    if (logBuffer.size() > 0) {
                        lastFlushTime = flush(lastFlushTime);
                    } else {
                        Thread.sleep(TaskConstants.DEFAULT_LOG_FLUSH_INTERVAL);
                    }
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                logger.error(e.getMessage(), e);
            } finally {
                clear();
            }
        });
        parseProcessOutputExecutorService.shutdown();
    }

    /**
     * find var pool
     *
     * @param line
     * @return
     */
    private String findVarPool(String line) {
        Matcher matcher = SETVALUE_REGEX.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * get remain time（s）
     *
     * @return remain time
     */
    private long getRemainTime() {
        long usedTime = (System.currentTimeMillis() - taskRequest.getStartTime()) / 1000;
        long remainTime = taskRequest.getTaskTimeout() - usedTime;

        if (remainTime < 0) {
            throw new RuntimeException("task execution time out");
        }

        return remainTime;
    }

    /**
     * get process id
     *
     * @param process process
     * @return process id
     */
    private int getProcessId(Process process) {
        int processId = 0;

        try {
            Field f = process.getClass().getDeclaredField(TaskConstants.PID);
            f.setAccessible(true);

            processId = f.getInt(process);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }

        return processId;
    }

    /**
     * when log buffer siz or flush time reach condition , then flush
     *
     * @param lastFlushTime last flush time
     * @return last flush time
     */
    private long flush(long lastFlushTime) {
        long now = System.currentTimeMillis();

        /*
         * when log buffer siz or flush time reach condition , then flush
         */
        if (logBuffer.size() >= TaskConstants.DEFAULT_LOG_ROWS_NUM
                || now - lastFlushTime > TaskConstants.DEFAULT_LOG_FLUSH_INTERVAL) {
            lastFlushTime = now;
            logHandler.accept(logBuffer);

            logBuffer.clear();
        }
        return lastFlushTime;
    }

    protected abstract String buildCommandFilePath();

    protected abstract void createCommandFileIfNotExists(String execCommand, String commandFile) throws IOException;

    ExecutorService newDaemonSingleThreadExecutor(String threadName) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(threadName)
                .build();
        return Executors.newSingleThreadExecutor(threadFactory);
    }

    protected abstract String commandInterpreter();
}
