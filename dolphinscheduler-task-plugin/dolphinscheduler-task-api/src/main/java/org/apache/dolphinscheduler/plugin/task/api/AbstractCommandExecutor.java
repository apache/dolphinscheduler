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
import static org.apache.dolphinscheduler.common.constants.Constants.APPID_COLLECT;
import static org.apache.dolphinscheduler.common.constants.Constants.DEFAULT_COLLECT_WAY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.*;

import org.apache.dolphinscheduler.common.constants.TenantConstants;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.shell.IShellInterceptor;
import org.apache.dolphinscheduler.plugin.task.api.shell.IShellInterceptorBuilder;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ProcessUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ShellUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

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

    // todo: We need to build the IShellActuator in outer class, since different task may have specific logic to build
    // the IShellActuator
    public TaskResponse run(IShellInterceptorBuilder iShellInterceptorBuilder,
                            TaskCallBack taskCallBack) throws Exception {
        TaskResponse result = new TaskResponse();
        int taskInstanceId = taskRequest.getTaskInstanceId();
        if (null == TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId)) {
            logger.warn(
                    "Cannot find the taskInstance: {} from TaskExecutionContextCacheManager, the task might already been killed",
                    taskInstanceId);
            result.setExitStatusCode(EXIT_CODE_KILL);
            return result;
        }
        // the task instance needs fault tolerance
        if(Objects.nonNull(taskRequest.getProcessId()) || StringUtils.isNotEmpty(taskRequest.getAppIds())){

            int pid = taskRequest.getProcessId();
            // Yarn task is determined by parsing whether the task log contains the content of the application
            String applicationId  = String.join(TaskConstants.COMMA, LogUtils.getAppIds(taskRequest.getLogPath(), taskRequest.getAppInfoPath(),
                    PropertyUtils.getString(APPID_COLLECT, DEFAULT_COLLECT_WAY)));
            boolean isRunningTaskFaultTolerance = false;
            if(applicationId.isEmpty()){
                // not a Yarn task
                isRunningTaskFaultTolerance = isProcessRunning(pid);
            }else {
                // is a Yarn task
                isRunningTaskFaultTolerance = isApplicationRunning(applicationId);
            }
            //determines whether the task is running
            if(Boolean.TRUE.equals(isRunningTaskFaultTolerance)){
                logger.warn(
                        "Can not recover tolerance fault task instance: {} , the task may be running and does not need to be repeated",
                        taskInstanceId);
                result.setExitStatusCode(EXIT_CODE_KILL);
                cancelApplication();
                return result;
            }
        }

        iShellInterceptorBuilder = iShellInterceptorBuilder
                .shellDirectory(taskRequest.getExecutePath())
                .shellName(taskRequest.getTaskAppId());
        // Set system env
        if (CollectionUtils.isNotEmpty(ShellUtils.ENV_SOURCE_LIST)) {
            ShellUtils.ENV_SOURCE_LIST.forEach(iShellInterceptorBuilder::appendSystemEnv);
        }
        // Set custom env
        if (StringUtils.isNotBlank(taskRequest.getEnvironmentConfig())) {
            iShellInterceptorBuilder.appendCustomEnvScript(taskRequest.getEnvironmentConfig());
        }
        // Set k8s config (This is only work in Linux)
        if (taskRequest.getK8sTaskExecutionContext() != null) {
            iShellInterceptorBuilder.k8sConfigYaml(taskRequest.getK8sTaskExecutionContext().getConfigYaml());
        }
        // Set sudo (This is only work in Linux)
        iShellInterceptorBuilder.sudoMode(OSUtils.isSudoEnable());
        // Set tenant (This is only work in Linux)
        if (TenantConstants.DEFAULT_TENANT_CODE.equals(taskRequest.getTenantCode())) {
            iShellInterceptorBuilder.runUser(TenantConstants.BOOTSTRAPT_SYSTEM_USER);
        } else {
            iShellInterceptorBuilder.runUser(taskRequest.getTenantCode());
        }
        // Set CPU Quota (This is only work in Linux)
        if (taskRequest.getCpuQuota() != null) {
            iShellInterceptorBuilder.cpuQuota(taskRequest.getCpuQuota());
        }
        // Set memory Quota (This is only work in Linux)
        if (taskRequest.getMemoryMax() != null) {
            iShellInterceptorBuilder.memoryQuota(taskRequest.getMemoryMax());
        }

        IShellInterceptor iShellInterceptor = iShellInterceptorBuilder.build();
        process = iShellInterceptor.execute();

        // parse process output
        parseProcessOutput(this.process);

        // collect pod log
        collectPodLogIfNeeded();

        int processId = getProcessId(this.process);

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
        boolean status = this.process.waitFor(remainTime, TimeUnit.SECONDS);

        TaskExecutionStatus kubernetesStatus =
                ProcessUtils.getApplicationStatus(taskRequest.getK8sTaskExecutionContext(), taskRequest.getTaskAppId());

        if (taskOutputFuture != null) {
            try {
                // Wait the task log process finished.
                taskOutputFuture.get();
            } catch (ExecutionException e) {
                logger.error("Handle task log error", e);
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
            result.setExitStatusCode(this.process.exitValue());

        } else {
            logger.error("process has failure, the task timeout configuration value is:{}, ready to kill ...",
                    taskRequest.getTaskTimeout());
            result.setExitStatusCode(EXIT_CODE_FAILURE);
            cancelApplication();
        }
        int exitCode = this.process.exitValue();
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
                            taskRequest.getTaskAppId(), "")) {
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

    /**
     * determines whether the process is running
     *
     * @param pid process_id
     * @return boolean
     */
    public boolean isProcessRunning(int pid) throws Exception {

        String processPath = String.valueOf(pid);

        // build shell commands, use ps-ef to list all processes, and use GREP filters to match the process id
        String[] command = { "/bin/sh", "-c", "ps -ef | grep " + processPath };

        // use the ProcessBuilder class to create a new process and set its command
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        // start the process
        Process process = processBuilder.start();

        // reads the output of the command and gets the standard input stream for the process
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        StringBuilder output = new StringBuilder();
        // reads each line in the process input stream
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }
        try {
            // gets the exit code for the command.
            int exitCode = process.waitFor();
            // if the exit code is 0, and the output contains process id, the process exists.
            if (exitCode == 0 && output.toString().contains(processPath)) {
                // process is running
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * determine if the yarn task is running
     *
     * @param applicationId applicationId
     * @return boolean
     */
    public boolean isApplicationRunning(String applicationId ) throws Exception {

        // build shell commands, use yarn application -status applicationId
        String[] command = {"/bin/bash", "-c", "yarn application -status " + applicationId};

        // use the ProcessBuilder class to create a new process and set its command
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        // start the process
        Process process = processBuilder.start();

        // reads the output of the command and gets the standard input stream for the process
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        String state = null;
        // reads each line in the process input stream
        while ((line = reader.readLine()) != null) {
            // get the yarn task running status
            if (line.contains("State")) {
                state = line.split(":")[1].trim();
                break;
            }
        }
        try {
            // gets the exit code for the command.
            int exitCode = process.waitFor();
            // if the exit code is 0, and the yarn task is running
            if (exitCode == 0 && state.equals("RUNNING")) {
                // process is running
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;

    }

}
