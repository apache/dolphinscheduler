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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TaskProcessGroupUtils {

    private static final String SETSID_COMMAND = "setsid";

    private static final String SIGINT = "2";
    private static final String SIGTERM = "15";
    private static final String SIGKILL = "9";

    private TaskProcessGroupUtils() {
        throw new IllegalStateException("Utility class");
    }

    static boolean isEnabled() {
        return SystemUtils.IS_OS_LINUX
                && PropertyUtils.getBoolean(AbstractCommandExecutorConstants.TASK_PROCESS_GROUP_ENABLED,
                        AbstractCommandExecutorConstants.TASK_PROCESS_GROUP_ENABLED_DEFAULT);
    }

    public static List<String> prependSetsidIfEnabled(List<String> bootstrapCommand) {
        if (!isEnabled()) {
            return bootstrapCommand;
        }

        List<String> processGroupCommand = new ArrayList<>(bootstrapCommand.size() + 1);
        processGroupCommand.add(SETSID_COMMAND);
        processGroupCommand.addAll(bootstrapCommand);
        return processGroupCommand;
    }

    static boolean kill(int processGroupId, int timeoutSeconds) {
        if (!isProcessAlive(processGroupId)) {
            log.info("Process already terminated before killing process group, processId: {}", processGroupId);
            return true;
        }
        if (!isProcessGroupLeader(processGroupId)) {
            log.warn("Process is not a process group leader, fallback to pid tree, processId: {}", processGroupId);
            return false;
        }

        if (sendKillSignal(SIGINT, processGroupId, timeoutSeconds)) {
            log.info("Successfully killed process group by SIGINT, processGroupId: {}", processGroupId);
            return true;
        }

        if (sendKillSignal(SIGTERM, processGroupId, timeoutSeconds)) {
            log.info("Successfully killed process group by SIGTERM, processGroupId: {}", processGroupId);
            return true;
        }

        log.warn("Killing process group by SIGINT & SIGTERM failed, using SIGKILL as a last resort, processGroupId: {}",
                processGroupId);
        boolean killed = sendKillSignal(SIGKILL, processGroupId, timeoutSeconds);
        if (killed) {
            log.info("Successfully killed process group by SIGKILL, processGroupId: {}", processGroupId);
        } else {
            log.error("Error killing process group by SIGKILL, processGroupId: {}", processGroupId);
        }
        return killed;
    }

    private static boolean sendKillSignal(String signal, int processGroupId, int timeoutSeconds) {
        if (!isProcessGroupAlive(processGroupId)) {
            log.info("Process group already terminated, processGroupId: {}", processGroupId);
            return true;
        }

        String killCmd = withRootSudoIfEnabled(String.format("kill -%s -- -%d", signal, processGroupId));
        try {
            log.info("Sending {} to process group: {}, command: {}", signal, processGroupId, killCmd);
            OSUtils.exeCmd(killCmd);

            long timeoutMillis = TimeUnit.SECONDS.toMillis(timeoutSeconds);
            long startTime = System.currentTimeMillis();
            while (isProcessGroupAlive(processGroupId) && (System.currentTimeMillis() - startTime < timeoutMillis)) {
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
            }

            if (!isProcessGroupAlive(processGroupId)) {
                log.debug("Kill command: {}, process group kill succeeded", killCmd);
                return true;
            }
            log.info("Kill command: {}, timed out, process group is still running: {}", killCmd, processGroupId);
            return false;
        } catch (Exception e) {
            log.error("Error sending {} to process group: {}", signal, processGroupId, e);
            return false;
        }
    }

    private static boolean isProcessAlive(int processId) {
        try {
            OSUtils.exeCmd(withRootSudoIfEnabled(String.format("kill -0 %d", processId)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isProcessGroupLeader(int processId) {
        try {
            String processGroupId = OSUtils.exeCmd(String.format("ps -o pgid= -p %d", processId));
            return StringUtils.equals(String.valueOf(processId), StringUtils.trim(processGroupId));
        } catch (Exception e) {
            log.warn("Failed to verify process group leader, processId: {}", processId, e);
            return false;
        }
    }

    private static boolean isProcessGroupAlive(int processGroupId) {
        try {
            OSUtils.exeCmd(withRootSudoIfEnabled(String.format("kill -0 -- -%d", processGroupId)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String withRootSudoIfEnabled(String command) {
        if (OSUtils.isSudoEnable()) {
            return "sudo " + command;
        }
        return command;
    }
}
