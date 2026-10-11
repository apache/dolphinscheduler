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

package org.apache.dolphinscheduler.common.log.remote;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Contract of the atomic download helper every remote handler publishes through: the archive is
 * only ever replaced by a COMPLETE download, and a failed transfer propagates instead of leaving
 * a partial file behind.
 */
public class RemoteLogUtilsTest {

    @Test
    public void downloadToLocalFileAtomically_completeTransferPublishesArchive(@TempDir Path tempDir) throws Exception {
        final Path archive = tempDir.resolve("task.log");
        final byte[] content = "complete remote log".getBytes(StandardCharsets.UTF_8);

        RemoteLogUtils.downloadToLocalFileAtomically(archive.toString(), staging -> Files.write(staging, content));

        assertArrayEquals(content, Files.readAllBytes(archive));
        assertEquals(1, fileCount(tempDir), "Only the published archive must remain");
    }

    @Test
    public void downloadToLocalFileAtomically_completeTransferReplacesExistingArchive(@TempDir Path tempDir) throws Exception {
        final Path archive = tempDir.resolve("task.log");
        Files.write(archive, "previously published log".getBytes(StandardCharsets.UTF_8));
        final byte[] fresh = "freshly downloaded log".getBytes(StandardCharsets.UTF_8);

        RemoteLogUtils.downloadToLocalFileAtomically(archive.toString(), staging -> Files.write(staging, fresh));

        assertArrayEquals(fresh, Files.readAllBytes(archive));
        assertEquals(1, fileCount(tempDir), "Only the published archive must remain");
    }

    /**
     * A transfer failing mid-way must not publish anything: the archive is absent, the partial
     * staging file is deleted, and the failure propagates so the download is not reported as a
     * successful one.
     */
    @Test
    public void downloadToLocalFileAtomically_midTransferFailure_publishesNothing(@TempDir Path tempDir) {
        final Path archive = tempDir.resolve("task.log");

        final IOException thrown = assertThrows(IOException.class,
                () -> RemoteLogUtils.downloadToLocalFileAtomically(archive.toString(), staging -> {
                    Files.write(staging, "partial download".getBytes(StandardCharsets.UTF_8));
                    throw new IOException("connection reset mid-transfer");
                }));

        assertEquals("connection reset mid-transfer", thrown.getMessage());
        assertTrue(Files.notExists(archive), "A failed download must never be published");
        assertEquals(0, fileCount(tempDir), "The partial staging file must be deleted");
    }

    /**
     * A failed re-download must leave an already cached archive byte-identical: the previously
     * published complete download must not be replaced by a partial transfer.
     */
    @Test
    public void downloadToLocalFileAtomically_midTransferFailure_keepsExistingCachedArchive(@TempDir Path tempDir) throws Exception {
        final Path archive = tempDir.resolve("task.log");
        final byte[] cached = "previously published complete log".getBytes(StandardCharsets.UTF_8);
        Files.write(archive, cached);

        assertThrows(IOException.class,
                () -> RemoteLogUtils.downloadToLocalFileAtomically(archive.toString(), staging -> {
                    Files.write(staging, "partial".getBytes(StandardCharsets.UTF_8));
                    throw new IOException("connection reset mid-transfer");
                }));

        assertArrayEquals(cached, Files.readAllBytes(archive),
                "The cached archive must not be touched by a failed download");
        assertEquals(1, fileCount(tempDir), "Only the cached archive must remain");
    }

    /**
     * A provider reporting a missing object with a runtime exception (e.g. the GCS null-blob
     * case) must surface as a download failure, never as a successful empty download.
     */
    @Test
    public void downloadToLocalFileAtomically_runtimeFailure_isWrappedAsIOException(@TempDir Path tempDir) {
        final Path archive = tempDir.resolve("task.log");

        final IOException thrown = assertThrows(IOException.class,
                () -> RemoteLogUtils.downloadToLocalFileAtomically(archive.toString(), staging -> {
                    throw new IllegalStateException("object not found");
                }));

        assertEquals("object not found", thrown.getCause().getMessage());
        assertTrue(Files.notExists(archive));
        assertEquals(0, fileCount(tempDir));
    }

    /**
     * The per-download sweep must delete only per-download temp files (snapshot or staging, the
     * marker decides) that are past the age gate — a freshly created temp file may belong to an
     * in-flight transfer and ordinary log files are never touched.
     */
    @Test
    public void deleteAgedDownloadTempFiles_removesOnlyAgedTempFiles(@TempDir Path dir) throws Exception {
        final Path agedTemp = Files.write(dir.resolve("1.log" + RemoteLogUtils.DOWNLOAD_TEMP_FILE_MARKER + "orphan"),
                new byte[]{1});
        final Path freshTemp = Files.write(dir.resolve("1.log" + RemoteLogUtils.DOWNLOAD_TEMP_FILE_MARKER + "inflight"),
                new byte[]{2});
        final Path ordinaryLog = Files.write(dir.resolve("1.log"), new byte[]{3});
        agedTemp.toFile()
                .setLastModified(System.currentTimeMillis() - 2 * RemoteLogUtils.ORPHAN_DOWNLOAD_TEMP_MIN_AGE_MILLIS);

        RemoteLogUtils.deleteAgedDownloadTempFiles(dir);

        assertTrue(Files.notExists(agedTemp), "An aged temp file is an orphan and must be swept");
        assertTrue(Files.exists(freshTemp), "A temp file within the age gate may be in flight and must be kept");
        assertTrue(Files.exists(ordinaryLog), "Ordinary log files must never be swept");
    }

    /**
     * Regression: archives are downloaded to the task's log path, which can live outside the
     * API's local log base dir — the startup sweep alone would never see a leftover there. The
     * sweep therefore also runs before each download, in the archive's own directory.
     */
    @Test
    public void getRemoteLog_sweepsAgedTempFilesNextToTheArchive(@TempDir Path tempDir) throws Exception {
        final Path logDir = Files.createDirectories(tempDir.resolve("logs/20261004/1/1"));
        final Path logPath = logDir.resolve("1.log");
        final Path agedOrphan =
                Files.write(logDir.resolve("1.log" + RemoteLogUtils.DOWNLOAD_TEMP_FILE_MARKER + "orphan"),
                        new byte[]{1});
        agedOrphan.toFile()
                .setLastModified(System.currentTimeMillis() - 2 * RemoteLogUtils.ORPHAN_DOWNLOAD_TEMP_MIN_AGE_MILLIS);

        try (MockedStatic<PropertyUtils> propertyUtils = Mockito.mockStatic(PropertyUtils.class)) {
            // Remote logging enabled with an unknown target: the handler factory returns null and
            // the download never starts, but the opportunistic sweep must already have run.
            propertyUtils.when(() -> PropertyUtils.getBoolean(Constants.REMOTE_LOGGING_ENABLE, Boolean.FALSE))
                    .thenReturn(true);
            propertyUtils.when(() -> PropertyUtils.getString(Constants.REMOTE_LOGGING_TARGET))
                    .thenReturn("not-a-target");
            propertyUtils.when(() -> PropertyUtils.getUpperCaseString(Constants.REMOTE_LOGGING_TARGET))
                    .thenReturn("NOT-A-TARGET");

            RemoteLogUtils.getRemoteLog(logPath.toString());
        }

        assertTrue(Files.notExists(agedOrphan),
                "The aged temp file next to the archive must be swept before the download");
    }

    private static long fileCount(final Path dir) {
        try (Stream<Path> files = Files.list(dir)) {
            return files.count();
        } catch (IOException e) {
            throw new AssertionError("Failed to list " + dir, e);
        }
    }
}
