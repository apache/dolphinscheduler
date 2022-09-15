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

package org.apache.dolphinscheduler.server.utils;

import lombok.NonNull;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.log.LogClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * mainly used to get the start command line of a process.
 */
public class ProcessUtils {

    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

    /**
     * Initialization regularization, solve the problem of pre-compilation performance,
     * avoid the thread safety problem of multi-thread operation
     */
    private static final Pattern MACPATTERN = Pattern.compile("-[+|-]-\\s(\\d+)");

    /**
     * Expression of PID recognition in Windows scene
     */
    private static final Pattern WINDOWSATTERN = Pattern.compile("\\w+\\((\\d+)\\)");

    /**
     * get pids str.
     *
     * @param processId process id
     * @return pids pid String
     * @throws Exception exception
     */
    public static String getPidsStr(int processId) throws Exception {
        List<String> pidList = new ArrayList<>();
        Matcher mat = null;
        // pstree pid get sub pids
        if (SystemUtils.IS_OS_MAC) {
            String pids = OSUtils.exeCmd(String.format("%s -sp %d", Constants.PSTREE, processId));
            if (null != pids) {
                mat = MACPATTERN.matcher(pids);
            }
        } else {
            String pids = OSUtils.exeCmd(String.format("%s -p %d", Constants.PSTREE, processId));
            mat = WINDOWSATTERN.matcher(pids);
        }

        if (null != mat) {
            while (mat.find()) {
                pidList.add(mat.group(1));
            }
        }

        if (CommonUtils.isSudoEnable() && !pidList.isEmpty()) {
            pidList = pidList.subList(1, pidList.size());
        }
        return String.join(" ", pidList).trim();
    }

    /**
     * find logs and kill yarn tasks.
     *
     * @param taskExecutionContext taskExecutionContext
     * @return yarn application ids
     */
    public static @Nullable List<String> killYarnJob(@NonNull LogClient logClient,
                                                     @NonNull TaskExecutionContext taskExecutionContext) {
        if (taskExecutionContext.getLogPath() == null) {
            return Collections.emptyList();
        }
        try {
            Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            Host host = Host.of(taskExecutionContext.getHost());
            List<String> appIds = logClient.getAppIds(host.getIp(), host.getPort(), taskExecutionContext.getLogPath());
            if (CollectionUtils.isNotEmpty(appIds)) {
                if (StringUtils.isEmpty(taskExecutionContext.getExecutePath())) {
                    taskExecutionContext
                            .setExecutePath(FileUtils.getProcessExecDir(taskExecutionContext.getProjectCode(),
                                    taskExecutionContext.getProcessDefineCode(),
                                    taskExecutionContext.getProcessDefineVersion(),
                                    taskExecutionContext.getProcessInstanceId(),
                                    taskExecutionContext.getTaskInstanceId()));
                }
                FileUtils.createWorkDirIfAbsent(taskExecutionContext.getExecutePath());
                LogUtils.cancelApplication(appIds, logger, taskExecutionContext.getTenantCode(), taskExecutionContext.getExecutePath());
                return appIds;
            } else {
                logger.info("The current appId is empty, don't need to kill the yarn job, taskInstanceId: {}", taskExecutionContext.getTaskInstanceId());
            }
        } catch (Exception e) {
            logger.error("Kill yarn job failure, taskInstanceId: {}", taskExecutionContext.getTaskInstanceId(), e);
        }
        return Collections.emptyList();
    }
}
