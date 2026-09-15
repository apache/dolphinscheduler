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

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LogUtilsTest {

    /**
     * The caller passes the file length it observed ONCE (single-stat contract): the read must
     * clamp against that length, never stat the file again — re-statting would race against
     * rotation/shrink between the two observations.
     */
    @Test
    public void readFileRange_readsClampedToObservedLength(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("chunk.log");
        byte[] content = "0123456789ABCDEFG".getBytes(StandardCharsets.UTF_8);
        Files.write(file, content);

        byte[] data = LogUtils.readFileRange(file.toFile(), 2, 5, content.length);
        assertArrayEquals("23456".getBytes(StandardCharsets.UTF_8), data);
    }

    @Test
    public void readFileRange_offsetAtObservedLengthReturnsEmpty(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("eof.log");
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);
        Files.write(file, content);

        byte[] data = LogUtils.readFileRange(file.toFile(), content.length, 10, content.length);
        assertEquals(0, data.length);
    }

    /**
     * If the file SHRANK after the caller's single stat (rotation/truncation), the read must
     * fail explicitly (EOF) instead of silently returning a short chunk — the caller turns
     * this into an error response.
     */
    @Test
    public void readFileRange_fileShrankAfterObservation_failsExplicitly(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("shrunk.log");
        Files.write(file, "short".getBytes(StandardCharsets.UTF_8));

        // Caller observed 100 bytes; the file is now only 5 — readFully must hit EOF.
        assertThrows(EOFException.class,
                () -> LogUtils.readFileRange(file.toFile(), 0, 10, 100));
    }

    @Test
    public void readFileRange_missingFileThrows(@TempDir Path tempDir) {
        assertThrows(FileNotFoundException.class,
                () -> LogUtils.readFileRange(tempDir.resolve("absent.log").toFile(), 0, 10, 100));
    }
}
