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

import static org.apache.dolphinscheduler.common.constants.Constants.EMPTY_STRING;
import static org.apache.dolphinscheduler.common.constants.Constants.SLEEP_TIME_MILLIS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_KILL;

import org.apache.dolphinscheduler.common.constants.TenantConstants;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.AbstractCommandExecutorConstants;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ProcessUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.fabric8.kubernetes.client.dsl.LogWatch;

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

    protected boolean processLogOutputIsSuccess = false;

    protected boolean podLogOutputIsFinished = false;

    /*
     * SHELL result string
     */
    protected String taskResultString;

    /**
     * taskRequest
     */
    protected TaskExecutionContext taskRequest;

    protected Future<?> taskOutputFuture;

    protected Future<?> podLogOutputFuture;

    public AbstractCommandExecutor(Consumer<LinkedBlockingQueue<String>> logHandler,
                                   TaskExecutionContext taskRequest,
                                   Logger logger) {
        this.logHandler = logHandler;
        this.taskRequest = taskRequest;
        this.logger = logger;
        this.logBuffer = new LinkedBlockingQueue<>();
        this.logBuffer.add(EMPTY_STRING);

        if (this.taskRequest != null) {
            // set logBufferEnable=true if the task uses logHandler and logBuffer to buffer log messages
            this.taskRequest.setLogBufferEnable(true);
        }
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
        // todo: Create a ShellExecuteClass to generate the shell and execute shell commands
        if (OSUtils.isSudoEnable() && !TenantConstants.DEFAULT_TENANT_CODE.equals(taskRequest.getTenantCode())) {
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
     * eg: sudo systemd-run -q --scope -p CPUQuota=100% -p MemoryLimit=200M --uid=root
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

        // use `man systemd.resource-control` to find available parameter
        if (memoryMax == -1) {
            command.add("-p");
            command.add(String.format("MemoryLimit=%s", "infinity"));
        } else {
            command.add("-p");
            command.add(String.format("MemoryLimit=%sM", taskRequest.getMemoryMax()));
        }

        command.add(String.format("--uid=%s", taskRequest.getTenantCode()));
    }

    public TaskResponse run(String execCommand, TaskCallBack taskCallBack) throws Exception {
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

        // build process
        buildProcess(commandFilePath);

        // parse process output
        parseProcessOutput(process);

        // collect pod log
        collectPodLogIfNeeded();

        int processId = getProcessId(process);

        result.setProcessId(processId);

        // cache processId
        taskRequest.setProcessId(processId);
        boolean updateTaskExecutionContextStatus =
                TaskExecutionContextCacheManager.updateTaskExecutionContext(taskRequest);
        if (Boolean.FALSE.equals(updateTaskExecutionContextStatus)) {
            result.setExitStatusCode(EXIT_CODE_KILL);
            cancelApplication();
            return result;
        }
        // print process id
        logger.info("process start, process id is: {}", processId);

        // if timeout occurs, exit directly
        long remainTime = getRemainTime();

        // update pid before waiting for the run to finish
        if (null != taskCallBack) {
            taskCallBack.updateTaskInstanceInfo(taskInstanceId);
        }

        // waiting for the run to finish
        boolean status = process.waitFor(remainTime, TimeUnit.SECONDS);

        TaskExecutionStatus kubernetesStatus =
                ProcessUtils.getApplicationStatus(taskRequest.getK8sTaskExecutionContext(), taskRequest.getTaskAppId());

        if (taskOutputFuture != null) {
            try {
                // Wait the task log process finished.
                taskOutputFuture.get();
            } catch (ExecutionException e) {
                logger.info("Handle task log error", e);
            }
        }

        if (podLogOutputFuture != null) {
            try {
                // Wait kubernetes pod log collection finished
                podLogOutputFuture.get();
                // delete pod after successful execution and log collection
                ProcessUtils.cancelApplication(taskRequest);
            } catch (ExecutionException e) {
                logger.error("Handle pod log error", e);
            }
        }

        // if SHELL task exit
        if (status && kubernetesStatus.isSuccess()) {

            // SHELL task state
            result.setExitStatusCode(process.exitValue());

        } else {
            logger.error("process has failure, the task timeout configuration value is:{}, ready to kill ...",
                    taskRequest.getTaskTimeout());
            result.setExitStatusCode(EXIT_CODE_FAILURE);
            cancelApplication();
        }
        int exitCode = process.exitValue();
        String exitLogMessage = EXIT_CODE_KILL == exitCode ? "process has killed." : "process has exited.";
        logger.info("{} execute path:{}, processId:{} ,exitStatusCode:{} ,processWaitForStatus:{} ,processExitValue:{}",
                exitLogMessage, taskRequest.getExecutePath(), processId, result.getExitStatusCode(), status, exitCode);
        return result;

    }

    public String getVarPool() {
        return varPool.toString();
    }

    public void cancelApplication() throws InterruptedException {
        if (process == null) {
            return;
        }

        // soft kill
        logger.info("Begin to kill process process, pid is : {}", taskRequest.getProcessId());
        process.destroy();
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            process.destroyForcibly();
        }
        logger.info("Success kill task: {}, pid: {}", taskRequest.getTaskAppId(), taskRequest.getProcessId());
    }

    private void printCommand(List<String> commands) {
        logger.info("task run command: {}", String.join(" ", commands));
    }

    private void collectPodLogIfNeeded() {
        if (null == taskRequest.getK8sTaskExecutionContext()) {
            podLogOutputIsFinished = true;
            return;
        }

        ExecutorService collectPodLogExecutorService = ThreadUtils
                .newSingleDaemonScheduledExecutorService("CollectPodLogOutput-thread-" + taskRequest.getTaskName());

        podLogOutputFuture = collectPodLogExecutorService.submit(() -> {
            // wait for launching (driver) pod
            ThreadUtils.sleep(SLEEP_TIME_MILLIS * 5L);
            try (
                    LogWatch watcher = ProcessUtils.getPodLogWatcher(taskRequest.getK8sTaskExecutionContext(),
                            taskRequest.getTaskAppId())) {
                if (watcher == null) {
                    throw new RuntimeException("The driver pod does not exist.");
                } else {
                    String line;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(watcher.getOutput()))) {
                        while ((line = reader.readLine()) != null) {
                            logBuffer.add(String.format("[K8S-pod-log-%s]: %s", taskRequest.getTaskName(), line));
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                podLogOutputIsFinished = true;
            }

        });

        collectPodLogExecutorService.shutdown();
    }

    private void parseProcessOutput(Process process) {
        // todo: remove this this thread pool.
        ExecutorService getOutputLogService = ThreadUtils
                .newSingleDaemonScheduledExecutorService("ResolveOutputLog-thread-" + taskRequest.getTaskName());
        getOutputLogService.submit(() -> {
            try (
                    final LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                            LogUtils.setTaskInstanceLogFullPathMDC(taskRequest.getLogPath());
                    BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
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
                processLogOutputIsSuccess = true;
            } catch (Exception e) {
                logger.error("Parse var pool error", e);
                processLogOutputIsSuccess = true;
            }
        });

        getOutputLogService.shutdown();

        ExecutorService parseProcessOutputExecutorService = ThreadUtils
                .newSingleDaemonScheduledExecutorService("TaskInstanceLogOutput-thread-" + taskRequest.getTaskName());
        taskOutputFuture = parseProcessOutputExecutorService.submit(() -> {
            try (
                    final LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                            LogUtils.setTaskInstanceLogFullPathMDC(taskRequest.getLogPath());) {
                while (logBuffer.size() > 1 || !processLogOutputIsSuccess || !podLogOutputIsFinished) {
                    if (logBuffer.size() > 1) {
                        logHandler.accept(logBuffer);
                        logBuffer.clear();
                        logBuffer.add(EMPTY_STRING);
                    } else {
                        Thread.sleep(TaskConstants.DEFAULT_LOG_FLUSH_INTERVAL);
                    }
                }
            } catch (Exception e) {
                logger.error("Output task log error", e);
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
        } catch (Exception e) {
            logger.error("Get task pid failed", e);
        }

        return processId;
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
