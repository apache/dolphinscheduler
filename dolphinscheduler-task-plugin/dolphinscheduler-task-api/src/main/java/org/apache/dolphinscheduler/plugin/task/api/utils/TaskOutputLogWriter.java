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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.apache.commons.lang3.StringUtils;

import lombok.experimental.UtilityClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UtilityClass
public class TaskOutputLogWriter {

    private static final Logger TASK_OUTPUT_LOGGER = LoggerFactory.getLogger(LogUtils.TASK_OUTPUT_LOGGER_NAME);

    public static void writeTaskOutput(TaskExecutionContext taskExecutionContext, String content) {
        writeTaskOutput(taskExecutionContext, () -> TASK_OUTPUT_LOGGER.info(content));
    }

    public static void writeTaskOutput(String taskLogPath, String taskOutputLogPath, String content) {
        writeTaskOutput(taskLogPath, taskOutputLogPath, () -> TASK_OUTPUT_LOGGER.info(content));
    }

    public static void writeTaskOutput(TaskExecutionContext taskExecutionContext, String message, Object... args) {
        writeTaskOutput(taskExecutionContext, () -> TASK_OUTPUT_LOGGER.info(message, args));
    }

    private static void writeTaskOutput(TaskExecutionContext taskExecutionContext, Runnable runnable) {
        writeTaskOutput(taskExecutionContext.getLogPath(), taskExecutionContext.getTaskOutputLogPath(), runnable);
    }

    private static void writeTaskOutput(String taskLogPath, String taskOutputLogPath, Runnable runnable) {
        validatePath(taskLogPath, "task log");
        validatePath(taskOutputLogPath, "task output");
        LogUtils.setTaskInstanceLogFullPathMDC(taskLogPath);
        try (
                LogUtils.MDCAutoClosableContext ignored =
                        LogUtils.withTaskOutputLogPathMDC(taskOutputLogPath)) {
            runnable.run();
        } finally {
            LogUtils.removeTaskInstanceLogFullPathMDC();
        }
    }

    private static void validatePath(String path, String pathType) {
        if (StringUtils.isBlank(path)) {
            throw new IllegalStateException(String.format("The %s log path cannot be blank", pathType));
        }
    }
}
