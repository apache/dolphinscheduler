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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.common.utils.LogUtils;
import org.apache.dolphinscheduler.extract.common.transportor.LogResponseStatus;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileChunkRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileChunkResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LogServiceImpl} limit parameter validation.
 *
 * <p>Verifies that the worker-side RPC implementation clamps the limit parameter
 * to MAX_LOG_QUERY_LIMIT (10000) and skipLineNum to >= 0, preventing excessive
 * memory allocation when a malicious or buggy API server sends unvalidated values.
 */
@Slf4j
class LogServiceImplTest {

    private LogServiceImpl logService;
    private Path testLogFile;

    @BeforeEach
    void setUp() throws IOException {
        logService = new LogServiceImpl();
        logService.setMaxLogQueryLimit(10000);

        // Create a 100-line test file
        testLogFile = Files.createTempFile("ds-logservice-test", ".log");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            String filler = String.join("", Collections.nCopies(50, "x"));
            sb.append(String.format("line-%03d-", i)).append(filler).append("\n");
        }
        Files.write(testLogFile, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(testLogFile);
    }

    /**
     * Verify that limit=Integer.MAX_VALUE is clamped and does not cause issues.
     * The response should succeed and return truncated content.
     */
    @Test
    void pageQueryTaskInstanceLog_clampsMaxIntLimit() {
        TaskInstanceLogPageQueryRequest request = TaskInstanceLogPageQueryRequest.builder()
                .taskInstanceId(1)
                .taskInstanceLogAbsolutePath(testLogFile.toString())
                .skipLineNum(0)
                .limit(Integer.MAX_VALUE)
                .build();

        TaskInstanceLogPageQueryResponse response =
                logService.pageQueryTaskInstanceLog(request);

        log.info("Response code: {}, content length: {} bytes",
                response.getCode(),
                response.getLogContent() != null ? response.getLogContent().length() : 0);

        assertEquals(LogResponseStatus.SUCCESS, response.getCode(),
                "Should succeed with clamped limit");

        // Content should be truncated to ~64KB by rollViewLogLines
        if (response.getLogContent() != null) {
            int contentBytes = response.getLogContent().getBytes(StandardCharsets.UTF_8).length;
            assertTrue(contentBytes <= 65535 + 200,
                    "Content should be ~64KB, got " + contentBytes);
        }
    }

    /**
     * Verify that negative limit is clamped to 1.
     */
    @Test
    void pageQueryTaskInstanceLog_clampsNegativeLimit() {
        TaskInstanceLogPageQueryRequest request = TaskInstanceLogPageQueryRequest.builder()
                .taskInstanceId(1)
                .taskInstanceLogAbsolutePath(testLogFile.toString())
                .skipLineNum(0)
                .limit(-1)
                .build();

        TaskInstanceLogPageQueryResponse response =
                logService.pageQueryTaskInstanceLog(request);

        assertEquals(LogResponseStatus.SUCCESS, response.getCode(),
                "Should succeed with clamped negative limit");
    }

    /**
     * Verify that negative skipLineNum is clamped to 0.
     */
    @Test
    void pageQueryTaskInstanceLog_clampsNegativeSkipLine() {
        TaskInstanceLogPageQueryRequest request = TaskInstanceLogPageQueryRequest.builder()
                .taskInstanceId(1)
                .taskInstanceLogAbsolutePath(testLogFile.toString())
                .skipLineNum(-100)
                .limit(5)
                .build();

        TaskInstanceLogPageQueryResponse response =
                logService.pageQueryTaskInstanceLog(request);

        assertEquals(LogResponseStatus.SUCCESS, response.getCode(),
                "Should succeed with clamped negative skipLineNum");
    }

    /**
     * Verify that a normal paginated request works correctly.
     */
    @Test
    void pageQueryTaskInstanceLog_normalPagination() {
        TaskInstanceLogPageQueryRequest request = TaskInstanceLogPageQueryRequest.builder()
                .taskInstanceId(1)
                .taskInstanceLogAbsolutePath(testLogFile.toString())
                .skipLineNum(10)
                .limit(5)
                .build();

        TaskInstanceLogPageQueryResponse response =
                logService.pageQueryTaskInstanceLog(request);

        assertEquals(LogResponseStatus.SUCCESS, response.getCode());
        assertTrue(response.getLogContent().contains("line-010"),
                "Should start from line 10");
        assertTrue(response.getLogContent().contains("line-014"),
                "Should include line 14");
    }

    /**
     * Verify that non-existent file returns error response (not exception).
     */
    @Test
    void pageQueryTaskInstanceLog_nonExistentFile() {
        TaskInstanceLogPageQueryRequest request = TaskInstanceLogPageQueryRequest.builder()
                .taskInstanceId(1)
                .taskInstanceLogAbsolutePath("/nonexistent/file.log")
                .skipLineNum(0)
                .limit(10)
                .build();

        TaskInstanceLogPageQueryResponse response =
                logService.pageQueryTaskInstanceLog(request);

        assertEquals(LogResponseStatus.ERROR, response.getCode(),
                "Should return ERROR for non-existent file");
    }

    /**
     * Verify that a custom maxLogQueryLimit is respected.
     */
    @Test
    void pageQueryTaskInstanceLog_customLimit() {
        logService.setMaxLogQueryLimit(5);

        TaskInstanceLogPageQueryRequest request = TaskInstanceLogPageQueryRequest.builder()
                .taskInstanceId(1)
                .taskInstanceLogAbsolutePath(testLogFile.toString())
                .skipLineNum(0)
                .limit(Integer.MAX_VALUE)
                .build();

        TaskInstanceLogPageQueryResponse response =
                logService.pageQueryTaskInstanceLog(request);

        assertEquals(LogResponseStatus.SUCCESS, response.getCode(),
                "Should succeed with custom limit");
        // With limit=5 and 100 lines available, should only get 5 lines worth of content
        // (well within 64KB so all 5 lines should be present)
        String content = response.getLogContent();
        assertTrue(content.contains("line-000"), "Should contain line 0");
        assertTrue(content.contains("line-004"), "Should contain line 4");
    }

    /**
     * Verify that default maxLogQueryLimit is 10000.
     */
    @Test
    void defaultMaxLogQueryLimitIs10000() {
        LogServiceImpl service = new LogServiceImpl();
        // With default limit, requesting 100 lines should return all 100
        TaskInstanceLogPageQueryRequest request = TaskInstanceLogPageQueryRequest.builder()
                .taskInstanceId(1)
                .taskInstanceLogAbsolutePath(testLogFile.toString())
                .skipLineNum(0)
                .limit(100)
                .build();

        TaskInstanceLogPageQueryResponse response = service.pageQueryTaskInstanceLog(request);
        assertEquals(LogResponseStatus.SUCCESS, response.getCode());
        assertTrue(response.getLogContent().contains("line-099"),
                "All 100 lines should be returned with default limit of 10000");
    }

    // ==================== getTaskInstanceWholeLogFileBytes tests ====================

    /**
     * Verify that a normal small log file can be downloaded successfully.
     */
    @Test
    void getTaskInstanceWholeLogFileBytes_normalFile() {
        TaskInstanceLogFileDownloadRequest request = TaskInstanceLogFileDownloadRequest.builder()
                .taskInstanceId(1)
                .taskInstanceLogAbsolutePath(testLogFile.toString())
                .build();

        TaskInstanceLogFileDownloadResponse response =
                logService.getTaskInstanceWholeLogFileBytes(request);

        assertEquals(LogResponseStatus.SUCCESS, response.getCode(),
                "Should succeed for normal file");
        assertNotNull(response.getLogBytes(), "Log bytes should not be null");
        assertTrue(response.getLogBytes().length > 0, "Log bytes should not be empty");
    }

    /**
     * Verify that a file exceeding the download size limit is rejected with an error response.
     * Uses a small injected threshold so the oversized branch can be exercised without creating a
     * real 64MB file.
     */
    @Test
    void getTaskInstanceWholeLogFileBytes_oversizedFileRejected() throws IOException {
        final Path bigFile = Files.createTempFile("ds-oversized", ".log");
        try {
            final byte[] data = new byte[200];
            Arrays.fill(data, (byte) 'x');
            Files.write(bigFile, data);
            logService.setMaxLogDownloadSize(100); // threshold = 100 bytes; file is 200

            final TaskInstanceLogFileDownloadRequest request = TaskInstanceLogFileDownloadRequest.builder()
                    .taskInstanceId(1)
                    .taskInstanceLogAbsolutePath(bigFile.toString())
                    .build();
            final TaskInstanceLogFileDownloadResponse response =
                    logService.getTaskInstanceWholeLogFileBytes(request);

            assertEquals(LogResponseStatus.ERROR, response.getCode(),
                    "Should return ERROR for a file exceeding the download size limit");
            assertTrue(response.getMessage().contains("exceeds maximum download size"),
                    "Message should mention the size limit, got: " + response.getMessage());
        } finally {
            logService.setMaxLogDownloadSize(LogUtils.MAX_LOG_DOWNLOAD_SIZE);
            Files.deleteIfExists(bigFile);
        }
    }

    /**
     * Verify that a non-existent file returns an error response (not an exception).
     */
    @Test
    void getTaskInstanceWholeLogFileBytes_nonExistentFile() {
        TaskInstanceLogFileDownloadRequest request = TaskInstanceLogFileDownloadRequest.builder()
                .taskInstanceId(1)
                .taskInstanceLogAbsolutePath("/nonexistent/path/to/file.log")
                .build();

        TaskInstanceLogFileDownloadResponse response =
                logService.getTaskInstanceWholeLogFileBytes(request);

        assertEquals(LogResponseStatus.ERROR, response.getCode(),
                "Should return ERROR for non-existent file");
    }

    // ==================== getTaskInstanceLogFileChunk tests ====================

    /**
     * Concatenating every chunk must reproduce the whole file; chunk size is kept small to force
     * multiple round-trips.
     */
    @Test
    void getTaskInstanceLogFileChunk_streamsFullFileAcrossChunks() throws IOException {
        final long fileSize = Files.size(testLogFile);
        final ByteArrayOutputStream collected = new ByteArrayOutputStream();
        long offset = 0;
        boolean sawEof = false;
        while (!sawEof) {
            final TaskInstanceLogFileChunkRequest request = TaskInstanceLogFileChunkRequest.builder()
                    .taskInstanceId(1)
                    .taskInstanceLogAbsolutePath(testLogFile.toString())
                    .offset(offset)
                    .length(1024)
                    .build();
            final TaskInstanceLogFileChunkResponse response = logService.getTaskInstanceLogFileChunk(request);
            assertEquals(LogResponseStatus.SUCCESS, response.getCode());
            assertEquals(fileSize, response.getFileSize());
            collected.write(response.getBytes());
            offset += response.getBytes().length;
            sawEof = response.isEof();
            if (response.getBytes().length == 0) {
                break;
            }
        }
        assertArrayEquals(Files.readAllBytes(testLogFile), collected.toByteArray());
        assertTrue(sawEof, "Should have reached EOF");
    }

    @Test
    void getTaskInstanceLogFileChunk_nonExistentFileReturnsNotFound() {
        final TaskInstanceLogFileChunkRequest request = TaskInstanceLogFileChunkRequest.builder()
                .taskInstanceId(1)
                .taskInstanceLogAbsolutePath("/nonexistent/chunk.log")
                .offset(0)
                .length(1024)
                .build();
        final TaskInstanceLogFileChunkResponse response = logService.getTaskInstanceLogFileChunk(request);
        assertEquals(LogResponseStatus.LOG_FILE_NOT_FOUND, response.getCode());
    }

    /**
     * An unbounded length request must be clamped to MAX_LOG_CHUNK_SIZE; the whole (small) file
     * still arrives in one chunk with eof=true.
     */
    @Test
    void getTaskInstanceLogFileChunk_clampsOversizedLength() throws IOException {
        final TaskInstanceLogFileChunkRequest request = TaskInstanceLogFileChunkRequest.builder()
                .taskInstanceId(1)
                .taskInstanceLogAbsolutePath(testLogFile.toString())
                .offset(0)
                .length(Integer.MAX_VALUE)
                .build();
        final TaskInstanceLogFileChunkResponse response = logService.getTaskInstanceLogFileChunk(request);
        assertEquals(LogResponseStatus.SUCCESS, response.getCode());
        assertTrue(response.isEof());
        assertArrayEquals(Files.readAllBytes(testLogFile), response.getBytes());
    }
}
