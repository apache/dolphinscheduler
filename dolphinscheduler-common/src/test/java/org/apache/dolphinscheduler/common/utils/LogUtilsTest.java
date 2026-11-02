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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

public class LogUtilsTest {

    private static final int MAX_RESPONSE_LOG_SIZE = 65535;

    @TempDir
    Path tempDir;

    @Test
    public void testGetFileContentBytesFromLocal() throws IOException {
        Path file = tempDir.resolve("task.log");
        byte[] content = "2026-11-02 INFO task started\r\n2026-11-02 INFO task finished\r\n"
                .getBytes(StandardCharsets.UTF_8);
        Files.write(file, content);
        assertArrayEquals(content, LogUtils.getFileContentBytesFromLocal(file.toString()));
    }

    @Test
    public void testGetFileContentBytesFromLocalFileNotExists() {
        Path notExists = tempDir.resolve("not-exists.log");
        assertEquals(0, LogUtils.getFileContentBytesFromLocal(notExists.toString()).length);
    }

    @Test
    public void testReadPartFileContentFromLocal() throws IOException {
        Path file = tempDir.resolve("part.log");
        List<String> lines = Arrays.asList("line-0", "line-1", "line-2", "line-3", "line-4");
        Files.write(file, lines, StandardCharsets.UTF_8);
        assertEquals(Arrays.asList("line-1", "line-2"),
                LogUtils.readPartFileContentFromLocal(file.toString(), 1, 2));
        assertEquals(Collections.emptyList(),
                LogUtils.readPartFileContentFromLocal(file.toString(), 10, 2));
        assertEquals(lines, LogUtils.readPartFileContentFromLocal(file.toString(), 0, 100));
    }

    @Test
    public void testReadPartFileContentFromLocalFileNotExists() {
        String notExists = tempDir.resolve("not-exists.log").toString();
        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> LogUtils.readPartFileContentFromLocal(notExists, 0, 10));
        assertTrue(exception.getMessage().contains(notExists));
    }

    @Test
    public void testRollViewLogLines() {
        List<String> lines = Arrays.asList("first line", "second line", "third line");
        assertEquals("first line\r\nsecond line\r\nthird line\r\n", LogUtils.rollViewLogLines(lines));
        assertEquals("", LogUtils.rollViewLogLines(Collections.emptyList()));
    }

    @Test
    public void testRollViewLogLinesWithOversizeLine() {
        String oversizeLine = new String(new char[70000]).replace('\0', 'a');
        String expected = oversizeLine.substring(0, MAX_RESPONSE_LOG_SIZE)
                + " [this line's size 70000 bytes is exceed " + MAX_RESPONSE_LOG_SIZE
                + " bytes, so only " + MAX_RESPONSE_LOG_SIZE
                + " characters are reserved for performance reasons.]\r\n";
        assertEquals(expected, LogUtils.rollViewLogLines(Collections.singletonList(oversizeLine)));
    }

    @Test
    public void testRollViewLogLinesStopsWhenTotalSizeExceedsLimit() {
        String bigLine = new String(new char[40000]).replace('\0', 'a');
        String result = LogUtils.rollViewLogLines(Arrays.asList(bigLine, bigLine, "tail"));
        assertEquals(bigLine + "\r\n" + bigLine + "\r\n", result);
    }

    @Test
    public void testGetLocalLogBaseDir() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        String original = loggerContext.getProperty("log.base.ctx");
        try {
            loggerContext.putProperty("log.base.ctx", "/tmp/dolphinscheduler-test-logs");
            assertEquals("/tmp/dolphinscheduler-test-logs", LogUtils.getLocalLogBaseDir());
        } finally {
            loggerContext.putProperty("log.base.ctx", original);
        }
    }

}
