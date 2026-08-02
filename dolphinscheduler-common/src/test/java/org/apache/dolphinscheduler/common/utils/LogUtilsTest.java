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

package org.apache.dolphinscheduler.common.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LogUtils} log reading optimizations.
 *
 * <p>Verifies that:
 * <ul>
 *   <li>{@code readPartFileContentFromLocal} stops reading early when accumulated bytes
 *       reach {@code MAX_RESPONSE_LOG_SIZE}, instead of loading all lines into memory.</li>
 *   <li>{@code getFileContentBytesFromLocal(path, maxSize)} respects the size limit.</li>
 *   <li>{@code rollViewLogLines} truncates output to {@code MAX_RESPONSE_LOG_SIZE}.</li>
 * </ul>
 */
@Slf4j
class LogUtilsTest {

    private Path largeLogFile;
    private Path smallLogFile;

    private static final int LINE_LENGTH = 200;

    @BeforeEach
    void setUp() throws IOException {
        String filler = String.join("", Collections.nCopies(LINE_LENGTH, "x"));

        // Create a 10,000-line file (~2MB)
        largeLogFile = Files.createTempFile("ds-log-utils-test-large", ".log");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10_000; i++) {
            sb.append(String.format("%05d: ", i))
                    .append(filler, 0, LINE_LENGTH - 7)
                    .append("\n");
        }
        Files.write(largeLogFile, sb.toString().getBytes(StandardCharsets.UTF_8));

        // Create a small 5-line file
        smallLogFile = Files.createTempFile("ds-log-utils-test-small", ".log");
        sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append("line ").append(i).append("\n");
        }
        Files.write(smallLogFile, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(largeLogFile);
        Files.deleteIfExists(smallLogFile);
    }

    /**
     * Verify that readPartFileContentFromLocal with limit=Integer.MAX_VALUE
     * stops early at ~64KB instead of loading all 10,000 lines (~2MB).
     */
    @Test
    void readPartFileContentFromLocal_stopsAtMaxResponseSize() {
        List<String> lines = LogUtils.readPartFileContentFromLocal(
                largeLogFile.toString(), 0, Integer.MAX_VALUE);

        long totalBytes = lines.stream()
                .mapToLong(s -> s.getBytes(StandardCharsets.UTF_8).length)
                .sum();

        log.info("readPartFileContentFromLocal returned {} lines, total {} bytes",
                lines.size(), totalBytes);

        // Should NOT load all 10,000 lines
        assertTrue(lines.size() < 10_000,
                "Should stop early, not load all 10,000 lines. Got: " + lines.size());

        // Total bytes should be around MAX_RESPONSE_LOG_SIZE (65535)
        // Allow some slack for the last line that pushed us over the limit
        assertTrue(totalBytes <= LogUtils.MAX_RESPONSE_LOG_SIZE + LINE_LENGTH,
                "Total bytes " + totalBytes + " should be ~64KB, not " + (2 * 1024 * 1024) + " bytes");
    }

    /**
     * Verify that readPartFileContentFromLocal respects skipLine.
     */
    @Test
    void readPartFileContentFromLocal_respectsSkipLine() {
        List<String> lines = LogUtils.readPartFileContentFromLocal(
                smallLogFile.toString(), 2, 10);

        log.info("Skipped 2 lines, got {} lines", lines.size());

        assertEquals(3, lines.size(), "Should get 3 lines (5 total - 2 skipped)");
        assertTrue(lines.get(0).startsWith("line 2"));
        assertTrue(lines.get(1).startsWith("line 3"));
        assertTrue(lines.get(2).startsWith("line 4"));
    }

    /**
     * Verify that readPartFileContentFromLocal respects limit when limit is small.
     */
    @Test
    void readPartFileContentFromLocal_respectsSmallLimit() {
        List<String> lines = LogUtils.readPartFileContentFromLocal(
                smallLogFile.toString(), 0, 3);

        log.info("Limit=3, got {} lines", lines.size());

        assertEquals(3, lines.size(), "Should get exactly 3 lines");
        assertTrue(lines.get(0).startsWith("line 0"));
        assertTrue(lines.get(1).startsWith("line 1"));
        assertTrue(lines.get(2).startsWith("line 2"));
    }

    /**
     * Verify that readPartFileContentFromLocal handles non-existent file.
     */
    @Test
    void readPartFileContentFromLocal_nonExistentFileThrows() {
        try {
            LogUtils.readPartFileContentFromLocal("/nonexistent/path/file.log", 0, 10);
            org.junit.jupiter.api.Assertions.fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("not exists"));
        }
    }

    /**
     * Verify that getFileContentBytesFromLocal with maxSize limits the output.
     */
    @Test
    void getFileContentBytesFromLocal_respectsMaxSize() throws IOException {
        long fileSize = Files.size(largeLogFile);
        log.info("File size: {} bytes", fileSize);

        int maxSize = 1024; // 1KB limit
        byte[] bytes = LogUtils.getFileContentBytesFromLocal(largeLogFile.toString(), maxSize);

        log.info("Got {} bytes with maxSize={}", bytes.length, maxSize);

        assertTrue(bytes.length <= maxSize,
                "Returned bytes " + bytes.length + " should not exceed maxSize " + maxSize);
        assertTrue(bytes.length > 0, "Should return some data");
    }

    /**
     * Verify that getFileContentBytesFromLocal without maxSize reads the entire file
     * (backward compatibility).
     */
    @Test
    void getFileContentBytesFromLocal_noMaxSizeReadsAll() throws IOException {
        long fileSize = Files.size(smallLogFile);
        byte[] bytes = LogUtils.getFileContentBytesFromLocal(smallLogFile.toString());

        log.info("File size: {} bytes, got {} bytes", fileSize, bytes.length);

        assertEquals(fileSize, bytes.length,
                "Without maxSize, should read entire file");
    }

    /**
     * Verify that getFileContentBytesFromLocal with maxSize larger than file reads all.
     */
    @Test
    void getFileContentBytesFromLocal_maxSizeLargerThanFile() throws IOException {
        long fileSize = Files.size(smallLogFile);
        byte[] bytes = LogUtils.getFileContentBytesFromLocal(smallLogFile.toString(), 1024 * 1024);

        assertEquals(fileSize, bytes.length,
                "With maxSize > file size, should read entire file");
    }

    /**
     * Verify that rollViewLogLines truncates to MAX_RESPONSE_LOG_SIZE.
     */
    @Test
    void rollViewLogLines_truncatesToMaxSize() {
        // Create lines that total more than 64KB
        List<String> lines = Collections.nCopies(1000, new String(new char[100]).replace('\0', 'x'));

        String result = LogUtils.rollViewLogLines(lines);
        int resultBytes = result.getBytes(StandardCharsets.UTF_8).length;

        log.info("rollViewLogLines: {} input lines, {} output bytes", lines.size(), resultBytes);

        // Should be truncated well below the full input size
        assertTrue(resultBytes < 2 * LogUtils.MAX_RESPONSE_LOG_SIZE,
                "Output " + resultBytes + " should be well under 128KB, not " + (1000 * 102) + " bytes");
    }

    /**
     * Verify that rollViewLogLines handles single very long line by truncating it.
     */
    @Test
    void rollViewLogLines_truncatesSingleLongLine() {
        String longLine = new String(new char[100_000]).replace('\0', 'x');
        List<String> lines = Collections.singletonList(longLine);

        String result = LogUtils.rollViewLogLines(lines);

        log.info("Input: 1 line of {} bytes, output: {} bytes",
                longLine.length(), result.getBytes(StandardCharsets.UTF_8).length);

        assertTrue(result.contains("exceed"),
                "Should contain truncation notice for long line");
    }

    /**
     * Verify that rollViewLogLines handles empty list.
     */
    @Test
    void rollViewLogLines_emptyList() {
        String result = LogUtils.rollViewLogLines(Collections.emptyList());
        assertEquals("", result, "Empty list should produce empty string");
    }

    /**
     * Verify the MAX_RESPONSE_LOG_SIZE constant is 65535.
     */
    @Test
    void maxResponseLogSizeIs65535() {
        assertEquals(65535, LogUtils.MAX_RESPONSE_LOG_SIZE,
                "MAX_RESPONSE_LOG_SIZE should be 65535");
    }

    /**
     * Verify the MAX_LOG_DOWNLOAD_SIZE constant is 64MB.
     */
    @Test
    void maxLogDownloadSizeIs64MB() {
        assertEquals(64 * 1024 * 1024, LogUtils.MAX_LOG_DOWNLOAD_SIZE,
                "MAX_LOG_DOWNLOAD_SIZE should be 64MB (67108864 bytes)");
    }

    /**
     * Verify that the single-arg getFileContentBytesFromLocal reads a small file fully
     * (backward compatibility with the new 64MB default cap).
     */
    @Test
    void getFileContentBytesFromLocal_singleArgReadsSmallFile() throws Exception {
        Path tempFile = Files.createTempFile("ds-logutils-test", ".log");
        try {
            String content = "hello world\nthis is a test\n";
            Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));

            byte[] result = LogUtils.getFileContentBytesFromLocal(tempFile.toString());
            assertEquals(content.getBytes(StandardCharsets.UTF_8).length, result.length,
                    "Single-arg should read entire small file");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * readFileRange must return exactly [offset, offset+length) and never load the whole file.
     */
    @Test
    void readFileRange_returnsBoundedRange() throws IOException {
        final Path file = Files.createTempFile("ds-log-utils-range", ".log");
        try {
            final byte[] content = new byte[250];
            for (int i = 0; i < content.length; i++) {
                content[i] = (byte) i;
            }
            Files.write(file, content);

            // full file (length larger than file)
            assertArrayEquals(content, LogUtils.readFileRange(file.toString(), 0, 1000));
            // first 100 bytes
            assertArrayEquals(Arrays.copyOfRange(content, 0, 100),
                    LogUtils.readFileRange(file.toString(), 0, 100));
            // middle chunk
            assertArrayEquals(Arrays.copyOfRange(content, 100, 200),
                    LogUtils.readFileRange(file.toString(), 100, 100));
            // partial at EOF (requested 100, only 50 remain)
            assertArrayEquals(Arrays.copyOfRange(content, 200, 250),
                    LogUtils.readFileRange(file.toString(), 200, 100));
            // at/over EOF -> empty
            assertEquals(0, LogUtils.readFileRange(file.toString(), 250, 100).length);
            assertEquals(0, LogUtils.readFileRange(file.toString(), 999, 100).length);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void readFileRange_nonExistentFileThrows() {
        assertThrows(RuntimeException.class,
                () -> LogUtils.readFileRange("/nonexistent/range.log", 0, 100));
    }
}
