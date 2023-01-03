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
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;

import java.io.*;
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

import org.slf4j.Logger;
import org.slf4j.MDC;

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

    public static String readWholeFileContent(String filePath) {
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
