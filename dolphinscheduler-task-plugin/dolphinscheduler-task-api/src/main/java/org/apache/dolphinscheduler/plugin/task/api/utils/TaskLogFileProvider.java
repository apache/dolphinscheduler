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

import org.apache.dolphinscheduler.common.constants.DateConstants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TaskLogFileProvider {

    public static String getTaskLogsRootPath(@NonNull TaskExecutionContext taskExecutionContext) {
        return getTaskLogsRootPath(
                DateUtils.timeStampToDate(taskExecutionContext.getFirstSubmitTime()),
                taskExecutionContext.getWorkflowDefinitionCode(),
                taskExecutionContext.getWorkflowDefinitionVersion(),
                taskExecutionContext.getWorkflowInstanceId(),
                taskExecutionContext.getTaskInstanceId());
    }

    public static String getTaskLogsRootPath(Date taskFirstSubmitTime,
                                             Long workflowDefinitionCode,
                                             int workflowDefinitionVersion,
                                             int workflowInstanceId,
                                             int taskInstanceId) {
        final Path taskInstanceLogBasePath = LogUtils.getTaskInstanceLogBasePath();
        if (taskInstanceLogBasePath == null) {
            throw new IllegalArgumentException(
                    "Cannot find the task instance log base path, please check your logback.xml file");
        }
        return taskInstanceLogBasePath
                .resolve(DateUtils.format(taskFirstSubmitTime, DateConstants.YYYYMMDD, null))
                .resolve(Paths.get(
                        String.valueOf(workflowDefinitionCode),
                        String.valueOf(workflowDefinitionVersion),
                        String.valueOf(workflowInstanceId),
                        String.valueOf(taskInstanceId)))
                .toString();
    }

    public static String getFilePath(String taskLogsRootPath, TaskLogFileType taskLogFileType) {
        if (StringUtils.isBlank(taskLogsRootPath)) {
            return null;
        }
        return Paths.get(taskLogsRootPath, taskLogFileType.getFileName()).toString();
    }

    public static String getFilePath(@NonNull TaskExecutionContext taskExecutionContext,
                                     TaskLogFileType taskLogFileType) {
        return getFilePath(taskExecutionContext.getTaskLogsRootPath(), taskLogFileType);
    }

    public static String getTaskLogsRootPathFromFilePath(String taskLogFilePath) {
        if (StringUtils.isBlank(taskLogFilePath)) {
            return null;
        }
        final Path parent = Paths.get(taskLogFilePath).getParent();
        return parent == null ? null : parent.toString();
    }
}
