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

package org.apache.dolphinscheduler.extract.common.service.impl;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.LogUtils;
import org.apache.dolphinscheduler.extract.common.ILogService;
import org.apache.dolphinscheduler.extract.common.transportor.LogResponseStatus;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;

public class LogServiceImpl implements ILogService {

    /**
     * Downloads the entire log file for a task instance.
     *
     * @param taskInstanceLogFileDownloadRequest Request object containing the path to the task instance log file.
     * @return Response object containing the log file content in byte array form.
     */
    @Override
    public TaskInstanceLogFileDownloadResponse getTaskInstanceWholeLogFileBytes(TaskInstanceLogFileDownloadRequest taskInstanceLogFileDownloadRequest) {
        final TaskInstanceLogFileDownloadResponse taskInstanceLogFileDownloadResponse =
                new TaskInstanceLogFileDownloadResponse();
        try {
            byte[] bytes = LogUtils
                    .getFileContentBytesFromLocal(taskInstanceLogFileDownloadRequest.getTaskInstanceLogAbsolutePath());
            taskInstanceLogFileDownloadResponse.setLogBytes(bytes);
        } catch (Exception e) {
            taskInstanceLogFileDownloadResponse.setCode(LogResponseStatus.ERROR);
            taskInstanceLogFileDownloadResponse.setMessage(ExceptionUtils.getRootCauseMessage(e));
        }
        return taskInstanceLogFileDownloadResponse;
    }

    /**
     * Performs paginated queries on task instance logs.
     *
     * @param taskInstanceLogPageQueryRequest Request object containing the path to the task instance log file, the number of lines to skip, and the maximum number of lines to read.
     * @return Response object containing the log content.
     */
    @Override
    public TaskInstanceLogPageQueryResponse pageQueryTaskInstanceLog(TaskInstanceLogPageQueryRequest taskInstanceLogPageQueryRequest) {
        final TaskInstanceLogPageQueryResponse taskInstanceLogPageQueryResponse =
                new TaskInstanceLogPageQueryResponse();
        List<String> lines;
        try {
            lines = LogUtils.readPartFileContentFromLocal(
                    taskInstanceLogPageQueryRequest.getTaskInstanceLogAbsolutePath(),
                    taskInstanceLogPageQueryRequest.getSkipLineNum(),
                    taskInstanceLogPageQueryRequest.getLimit());
            taskInstanceLogPageQueryResponse.setLogContent(LogUtils.rollViewLogLines(lines));
        } catch (Exception e) {
            taskInstanceLogPageQueryResponse.setCode(LogResponseStatus.ERROR);
            taskInstanceLogPageQueryResponse.setMessage(ExceptionUtils.getMessage(e));
        }
        return taskInstanceLogPageQueryResponse;
    }

    @Override
    public void removeTaskInstanceLog(String taskInstanceLogAbsolutePath) {
        FileUtils.deleteFile(taskInstanceLogAbsolutePath);
    }

}
