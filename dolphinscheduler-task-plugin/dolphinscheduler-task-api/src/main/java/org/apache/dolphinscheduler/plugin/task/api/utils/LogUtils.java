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

import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;


import org.slf4j.Logger;

@Slf4j
@UtilityClass
public class LogUtils {

    private static final Pattern APPLICATION_REGEX = Pattern.compile(TaskConstants.YARN_APPLICATION_REGEX);

    public List<String> getAppIdsFromLogFile(@NonNull String logPath) {
        return getAppIdsFromLogFile(logPath, log);
    }

    public List<String> getAppIdsFromLogFile(@NonNull String logPath, Logger logger) {
        File logFile = new File(logPath);
        if (!logFile.exists() || !logFile.isFile()) {
            return Collections.emptyList();
        }
        Set<String> appIds = new HashSet<>();
        try (Stream<String> stream = Files.lines(Paths.get(logPath))) {
            stream.filter(line -> {
                Matcher matcher = APPLICATION_REGEX.matcher(line);
                return matcher.find();
            }).forEach(line -> {
                Matcher matcher = APPLICATION_REGEX.matcher(line);
                if (matcher.find()) {
                    String appId = matcher.group();
                    if (appIds.add(appId)) {
                        logger.info("Find appId: {} from {}", appId, logPath);
                    }
                }
            });
            return new ArrayList<>(appIds);
        } catch (IOException e) {
            logger.error("Get appId from log file erro, logPath: {}", logPath, e);
            return Collections.emptyList();
        }
    }

    /**
     * find logs and kill yarn tasks.
     *
     * @param taskExecutionContext taskExecutionContext
     * @return yarn application ids
     */
    public static List<String> killYarnJob(@NonNull TaskExecutionContext taskExecutionContext) {
        if (taskExecutionContext.getLogPath() == null) {
            return Collections.emptyList();
        }
        try {
            Thread.sleep(TaskConstants.SLEEP_TIME_MILLIS);
            List<String> appIds = getAppIdsFromLogFile(taskExecutionContext.getLogPath());
            if (CollectionUtils.isNotEmpty(appIds)) {
                if (StringUtils.isEmpty(taskExecutionContext.getExecutePath())) {
                    taskExecutionContext
                        .setExecutePath(CommonUtils.getProcessExecDir(taskExecutionContext.getProjectCode(),
                            taskExecutionContext.getProcessDefineCode(),
                            taskExecutionContext.getProcessDefineVersion(),
                            taskExecutionContext.getProcessInstanceId(),
                            taskExecutionContext.getTaskInstanceId()));
                }
                CommonUtils.createWorkDirIfAbsent(taskExecutionContext.getExecutePath());
                cancelApplication(appIds, log, taskExecutionContext.getTenantCode(), taskExecutionContext.getExecutePath());
                return appIds;
            } else {
                log.info("The current appId is empty, don't need to kill the yarn job, taskInstanceId: {}", taskExecutionContext.getTaskInstanceId());
            }
        } catch (Exception e) {
            log.error("Kill yarn job failure, taskInstanceId: {}", taskExecutionContext.getTaskInstanceId(), e);
        }
        return Collections.emptyList();
    }

    /**
     * kill yarn application.
     *
     * @param appIds app id list
     * @param logger logger
     * @param tenantCode tenant code
     * @param executePath execute path
     */
    public static void cancelApplication(List<String> appIds, Logger logger, String tenantCode, String executePath) {
        if (appIds == null || appIds.isEmpty()) {
            return;
        }

        for (String appId : appIds) {
            try {
                String commandFile = String.format("%s/%s.kill", executePath, appId);
                String cmd = getKerberosInitCommand() + "yarn application -kill " + appId;
                execYarnKillCommand(logger, tenantCode, appId, commandFile, cmd);
            } catch (Exception e) {
                logger.error("Get yarn application app id [{}}] status failed", appId, e);
            }
        }
    }

    /**
     * get kerberos init command
     */
    public static String getKerberosInitCommand() {
        log.info("get kerberos init command");
        StringBuilder kerberosCommandBuilder = new StringBuilder();
        boolean hadoopKerberosState =
            PropertyUtils.getBoolean(TaskConstants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);
        if (hadoopKerberosState) {
            kerberosCommandBuilder.append("export KRB5_CONFIG=")
                .append(PropertyUtils.getString(TaskConstants.JAVA_SECURITY_KRB5_CONF_PATH))
                .append("\n\n")
                .append(String.format("kinit -k -t %s %s || true",
                    PropertyUtils.getString(TaskConstants.LOGIN_USER_KEY_TAB_PATH),
                    PropertyUtils.getString(TaskConstants.LOGIN_USER_KEY_TAB_USERNAME)))
                .append("\n\n");
            log.info("kerberos init command: {}", kerberosCommandBuilder);
        }
        return kerberosCommandBuilder.toString();
    }

    /**
     * build kill command for yarn application
     *
     * @param logger logger
     * @param tenantCode tenant code
     * @param appId app id
     * @param commandFile command file
     * @param cmd cmd
     */
    public static void execYarnKillCommand(Logger logger, String tenantCode, String appId, String commandFile,
                                            String cmd) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("#!/bin/sh\n");
            sb.append("BASEDIR=$(cd `dirname $0`; pwd)\n");
            sb.append("cd $BASEDIR\n");
            if (CommonUtils.getSystemEnvPath() != null) {
                sb.append("source ").append(CommonUtils.getSystemEnvPath()).append("\n");
            }
            sb.append("\n\n");
            sb.append(cmd);

            File f = new File(commandFile);

            if (!f.exists()) {
                org.apache.commons.io.FileUtils.writeStringToFile(new File(commandFile), sb.toString(),
                    StandardCharsets.UTF_8);
            }

            String runCmd = String.format("%s %s", TaskConstants.SH, commandFile);
            runCmd = OSUtils.getSudoCmd(tenantCode, runCmd);
            logger.info("kill cmd:{}", runCmd);
            OSUtils.exeCmd(runCmd);
        } catch (Exception e) {
            logger.error(String.format("Kill yarn application app id [%s] failed: [%s]", appId, e.getMessage()));
        }
    }
}
