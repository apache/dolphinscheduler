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

import static org.apache.dolphinscheduler.common.constants.Constants.APPID_COLLECT;
import static org.apache.dolphinscheduler.common.constants.Constants.DEFAULT_COLLECT_WAY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.COMMA;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SET_K8S;

import org.apache.dolphinscheduler.common.enums.ResourceManagerType;
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
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

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
    private static final Pattern MACPATTERN = Pattern.compile("-[+|-]-\\s(\\d+)");

    /**
     * Expression of PID recognition in Windows scene
     */
    private static final Pattern WINDOWSPATTERN = Pattern.compile("(\\d+)");

    /**
     * Expression of PID recognition in Linux scene
     */
    private static final Pattern LINUXPATTERN = Pattern.compile("\\((\\d+)\\)");

    /**
     * kill tasks according to different task types.
     */
    @Deprecated
    public static boolean kill(@NonNull TaskExecutionContext request) {
        try {
            log.info("Begin kill task instance, processId: {}", request.getProcessId());
            int processId = request.getProcessId();
            if (processId == 0) {
                log.error("Task instance kill failed, processId is not exist");
                return false;
            }

            String cmd = String.format("kill -9 %s", getPidsStr(processId));
            cmd = OSUtils.getSudoCmd(request.getTenantCode(), cmd);
            log.info("process id:{}, cmd:{}", processId, cmd);

            OSUtils.exeCmd(cmd);
            log.info("Success kill task instance, processId: {}", request.getProcessId());
            return true;
        } catch (Exception e) {
            log.error("Kill task instance error, processId: {}", request.getProcessId(), e);
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
        StringBuilder sb = new StringBuilder();
        Matcher mat = null;
        // pstree pid get sub pids
        if (SystemUtils.IS_OS_MAC) {
            String pids = OSUtils.exeCmd(String.format("%s -sp %d", TaskConstants.PSTREE, processId));
            if (StringUtils.isNotEmpty(pids)) {
                mat = MACPATTERN.matcher(pids);
            }
        } else if (SystemUtils.IS_OS_LINUX) {
            String pids = OSUtils.exeCmd(String.format("%s -p %d", TaskConstants.PSTREE, processId));
            if (StringUtils.isNotEmpty(pids)) {
                mat = LINUXPATTERN.matcher(pids);
            }
        } else {
            String pids = OSUtils.exeCmd(String.format("%s -p %d", TaskConstants.PSTREE, processId));
            if (StringUtils.isNotEmpty(pids)) {
                mat = WINDOWSPATTERN.matcher(pids);
            }
        }

        if (null != mat) {
            while (mat.find()) {
                sb.append(mat.group(1)).append(" ");
            }
        }

        return sb.toString().trim();
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
}
