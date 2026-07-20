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

package org.apache.dolphinscheduler.server.master.utils;

import org.apache.dolphinscheduler.common.constants.DateConstants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.server.master.log.WorkflowLogDiscriminator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachable;

@Slf4j
@UtilityClass
public class WorkflowLogUtils {

    private static final Path WORKFLOW_INSTANCE_LOG_BASE_PATH = getWorkflowInstanceLogBasePath();
    public static final String WORKFLOW_INSTANCE_LOG_FULL_PATH_MDC_KEY = "workflowInstanceLogFullPath";

    public static String getWorkflowInstanceLogFullPath(Date workflowStartTime,
                                                        Long workflowDefinitionCode,
                                                        int workflowDefinitionVersion,
                                                        int workflowInstanceId) {
        if (WORKFLOW_INSTANCE_LOG_BASE_PATH == null) {
            throw new IllegalArgumentException(
                    "Cannot find the workflow instance log base path, please check your logback.xml file");
        }
        final String workflowLogFileName = Paths.get(
                String.valueOf(workflowDefinitionCode),
                String.valueOf(workflowDefinitionVersion),
                String.format("%s.log", workflowInstanceId)).toString();
        return WORKFLOW_INSTANCE_LOG_BASE_PATH
                .resolve(DateUtils.format(workflowStartTime, DateConstants.YYYYMMDD, null))
                .resolve(workflowLogFileName)
                .toString();
    }

    /**
     * Get workflow instance log base absolute path, this is defined in logback.xml
     */
    public static Path getWorkflowInstanceLogBasePath() {
        return Optional.of(LoggerFactory.getILoggerFactory())
                .map(e -> (AppenderAttachable<ILoggingEvent>) (e.getLogger("ROOT")))
                .map(e -> (SiftingAppender) (e.getAppender("WORKFLOWLOGFILE")))
                .map(e -> ((WorkflowLogDiscriminator) (e.getDiscriminator())))
                .map(WorkflowLogDiscriminator::getLogBase)
                .map(e -> Paths.get(e).toAbsolutePath())
                .orElse(null);
    }

    public static String getWorkflowInstanceLogFullPathMDC() {
        return MDC.get(WORKFLOW_INSTANCE_LOG_FULL_PATH_MDC_KEY);
    }

    public static void setWorkflowInstanceLogFullPathMDC(String workflowInstanceLogFullPath) {
        if (workflowInstanceLogFullPath == null) {
            log.warn("workflowInstanceLogFullPath is null");
            return;
        }
        MDC.put(WORKFLOW_INSTANCE_LOG_FULL_PATH_MDC_KEY, workflowInstanceLogFullPath);
    }

    public static void removeWorkflowInstanceLogFullPathMDC() {
        MDC.remove(WORKFLOW_INSTANCE_LOG_FULL_PATH_MDC_KEY);
    }
}
