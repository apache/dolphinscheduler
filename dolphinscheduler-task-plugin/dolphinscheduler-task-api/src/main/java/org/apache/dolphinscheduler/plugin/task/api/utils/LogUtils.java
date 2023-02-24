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
import org.apache.dolphinscheduler.common.constants.DateConstants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.log.TaskLogDiscriminator;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachable;

@Slf4j
@UtilityClass
public class LogUtils {

    private static final String LOG_TAILFIX = ".log";
    private static final Pattern APPLICATION_REGEX = Pattern.compile(TaskConstants.YARN_APPLICATION_REGEX);

    public List<String> getAppIds(@NonNull String logPath, @NonNull String appInfoPath, String fetchWay) {
        if (!StringUtils.isEmpty(fetchWay) && fetchWay.equals("aop")) {
            log.info("Start finding appId in {}, fetch way: {} ", appInfoPath);
            return getAppIdsFromAppInfoFile(appInfoPath, log);
        } else {
            log.info("Start finding appId in {}, fetch way: {} ", logPath);
            return getAppIdsFromLogFile(logPath, log);
        }
    }

    public static String getTaskLogPath(TaskExecutionContext taskExecutionContext) {
        return getTaskLogPath(DateUtils.timeStampToDate(taskExecutionContext.getFirstSubmitTime()),
                taskExecutionContext.getProcessDefineCode(),
                taskExecutionContext.getProcessDefineVersion(),
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId());
    }

    public static String getTaskLogPath(Date firstSubmitTime,
                                        Long processDefineCode,
                                        int processDefineVersion,
                                        int processInstanceId,
                                        int taskInstanceId) {
        // format /logs/YYYYMMDD/defintion-code_defintion_version-processInstanceId-taskInstanceId.log
        final String taskLogFileName = new StringBuilder(String.valueOf(processDefineCode))
                .append(Constants.UNDERLINE)
                .append(processDefineVersion)
                .append(Constants.SUBTRACT_CHAR)
                .append(processInstanceId)
                .append(Constants.SUBTRACT_CHAR)
                .append(taskInstanceId)
                .append(LOG_TAILFIX)
                .toString();
        // Optional.map will be skipped if null
        return Optional.of(LoggerFactory.getILoggerFactory())
                .map(e -> (AppenderAttachable<ILoggingEvent>) (e.getLogger("ROOT")))
                .map(e -> (SiftingAppender) (e.getAppender("TASKLOGFILE")))
                .map(e -> ((TaskLogDiscriminator) (e.getDiscriminator())))
                .map(TaskLogDiscriminator::getLogBase)
                .map(e -> Paths.get(e)
                        .toAbsolutePath()
                        .resolve(DateUtils.format(firstSubmitTime, DateConstants.YYYYMMDD, null))
                        .resolve(taskLogFileName))
                .map(Path::toString)
                .orElse("");
    }

    public static String buildTaskId(Date firstSubmitTime,
                                     Long processDefineCode,
                                     int processDefineVersion,
                                     int processInstId,
                                     int taskId) {
        // like TaskAppId=TASK-20211107-798_1-4084-15210
        String firstSubmitTimeStr = DateUtils.format(firstSubmitTime, DateConstants.YYYYMMDD, null);
        return String.format("%s=%s-%s-%s_%s-%s-%s",
                TaskConstants.TASK_APPID_LOG_FORMAT, TaskConstants.TASK_LOGGER_INFO_PREFIX, firstSubmitTimeStr,
                processDefineCode, processDefineVersion, processInstId, taskId);
    }

    public List<String> getAppIdsFromAppInfoFile(@NonNull String appInfoPath, Logger logger) {
        File appInfoFile = new File(appInfoPath);
        if (!appInfoFile.exists() || !appInfoFile.isFile()) {
            return Collections.emptyList();
        }
        List<String> appIds = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(appInfoPath))) {
            stream.forEach(appIds::add);
            return new ArrayList<>(appIds);
        } catch (IOException e) {
            logger.error("Get appId from appInfo file error, appInfoPath: {}", appInfoPath, e);
            return Collections.emptyList();
        }
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
            logger.error("Get appId from log file error, logPath: {}", logPath, e);
            return Collections.emptyList();
        }
    }

    public static String readWholeFileContentFromLocal(String filePath) {
        String line;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
            while ((line = br.readLine()) != null) {
                sb.append(line + "\r\n");
            }
            return sb.toString();
        } catch (IOException e) {
            log.error("read file error", e);
        }
        return "";
    }

    public static void setWorkflowAndTaskInstanceIDMDC(Integer workflowInstanceId, Integer taskInstanceId) {
        setWorkflowInstanceIdMDC(workflowInstanceId);
        setTaskInstanceIdMDC(taskInstanceId);
    }

    public static void setWorkflowInstanceIdMDC(Integer workflowInstanceId) {
        MDC.put(Constants.WORKFLOW_INSTANCE_ID_MDC_KEY, String.valueOf(workflowInstanceId));
    }

    public static void setTaskInstanceIdMDC(Integer taskInstanceId) {
        MDC.put(Constants.TASK_INSTANCE_ID_MDC_KEY, String.valueOf(taskInstanceId));
    }

    public static void removeWorkflowAndTaskInstanceIdMDC() {
        removeWorkflowInstanceIdMDC();
        removeTaskInstanceIdMDC();
    }

    public static void removeWorkflowInstanceIdMDC() {
        MDC.remove(Constants.WORKFLOW_INSTANCE_ID_MDC_KEY);
    }

    public static void removeTaskInstanceIdMDC() {
        MDC.remove(Constants.TASK_INSTANCE_ID_MDC_KEY);
    }
}
