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

import static org.apache.dolphinscheduler.spi.task.Constants.EXIT_CODE_KILL;

import org.apache.dolphinscheduler.spi.task.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.spi.task.TaskRequest;
import org.apache.dolphinscheduler.spi.task.TaskResponse;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

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


    /**
     * log list
     */
    protected final List<String> logBuffer;

    public TaskResponse run(String execCommand, TaskRequest req, Logger logger) throws IOException {
        this.logger = logger;
        //  this.logBuffer=req.get

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
        // 需要从缓存判断是否被Kill  因此 此缓存需要下沉到task层面 todo taskExecutionContextCacheManager
        String commandFilePath = buildCommandFilePath(req.getExecutePath(), req.getTaskAppId());


        // create command file if not exists
        createCommandFileIfNotExists(execCommand, commandFilePath, req);

        //build process
        buildProcess(commandFilePath, req);

        // parse process output
        parseProcessOutput(process);

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

        return result;

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

}
