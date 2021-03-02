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

package org.apache.dolphinscheduler.plugin.task.shell;

import static org.apache.dolphinscheduler.spi.task.Constants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.spi.task.Constants.EXIT_CODE_KILL;
import static org.apache.dolphinscheduler.spi.task.Constants.EXIT_CODE_SUCCESS;

import org.apache.dolphinscheduler.spi.task.Constants;
import org.apache.dolphinscheduler.spi.task.LoggerUtils;
import org.apache.dolphinscheduler.spi.task.Stopper;
import org.apache.dolphinscheduler.spi.task.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.spi.task.TaskRequest;
import org.apache.dolphinscheduler.spi.task.TaskResponse;
import org.apache.dolphinscheduler.spi.task.ThreadUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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


public class ShellCommandExecutor {

    /**
     * For Unix-like, using sh
     */
    public static final String SH = "sh";

    private Logger logger;

    /**
     * process
     */
    private Process process;

    protected StringBuilder varPool = new StringBuilder();


    protected boolean logOutputIsScuccess = false;

    /**
     * SHELL result string
     */
    protected String taskResultString;

    /**
     * log handler
     */
    protected Consumer<List<String>> logHandler;

    /**
     * rules for extracting application ID
     */
    protected static final Pattern APPLICATION_REGEX = Pattern.compile(Constants.APPLICATION_REGEX);


    /**
     * log list
     */
    protected List<String> logBuffer;

    public TaskResponse run(Consumer<List<String>> logHandler, String execCommand, TaskRequest req, Logger logger) throws IOException, InterruptedException {
        this.logger = logger;
        this.logHandler = logHandler;
        this.logBuffer = Collections.synchronizedList(new ArrayList<>());

        TaskResponse result = new TaskResponse();
        int taskInstanceId = req.getTaskInstanceId();
        if(null== TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId)){
            result.setExitStatusCode(EXIT_CODE_KILL);
            return result;
        }
        if (StringUtils.isEmpty(execCommand)) {
            TaskExecutionContextCacheManager.removeByTaskInstanceId(taskInstanceId);
            return result;
        }

        String commandFilePath = buildCommandFilePath(req.getExecutePath(), req.getTaskAppId());

        // create command file if not exists
        createCommandFileIfNotExists(execCommand, commandFilePath, req);

        //build process
        buildProcess(commandFilePath, req);

        // parse process output
        parseProcessOutput(process,req);

        Integer processId = getProcessId(process);

        result.setProcessId(processId);

        // cache processId
        req.setProcessId(processId);
        boolean updateTaskExecutionContextStatus = TaskExecutionContextCacheManager.updateTaskExecutionContext(req);
        if (Boolean.FALSE.equals(updateTaskExecutionContextStatus)) {
            ProcessUtils.kill(req);
            result.setExitStatusCode(EXIT_CODE_KILL);
            return result;
        }
        // print process id
        logger.info("process start, process id is: {}", processId);

        // if timeout occurs, exit directly
        long remainTime = getRemaintime(req);

        // waiting for the run to finish
        boolean status = process.waitFor(remainTime, TimeUnit.SECONDS);
        logger.info("process has exited, execute path:{}, processId:{} ,exitStatusCode:{}",
                req.getExecutePath(),
                processId
                , result.getExitStatusCode());

        // if SHELL task exit
        if (status) {
            // set appIds
            List<String> appIds = getAppIds(req.getLogPath());
            result.setAppIds(String.join(Constants.COMMA, appIds));

            // SHELL task state
            result.setExitStatusCode(process.exitValue());

            // if yarn task , yarn state is final state
            if (process.exitValue() == 0) {
                result.setExitStatusCode(isSuccessOfYarnState(appIds) ? EXIT_CODE_SUCCESS : EXIT_CODE_FAILURE);
            }
        } else {
            logger.error("process has failure , exitStatusCode : {} , ready to kill ...", result.getExitStatusCode());
            ProcessUtils.kill(req);
            result.setExitStatusCode(EXIT_CODE_FAILURE);
        }

        return result;

    }

    /**
     * check yarn state
     *
     * @param appIds application id list
     * @return is success of yarn task state
     */
    public boolean isSuccessOfYarnState(List<String> appIds) {
        boolean result = true;
        try {
            for (String appId : appIds) {
                while (Stopper.isRunning()) {
                    ExecutionStatus applicationStatus = HadoopUtils.getInstance().getApplicationStatus(appId);
                    logger.info("appId:{}, final state:{}", appId, applicationStatus.name());
                    if (applicationStatus.equals(ExecutionStatus.FAILURE)
                            || applicationStatus.equals(ExecutionStatus.KILL)) {
                        return false;
                    }

                    if (applicationStatus.equals(ExecutionStatus.SUCCESS)) {
                        break;
                    }
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("yarn applications: %s  status failed ", appIds.toString()), e);
            result = false;
        }
        return result;

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

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
            String line = null;
            while ((line = br.readLine()) != null) {
                lineList.add(line);
            }
        } catch (Exception e) {
            logger.error(String.format("read file: %s failed : ", filename), e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }

        }
        return lineList;
    }

    /**
     * get remain time（s）
     *
     * @return remain time
     */
    private long getRemaintime(TaskRequest request) {
        long usedTime = (System.currentTimeMillis() - request.getStartTime().getTime()) / 1000;
        long remainTime = request.getTaskTimeout() - usedTime;

        if (remainTime < 0) {
            throw new RuntimeException("task execution time out");
        }

        return remainTime;
    }

    /**
     * build process
     *
     * @param commandFile command file
     * @throws IOException IO Exception
     */
    private void buildProcess(String commandFile, TaskRequest req) throws IOException {
        // setting up user to run commands
        List<String> command = new LinkedList<>();

        //init process builder
        ProcessBuilder processBuilder = new ProcessBuilder();
        // setting up a working directory
        processBuilder.directory(new File(req.getExecutePath()));
        // merge error information to standard output stream
        processBuilder.redirectErrorStream(true);

        // setting up user to run commands
        command.add("sudo");
        command.add("-u");
        command.add(req.getTenantCode());
        command.add(SH);
        command.addAll(Collections.emptyList());
        command.add(commandFile);

        // setting commands
        processBuilder.command(command);
        process = processBuilder.start();

        // print command
        printCommand(command);
    }

    private String buildCommandFilePath(String executePath, String taskAppId) {
        // command file
        return String.format("%s/%s.command"
                , executePath
                , taskAppId
                // 不支持bat
        );
    }


    /**
     * create command file if not exists
     *
     * @param execCommand exec command
     * @param commandFile command file
     * @throws IOException io exception
     */
    private void createCommandFileIfNotExists(String execCommand, String commandFile, TaskRequest req) throws IOException {
        logger.info("tenantCode user:{}, task dir:{}", req.getTenantCode(),
                req.getTaskAppId());


        // create if non existence
        if (!Files.exists(Paths.get(commandFile))) {
            logger.info("create command file:{}", commandFile);

            StringBuilder sb = new StringBuilder();
            // os 判断前置

            sb.append("#!/bin/sh\n");
            sb.append("BASEDIR=$(cd `dirname $0`; pwd)\n");
            sb.append("cd $BASEDIR\n");
            if (req.getEnvFile() != null) {
                sb.append("source ").append(req.getEnvFile()).append("\n");
            }


            sb.append(execCommand);
            logger.info("command : {}", sb.toString());

            // write data to file
            FileUtils.writeStringToFile(new File(commandFile), sb.toString(), StandardCharsets.UTF_8);
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
     * get the standard output of the process
     *
     * @param process process
     */
    private void parseProcessOutput(Process process, TaskRequest req) {
        String threadLoggerInfoName = String.format(LoggerUtils.TASK_LOGGER_THREAD_NAME + "-%s", req.getTaskAppId());
        ExecutorService getOutputLogService = ThreadUtils.newDaemonSingleThreadExecutor(threadLoggerInfoName + "-" + "getOutputLogService");
        getOutputLogService.submit(() -> {
            BufferedReader inReader = null;
            try {
                inReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                logBuffer.add("welcome to use bigdata scheduling system...");
                while ((line = inReader.readLine()) != null) {
                    if (line.startsWith("${setValue(")) {
                        varPool.append(line.substring("${setValue(".length(), line.length() - 2));
                        varPool.append("$VarPool$");
                    } else {
                        logBuffer.add(line);
                        taskResultString = line;
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                logOutputIsScuccess = true;
                close(inReader);
            }
        });
        getOutputLogService.shutdown();

        ExecutorService parseProcessOutputExecutorService = ThreadUtils.newDaemonSingleThreadExecutor(threadLoggerInfoName);
        parseProcessOutputExecutorService.submit(() -> {
            try {
                long lastFlushTime = System.currentTimeMillis();
                while (logBuffer.size() > 0 || !logOutputIsScuccess) {
                    if (logBuffer.size() > 0) {
                        lastFlushTime = flush(lastFlushTime);
                    } else {
                        Thread.sleep(Constants.DEFAULT_LOG_FLUSH_INTERVAL);
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
    /**
     * get process id
     *
     * @param process process
     * @return process id
     */
    private int getProcessId(Process process) {
        int processId = 0;

        try {
            Field f = process.getClass().getDeclaredField(Constants.PID);
            f.setAccessible(true);

            processId = f.getInt(process);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }

        return processId;
    }

    /**
     * close buffer reader
     *
     * @param inReader in reader
     */
    private void close(BufferedReader inReader) {
        if (inReader != null) {
            try {
                inReader.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
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
        if (logBuffer.size() >= Constants.DEFAULT_LOG_ROWS_NUM || now - lastFlushTime > Constants.DEFAULT_LOG_FLUSH_INTERVAL) {
            lastFlushTime = now;
            /** log handle */
            logHandler.accept(logBuffer);

            logBuffer.clear();
        }
        return lastFlushTime;
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
}
