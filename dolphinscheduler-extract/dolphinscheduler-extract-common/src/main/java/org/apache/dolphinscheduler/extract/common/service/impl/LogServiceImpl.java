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
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileChunkRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileChunkResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.util.List;

public class LogServiceImpl implements ILogService {

    protected int maxLogQueryLimit = 10000;

    public void setMaxLogQueryLimit(int maxLogQueryLimit) {
        this.maxLogQueryLimit = maxLogQueryLimit;
    }

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
            String logPath = taskInstanceLogFileDownloadRequest.getTaskInstanceLogAbsolutePath();
            File logFile = new File(logPath);
            if (!logFile.exists() || !logFile.isFile()) {
                taskInstanceLogFileDownloadResponse.setCode(LogResponseStatus.ERROR);
                taskInstanceLogFileDownloadResponse.setMessage("Log file: " + logPath + " not exists");
                return taskInstanceLogFileDownloadResponse;
            }
            if (logFile.length() > LogUtils.MAX_LOG_DOWNLOAD_SIZE) {
                taskInstanceLogFileDownloadResponse.setCode(LogResponseStatus.ERROR);
                taskInstanceLogFileDownloadResponse.setMessage(
                        "Log file size " + logFile.length() + " exceeds maximum download size "
                                + LogUtils.MAX_LOG_DOWNLOAD_SIZE);
                return taskInstanceLogFileDownloadResponse;
            }
            byte[] bytes = LogUtils.getFileContentBytesFromLocal(logPath);
            taskInstanceLogFileDownloadResponse.setLogBytes(bytes);
        } catch (Exception e) {
            taskInstanceLogFileDownloadResponse.setCode(LogResponseStatus.ERROR);
            taskInstanceLogFileDownloadResponse.setMessage(ExceptionUtils.getRootCauseMessage(e));
        }
        return taskInstanceLogFileDownloadResponse;
    }

    /**
     * Streams a single bounded chunk of the log file. The caller drives the loop: advance
     * {@code offset} by the returned {@code bytes.length} until {@code eof} is true. Each chunk is
     * capped at {@link LogUtils#MAX_LOG_CHUNK_SIZE}, so a multi-GB log is transferred as many small
     * RPCs instead of one giant one — neither this worker nor the API server holds the whole file.
     */
    @Override
    public TaskInstanceLogFileChunkResponse getTaskInstanceLogFileChunk(
                                                                        final TaskInstanceLogFileChunkRequest request) {
        final TaskInstanceLogFileChunkResponse response = new TaskInstanceLogFileChunkResponse();
        try {
            final String logPath = request.getTaskInstanceLogAbsolutePath();
            final File logFile = new File(logPath);
            if (!logFile.exists() || !logFile.isFile()) {
                response.setCode(LogResponseStatus.LOG_FILE_NOT_FOUND);
                response.setMessage("Log file: " + logPath + " not exists");
                return response;
            }
            final long fileSize = logFile.length();
            final long offset = Math.max(request.getOffset(), 0);
            final int length = Math.min(Math.max(request.getLength(), 1), LogUtils.MAX_LOG_CHUNK_SIZE);
            final byte[] bytes = LogUtils.readFileRange(logPath, offset, length);
            response.setBytes(bytes);
            response.setFileSize(fileSize);
            response.setEof(offset + bytes.length >= fileSize);
        } catch (Exception e) {
            response.setCode(LogResponseStatus.ERROR);
            response.setMessage(ExceptionUtils.getRootCauseMessage(e));
        }
        return response;
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
        // Clamp limit to prevent excessive memory allocation on worker side
        int limit = Math.min(Math.max(taskInstanceLogPageQueryRequest.getLimit(), 1), maxLogQueryLimit);
        int skipLineNum = Math.max(taskInstanceLogPageQueryRequest.getSkipLineNum(), 0);
        List<String> lines;
        try {
            lines = LogUtils.readPartFileContentFromLocal(
                    taskInstanceLogPageQueryRequest.getTaskInstanceLogAbsolutePath(),
                    skipLineNum,
                    limit);
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
