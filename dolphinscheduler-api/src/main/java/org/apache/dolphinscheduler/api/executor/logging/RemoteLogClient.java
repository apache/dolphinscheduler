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

import org.apache.dolphinscheduler.common.log.remote.RemoteLogUtils;
import org.apache.dolphinscheduler.common.utils.LogUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RemoteLogClient {

    /**
     * Marker of the per-download temporary files ({@code <archive>.download-<uuid>}): the private
     * streaming snapshots of this class and the staging files the remote log handlers download
     * into (see {@link RemoteLogUtils#downloadToLocalFileAtomically}). Also used by the startup
     * sweep to recognize orphans.
     */
    private static final String DOWNLOAD_TEMP_MARKER = RemoteLogUtils.DOWNLOAD_TEMP_FILE_MARKER;

    /**
     * Per-download temp files younger than this are never swept as orphans: on the (unsupported
     * but possible) shared-disk setup the age gate protects another live instance's in-flight
     * transfer. Shared with {@link RemoteLogUtils#deleteAgedDownloadTempFiles(Path)}, the
     * per-download cleanup that also covers the directories this startup sweep cannot reach.
     */
    private static final long ORPHAN_TEMP_FILE_MIN_AGE_MILLIS =
            RemoteLogUtils.ORPHAN_DOWNLOAD_TEMP_MIN_AGE_MILLIS;

    /**
     * Per-log-path locks: concurrent requests for the SAME log coalesce (the second waits for the
     * in-flight download instead of re-downloading a possibly multi-GB archive), while requests
     * for different logs never block each other — the previous fixed 64-stripe array made
     * unrelated logs that hashed to the same stripe wait behind a whole remote download. Entries
     * are reference-counted and removed with the last user, so the map is bounded by the number
     * of in-flight log paths, and {@link #streamWholeLog}'s private snapshot keeps its lock-free
     * streaming read stable across later cache replacements. Each API instance downloads to its
     * own local disk, so per-JVM locking is sufficient.
     */
    private static final ConcurrentHashMap<String, LockEntry> LOG_PATH_LOCKS = new ConcurrentHashMap<>();

    private static final class LockEntry {

        private final ReentrantLock lock = new ReentrantLock();

        /** Only touched inside the map's per-key compute calls — never read or written outside. */
        private int references;
    }

    /**
     * Take a reference to the lock of {@code logPath}; every call must be paired with
     * {@link #unlockFor(String)} (after {@code lock.unlock()}) so the entry can be released.
     */
    static ReentrantLock lockFor(final String logPath) {
        return LOG_PATH_LOCKS.compute(logPath, (path, entry) -> {
            final LockEntry current = entry != null ? entry : new LockEntry();
            current.references++;
            return current;
        }).lock;
    }

    /**
     * Drop one reference to {@code logPath}'s lock entry, removing it when the last user is gone.
     * Must run AFTER {@code lock.unlock()}: an entry removed while still held would let a new
     * arrival lock a fresh entry concurrently with the holder.
     */
    static void unlockFor(final String logPath) {
        LOG_PATH_LOCKS.computeIfPresent(logPath, (path, entry) -> --entry.references == 0 ? null : entry);
    }

    /**
     * At startup this JVM can have no in-flight transfer, so any per-download temp file left over
     * from a previous life — this class's streaming snapshot or a remote handler's download
     * staging file (graceful shutdown and kill -9 alike skip the transfer's {@code finally}) — is
     * an orphan and is swept. Only the local log base dir can be walked here; the archive
     * directories (which follow task log paths and may live elsewhere) are covered by
     * {@link RemoteLogUtils#deleteAgedDownloadTempFiles(Path)} before each download. Best effort:
     * any error just skips the sweep. Does nothing when logging is not initialized (e.g. plain
     * unit tests).
     */
    @PostConstruct
    public void deleteOrphanedTempFiles() {
        final String baseDir = LogUtils.getLocalLogBaseDir();
        if (baseDir != null) {
            deleteOrphanedTempFiles(Paths.get(baseDir));
        }
    }

    void deleteOrphanedTempFiles(final Path baseDir) {
        try (Stream<Path> walk = Files.walk(baseDir)) {
            walk.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().contains(DOWNLOAD_TEMP_MARKER))
                    .filter(RemoteLogClient::olderThanOrphanAge)
                    .forEach(this::deleteTempFileQuietly);
        } catch (Exception e) {
            log.warn("Failed to sweep orphaned log download temp files under {}", baseDir, e);
        }
    }

    private static boolean olderThanOrphanAge(final Path file) {
        // java.io.File#lastModified returns 0 when the time cannot be read — treat that as
        // "unknown age, keep it" rather than sweeping blindly.
        final long lastModified = file.toFile().lastModified();
        return lastModified > 0 && lastModified < System.currentTimeMillis() - ORPHAN_TEMP_FILE_MIN_AGE_MILLIS;
    }

    /**
     * Stream the entire remote-archived log to {@code outputStream} without loading the whole
     * file into memory. Downloads the archive to a local file, snapshots it into a private temp
     * file ({@code <archive>.download-<uuid>}, deleted when the transfer ends; leftovers are
     * swept by {@link #deleteOrphanedTempFiles()}) and streams that snapshot — see the
     * {@code LOG_PATH_LOCKS} note for why the snapshot is required. Costs a second write of the
     * archive (~2x peak disk for one download).
     *
     * <p>A failed remote download propagates — nothing is streamed and any previously published
     * archive is left untouched (the handler publishes a download only after a complete
     * transfer), so a truncated log can never be served as a successful download.
     *
     * @throws IOException if the log cannot be downloaded, the file is missing, no data is
     *                     available, or the snapshot ends prematurely (local disk trouble).
     */
    public void streamWholeLog(final TaskInstance taskInstance,
                               final OutputStream outputStream) throws IOException {
        final String logPath = taskInstance.getLogPath();
        final Path archive = Paths.get(logPath);
        final InputStream in;
        final long expectedLength;
        Path snapshot = null;
        final ReentrantLock lock = lockFor(logPath);
        lock.lock();
        try {
            RemoteLogUtils.getRemoteLog(logPath);
            if (!Files.isRegularFile(archive)) {
                throw new IOException("Remote log file not found after download (remote log archiving may not be "
                        + "enabled or the archive is missing): " + logPath);
            }
            // A 0-byte archive is a LEGAL empty log ("task produced no output") — the same
            // terminal state as an empty log served by a live worker — and must stream
            // normally (zero bytes; the caller appends the head). Only a MISSING file is an
            // error: a missing archive must not be reported as a successful empty download.
            snapshot = archive.resolveSibling(archive.getFileName() + DOWNLOAD_TEMP_MARKER + UUID.randomUUID());
            boolean opened = false;
            try {
                Files.copy(archive, snapshot, StandardCopyOption.REPLACE_EXISTING);
                expectedLength = Files.size(snapshot);
                // Open inside the lock, after the size capture — the stream reads our private
                // snapshot, which nothing else can modify.
                in = new FileInputStream(snapshot.toFile());
                opened = true;
            } finally {
                // finally (not catch): a partial snapshot must never survive a failed creation,
                // whatever the failure type — up to and including Error (e.g. OOM mid-copy).
                if (!opened) {
                    deleteTempFileQuietly(snapshot);
                }
            }
        } finally {
            lock.unlock();
            unlockFor(logPath);
        }
        try {
            streamBounded(in, expectedLength, outputStream);
        } finally {
            try {
                in.close();
            } catch (Throwable e) {
                // Throwable, not IOException: a close failure must neither mask the transfer's
                // own exception nor skip the snapshot deletion below.
                log.warn("Failed to close the log download snapshot stream for {}", logPath, e);
            }
            deleteTempFileQuietly(snapshot);
        }
        outputStream.flush();
    }

    private void deleteTempFileQuietly(final Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
        } catch (Throwable e) {
            // Throwable, not IOException: this runs in finally blocks and must never throw
            // through them (masking the original failure) or give up on deletion early.
            log.warn("Failed to delete the log download temp file {}", tempFile, e);
        }
    }

    /**
     * Copies exactly {@code expectedLength} bytes from {@code in} to {@code outputStream}; an
     * early EOF must fail explicitly rather than end the HTTP response cleanly with a short
     * body. With the private snapshot this should be unreachable — nothing else modifies it —
     * and is kept as defense in depth against local disk trouble. {@code IOUtils.copyLarge}
     * clamps every buffer write to the remaining length, so bytes appended after our size
     * snapshot never leak into the stream.
     */
    void streamBounded(final InputStream in, final long expectedLength,
                       final OutputStream outputStream) throws IOException {
        final long copied = IOUtils.copyLarge(in, outputStream, 0, expectedLength);
        if (copied < expectedLength) {
            throw new IOException("Log file short read: expected " + expectedLength + " bytes but the local"
                    + " snapshot ended after " + copied + " (local disk trouble), please retry");
        }
    }

    /**
     * Retrieves part of the log content for a given task instance, based on the specified line number and the number of lines to read.
     * This method is used when it is necessary to browse a portion of the log content, allowing for skipping a certain number of lines and limiting the number of lines to read.
     *
     * @param taskInstance The task instance object, containing information such as the task ID and log path.
     * @param skipLineNum The number of lines to skip, starting from the beginning of the log.
     * @param limit The maximum number of lines to read.
     * @return Returns the specified part of the log content in string format.
     */
    public String getPartLog(TaskInstance taskInstance, int skipLineNum, int limit) {
        final ReentrantLock lock = lockFor(taskInstance.getLogPath());
        lock.lock();
        try {
            // Download + bounded partial read (response is line-limited) under one lock; the
            // download dominates the duration — only same-log requests (or stripe collisions)
            // serialize.
            // todo We can optimize requests by the actual range, reducing disk usage and network traffic.
            return LogUtils.rollViewLogLines(
                    LogUtils.readPartFileContentFromRemote(taskInstance.getLogPath(), skipLineNum, limit));
        } finally {
            lock.unlock();
            unlockFor(taskInstance.getLogPath());
        }
    }

}
