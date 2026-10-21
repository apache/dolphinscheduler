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

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class LogServiceImpl implements ILogService {

    private static final int MAX_CHUNK_SIZE = 8 * 1024 * 1024; // 8 MB

    /**
     * Maximum number of log lines returned by a single {@link #pageQueryTaskInstanceLog} call.
     * The request parameters are user-controlled all the way down from the REST layer; without
     * this clamp, {@code limit=Integer.MAX_VALUE} materializes the whole log as a
     * {@code List<String>} and re-introduces the OOM this streaming work exists to prevent.
     */
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
            final File logFile = new File(taskInstanceLogFileDownloadRequest.getTaskInstanceLogAbsolutePath());
            if (!logFile.exists() || !logFile.isFile()) {
                // Distinguish MISSING from EMPTY: an empty log is a valid SUCCESS ("task produced
                // no output"), a missing one must signal callers to fall back to the remote
                // archive. getFileContentBytesFromLocal swallows the FileNotFoundException and
                // returns an empty array, so without this check the two states are
                // indistinguishable on the wire.
                taskInstanceLogFileDownloadResponse.setCode(LogResponseStatus.LOG_FILE_NOT_FOUND);
                taskInstanceLogFileDownloadResponse.setMessage(
                        "Log file not found: " + taskInstanceLogFileDownloadRequest.getTaskInstanceLogAbsolutePath());
                return taskInstanceLogFileDownloadResponse;
            }
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
            // Clamp both parameters: the request comes from the REST layer with no safe upper
            // bound, and a negative skip/limit makes Files.lines().skip()/limit() throw.
            final int skipLineNum = Math.max(taskInstanceLogPageQueryRequest.getSkipLineNum(), 0);
            final int limit = Math.min(Math.max(taskInstanceLogPageQueryRequest.getLimit(), 1), maxLogQueryLimit);
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

    @Override
    public TaskInstanceLogFileDownloadResponse getTaskInstanceLogFileChunk(
                                                                           final TaskInstanceLogFileDownloadRequest request) {
        final TaskInstanceLogFileDownloadResponse response = new TaskInstanceLogFileDownloadResponse();
        try {
            final String path = request.getTaskInstanceLogAbsolutePath();
            final long offset = Math.max(request.getOffset(), 0);
            final int length = request.getLength() <= 0
                    ? MAX_CHUNK_SIZE
                    : Math.min(request.getLength(), MAX_CHUNK_SIZE);
            final File logFile = new File(path);
            if (!logFile.exists() || !logFile.isFile()) {
                response.setCode(LogResponseStatus.LOG_FILE_NOT_FOUND);
                response.setMessage("Log file not found: " + path);
                response.setEof(true);
                return response;
            }
            // ONE atomic stat drives ALL decisions below (identity / missing / truncated / EOF /
            // read clamp): File.length()/lastModified() are SEPARATE syscalls — a rotation
            // landing between them would report the OLD file's length with the NEW file's
            // mtime/inode, pinning a hybrid identity that defeats the replacement guard below.
            // Files.readAttributes snapshots all attributes from a single kernel call; its
            // fileKey is the strongest identity signal available (null when unsupported).
            final BasicFileAttributes attrs = Files.readAttributes(logFile.toPath(), BasicFileAttributes.class);
            final long observedFileLength = attrs.size();
            final long observedLastModified = attrs.lastModifiedTime().toMillis();
            final String observedFileKey = attrs.fileKey() == null ? null : String.valueOf(attrs.fileKey());
            response.setObservedLength(observedFileLength);
            response.setObservedLastModifiedMillis(observedLastModified);
            response.setObservedFileKey(observedFileKey);
            // --- request-pinned identity check (file replacement across chunks) ---
            // The caller pins identity from the first chunk and echoes it on every subsequent
            // one. A replacement the length check cannot see — a rotation that swaps in a
            // different file of equal-or-greater length — would otherwise produce an A-prefix +
            // B-suffix "successful" download. Checks, most to least reliable:
            //   1. inode key: present on BOTH sides -> must be identical.
            //   2. mtime: a replaced file's mtime is always EARLIER than the pin (the pin was
            //      taken from the previous live file; a fresh file is born "now"). Appends move
            //      mtime FORWARD and stay accepted; same-mtime keeps passing (granularity).
            // There remains a stat->read window inside which a replacement can still slip
            // through (a true fix needs an inode-pinned open handle, a larger change); with the
            // single atomic stat above the window is micro- rather than chunk-sized.
            if (request.getExpectedFileLength() > 0 || request.getExpectedLastModifiedMillis() > 0
                    || request.getExpectedFileKey() != null) {
                if (request.getExpectedFileLength() > 0 && observedFileLength < request.getExpectedFileLength()) {
                    // Shrink: rotation renamed the active file — the existing truncation
                    // semantics (kept distinct from replacement so operators can tell them
                    // apart; the API rejects a shrink against its pin too).
                    response.setCode(LogResponseStatus.LOG_TRUNCATED);
                    response.setMessage("Log file was truncated/rotated: size "
                            + observedFileLength + " < pinned " + request.getExpectedFileLength());
                    response.setEof(true);
                    return response;
                }
                if (request.getExpectedFileKey() != null && observedFileKey != null
                        && !request.getExpectedFileKey().equals(observedFileKey)) {
                    response.setCode(LogResponseStatus.LOG_FILE_REPLACED);
                    response.setMessage("Log file identity changed: pinned fileKey "
                            + request.getExpectedFileKey() + " but observed " + observedFileKey);
                    response.setEof(true);
                    return response;
                }
                if (request.getExpectedLastModifiedMillis() > 0 && observedLastModified > 0
                        && observedLastModified < request.getExpectedLastModifiedMillis()) {
                    response.setCode(LogResponseStatus.LOG_FILE_REPLACED);
                    response.setMessage("Log file identity changed: mtime went backwards, pinned "
                            + request.getExpectedLastModifiedMillis() + " but observed " + observedLastModified);
                    response.setEof(true);
                    return response;
                }
            }
            if (offset > observedFileLength) {
                // offset is PAST the observed file size: the file shrank underneath the reader
                // (log rotation renamed it, a fresh one started from 0). offset == fileLength
                // is the normal clean EOF, but offset > fileLength can only mean truncation —
                // reporting SUCCESS here would silently hand the caller a truncated download.
                response.setCode(LogResponseStatus.LOG_TRUNCATED);
                response.setMessage("Log file was truncated/rotated: size "
                        + observedFileLength + " < requested offset " + offset);
                response.setEof(true);
                return response;
            }
            final byte[] data = LogUtils.readFileRange(logFile, offset, length, observedFileLength);
            response.setLogBytes(data);
            response.setEof(data.length < length);
        } catch (FileNotFoundException e) {
            // The file passed the existence check above but vanished before the read opened it.
            // Typed — no re-stat: re-statting would race against rotation and could misclassify.
            response.setCode(LogResponseStatus.LOG_FILE_NOT_FOUND);
            response.setMessage(ExceptionUtils.getRootCauseMessage(e));
            response.setEof(true);
        } catch (Exception e) {
            response.setCode(LogResponseStatus.ERROR);
            response.setMessage(ExceptionUtils.getRootCauseMessage(e));
            response.setEof(true);
        }
        return response;
    }

}
