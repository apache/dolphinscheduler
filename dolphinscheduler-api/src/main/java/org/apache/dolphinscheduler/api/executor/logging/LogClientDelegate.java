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

package org.apache.dolphinscheduler.api.executor.logging;

import org.apache.dolphinscheduler.common.utils.LogUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.common.ILogService;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogClientDelegate {

    /**
     * Retrieves a portion of the log string for a given task instance.
     * This method first attempts to fetch the log from local storage; if unsuccessful, it tries to obtain the log from remote storage.
     *
     * @param taskInstance The task instance object, containing information needed for log retrieval.
     * @param skipLineNum The number of log lines to skip from the beginning.
     * @param limit The maximum number of log lines to retrieve.
     * @return A string containing the specified portion of the log.
     */
    public String getPartLogString(TaskInstance taskInstance, int skipLineNum, int limit) {
        TaskInstanceLogPageQueryResponse response = getLocalPartLog(taskInstance, skipLineNum, limit);
        if (response.getCode() == 0) {
            return response.getLogContent();
        } else {
            log.warn("Failed to get local part log, trying remote: {}", response.getMessage());
            // todo We can optimize requests by the actual range, reducing disk usage and network traffic.
            return LogUtils.rollViewLogLines(
                    LogUtils.readPartFileContentFromRemote(taskInstance.getLogPath(), skipLineNum, limit));
        }
    }

    /**
     * Retrieves the complete log content for a given task instance as a byte array.
     * This method first attempts to fetch the log from local storage; if unsuccessful, it tries to obtain the log from remote storage.
     *
     * @param taskInstance The task instance object, containing information needed for log retrieval.
     * @return A byte array containing the complete log content.
     */
    public byte[] getWholeLogBytes(TaskInstance taskInstance) {
        TaskInstanceLogFileDownloadResponse response = getLocalWholeLog(taskInstance);
        if (response.getCode() == 0) {
            return response.getLogBytes();
        } else {
            log.warn("Failed to get local whole log, trying remote: {}", response.getMessage());
            return getRemoteWholeLog(taskInstance.getLogPath());
        }
    }

    private byte[] getRemoteWholeLog(String logPath) {
        return LogUtils.getFileContentBytesFromRemote(logPath);
    }

    private TaskInstanceLogFileDownloadResponse getLocalWholeLog(TaskInstance taskInstance) {
        TaskInstanceLogFileDownloadRequest request = buildLogFileDownloadRequest(taskInstance);
        return getProxyLogService(taskInstance).getTaskInstanceWholeLogFileBytes(request);
    }

    private TaskInstanceLogPageQueryResponse getLocalPartLog(TaskInstance taskInstance, int skipLineNum, int limit) {
        TaskInstanceLogPageQueryRequest request = buildLogPageQueryRequest(taskInstance, skipLineNum, limit);
        return getProxyLogService(taskInstance).pageQueryTaskInstanceLog(request);
    }

    private TaskInstanceLogFileDownloadRequest buildLogFileDownloadRequest(TaskInstance taskInstance) {
        return new TaskInstanceLogFileDownloadRequest(
                taskInstance.getId(),
                taskInstance.getLogPath());
    }

    private TaskInstanceLogPageQueryRequest buildLogPageQueryRequest(TaskInstance taskInstance, int skipLineNum,
                                                                     int limit) {
        return TaskInstanceLogPageQueryRequest.builder()
                .taskInstanceId(taskInstance.getId())
                .taskInstanceLogAbsolutePath(taskInstance.getLogPath())
                .skipLineNum(skipLineNum)
                .limit(limit)
                .build();
    }

    private ILogService getProxyLogService(TaskInstance taskInstance) {
        return Clients
                .withService(ILogService.class)
                .withHost(taskInstance.getHost());
    }
}
