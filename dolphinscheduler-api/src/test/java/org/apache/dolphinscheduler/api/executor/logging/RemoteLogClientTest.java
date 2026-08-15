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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class RemoteLogClientTest {

    /**
     * Remote logging is disabled in tests, so RemoteLogUtils.getRemoteLog is a no-op and the
     * local file placed by the test is streamed as-is.
     */
    @Test
    public void streamWholeLog_streamsLocalArchiveFile(@TempDir Path tempDir) throws Exception {
        Path logFile = tempDir.resolve("task.log");
        byte[] content = "0123456789ABCDEFG".getBytes(StandardCharsets.UTF_8);
        Files.write(logFile, content);

        RemoteLogClient client = new RemoteLogClient();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        client.streamWholeLog(taskInstance(logFile.toString()), out);

        assertArrayEquals(content, out.toByteArray());
    }

    @Test
    public void streamWholeLog_missingFileThrows(@TempDir Path tempDir) {
        RemoteLogClient client = new RemoteLogClient();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        IOException thrown = assertThrows(IOException.class,
                () -> client.streamWholeLog(taskInstance(tempDir.resolve("absent.log").toString()), out));
        assertTrue(thrown.getMessage().contains("not found"));
    }

    /**
     * An archived log that EXISTS but is 0 bytes is a legal empty log ("task produced no
     * output") — the same terminal state as an empty log on a live worker. It must stream
     * normally (zero bytes written; the caller appends the head), NOT be treated as
     * "log unavailable". Only a file that is MISSING after the download is an error.
     */
    @Test
    public void streamWholeLog_emptyArchiveIsLegalEmptyLog(@TempDir Path tempDir) throws Exception {
        Path logFile = tempDir.resolve("empty.log");
        Files.write(logFile, new byte[0]);

        RemoteLogClient client = new RemoteLogClient();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        client.streamWholeLog(taskInstance(logFile.toString()), out);

        assertEquals(0, out.toByteArray().length);
    }

    /**
     * The streaming read is bounded by the file size captured at open time: if a concurrent
     * re-download truncates the file mid-transfer, the reader hits EOF early and MUST fail
     * explicitly — silently returning a short download that looks complete is data corruption.
     */
    @Test
    public void streamBounded_shortReadThrowsInsteadOfSilentTruncation() {
        final RemoteLogClient client = new RemoteLogClient();
        final byte[] onlyHalfThere = new byte[50];
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        // File was 100 bytes when opened; a concurrent truncate left only 50 readable.
        IOException thrown = assertThrows(IOException.class,
                () -> client.streamBounded(new ByteArrayInputStream(onlyHalfThere), 100, out));
        assertTrue(thrown.getMessage().contains("modified during transfer"));
        // whatever was read before the failure was already written — that is fine, the point
        // is that the failure is EXPLICIT, the client sees a broken transfer not a clean 200.
    }

    private static TaskInstance taskInstance(final String logPath) {
        final TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setLogPath(logPath);
        return taskInstance;
    }
}
