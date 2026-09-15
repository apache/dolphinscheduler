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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.extract.common.transportor.LogResponseStatus;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class LogServiceImplTest {

    private final LogServiceImpl logService = new LogServiceImpl();

    @Test
    void getTaskInstanceLogFileChunk_normalRangeReturnsSuccess() throws IOException {
        Path file = Files.createTempFile("ds-chunk", ".log");
        try {
            byte[] content = new byte[20];
            for (int i = 0; i < 20; i++) {
                content[i] = (byte) i;
            }
            Files.write(file, content);

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(0);
            req.setLength(10);

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
            assertEquals(LogResponseStatus.SUCCESS, resp.getCode());
            assertEquals(10, resp.getLogBytes().length);
            assertTrue(!resp.isEof());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void getTaskInstanceLogFileChunk_lastChunkEof() throws IOException {
        Path file = Files.createTempFile("ds-chunk-eof", ".log");
        try {
            byte[] content = new byte[20];
            Files.write(file, content);

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(16);
            req.setLength(10);

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
            assertEquals(LogResponseStatus.SUCCESS, resp.getCode());
            assertEquals(4, resp.getLogBytes().length);
            assertTrue(resp.isEof());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    /**
     * offset BEYOND the file length can only mean the file shrank underneath the reader —
     * log rotation (fixed-window renames the active file, a fresh one starts from 0) or
     * truncation. A well-behaved client advances offset monotonically from 0, so it can never
     * legitimately be past EOF. Reporting SUCCESS+empty here would silently hand the caller a
     * truncated download that looks complete.
     */
    @Test
    void getTaskInstanceLogFileChunk_offsetBeyondShrunkFileReturnsTruncated() throws IOException {
        Path file = Files.createTempFile("ds-chunk-beyond", ".log");
        try {
            Files.write(file, "hello".getBytes(StandardCharsets.UTF_8));

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(100);
            req.setLength(10);

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
            assertEquals(LogResponseStatus.LOG_TRUNCATED, resp.getCode());
            assertTrue(resp.isEof());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    /**
     * offset EXACTLY at the file length is the normal end-of-file reached after consuming the
     * whole file — must stay SUCCESS+empty+eof (the chunk loop's clean termination path).
     */
    @Test
    void getTaskInstanceLogFileChunk_offsetEqualsFileLengthIsNormalEof() throws IOException {
        Path file = Files.createTempFile("ds-chunk-at-eof", ".log");
        try {
            Files.write(file, "hello".getBytes(StandardCharsets.UTF_8));

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(5);
            req.setLength(10);

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
            assertEquals(LogResponseStatus.SUCCESS, resp.getCode());
            assertEquals(0, resp.getLogBytes().length);
            assertTrue(resp.isEof());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void getTaskInstanceLogFileChunk_fileNotFound() {
        TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
        req.setTaskInstanceLogAbsolutePath("/nonexistent/chunk.log");
        req.setOffset(0);
        req.setLength(10);

        TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
        assertEquals(LogResponseStatus.LOG_FILE_NOT_FOUND, resp.getCode());
        assertTrue(resp.isEof());
    }

    /**
     * A MISSING log file must be reported as LOG_FILE_NOT_FOUND, not as SUCCESS with an empty
     * body: an empty log is a valid terminal state ("task produced no output") while a missing
     * one tells the caller to fall back to the remote archive. getFileContentBytesFromLocal
     * swallows the FileNotFoundException and returns an empty array, so without this explicit
     * check the two states are indistinguishable on the wire.
     */
    @Test
    void getTaskInstanceWholeLogFileBytes_missingFileReturnsNotFound() {
        TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
        req.setTaskInstanceLogAbsolutePath("/nonexistent/whole.log");

        TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceWholeLogFileBytes(req);
        assertEquals(LogResponseStatus.LOG_FILE_NOT_FOUND, resp.getCode());
    }

    /**
     * An EXISTING but empty log file stays SUCCESS with empty bytes — the valid terminal state
     * for a task that produced no output.
     */
    @Test
    void getTaskInstanceWholeLogFileBytes_emptyFileReturnsSuccess() throws IOException {
        Path file = Files.createTempFile("ds-whole-empty", ".log");
        try {
            Files.write(file, new byte[0]);

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceWholeLogFileBytes(req);
            assertEquals(LogResponseStatus.SUCCESS, resp.getCode());
            assertEquals(0, resp.getLogBytes().length);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void getTaskInstanceLogFileChunk_lengthClampedToMaxChunkSize() throws IOException {
        Path file = Files.createTempFile("ds-chunk-clamp", ".log");
        try {
            // Write more than MAX_CHUNK_SIZE (8 MB) — 9 MB
            byte[] mb = new byte[1024 * 1024];
            Files.write(file, mb);
            for (int i = 1; i < 9; i++) {
                Files.write(file, mb, java.nio.file.StandardOpenOption.APPEND);
            }

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(0);
            req.setLength(100 * 1024 * 1024); // request 100MB

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
            assertEquals(LogResponseStatus.SUCCESS, resp.getCode());
            assertEquals(8 * 1024 * 1024, resp.getLogBytes().length, "Should be clamped to 8MB");
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
