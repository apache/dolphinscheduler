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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RemoteLogClient {

    /**
     * Striped locks serializing download/open against each other per log path. The remote log
     * handlers rewrite the local archive file in place ({@code new FileOutputStream(logPath)}
     * truncates it), so an uncoordinated concurrent download — another download request, or a
     * UI log view of the same task — would truncate the file underneath an ongoing reader.
     * A fixed stripe array avoids unbounded lock-map growth; different logs sharing a stripe
     * only lose a little parallelism, never correctness. Each API instance downloads to its
     * own local disk, so per-JVM locking is sufficient.
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
     * Retrieves the entire log content for a given task instance.
     * This method is used when it is necessary to obtain all the log information for a task instance.
     *
     * @param taskInstance The task instance object, containing information such as the task ID and log path.
     * @return Returns the log content in byte array format.
     */
    public byte[] getWholeLog(TaskInstance taskInstance) {
        final ReentrantLock lock = lockFor(taskInstance.getLogPath());
        lock.lock();
        try {
            // Download + full read under one lock. The download dominates and can take seconds
            // for large archives; only same-log requests (or stripe collisions) serialize.
            return LogUtils.getFileContentBytesFromRemote(taskInstance.getLogPath());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Stream the entire remote-archived log to {@code outputStream} without loading the whole file
     * into memory. Downloads the archive to a local file first (streaming), then streams the local
     * file to the output in fixed-size chunks.
     *
     * <p>The lock covers only download + open + size capture (short windows); the potentially
     * long streaming read runs OUTSIDE the lock, bounded by the captured size — otherwise a
     * second viewer of the same log would block for the entire first transfer. If a concurrent
     * re-download still truncates the file mid-transfer, the bounded read hits EOF early and
     * fails explicitly instead of silently delivering a truncated download.
     *
     * @throws IOException if the log cannot be downloaded, the file is missing, no data is
     *                     available, or the file was modified during the transfer.
     */
    public void streamWholeLog(final TaskInstance taskInstance,
                               final OutputStream outputStream) throws IOException {
        final String logPath = taskInstance.getLogPath();
        final InputStream in;
        final long expectedLength;
        final ReentrantLock lock = lockFor(logPath);
        lock.lock();
        try {
            RemoteLogUtils.getRemoteLog(logPath);
            final File file = new File(logPath);
            if (!file.exists() || !file.isFile()) {
                throw new IOException("Remote log file not found after download: " + logPath);
            }
            // A 0-byte archive is a LEGAL empty log ("task produced no output") — the same
            // terminal state as an empty log served by a live worker — and must stream
            // normally (zero bytes; the caller appends the head). Only a MISSING file is an
            // error: a missing archive must not be reported as a successful empty download.
            expectedLength = file.length();
            // Open inside the lock: the fd is bound to the inode we just measured.
            in = new FileInputStream(file);
        } finally {
            lock.unlock();
        }
        try {
            streamBounded(in, expectedLength, outputStream);
        } finally {
            in.close();
        }
        outputStream.flush();
    }

    /**
     * Copies exactly {@code expectedLength} bytes from {@code in} to {@code outputStream}; an
     * early EOF means the file was truncated/rewritten underneath the reader (concurrent
     * download of the same log) and must fail explicitly rather than end the HTTP response
     * cleanly with a short body. {@code IOUtils.copyLarge} clamps every buffer write to the
     * remaining length, so bytes appended after our size snapshot never leak into the stream.
     */
    void streamBounded(final InputStream in, final long expectedLength,
                       final OutputStream outputStream) throws IOException {
        final long copied = IOUtils.copyLarge(in, outputStream, 0, expectedLength);
        if (copied < expectedLength) {
            throw new IOException("Log file was modified during transfer: expected "
                    + expectedLength + " bytes, read " + copied
                    + " — a concurrent download replaced it, please retry");
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
