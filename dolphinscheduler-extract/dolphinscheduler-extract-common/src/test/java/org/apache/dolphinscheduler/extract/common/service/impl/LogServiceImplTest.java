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

package org.apache.dolphinscheduler.extract.common.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.apache.dolphinscheduler.extract.common.transportor.LogResponseStatus;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class LogServiceImplTest {

    private final LogServiceImpl logService = new LogServiceImpl();

    @Test
    void getTaskInstanceLogFileChunk_normalRangeReturnsSuccess() throws IOException {
        Path file = Files.createTempFile("ds-chunk", ".log");
        try {
            byte[] content = new byte[20];
            for (int i = 0; i < 20; i++) {
                content[i] = (byte) i;
            }
            Files.write(file, content);

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(0);
            req.setLength(10);

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
            assertEquals(LogResponseStatus.SUCCESS, resp.getCode());
            assertEquals(10, resp.getLogBytes().length);
            assertTrue(!resp.isEof());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    /**
     * The chunk response reports the worker's single-stat file length: the API pins the
     * download's snapshot target to the FIRST chunk's value, so it must be present (0 is only
     * for workers that do not report it, e.g. during a rolling upgrade).
     */
    @Test
    void getTaskInstanceLogFileChunk_reportsObservedFileLength() throws IOException {
        Path file = Files.createTempFile("ds-chunk-observed", ".log");
        try {
            Files.write(file, new byte[20]);

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(0);
            req.setLength(10);

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);

            assertEquals(LogResponseStatus.SUCCESS, resp.getCode());
            assertEquals(20, resp.getObservedLength(), "the snapshot target comes from this value");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void getTaskInstanceLogFileChunk_lastChunkEof() throws IOException {
        Path file = Files.createTempFile("ds-chunk-eof", ".log");
        try {
            byte[] content = new byte[20];
            Files.write(file, content);

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(16);
            req.setLength(10);

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
            assertEquals(LogResponseStatus.SUCCESS, resp.getCode());
            assertEquals(4, resp.getLogBytes().length);
            assertTrue(resp.isEof());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    /**
     * offset BEYOND the file length can only mean the file shrank underneath the reader —
     * log rotation (fixed-window renames the active file, a fresh one starts from 0) or
     * truncation. A well-behaved client advances offset monotonically from 0, so it can never
     * legitimately be past EOF. Reporting SUCCESS+empty here would silently hand the caller a
     * truncated download that looks complete.
     */
    @Test
    void getTaskInstanceLogFileChunk_offsetBeyondShrunkFileReturnsTruncated() throws IOException {
        Path file = Files.createTempFile("ds-chunk-beyond", ".log");
        try {
            Files.write(file, "hello".getBytes(StandardCharsets.UTF_8));

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(100);
            req.setLength(10);

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
            assertEquals(LogResponseStatus.LOG_TRUNCATED, resp.getCode());
            assertTrue(resp.isEof());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    /**
     * offset EXACTLY at the file length is the normal end-of-file reached after consuming the
     * whole file — must stay SUCCESS+empty+eof (the chunk loop's clean termination path).
     */
    @Test
    void getTaskInstanceLogFileChunk_offsetEqualsFileLengthIsNormalEof() throws IOException {
        Path file = Files.createTempFile("ds-chunk-at-eof", ".log");
        try {
            Files.write(file, "hello".getBytes(StandardCharsets.UTF_8));

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(5);
            req.setLength(10);

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
            assertEquals(LogResponseStatus.SUCCESS, resp.getCode());
            assertEquals(0, resp.getLogBytes().length);
            assertTrue(resp.isEof());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void getTaskInstanceLogFileChunk_fileNotFound() {
        TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
        req.setTaskInstanceLogAbsolutePath("/nonexistent/chunk.log");
        req.setOffset(0);
        req.setLength(10);

        TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
        assertEquals(LogResponseStatus.LOG_FILE_NOT_FOUND, resp.getCode());
        assertTrue(resp.isEof());
    }

    /**
     * A MISSING log file must be reported as LOG_FILE_NOT_FOUND, not as SUCCESS with an empty
     * body: an empty log is a valid terminal state ("task produced no output") while a missing
     * one tells the caller to fall back to the remote archive. getFileContentBytesFromLocal
     * swallows the FileNotFoundException and returns an empty array, so without this explicit
     * check the two states are indistinguishable on the wire.
     */
    @Test
    void getTaskInstanceWholeLogFileBytes_missingFileReturnsNotFound() {
        TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
        req.setTaskInstanceLogAbsolutePath("/nonexistent/whole.log");

        TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceWholeLogFileBytes(req);
        assertEquals(LogResponseStatus.LOG_FILE_NOT_FOUND, resp.getCode());
    }

    /**
     * An EXISTING but empty log file stays SUCCESS with empty bytes — the valid terminal state
     * for a task that produced no output.
     */
    @Test
    void getTaskInstanceWholeLogFileBytes_emptyFileReturnsSuccess() throws IOException {
        Path file = Files.createTempFile("ds-whole-empty", ".log");
        try {
            Files.write(file, new byte[0]);

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceWholeLogFileBytes(req);
            assertEquals(LogResponseStatus.SUCCESS, resp.getCode());
            assertEquals(0, resp.getLogBytes().length);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void getTaskInstanceLogFileChunk_lengthClampedToMaxChunkSize() throws IOException {
        Path file = Files.createTempFile("ds-chunk-clamp", ".log");
        try {
            // Write more than MAX_CHUNK_SIZE (8 MB) — 9 MB
            byte[] mb = new byte[1024 * 1024];
            Files.write(file, mb);
            for (int i = 1; i < 9; i++) {
                Files.write(file, mb, java.nio.file.StandardOpenOption.APPEND);
            }

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(0);
            req.setLength(100 * 1024 * 1024); // request 100MB

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
            assertEquals(LogResponseStatus.SUCCESS, resp.getCode());
            assertEquals(8 * 1024 * 1024, resp.getLogBytes().length, "Should be clamped to 8MB");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    // ---------------- page-query clamp ----------------
    // The API sends user-controlled skipLineNum/limit straight through this RPC. Without the
    // worker-side clamp, limit=Integer.MAX_VALUE materializes the whole log as a List<String>
    // (multi-GB heap) — the exact OOM class this PR exists to prevent, on the VIEW path. The
    // clamp (dev behavior) bounds the read to maxLogQueryLimit lines per request.

    @Test
    void pageQueryTaskInstanceLog_limitClampedToMax() throws IOException {
        Path file = Files.createTempFile("ds-page-clamp", ".log");
        try {
            // 20 000 lines, one marker line per line
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 20_000; i++) {
                sb.append("line-").append(i).append('\n');
            }
            Files.write(file, sb.toString().getBytes(StandardCharsets.UTF_8));

            TaskInstanceLogPageQueryRequest req = new TaskInstanceLogPageQueryRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setSkipLineNum(0);
            req.setLimit(Integer.MAX_VALUE);

            TaskInstanceLogPageQueryResponse resp = logService.pageQueryTaskInstanceLog(req);
            assertEquals(LogResponseStatus.SUCCESS, resp.getCode());
            // Default maxLogQueryLimit is 10 000: the worker must NOT materialize more lines
            // than the clamp, even though the client asked for Integer.MAX_VALUE.
            String[] lines = resp.getLogContent().split("\\r?\\n");
            assertTrue(lines.length <= 10_000,
                    "unclamped limit=Integer.MAX_VALUE materialized the whole file: " + lines.length);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void pageQueryTaskInstanceLog_negativeSkipAndLimitAreSanitized() throws IOException {
        Path file = Files.createTempFile("ds-page-negative", ".log");
        try {
            Files.write(file, "l0\nl1\nl2\nl3\nl4\n".getBytes(StandardCharsets.UTF_8));

            TaskInstanceLogPageQueryRequest req = new TaskInstanceLogPageQueryRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setSkipLineNum(-5);
            req.setLimit(-3);

            TaskInstanceLogPageQueryResponse resp = logService.pageQueryTaskInstanceLog(req);
            assertEquals(LogResponseStatus.SUCCESS, resp.getCode(),
                    "negative params must be clamped to a sane read, not a stream IAE");
            assertTrue(resp.getLogContent().contains("l0"));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    // ---------------- request-pinned identity (file replacement across chunks) ----------------
    // Reviewer finding: per-chunk length-only validation accepts A-prefix + B-suffix when a
    // rotation swaps in a DIFFERENT file of coincidentally equal-or-greater length mid-download.
    // The chunk REQUEST therefore carries the identity pinned from the first chunk (length +
    // mtime + fileKey-or-null), and the worker stats before every read and rejects with
    // LOG_FILE_REPLACED when the file no longer matches the pin.

    /**
     * Same-size in-place replacement: the pinned length matches (so the old length-only guard
     * would pass) and the mtime pin is back-dated, but the fileKey differs — the worker must
     * reject the chunk. Pins the REAL identity (from a first-chunk response) the way the API
     * does, then replaces the file in place.
     */
    @Test
    void getTaskInstanceLogFileChunk_sameSizeReplacedFileRejected() throws IOException {
        Path file = Files.createTempFile("ds-chunk-replaced", ".log");
        try {
            Files.write(file, "AAAAAAAAAAAAAAAAAAAA".getBytes(StandardCharsets.UTF_8));

            // First chunk: pin identity from the worker's own observation.
            TaskInstanceLogFileDownloadRequest first = new TaskInstanceLogFileDownloadRequest();
            first.setTaskInstanceLogAbsolutePath(file.toString());
            first.setOffset(0);
            first.setLength(10);
            TaskInstanceLogFileDownloadResponse firstResp = logService.getTaskInstanceLogFileChunk(first);
            assertEquals(LogResponseStatus.SUCCESS, firstResp.getCode());

            // Replace the file the way a rotation does: a NEW file at the same path (new inode),
            // same length (20), different content. (An in-place truncate+write keeps the inode,
            // so delete + recreate is the honest simulation.)
            Files.delete(file);
            Files.write(file, "BBBBBBBBBBBBBBBBBBBB".getBytes(StandardCharsets.UTF_8));

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(10);
            req.setLength(10);
            req.setExpectedFileLength(firstResp.getObservedLength());
            // Back-date the mtime pin so ONLY the fileKey can catch the replacement — proving
            // the inode check is doing the work, not a lucky mtime comparison.
            req.setExpectedLastModifiedMillis(
                    Files.getLastModifiedTime(file).toMillis() - 60_000);
            req.setExpectedFileKey(firstResp.getObservedFileKey());

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
            if (firstResp.getObservedFileKey() != null) {
                // POSIX: the inode check must fire.
                assertEquals(LogResponseStatus.LOG_FILE_REPLACED, resp.getCode(),
                        "a file that no longer matches the pinned identity must be rejected");
            } else {
                // Platform without fileKey: mtime moved FORWARD (rewrite), so this falls back to
                // SUCCESS — the documented limitation of mtime-only identity.
                assertEquals(LogResponseStatus.SUCCESS, resp.getCode());
            }
            assertTrue(resp.isEof());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    /**
     * The happy path must be untouched: identity present and matching → SUCCESS with data.
     */
    @Test
    void getTaskInstanceLogFileChunk_matchingIdentitySucceeds() throws IOException {
        Path file = Files.createTempFile("ds-chunk-match", ".log");
        try {
            byte[] content = new byte[20];
            Files.write(file, content);

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(0);
            req.setLength(10);
            req.setExpectedFileLength(20);
            req.setExpectedLastModifiedMillis(Files.getLastModifiedTime(file).toMillis());
            req.setExpectedFileKey(null); // platform without fileKey support

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
            assertEquals(LogResponseStatus.SUCCESS, resp.getCode());
            assertEquals(10, resp.getLogBytes().length);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    /**
     * A GROWING live log must still be accepted: append only changes content AFTER the pinned
     * region, and does not change mtime?? — appending DOES change mtime. Growth is tolerated via
     * the strict rule: length must be >= pinned length and the prefix region [0, pinnedLength)
     * must be the same file (mtime/fileKey). mtime alone cannot distinguish append from replace,
     * so the worker accepts mtime changes as long as the file did not SHRINK below the pin and
     * the fileKey (when available on both sides) is unchanged.
     */
    @Test
    void getTaskInstanceLogFileChunk_appendedFileStillAccepted() throws IOException, InterruptedException {
        Path file = Files.createTempFile("ds-chunk-append", ".log");
        try {
            Files.write(file, "AAAAAAAAAAAAAAAAAAAA".getBytes(StandardCharsets.UTF_8));
            long pinnedMtime = Files.getLastModifiedTime(file).toMillis();

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(0);
            req.setLength(10);
            req.setExpectedFileLength(20);
            req.setExpectedLastModifiedMillis(pinnedMtime);
            req.setExpectedFileKey(null); // mtime-only pinning on this platform

            // The task keeps writing (append) — mtime changes, length grows.
            Thread.sleep(10);
            Files.write(file, "BBBB".getBytes(StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.APPEND);

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
            assertEquals(LogResponseStatus.SUCCESS, resp.getCode(),
                    "appending to the live log must not fail the download");
            assertEquals(10, resp.getLogBytes().length);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    /**
     * Shrink below the pin keeps the existing LOG_TRUNCATED semantics (rotation), while a
     * replacement that does NOT shrink is LOG_FILE_REPLACED — the two failure modes stay
     * distinguishable.
     */
    @Test
    void getTaskInstanceLogFileChunk_shrunkFileStillTruncated() throws IOException {
        Path file = Files.createTempFile("ds-chunk-shrink", ".log");
        try {
            Files.write(file, "AAAAAAAAAAAAAAAAAAAA".getBytes(StandardCharsets.UTF_8));

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(16);
            req.setLength(10);
            req.setExpectedFileLength(20);
            req.setExpectedLastModifiedMillis(Files.getLastModifiedTime(file).toMillis());
            req.setExpectedFileKey(null);

            Files.write(file, "hi".getBytes(StandardCharsets.UTF_8)); // shrank to 2 < offset 16

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
            assertEquals(LogResponseStatus.LOG_TRUNCATED, resp.getCode());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    /**
     * The response reports the observed identity (mtime/fileKey) alongside the observed length so
     * the API can pin ALL three from the first chunk.
     */
    @Test
    void getTaskInstanceLogFileChunk_reportsObservedIdentity() throws IOException {
        Path file = Files.createTempFile("ds-chunk-identity", ".log");
        try {
            Files.write(file, new byte[20]);
            long mtime = Files.getLastModifiedTime(file).toMillis();

            TaskInstanceLogFileDownloadRequest req = new TaskInstanceLogFileDownloadRequest();
            req.setTaskInstanceLogAbsolutePath(file.toString());
            req.setOffset(0);
            req.setLength(10);

            TaskInstanceLogFileDownloadResponse resp = logService.getTaskInstanceLogFileChunk(req);
            assertEquals(LogResponseStatus.SUCCESS, resp.getCode());
            assertEquals(20, resp.getObservedLength());
            assertEquals(mtime, resp.getObservedLastModifiedMillis(),
                    "mtime observed on this chunk, used by the API to pin identity");
            // fileKey may be null on platforms without inode support — require it wherever the
            // platform exposes inodes (Linux CI/CI containers): a regression that nulls the key
            // on POSIX would silently downgrade the replacement guard to mtime-only.
            assumeTrue(File.separatorChar == '/', "fileKey presence asserted on POSIX only");
            assertNotNull(resp.getObservedFileKey(),
                    "POSIX filesystems expose inodes — the strongest identity signal must be reported");
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
