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

import static org.apache.dolphinscheduler.spi.task.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.EXIT_CODE_KILL;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.SH;

import org.apache.dolphinscheduler.plugin.task.util.LoggerUtils;
import org.apache.dolphinscheduler.plugin.task.util.OSUtils;
import org.apache.dolphinscheduler.plugin.task.util.ThreadUtils;
import org.apache.dolphinscheduler.spi.task.TaskConstants;
import org.apache.dolphinscheduler.spi.task.request.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

/**
 * abstract command executor
 */
public abstract class AbstractCommandExecutor {
    /**
     * rules for extracting application ID
     */
    protected static final Pattern APPLICATION_REGEX = Pattern.compile(TaskConstants.APPLICATION_REGEX);

    protected StringBuilder varPool = new StringBuilder();
    /**
     * process
     */
    private Process process;

    /**
     * log handler
     */
    protected Consumer<List<String>> logHandler;

    /**
     * logger
     */
    protected Logger logger;

    /**
     * log list
     */
    protected List<String> logBuffer;

    protected boolean logOutputIsSuccess = false;

    /*
     * SHELL result string
     */
    protected String taskResultString;

    /**
     * taskRequest
     */
    protected TaskRequest taskRequest;

    public AbstractCommandExecutor(Consumer<List<String>> logHandler,
                                   TaskRequest taskRequest,
                                   Logger logger) {
        this.logHandler = logHandler;
        this.taskRequest = taskRequest;
        this.logger = logger;
        this.logBuffer = Collections.synchronizedList(new ArrayList<>());
    }

    public AbstractCommandExecutor(List<String> logBuffer) {
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

        //init process builder
        ProcessBuilder processBuilder = new ProcessBuilder();
        // setting up a working directory
        processBuilder.directory(new File(taskRequest.getExecutePath()));
        // merge error information to standard output stream
        processBuilder.redirectErrorStream(true);

        // setting up user to run commands
        command.add("sudo");
        command.add("-u");
        command.add(taskRequest.getTenantCode());
        command.add(SH);
        command.addAll(Collections.emptyList());
        command.add(commandFile);

        // setting commands
        processBuilder.command(command);
        process = processBuilder.start();

        // print command
        printCommand(command);
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

        String commandFilePath = buildCommandFilePath();

        // create command file if not exists
        createCommandFileIfNotExists(execCommand, commandFilePath);

        //build process
        buildProcess(commandFilePath);

        // parse process output
        parseProcessOutput(process);

        Integer processId = getProcessId(process);

        result.setProcessId(processId);

        // cache processId
        taskRequest.setProcessId(processId);
        boolean updateTaskExecutionContextStatus = TaskExecutionContextCacheManager.updateTaskExecutionContext(taskRequest);
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
            // set appIds
            List<String> appIds = getAppIds(taskRequest.getLogPath());
            result.setAppIds(String.join(TaskConstants.COMMA, appIds));

            // SHELL task state
            result.setExitStatusCode(process.exitValue());

        } else {
            logger.error("process has failure , exitStatusCode:{}, processExitValue:{}, ready to kill ...",
                    result.getExitStatusCode(), process.exitValue());
            ProcessUtils.kill(taskRequest);
            result.setExitStatusCode(EXIT_CODE_FAILURE);
        }

        logger.info("process has exited, execute path:{}, processId:{} ,exitStatusCode:{} ,processWaitForStatus:{} ,processExitValue:{}",
                taskRequest.getExecutePath(), processId, result.getExitStatusCode(), status, process.exitValue());
        return result;

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

    /**
     * soft kill
     *
     * @param processId process id
     * @return process is alive
     * @throws InterruptedException interrupted exception
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

    /**
     * print command
     *
     * @param commands process builder
     */
    private void printCommand(List<String> commands) {
        String cmdStr;

        try {
            cmdStr = ProcessUtils.buildCommandStr(commands);
            logger.info("task run command:\n{}", cmdStr);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * clear
     */
    private void clear() {

        List<String> markerList = new ArrayList<>();
        markerList.add(ch.qos.logback.classic.ClassicConstants.FINALIZE_SESSION_MARKER.toString());

        if (!logBuffer.isEmpty()) {
            // log handle
            logHandler.accept(logBuffer);
            logBuffer.clear();
        }
        logHandler.accept(markerList);
    }

    /**
     * get the standard output of the process
     *
     * @param process process
     */
    private void parseProcessOutput(Process process) {
        String threadLoggerInfoName = String.format(LoggerUtils.TASK_LOGGER_THREAD_NAME + "-%s", taskRequest.getTaskAppId());
        ExecutorService getOutputLogService = ThreadUtils.newDaemonSingleThreadExecutor(threadLoggerInfoName + "-" + "getOutputLogService");
        getOutputLogService.submit(() -> {
            try (BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                logBuffer.add("welcome to use bigdata scheduling system...");
                while ((line = inReader.readLine()) != null) {
                    if (line.startsWith("${setValue(")) {
                        varPool.append(line, "${setValue(".length(), line.length() - 2);
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

        ExecutorService parseProcessOutputExecutorService = ThreadUtils.newDaemonSingleThreadExecutor(threadLoggerInfoName);
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
                logger.error(e.getMessage(), e);
            } finally {
                clear();
            }
        });
        parseProcessOutputExecutorService.shutdown();
    }

    public int getProcessId() {
        return getProcessId(process);
    }

    /**
     * get app links
     *
     * @param logPath log path
     * @return app id list
     */
    private List<String> getAppIds(String logPath) {
        List<String> logs = convertFile2List(logPath);

        List<String> appIds = new ArrayList<>();
        /**
         * analysis log?get submited yarn application id
         */
        for (String log : logs) {
            String appId = findAppId(log);
            if (StringUtils.isNotEmpty(appId) && !appIds.contains(appId)) {
                logger.info("find app id: {}", appId);
                appIds.add(appId);
            }
        }
        return appIds;
    }

    /**
     * convert file to list
     *
     * @param filename file name
     * @return line list
     */
    private List<String> convertFile2List(String filename) {
        List lineList = new ArrayList<String>(100);
        File file = new File(filename);

        if (!file.exists()) {
            return lineList;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineList.add(line);
            }
        } catch (Exception e) {
            logger.error(String.format("read file: %s failed : ", filename), e);
        }

        return lineList;
    }

    /**
     * find app id
     *
     * @param line line
     * @return appid
     */
    private String findAppId(String line) {
        Matcher matcher = APPLICATION_REGEX.matcher(line);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * get remain time（s）
     *
     * @return remain time
     */
    private long getRemainTime() {
        long usedTime = (System.currentTimeMillis() - taskRequest.getStartTime().getTime()) / 1000;
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

        /**
         * when log buffer siz or flush time reach condition , then flush
         */
        if (logBuffer.size() >= TaskConstants.DEFAULT_LOG_ROWS_NUM || now - lastFlushTime > TaskConstants.DEFAULT_LOG_FLUSH_INTERVAL) {
            lastFlushTime = now;
            /** log handle */
            logHandler.accept(logBuffer);

            logBuffer.clear();
        }
        return lastFlushTime;
    }

    protected List<String> commandOptions() {
        return Collections.emptyList();
    }

    protected abstract String buildCommandFilePath();

    protected abstract String commandInterpreter();

    protected abstract void createCommandFileIfNotExists(String execCommand, String commandFile) throws IOException;

    public String getTaskResultString() {
        return taskResultString;
    }

    public void setTaskResultString(String taskResultString) {
        this.taskResultString = taskResultString;
    }
}