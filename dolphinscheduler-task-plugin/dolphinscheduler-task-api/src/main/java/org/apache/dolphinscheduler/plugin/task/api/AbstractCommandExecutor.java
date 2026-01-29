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

import static org.apache.dolphinscheduler.common.constants.Constants.SLEEP_TIME_MILLIS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_HARD_KILL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_KILL;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parser.TaskOutputParameterParser;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import io.fabric8.kubernetes.client.dsl.LogWatch;

/**
 * abstract command executor
 */
@Slf4j
public abstract class AbstractCommandExecutor {

    protected volatile Map<String, String> taskOutputParams = new HashMap<>();
    private Process process;

    protected TaskExecutionContext taskRequest;

    public AbstractCommandExecutor(TaskExecutionContext taskRequest) {
        this.taskRequest = taskRequest;
    }

    // todo: We need to build the IShellActuator in outer class, since different task may have specific logic to build
    // the IShellActuator
    public TaskResponse run(IShellInterceptorBuilder iShellInterceptorBuilder,
                            TaskCallBack taskCallBack) throws Exception {
        TaskResponse result = new TaskResponse();
        int taskInstanceId = taskRequest.getTaskInstanceId();
        // todo: we need to use state like JDK Thread to make sure the killed task should not be executed
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
        iShellInterceptorBuilder.runUser(taskRequest.getTenantCode());
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
        final CompletableFuture<Void> collectProcessLogFuture = collectProcessLog(this.process);

        // collect pod log
        final Optional<CompletableFuture<?>> collectPodLogFuture = collectPodLogIfNeeded();

        int processId = getProcessId(this.process);

        result.setProcessId(processId);

        // cache processId
        taskRequest.setProcessId(processId);

        // print process id
        log.info("process start, process id is: {}", processId);

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

        // Wait the task log process finished.
        collectProcessLogFuture.join();

        if (collectPodLogFuture.isPresent()) {
            // Wait kubernetes pod log collection finished
            collectPodLogFuture.get().join();
            // delete pod after successful execution and log collection
            ProcessUtils.cancelApplication(taskRequest);
        }

        // if SHELL task exit
        if (status && kubernetesStatus.isSuccess()) {

            // SHELL task state
            result.setExitStatusCode(this.process.exitValue());

        } else {
            log.error("process has failure due to timeout kill, timeout value is:{}, timeoutStrategy is:{}",
                    taskRequest.getTaskTimeout(), taskRequest.getTaskTimeoutStrategy());
            result.setExitStatusCode(EXIT_CODE_FAILURE);
        }
        int exitCode = this.process.exitValue();
        String exitLogMessage = (EXIT_CODE_KILL == exitCode || EXIT_CODE_HARD_KILL == exitCode) ? "process has killed."
                : "process has exited.";
        log.info("{} execute path:{}, processId:{} ,exitStatusCode:{} ,processWaitForStatus:{} ,processExitValue:{}",
                exitLogMessage, taskRequest.getExecutePath(), processId, result.getExitStatusCode(), status, exitCode);
        return result;

    }

    public Map<String, String> getTaskOutputParams() {
        return taskOutputParams;
    }

    public void cancelApplication() throws InterruptedException {
        if (process == null) {
            return;
        }

        // Try to kill process tree
        boolean killed = ProcessUtils.kill(taskRequest);
        if (killed) {
            log.info("Process tree for task: {} is killed or already finished, pid: {}",
                    taskRequest.getTaskAppId(), taskRequest.getProcessId());
        } else {
            log.error("Failed to kill process tree for task: {}, pid: {}",
                    taskRequest.getTaskAppId(), taskRequest.getProcessId());
        }

        // Try to kill yarn or k8s application
        ProcessUtils.cancelApplication(taskRequest);
    }

    private Optional<CompletableFuture<?>> collectPodLogIfNeeded() {
        if (null == taskRequest.getK8sTaskExecutionContext()) {
            return Optional.empty();
        }

        ExecutorService collectPodLogExecutorService = ThreadUtils
                .newSingleDaemonScheduledExecutorService("CollectPodLogOutput-thread-" + taskRequest.getTaskName());

        final CompletableFuture<Void> collectPodLogFuture = CompletableFuture.runAsync(() -> {
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
                            log.info("[K8S-pod-log-{}]: {}", taskRequest.getTaskName(), line);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Collect pod log error", e);
                throw new RuntimeException(e);
            }
        }, collectPodLogExecutorService);

        collectPodLogExecutorService.shutdown();
        return Optional.of(collectPodLogFuture);
    }

    private CompletableFuture<Void> collectProcessLog(Process process) {
        // todo: remove this this thread pool.
        final ExecutorService collectProcessLogService = ThreadUtils.newSingleDaemonScheduledExecutorService(
                "ResolveOutputLog-thread-" + taskRequest.getTaskName());
        final CompletableFuture<Void> collectProcessLogFuture = CompletableFuture.runAsync(() -> {
            TaskOutputParameterParser taskOutputParameterParser = new TaskOutputParameterParser();
            try (BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                LogUtils.setTaskInstanceLogFullPathMDC(taskRequest.getLogPath());
                String line;
                while ((line = inReader.readLine()) != null) {
                    log.info(" -> {}", line);
                    taskOutputParameterParser.appendParseLog(line);
                }
            } catch (Exception e) {
                log.error("Parse var pool error", e);
            } finally {
                LogUtils.removeTaskInstanceLogFullPathMDC();
            }
            taskOutputParams = taskOutputParameterParser.getTaskOutputParams();
        }, collectProcessLogService);

        collectProcessLogService.shutdown();
        return collectProcessLogFuture;
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
            log.error("Get task pid failed", e);
        }

        return processId;
    }

}
