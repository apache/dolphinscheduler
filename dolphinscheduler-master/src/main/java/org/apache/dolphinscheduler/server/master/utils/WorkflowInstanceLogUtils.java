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

import static ch.qos.logback.classic.ClassicConstants.FINALIZE_SESSION_MARKER;
import static org.apache.dolphinscheduler.common.constants.DateConstants.YYYYMMDD;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.master.log.WorkflowInstanceLogDiscriminator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachable;

// todo: move this class to master module, we need to move the workflow instance creator to master module.
@Slf4j
@UtilityClass
public class WorkflowInstanceLogUtils {

    public static final String WORKFLOW_INSTANCE_LOG_FULL_PATH_MDC_KEY = "workflowInstanceLogFullPath";
    private final Path WORKFLOW_INSTANCE_LOG_BASE_PATH = getWorkflowInstanceLogBasePath();

    private final String WORKFLOW_INSTANCE_LOG_APPENDER_NAME = "WORKFLOW_INSTANCE_LOG_FILE";

    private final Path LOG_BASE = Optional.of(LoggerFactory.getILoggerFactory())
            .map(e -> (AppenderAttachable<ILoggingEvent>) (e.getLogger("ROOT")))
            .map(e -> (SiftingAppender) (e.getAppender(WORKFLOW_INSTANCE_LOG_APPENDER_NAME)))
            .map(e -> ((WorkflowInstanceLogDiscriminator) (e.getDiscriminator())))
            .map(WorkflowInstanceLogDiscriminator::getLogBase)
            .map(logBase -> Paths.get(logBase).toAbsolutePath())
            .orElse(null);

    public String generateWorkflowInstanceLogFullPath(ProcessInstance workflowInstance) {
        return generateWorkflowInstanceLogFullPath(workflowInstance.getStartTime(), workflowInstance.getId());
    }

    public String generateWorkflowInstanceLogFullPath(Date workflowInstanceCreateTime, long workflowInstanceId) {
        if (WORKFLOW_INSTANCE_LOG_BASE_PATH == null) {
            log.error("Cannot find the WORKFLOW_INSTANCE_LOG_FILE, please check your logback.xml file");
            return null;
        }
        final String taskLogFileName = Paths.get(
                String.format("%s.log", workflowInstanceId)).toString();
        return WORKFLOW_INSTANCE_LOG_BASE_PATH
                .resolve(DateUtils.format(workflowInstanceCreateTime, YYYYMMDD, null))
                .resolve(taskLogFileName)
                .toString();
    }

    public void closeLogAppender() {
        log.info(FINALIZE_SESSION_MARKER, FINALIZE_SESSION_MARKER.toString());
    }

    public void setWorkflowInstanceLogFullPathMdcKey(String workflowInstanceLogFullPath) {
        MDC.put(WORKFLOW_INSTANCE_LOG_FULL_PATH_MDC_KEY, workflowInstanceLogFullPath);
    }

    public void removeWorkflowInstanceLogFullPathMdcKey() {
        MDC.remove(WORKFLOW_INSTANCE_LOG_FULL_PATH_MDC_KEY);
    }

    private Path getWorkflowInstanceLogBasePath() {
        return Optional.of(LoggerFactory.getILoggerFactory())
                .map(e -> (AppenderAttachable<ILoggingEvent>) (e.getLogger("ROOT")))
                .map(e -> (SiftingAppender) (e.getAppender(WORKFLOW_INSTANCE_LOG_APPENDER_NAME)))
                .map(e -> ((WorkflowInstanceLogDiscriminator) (e.getDiscriminator())))
                .map(WorkflowInstanceLogDiscriminator::getLogBase)
                .map(e -> Paths.get(e).toAbsolutePath())
                .orElse(null);
    }

    public static void removeWorkflowInstanceLogByWorkflowInstanceIds(List<String> workflowInstanceLogPaths) {
        if (CollectionUtils.isEmpty(workflowInstanceLogPaths)) {
            return;
        }
        for (String workflowInstanceLogPath : workflowInstanceLogPaths) {
            try {
                FileUtils.deleteDirectory(new File(workflowInstanceLogPath));
            } catch (FileNotFoundException ignore) {
                // This only happens when the master/worker is deployed in one machine, and they delete concurrently.
                // This will not affect the result.
                log.debug("Delete workflowInstanceLog : {} successfully", workflowInstanceLogPath, ignore);
            } catch (IOException e) {
                if (!(e.getCause() instanceof FileNotFoundException)) {
                    log.error("Delete workflowInstanceLog : {} error", workflowInstanceLogPath, e);
                } else {
                    log.debug("Delete workflowInstanceLog : {} error", workflowInstanceLogPath, e);
                }
            }
        }
    }
}
