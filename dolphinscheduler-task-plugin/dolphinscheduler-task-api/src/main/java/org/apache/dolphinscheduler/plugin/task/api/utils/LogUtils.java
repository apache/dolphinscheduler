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

import java.io.File;
import java.io.IOException;
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

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachable;

@Slf4j
@UtilityClass
public class LogUtils {

    private static Path TASK_INSTANCE_LOG_BASE_PATH = getTaskInstanceLogBasePath();
    public static final String TASK_INSTANCE_LOG_FULL_PATH_MDC_KEY = "taskInstanceLogFullPath";

    private static final Pattern APPLICATION_REGEX = Pattern.compile(TaskConstants.YARN_APPLICATION_REGEX);

    /**
     * Get application_id from log file.
     *
     * @param logPath     log file path
     * @param appInfoPath appInfo file path
     * @param fetchWay    fetch way
     * @return application id list.
     */
    public List<String> getAppIds(String logPath, String appInfoPath, String fetchWay) {
        if (!StringUtils.isEmpty(fetchWay) && fetchWay.equals("aop")) {
            log.info("Start finding appId in {}, fetch way: {} ", appInfoPath, fetchWay);
            return getAppIdsFromAppInfoFile(appInfoPath);
        } else {
            log.info("Start finding appId in {}, fetch way: {} ", logPath, fetchWay);
            return getAppIdsFromLogFile(logPath);
        }
    }

    /**
     * Get task instance log full path.
     *
     * @param taskExecutionContext task execution context.
     * @return task instance log full path.
     */
    public static String getTaskInstanceLogFullPath(TaskExecutionContext taskExecutionContext) {
        return getTaskInstanceLogFullPath(
                DateUtils.timeStampToDate(taskExecutionContext.getFirstSubmitTime()),
                taskExecutionContext.getProcessDefineCode(),
                taskExecutionContext.getProcessDefineVersion(),
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId());
    }

    /**
     * todo: Remove the submitTime parameter?
     * The task instance log full path, the path is like:{log.base}/{taskSubmitTime}/{workflowDefinitionCode}/{workflowDefinitionVersion}/{}workflowInstance}/{taskInstance}.log
     *
     * @param taskFirstSubmitTime       task first submit time
     * @param workflowDefinitionCode    workflow definition code
     * @param workflowDefinitionVersion workflow definition version
     * @param workflowInstanceId        workflow instance id
     * @param taskInstanceId            task instance id.
     * @return task instance log full path.
     */
    public static String getTaskInstanceLogFullPath(Date taskFirstSubmitTime,
                                                    Long workflowDefinitionCode,
                                                    int workflowDefinitionVersion,
                                                    int workflowInstanceId,
                                                    int taskInstanceId) {
        if (TASK_INSTANCE_LOG_BASE_PATH == null) {
            throw new IllegalArgumentException(
                    "Cannot find the task instance log base path, please check your logback.xml file");
        }
        final String taskLogFileName = Paths.get(
                String.valueOf(workflowDefinitionCode),
                String.valueOf(workflowDefinitionVersion),
                String.valueOf(workflowInstanceId),
                String.format("%s.log", taskInstanceId)).toString();
        return TASK_INSTANCE_LOG_BASE_PATH
                .resolve(DateUtils.format(taskFirstSubmitTime, DateConstants.YYYYMMDD, null))
                .resolve(taskLogFileName)
                .toString();
    }

    /**
     * Get task instance log base absolute path, this is defined in logback.xml
     *
     * @return
     */
    public static Path getTaskInstanceLogBasePath() {
        return Optional.of(LoggerFactory.getILoggerFactory())
                .map(e -> (AppenderAttachable<ILoggingEvent>) (e.getLogger("ROOT")))
                .map(e -> (SiftingAppender) (e.getAppender("TASKLOGFILE")))
                .map(e -> ((TaskLogDiscriminator) (e.getDiscriminator())))
                .map(TaskLogDiscriminator::getLogBase)
                .map(e -> Paths.get(e).toAbsolutePath())
                .orElse(null);
    }

    public List<String> getAppIdsFromAppInfoFile(String appInfoPath) {
        if (StringUtils.isEmpty(appInfoPath)) {
            log.warn("appInfoPath is empty");
            return Collections.emptyList();
        }
        File appInfoFile = new File(appInfoPath);
        if (!appInfoFile.exists() || !appInfoFile.isFile()) {
            return Collections.emptyList();
        }
        List<String> appIds = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(appInfoPath))) {
            stream.forEach(appIds::add);
            return new ArrayList<>(appIds);
        } catch (IOException e) {
            log.error("Get appId from appInfo file error, appInfoPath: {}", appInfoPath, e);
            return Collections.emptyList();
        }
    }

    public List<String> getAppIdsFromLogFile(@NonNull String logPath) {
        File logFile = new File(logPath);
        if (!logFile.exists() || !logFile.isFile()) {
            return Collections.emptyList();
        }
        Set<String> appIds = new HashSet<>();
        try (Stream<String> stream = Files.lines(Paths.get(logPath))) {
            stream.forEach(line -> {
                Matcher matcher = APPLICATION_REGEX.matcher(line);
                if (matcher.find()) {
                    String appId = matcher.group();
                    if (appIds.add(appId)) {
                        log.info("Find appId: {} from {}", appId, logPath);
                    }
                }
            });
            return new ArrayList<>(appIds);
        } catch (IOException e) {
            log.error("Get appId from log file error, logPath: {}", logPath, e);
            return Collections.emptyList();
        }
    }

    public static String getTaskInstanceLogFullPathMdc() {
        return MDC.get(TASK_INSTANCE_LOG_FULL_PATH_MDC_KEY);
    }

    public static void setTaskInstanceLogFullPathMDC(String taskInstanceLogFullPath) {
        if (taskInstanceLogFullPath == null) {
            log.warn("taskInstanceLogFullPath is null");
            return;
        }
        MDC.put(TASK_INSTANCE_LOG_FULL_PATH_MDC_KEY, taskInstanceLogFullPath);
    }

    public static void removeTaskInstanceLogFullPathMDC() {
        MDC.remove(TASK_INSTANCE_LOG_FULL_PATH_MDC_KEY);
    }

    public static void setWorkflowAndTaskInstanceIDMDC(Integer workflowInstanceId,
                                                       Integer taskInstanceId) {
        MDC.put(Constants.WORKFLOW_INSTANCE_ID_MDC_KEY, String.valueOf(workflowInstanceId));
        MDC.put(Constants.TASK_INSTANCE_ID_MDC_KEY, String.valueOf(taskInstanceId));
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

    @AllArgsConstructor
    public static class MDCAutoClosableContext implements AutoCloseable {

        private final Runnable closeAction;

        @Override
        public void close() {
            closeAction.run();
        }
    }
}
