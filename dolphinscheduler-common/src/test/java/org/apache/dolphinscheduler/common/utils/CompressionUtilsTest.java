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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CompressionUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void zipDirectory_preservesNestedStructureAndContent() throws IOException {
        Path source = tempDir.resolve("20240115");
        Path nested = source.resolve("7654321").resolve("1").resolve("42");
        Files.createDirectories(nested);
        Path logFile = nested.resolve("100.log");
        String content = "line-1\nline-2\nline-3\n";
        Files.write(logFile, content.getBytes(StandardCharsets.UTF_8));

        Path zip = tempDir.resolve("archive").resolve("20240115.zip");
        CompressionUtils.zipDirectory(source, zip);

        assertTrue(Files.exists(zip));
        byte[] entryBytes = CompressionUtils.readEntryBytes(zip, "7654321/1/42/100.log");
        assertArrayEquals(content.getBytes(StandardCharsets.UTF_8), entryBytes);
    }

    @Test
    void readEntryBytes_returnsNullForMissingEntry() throws IOException {
        Path source = tempDir.resolve("day");
        Files.createDirectories(source);
        Files.write(source.resolve("a.log"), "x".getBytes(StandardCharsets.UTF_8));
        Path zip = tempDir.resolve("out.zip");
        CompressionUtils.zipDirectory(source, zip);

        assertNull(CompressionUtils.readEntryBytes(zip, "does/not/exist.log"));
    }

    @Test
    void readEntryLines_appliesSkipAndLimit() throws IOException {
        Path source = tempDir.resolve("day");
        Files.createDirectories(source);
        Files.write(source.resolve("a.log"),
                "l0\nl1\nl2\nl3\nl4\n".getBytes(StandardCharsets.UTF_8));
        Path zip = tempDir.resolve("out.zip");
        CompressionUtils.zipDirectory(source, zip);

        List<String> lines = CompressionUtils.readEntryLines(zip, "a.log", 1, 2);
        assertEquals(2, lines.size());
        assertEquals("l1", lines.get(0));
        assertEquals("l2", lines.get(1));
    }

    @Test
    void readEntryLines_handlesFinalLineWithoutTrailingNewline() throws IOException {
        Path source = tempDir.resolve("day");
        Files.createDirectories(source);
        Files.write(source.resolve("a.log"), "only-line".getBytes(StandardCharsets.UTF_8));
        Path zip = tempDir.resolve("out.zip");
        CompressionUtils.zipDirectory(source, zip);

        List<String> lines = CompressionUtils.readEntryLines(zip, "a.log", 0, 10);
        assertEquals(1, lines.size());
        assertEquals("only-line", lines.get(0));
    }

    @Test
    void readEntryLines_stripsCarriageReturn() throws IOException {
        Path source = tempDir.resolve("day");
        Files.createDirectories(source);
        Files.write(source.resolve("a.log"), "l0\r\nl1\r\n".getBytes(StandardCharsets.UTF_8));
        Path zip = tempDir.resolve("out.zip");
        CompressionUtils.zipDirectory(source, zip);

        List<String> lines = CompressionUtils.readEntryLines(zip, "a.log", 0, 10);
        assertEquals(2, lines.size());
        assertEquals("l0", lines.get(0));
        assertEquals("l1", lines.get(1));
    }

    @Test
    void zipDirectory_rejectsNonDirectorySource() {
        Path notADir = tempDir.resolve("missing");
        assertThrows(IOException.class, () -> CompressionUtils.zipDirectory(notADir, tempDir.resolve("x.zip")));
    }
}
