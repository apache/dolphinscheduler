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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.APPID_COLLECT;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.COMMA;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.DEFAULT_COLLECT_WAY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SET_K8S;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import io.fabric8.kubernetes.client.dsl.LogWatch;

@Slf4j
public final class ProcessUtils {

    // Wait 5 seconds to check process status
    private static final int CHECK_PROCESS_WAITING_MILLIS = 5000;

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
    private static final Pattern WINDOWSPATTERN = Pattern.compile("(\\d+)");

    /**
     * Expression of PID recognition in Linux scene
     */
    private static final Pattern LINUXPATTERN = Pattern.compile("\\((\\d+)\\)");

    /**
     * PID recognition pattern
     */
    private static final Pattern PID_PATTERN = Pattern.compile("\\s+");

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
            String pids = getPidsStr(processId);
            String[] pidArray = PID_PATTERN.split(pids);
            if (pidArray.length == 0) {
                log.warn("No valid PIDs found for process: {}", processId);
                return true;
            }

            // 1. Try to terminate gracefully (SIGINT)
            boolean gracefulKillSuccess = sendKillSignal("SIGINT", pids, request.getTenantCode());
            if (gracefulKillSuccess) {
                log.info("Successfully killed process tree using SIGINT, processId: {}", processId);
                return true;
            }

            // 2. Try to terminate forcefully (SIGTERM)
            boolean termKillSuccess = sendKillSignal("SIGTERM", pids, request.getTenantCode());
            if (termKillSuccess) {
                log.info("Successfully killed process tree using SIGTERM, processId: {}", processId);
                return true;
            }

            // 3. As a last resort, use `kill -9`
            log.warn("SIGINT & SIGTERM failed, using SIGKILL as a last resort for processId: {}", processId);
            boolean forceKillSuccess = sendKillSignal("SIGKILL", pids, request.getTenantCode());
            if (forceKillSuccess) {
                log.info("Successfully sent SIGKILL signal to process tree, processId: {}", processId);
            } else {
                log.error("Error sending SIGKILL signal to process tree, processId: {}", processId);
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
     * @param pids Process ID list
     * @param tenantCode Tenant code
     */
    private static boolean sendKillSignal(String signal, String pids, String tenantCode) {
        try {
            // 1. Send the kill signal
            String killCmd = String.format("kill -s %s %s", signal, pids);
            log.info("Kill command: {}, trying to terminate process", killCmd);
            killCmd = OSUtils.getSudoCmd(tenantCode, killCmd);
            log.info("Sending {} to process group: {}, command: {}", signal, pids, killCmd);
            OSUtils.exeCmd(killCmd);

            // 2. Wait for the process to respond to the signal
            Thread.sleep(CHECK_PROCESS_WAITING_MILLIS);

            // 3. Check if the processes are still running
            String[] pidArray = PID_PATTERN.split(pids);
            for (String pid : pidArray) {
                // Check if each PID is still alive
                if (isProcessAlive(Integer.parseInt(pid))) {
                    log.info("Kill command: {}, kill failed, the process: {} is still running", killCmd, pid);
                    // Return false if any process is still alive
                    return false;
                }
            }
            log.info("Kill command: {}, kill succeeded", killCmd);
            // All processes have been successfully terminated
            return true;
        } catch (Exception e) {
            log.error("Error sending {} to process: {}", signal, pids, e);
            return false;
        }
    }

    /**
     * Check if a process with the specified PID is alive.
     *
     * @param pid the process ID to check
     * @return true if the process exists and is running, false otherwise
     */
    private static boolean isProcessAlive(int pid) {
        try {
            // Use kill -0 to check if the process exists; it does not actually send a signal
            String checkCmd = String.format("kill -0 %d", pid);
            OSUtils.exeCmd(checkCmd);
            // If the command executes successfully, the process exists
            return true;
        } catch (Exception e) {
            // If the command fails, the process does not exist
            return false;
        }
    }

    /**
     * get pids str.
     *
     * @param processId process id
     * @return pids pid String
     * @throws Exception exception
     */
    public static String getPidsStr(int processId) throws Exception {

        String rawPidStr;

        // pstree pid get sub pids
        if (SystemUtils.IS_OS_MAC) {
            rawPidStr = OSUtils.exeCmd(String.format("%s -sp %d", TaskConstants.PSTREE, processId));
        } else if (SystemUtils.IS_OS_LINUX) {
            rawPidStr = OSUtils.exeCmd(String.format("%s -p %d", TaskConstants.PSTREE, processId));
        } else {
            rawPidStr = OSUtils.exeCmd(String.format("%s -p %d", TaskConstants.PSTREE, processId));
        }

        return parsePidStr(rawPidStr);
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
                    log.info("The appId is empty, so there is no need to kill yarn application.");
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
