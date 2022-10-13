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

package org.apache.dolphinscheduler.service.utils;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import lombok.experimental.UtilityClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * logger utils
 */
@UtilityClass
public class LoggerUtils {

    private static final Logger logger = LoggerFactory.getLogger(LoggerUtils.class);

    /**
     * build job id
     *
     * @return task id format
     */
    public static String buildTaskId(Date firstSubmitTime,
                                     Long processDefineCode,
                                     int processDefineVersion,
                                     int processInstId,
                                     int taskId) {
        // like TaskAppId=TASK-20211107-798_1-4084-15210
        String firstSubmitTimeStr = DateUtils.format(firstSubmitTime, Constants.YYYYMMDD, null);
        return String.format("%s=%s-%s-%s_%s-%s-%s",
                TaskConstants.TASK_APPID_LOG_FORMAT, TaskConstants.TASK_LOGGER_INFO_PREFIX, firstSubmitTimeStr,
                processDefineCode, processDefineVersion, processInstId, taskId);
    }

    /**
     * read whole file content
     *
     * @param filePath file path
     * @return whole file content
     */
    public static String readWholeFileContent(String filePath) {
        String line;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
            while ((line = br.readLine()) != null) {
                sb.append(line + "\r\n");
            }
            return sb.toString();
        } catch (IOException e) {
            logger.error("read file error", e);
        }
        return "";
    }

    public static void setWorkflowAndTaskInstanceIDMDC(int workflowInstanceId, int taskInstanceId) {
        setWorkflowInstanceIdMDC(workflowInstanceId);
        setTaskInstanceIdMDC(taskInstanceId);
    }

    public static void setWorkflowInstanceIdMDC(int workflowInstanceId) {
        MDC.put(Constants.WORKFLOW_INSTANCE_ID_MDC_KEY, String.valueOf(workflowInstanceId));
    }

    public static void setTaskInstanceIdMDC(int taskInstanceId) {
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
