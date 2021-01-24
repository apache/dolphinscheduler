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

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.log.TaskLogDiscriminator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachable;

public class LogUtils {

    private LogUtils() throws IllegalStateException {
        throw new IllegalStateException("Utility class");
    }

    /**
     * get task log path
     */
    @SuppressWarnings("unchecked")
    private static String getTaskLogPath(int processDefinitionId, int processInstanceId, int taskInstanceId) {
        // Optional.map will be skipped if null
        return Optional.of(LoggerFactory.getILoggerFactory())
                .map(e -> (AppenderAttachable<ILoggingEvent>) (e.getLogger("ROOT")))
                .map(e -> (SiftingAppender) (e.getAppender("TASKLOGFILE")))
                .map(e -> ((TaskLogDiscriminator) (e.getDiscriminator())))
                .map(TaskLogDiscriminator::getLogBase)
                .map(e -> Paths.get(e)
                        .toAbsolutePath()
                        .resolve(String.valueOf(processDefinitionId))
                        .resolve(String.valueOf(processInstanceId))
                        .resolve(taskInstanceId + ".log"))
                .map(Path::toString)
                .orElse("");
    }

    /**
     * get task log path by TaskInstance
     */
    public static String getTaskLogPath(TaskInstance taskInstance) {
        return getTaskLogPath(taskInstance.getProcessDefinitionId(), taskInstance.getProcessInstanceId(), taskInstance.getId());
    }

    /**
     * get task log path by TaskExecutionContext
     */
    public static String getTaskLogPath(TaskExecutionContext taskExecutionContext) {
        return getTaskLogPath(taskExecutionContext.getProcessDefineId(), taskExecutionContext.getProcessInstanceId(), taskExecutionContext.getTaskInstanceId());
    }

}
