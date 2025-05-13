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

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.common.ILogService;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LocalLogClient {

    /**
     * Download the complete log of a task instance.
     * This method is used to retrieve all log information from the start to the end of a task instance,
     * suitable for scenarios where a complete log record is required.
     *
     * @param taskInstance The task instance object, containing information needed to retrieve the log.
     * @return The complete log file download response of the task instance, including log content and metadata.
     */
    public TaskInstanceLogFileDownloadResponse getWholeLog(TaskInstance taskInstance) {
        return getLocalWholeLog(taskInstance);
    }

    /**
     * Query a portion of the log of a task instance.
     * This method is used to query log information of a task instance in a paginated manner,
     * suitable for scenarios where the log content is large and needs to be retrieved in batches.
     *
     * @param taskInstance The task instance object, containing information needed to retrieve the log.
     * @param skipLineNum The number of lines to skip, indicating from which line to start reading the log.
     * @param limit The maximum number of lines to read, indicating the maximum number of lines to retrieve in this query.
     * @return The partial log query response, including log content within the specified range and metadata.
     */
    public TaskInstanceLogPageQueryResponse getPartLog(TaskInstance taskInstance, int skipLineNum, int limit) {
        return getLocalPartLog(taskInstance, skipLineNum, limit);
    }

    private TaskInstanceLogFileDownloadResponse getLocalWholeLog(TaskInstance taskInstance) {
        TaskInstanceLogFileDownloadRequest request = new TaskInstanceLogFileDownloadRequest(
                taskInstance.getId(),
                taskInstance.getLogPath());
        return getProxyLogService(taskInstance).getTaskInstanceWholeLogFileBytes(request);
    }

    private TaskInstanceLogPageQueryResponse getLocalPartLog(TaskInstance taskInstance, int skipLineNum,
                                                             int limit) {
        TaskInstanceLogPageQueryRequest request = TaskInstanceLogPageQueryRequest
                .builder()
                .taskInstanceId(taskInstance.getId())
                .taskInstanceLogAbsolutePath(taskInstance.getLogPath())
                .skipLineNum(skipLineNum)
                .limit(limit)
                .build();
        return getProxyLogService(taskInstance).pageQueryTaskInstanceLog(request);
    }

    private ILogService getProxyLogService(TaskInstance taskInstance) {
        ILogService logService = Clients
                .withService(ILogService.class)
                .withHost(taskInstance.getHost());
        log.debug("Created log service for host: {}", taskInstance.getHost());
        return logService;
    }
}
