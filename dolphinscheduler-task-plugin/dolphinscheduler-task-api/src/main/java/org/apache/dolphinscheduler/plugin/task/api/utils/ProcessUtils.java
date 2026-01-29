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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import static org.apache.dolphinscheduler.common.constants.Constants.SLEEP_TIME_MILLIS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.APPID_COLLECT;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.COMMA;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.DEFAULT_COLLECT_WAY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SET_K8S;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.am.ApplicationManager;
import org.apache.dolphinscheduler.plugin.task.api.am.KubernetesApplicationManager;
import org.apache.dolphinscheduler.plugin.task.api.am.KubernetesApplicationManagerContext;
import org.apache.dolphinscheduler.plugin.task.api.am.YarnApplicationManagerContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceManagerType;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import io.fabric8.kubernetes.client.dsl.LogWatch;

@Slf4j
public final class ProcessUtils {

    // If the shell process is still active after this timeout value (in seconds), then will use kill -9 to kill it
    private static final Integer SHELL_KILL_WAIT_TIMEOUT =
            PropertyUtils.getInt(Constants.SHELL_KILL_WAIT_TIMEOUT, 10);

    private ProcessUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final Map<ResourceManagerType, ApplicationManager> applicationManagerMap = new HashMap<>();

    static {
        ServiceLoader.load(ApplicationManager.class)
                .forEach(applicationManager -> applicationManagerMap.put(applicationManager.getResourceManagerType(),
                        applicationManager));
    }

    /**
     * Initialization regularization, solve the problem of pre-compilation performance,
     * avoid the thread safety problem of multi-thread operation
     */
    private static final Pattern MACPATTERN = Pattern.compile("-[+|-][-|=]\\s(\\d+)");

    /**
     * Expression of PID recognition in Windows scene
     */
    private static final Pattern WINDOWSPATTERN = Pattern.compile("\\((\\d+)\\)");

    /**
     * Expression of PID recognition in Linux scene
     */
    private static final Pattern LINUXPATTERN = Pattern.compile("\\((\\d+)\\)");

    /**
     * PID recognition pattern
     */
    private static final Pattern PID_PATTERN = Pattern.compile("\\s+");

    private static final String SIGINT = "2";
    private static final String SIGTERM = "15";
    private static final String SIGKILL = "9";

    /**
     * Terminate the task process, support multi-level signal processing and fallback strategy
     * @param request Task execution context
     * @return Whether the process was successfully terminated
     */
    public static boolean kill(@NonNull TaskExecutionContext request) {
        try {
            log.info("Begin killing task instance, processId: {}", request.getProcessId());
            int processId = request.getProcessId();
            if (processId == 0) {
                log.info("Task instance has already finished, no need to kill");
                return true;
            }

            // Get all child processes
            List<Integer> pidList = getPidList(processId);

            // 1. Try to terminate gracefully `kill -2`
            boolean gracefulKillSuccess = sendKillSignal(SIGINT, pidList, request.getTenantCode());
            if (gracefulKillSuccess) {
                log.info("Successfully killed process tree by SIGINT, processId: {}", processId);
                return true;
            }

            // 2. Try to terminate gracefully `kill -15`
            boolean termKillSuccess = sendKillSignal(SIGTERM, pidList, request.getTenantCode());
            if (termKillSuccess) {
                log.info("Successfully killed process tree by SIGTERM, processId: {}", processId);
                return true;
            }

            // 3. As a last resort, use `kill -9`
            log.warn("Killing process by SIGINT & SIGTERM failed, using SIGKILL as a last resort for processId: {}",
                    processId);
            boolean forceKillSuccess = sendKillSignal(SIGKILL, pidList, request.getTenantCode());
            if (forceKillSuccess) {
                log.info("Successfully killed process tree by SIGKILL, processId: {}", processId);
            } else {
                log.error("Error killing process tree by SIGKILL, processId: {}", processId);
            }
            return forceKillSuccess;

        } catch (Exception e) {
            log.error("Kill task instance error, processId: {}", request.getProcessId(), e);
            return false;
        }
    }

    /**
     * Send a kill signal to a process group
     * @param signal Signal type (SIGINT, SIGTERM, SIGKILL)
     * @param pidList Process ID list
     * @param tenantCode Tenant code
     */
    private static boolean sendKillSignal(String signal, List<Integer> pidList, String tenantCode) {
        if (pidList == null || pidList.isEmpty()) {
            log.info("No process needs to be killed.");
            return true;
        }

        List<Integer> alivePidList = getAlivePidList(pidList, tenantCode);
        if (alivePidList.isEmpty()) {
            log.info("All processes already terminated.");
            return true;
        }

        String pids = alivePidList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" "));

        try {
            // 1. Send the kill signal
            String killCmd = String.format("kill -%s %s", signal, pids);
            killCmd = OSUtils.getSudoCmd(tenantCode, killCmd);
            log.info("Sending {} to process group: {}, command: {}", signal, pids, killCmd);
            OSUtils.exeCmd(killCmd);

            // 2. Wait for the processes to terminate with a timeout-based polling mechanism
            // Max wait time
            long timeoutMillis = TimeUnit.SECONDS.toMillis(SHELL_KILL_WAIT_TIMEOUT);

            long startTime = System.currentTimeMillis();
            while (!alivePidList.isEmpty() && (System.currentTimeMillis() - startTime < timeoutMillis)) {
                // Remove if process is no longer alive
                alivePidList.removeIf(pid -> !isProcessAlive(pid, tenantCode));
                if (!alivePidList.isEmpty()) {
                    // Wait for a short interval before checking process statuses again, to avoid excessive CPU usage
                    // from tight-loop polling.
                    ThreadUtils.sleep(SLEEP_TIME_MILLIS);
                }
            }

            // 3. Return final result based on whether all processes were terminated
            if (alivePidList.isEmpty()) {
                // All processes have been successfully terminated
                log.debug("Kill command: {}, kill succeeded", killCmd);
                return true;
            } else {
                String remainingPids = alivePidList.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(" "));
                log.info("Kill command: {}, timed out, still running PIDs: {}", killCmd, remainingPids);
                return false;
            }
        } catch (Exception e) {
            log.error("Error sending {} to process: {}", signal, pids, e);
            return false;
        }
    }

    /**
     * Returns a list of process IDs that are still running.
     * This method filters the provided list of PIDs by checking whether each process is still active
     *
     * @param pidList   the list of process IDs to check
     * @param tenantCode the tenant identifier used for permission control or logging context
     * @return a new list containing only the PIDs of processes that are still running;
     *         returns an empty list if none are alive
     */
    private static List<Integer> getAlivePidList(List<Integer> pidList, String tenantCode) {
        return pidList.stream()
                .filter(pid -> isProcessAlive(pid, tenantCode))
                .collect(Collectors.toList());
    }

    /**
     * Check if a process with the specified PID is alive.
     *
     * @param pid the process ID to check
     * @return true if the process exists and is running, false otherwise
     */
    private static boolean isProcessAlive(int pid, String tenantCode) {
        try {
            // Use kill -0 to check if the process exists; it does not actually send a signal
            String checkCmd = String.format("kill -0 %d", pid);
            checkCmd = OSUtils.getSudoCmd(tenantCode, checkCmd);
            OSUtils.exeCmd(checkCmd);
            // If the command executes successfully, the process exists
            return true;
        } catch (Exception e) {
            // If the command fails, the process does not exist
            return false;
        }
    }

    /**
     * Get all descendant process IDs (including the given process) using pstree.
     *
     * @param processId the root process ID
     * @return list of process IDs; returns empty list if no PIDs found (e.g., process not exists)
     * @throws IllegalArgumentException if any PID is invalid (blank, non-numeric, or non-positive)
     * @throws Exception if command execution fails unexpectedly (e.g., command not found)
     */
    public static List<Integer> getPidList(int processId) throws Exception {
        String rawPidStr;

        try {
            if (SystemUtils.IS_OS_MAC) {
                rawPidStr = OSUtils.exeCmd(String.format("%s -sp %d", TaskConstants.PSTREE, processId));
            } else if (SystemUtils.IS_OS_LINUX) {
                rawPidStr = OSUtils.exeCmd(String.format("%s -p %d", TaskConstants.PSTREE, processId));
            } else {
                log.warn("Unsupported OS for pstree: {}. Attempting generic command.", SystemUtils.OS_NAME);
                rawPidStr = OSUtils.exeCmd(String.format("%s -p %d", TaskConstants.PSTREE, processId));
            }
        } catch (Exception ex) {
            log.error("Failed to execute 'pstree' command for process ID: {}", processId, ex);
            throw ex;
        }

        String pidsStr = parsePidStr(rawPidStr);
        if (StringUtils.isBlank(pidsStr)) {
            log.warn("No PIDs found for process: {}", processId);
            return Collections.emptyList();
        }

        String[] pidArray = PID_PATTERN.split(pidsStr.trim());
        if (pidArray.length == 0) {
            log.warn("No PIDs parsed for process: {}", processId);
            return Collections.emptyList();
        }

        List<Integer> pidList = new ArrayList<>();
        for (String pidStr : pidArray) {
            pidStr = pidStr.trim();

            if (StringUtils.isBlank(pidStr)) {
                log.error("Empty or blank PID found in output for process: {}, full PIDs string: {}", processId,
                        pidsStr);
                throw new IllegalArgumentException("Empty or blank PID found in output from process: " + processId);
            }

            try {
                int pid = Integer.parseInt(pidStr);
                if (pid <= 0) {
                    log.error("Invalid PID value (must be positive): {} for process: {}, full PIDs string: {}",
                            pidStr, processId, pidsStr);
                    throw new IllegalArgumentException("Invalid PID value (must be positive): " + pid);
                }
                pidList.add(pid);
            } catch (NumberFormatException e) {
                log.error("Invalid PID format in output: {} for process: {}, full PIDs string: {}",
                        pidStr, processId, pidsStr, e);
                throw new IllegalArgumentException(
                        "Invalid PID format in output: '" + pidStr + "' (from process " + processId + ")", e);
            }
        }

        return pidList;
    }

    public static String parsePidStr(String rawPidStr) {

        log.info("prepare to parse pid, raw pid string: {}", rawPidStr);
        ArrayList<String> allPidList = new ArrayList<>();
        Matcher mat = null;
        if (SystemUtils.IS_OS_MAC) {
            if (StringUtils.isNotEmpty(rawPidStr)) {
                mat = MACPATTERN.matcher(rawPidStr);
            }
        } else if (SystemUtils.IS_OS_LINUX) {
            if (StringUtils.isNotEmpty(rawPidStr)) {
                mat = LINUXPATTERN.matcher(rawPidStr);
            }
        } else {
            if (StringUtils.isNotEmpty(rawPidStr)) {
                mat = WINDOWSPATTERN.matcher(rawPidStr);
            }
        }
        if (null != mat) {
            while (mat.find()) {
                allPidList.add(mat.group(1));
            }
        }
        return String.join(" ", allPidList).trim();
    }

    /**
     * cancel k8s / yarn application
     *
     * @param taskExecutionContext
     * @return
     */
    public static void cancelApplication(TaskExecutionContext taskExecutionContext) {
        try {
            if (Objects.nonNull(taskExecutionContext.getK8sTaskExecutionContext())) {
                if (!TASK_TYPE_SET_K8S.contains(taskExecutionContext.getTaskType())) {
                    // Set empty container name for Spark on K8S task
                    applicationManagerMap.get(ResourceManagerType.KUBERNETES)
                            .killApplication(new KubernetesApplicationManagerContext(
                                    taskExecutionContext.getK8sTaskExecutionContext(),
                                    taskExecutionContext.getTaskAppId(), ""));
                }
            } else {
                String host = taskExecutionContext.getHost();
                String executePath = taskExecutionContext.getExecutePath();
                String tenantCode = taskExecutionContext.getTenantCode();
                List<String> appIds;
                if (StringUtils.isNotEmpty(taskExecutionContext.getAppIds())) {
                    // is failover
                    appIds = Arrays.asList(taskExecutionContext.getAppIds().split(COMMA));
                } else {
                    String logPath = taskExecutionContext.getLogPath();
                    String appInfoPath = taskExecutionContext.getAppInfoPath();
                    if (logPath == null || appInfoPath == null || executePath == null || tenantCode == null) {
                        log.error(
                                "Kill yarn job error, the input params is illegal, host: {}, logPath: {}, appInfoPath: {}, executePath: {}, tenantCode: {}",
                                host, logPath, appInfoPath, executePath, tenantCode);
                        throw new TaskException("Cancel application failed!");
                    }
                    log.info("Get appIds from worker {}, taskLogPath: {}", host, logPath);
                    appIds = LogUtils.getAppIds(logPath, appInfoPath,
                            PropertyUtils.getString(APPID_COLLECT, DEFAULT_COLLECT_WAY));
                    taskExecutionContext.setAppIds(String.join(TaskConstants.COMMA, appIds));
                }
                if (CollectionUtils.isEmpty(appIds)) {
                    log.info("The appId is empty");
                    return;
                }
                ApplicationManager applicationManager = applicationManagerMap.get(ResourceManagerType.YARN);
                applicationManager.killApplication(new YarnApplicationManagerContext(executePath, tenantCode, appIds));
                log.info("yarn application [{}] is killed or already finished", appIds);
            }
        } catch (Exception e) {
            log.error("Cancel application failed: {}", e.getMessage());
        }
    }

    /**
     * get k8s application status
     *
     * @param k8sTaskExecutionContext
     * @param taskAppId
     * @return
     */
    public static TaskExecutionStatus getApplicationStatus(K8sTaskExecutionContext k8sTaskExecutionContext,
                                                           String taskAppId) {
        if (Objects.isNull(k8sTaskExecutionContext)) {
            return TaskExecutionStatus.SUCCESS;
        }
        KubernetesApplicationManager applicationManager =
                (KubernetesApplicationManager) applicationManagerMap.get(ResourceManagerType.KUBERNETES);
        return applicationManager
                .getApplicationStatus(new KubernetesApplicationManagerContext(k8sTaskExecutionContext, taskAppId, ""));
    }

    /**
     * get driver pod logs
     *
     * @param k8sTaskExecutionContext
     * @param taskAppId
     * @return
     */
    public static LogWatch getPodLogWatcher(K8sTaskExecutionContext k8sTaskExecutionContext, String taskAppId,
                                            String containerName) {
        KubernetesApplicationManager applicationManager =
                (KubernetesApplicationManager) applicationManagerMap.get(ResourceManagerType.KUBERNETES);

        return applicationManager
                .getPodLogWatcher(
                        new KubernetesApplicationManagerContext(k8sTaskExecutionContext, taskAppId, containerName));
    }

    public static void removeK8sClientCache(String taskAppId) {
        KubernetesApplicationManager applicationManager =
                (KubernetesApplicationManager) applicationManagerMap.get(ResourceManagerType.KUBERNETES);
        applicationManager.removeCache(taskAppId);
    }
}
