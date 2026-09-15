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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
        assertTrue(thrown.getMessage().contains("short read"));
        // whatever was read before the failure was already written — that is fine, the point
        // is that the failure is EXPLICIT, the client sees a broken transfer not a clean 200.
    }

    /**
     * Regression: the archive cache is REWRITTEN IN PLACE by a concurrent download or log view
     * (a re-download truncates the file) while an active transfer is streaming. The active
     * transfer must still deliver the ORIGINAL bytes from its private snapshot — it must not
     * fail with a premature EOF, and must not return a truncated download. Deterministic: the
     * active download gates after its first written byte until the cache rewrite is done.
     */
    @Test
    public void streamWholeLog_concurrentCacheRewriteDuringTransfer_activeDownloadUnaffected(
                                                                                             @TempDir Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve("task.log");
        final byte[] original = patternedBytes(512 * 1024, 251);
        Files.write(logFile, original);

        final CountDownLatch firstByteWritten = new CountDownLatch(1);
        final CountDownLatch cacheRewritten = new CountDownLatch(1);
        final ByteArrayOutputStream out = new ByteArrayOutputStream() {

            @Override
            public void write(final byte[] b, final int off, final int len) {
                super.write(b, off, len);
                firstByteWritten.countDown();
                try {
                    if (!cacheRewritten.await(15, TimeUnit.SECONDS)) {
                        throw new RuntimeException("Test gate timed out waiting for the cache rewrite");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Test gate interrupted", e);
                }
            }
        };

        final RemoteLogClient client = new RemoteLogClient();
        final ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            final Future<Throwable> transfer = pool.submit(() -> {
                try {
                    client.streamWholeLog(taskInstance(logFile.toString()), out);
                    return null;
                } catch (Throwable t) {
                    return t;
                }
            });

            // Wait until the active transfer has its snapshot and started streaming, then
            // rewrite the cache exactly like a concurrent re-download would (truncate + write).
            assertTrue(firstByteWritten.await(15, TimeUnit.SECONDS),
                    "Active transfer never started — streaming did not begin");
            Files.write(logFile, "rewritten-by-a-concurrent-download".getBytes(StandardCharsets.UTF_8));
            cacheRewritten.countDown();

            final Throwable thrown = transfer.get(30, TimeUnit.SECONDS);
            assertNull(thrown, "Active download must survive the concurrent cache rewrite");
            assertArrayEquals(original, out.toByteArray(), "Active download must deliver the original bytes");
            // The private snapshot must be cleaned up after the transfer.
            assertOnlyArchiveRemains(tempDir);
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     * The snapshot must be deleted even when the TRANSFER fails (client disconnects mid-download
     * → the output stream throws): a "delete only on success" regression would slowly fill the
     * log dir with one orphaned file per aborted download. The transfer throws on the FIRST
     * write, so a small archive is enough — the content is never asserted.
     */
    @Test
    public void streamWholeLog_transferFailsMidStream_snapshotStillDeleted(@TempDir Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve("task.log");
        Files.write(logFile, new byte[64 * 1024]);

        final OutputStream clientDisconnected = new OutputStream() {

            @Override
            public void write(final int b) throws IOException {
                throw new IOException("client went away");
            }

            @Override
            public void write(final byte[] b, final int off, final int len) throws IOException {
                throw new IOException("client went away");
            }
        };

        final RemoteLogClient client = new RemoteLogClient();
        assertThrows(IOException.class,
                () -> client.streamWholeLog(taskInstance(logFile.toString()), clientDisconnected));

        // Exactly the archive remains — the failed transfer's snapshot was still cleaned up.
        assertOnlyArchiveRemains(tempDir);
    }

    /**
     * The startup sweep deletes snapshot files orphaned by a previous JVM life, keeps snapshots
     * younger than the age gate (another live instance's in-flight transfer on a shared disk)
     * and keeps ordinary log files.
     */
    @Test
    public void deleteOrphanedSnapshots_removesOnlyAgedSnapshots(@TempDir Path tempDir) throws Exception {
        final Path oldOrphan = tempDir.resolve("task.log.download-1111");
        Files.write(oldOrphan, new byte[]{1});
        final Path freshSnapshot = tempDir.resolve("task.log.download-2222");
        Files.write(freshSnapshot, new byte[]{2});
        final Path ordinaryLog = tempDir.resolve("task.log");
        Files.write(ordinaryLog, new byte[]{3});
        // Age the first file past the orphan threshold.
        oldOrphan.toFile().setLastModified(System.currentTimeMillis() - 2 * 60 * 60 * 1000L);

        final RemoteLogClient client = new RemoteLogClient();
        client.deleteOrphanedSnapshots(tempDir);

        assertTrue(Files.notExists(oldOrphan), "Aged orphan must be swept");
        assertTrue(Files.exists(freshSnapshot), "Snapshot within the age gate must be kept");
        assertTrue(Files.exists(ordinaryLog), "Ordinary log files must never be swept");
    }

    private static TaskInstance taskInstance(final String logPath) {
        final TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setLogPath(logPath);
        return taskInstance;
    }

    /**
     * A patterned (non-constant) buffer of {@code size} bytes — makes it possible to detect
     * content corruption instead of just length changes.
     */
    private static byte[] patternedBytes(final int size, final int modulus) {
        final byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = (byte) (i % modulus);
        }
        return bytes;
    }

    /**
     * After a completed (or failed) transfer of the archive {@code tempDir/task.log}, exactly
     * that archive must remain: the private download snapshot must be gone. Asserting the file
     * COUNT (not a name filter) keeps this honest if the snapshot naming convention ever
     * changes.
     */
    private static void assertOnlyArchiveRemains(final Path tempDir) throws IOException {
        try (Stream<Path> leftovers = Files.list(tempDir)) {
            assertEquals(1, leftovers.count(), "Only the archive must remain — the snapshot must be deleted");
        }
    }
}
