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
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RemoteLogClient {

    /**
     * Marker of the per-download snapshot files ({@code <archive>.download-<uuid>}), also used
     * by the startup sweep to recognize orphans.
     */
    private static final String SNAPSHOT_MARKER = ".download-";

    /**
     * Snapshots younger than this are never swept as orphans: on the (unsupported but possible)
     * shared-disk setup the age gate protects another live instance's in-flight transfer.
     */
    private static final long ORPHAN_SNAPSHOT_MIN_AGE_MILLIS = 60 * 60 * 1000L;

    /**
     * Striped locks serializing download + snapshot per log path. The remote log handlers
     * rewrite the local archive file in place ({@code new FileOutputStream(logPath)} truncates
     * it), so concurrent download/view requests for the same log must not interleave their
     * download windows; {@link #streamWholeLog} additionally snapshots the archive into a
     * private temp file, so its lock-free streaming read stays stable across later cache
     * rewrites. A fixed stripe array avoids unbounded lock-map growth; different logs sharing
     * a stripe only lose a little parallelism, never correctness. Each API instance downloads
     * to its own local disk, so per-JVM locking is sufficient.
     */
    private static final int LOCK_STRIPES = 64;
    private static final ReentrantLock[] LOG_PATH_LOCKS = new ReentrantLock[LOCK_STRIPES];

    static {
        for (int i = 0; i < LOCK_STRIPES; i++) {
            LOG_PATH_LOCKS[i] = new ReentrantLock();
        }
    }

    static ReentrantLock lockFor(final String logPath) {
        return LOG_PATH_LOCKS[(logPath.hashCode() & 0x7fffffff) % LOCK_STRIPES];
    }

    /**
     * At startup this JVM can have no in-flight transfer, so any snapshot file left over from a
     * previous life (graceful shutdown and kill -9 alike skip the transfer's {@code finally}) is
     * an orphan and is swept. Best effort: any error just skips the sweep. Does nothing when
     * logging is not initialized (e.g. plain unit tests).
     */
    @PostConstruct
    public void deleteOrphanedSnapshots() {
        final String baseDir = LogUtils.getLocalLogBaseDir();
        if (baseDir != null) {
            deleteOrphanedSnapshots(Paths.get(baseDir));
        }
    }

    void deleteOrphanedSnapshots(final Path baseDir) {
        try (Stream<Path> walk = Files.walk(baseDir)) {
            walk.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().contains(SNAPSHOT_MARKER))
                    .filter(RemoteLogClient::olderThanOrphanAge)
                    .forEach(this::deleteSnapshotQuietly);
        } catch (Exception e) {
            log.warn("Failed to sweep orphaned log download snapshots under {}", baseDir, e);
        }
    }

    private static boolean olderThanOrphanAge(final Path file) {
        // java.io.File#lastModified returns 0 when the time cannot be read — treat that as
        // "unknown age, keep it" rather than sweeping blindly.
        final long lastModified = file.toFile().lastModified();
        return lastModified > 0 && lastModified < System.currentTimeMillis() - ORPHAN_SNAPSHOT_MIN_AGE_MILLIS;
    }

    /**
     * Stream the entire remote-archived log to {@code outputStream} without loading the whole
     * file into memory. Downloads the archive to a local file, snapshots it into a private temp
     * file ({@code <archive>.download-<uuid>}, deleted when the transfer ends; leftovers are
     * swept by {@link #deleteOrphanedSnapshots()}) and streams that snapshot — see the
     * {@code LOG_PATH_LOCKS} note for why the snapshot is required. Costs a second write of the
     * archive (~2x peak disk for one download).
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
            snapshot = archive.resolveSibling(archive.getFileName() + SNAPSHOT_MARKER + UUID.randomUUID());
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
                    deleteSnapshotQuietly(snapshot);
                }
            }
        } finally {
            lock.unlock();
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
            deleteSnapshotQuietly(snapshot);
        }
        outputStream.flush();
    }

    private void deleteSnapshotQuietly(final Path snapshot) {
        try {
            Files.deleteIfExists(snapshot);
        } catch (Throwable e) {
            // Throwable, not IOException: this runs in finally blocks and must never throw
            // through them (masking the original failure) or give up on deletion early.
            log.warn("Failed to delete the log download snapshot {}", snapshot, e);
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
        }
    }

}
