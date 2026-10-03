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

import static org.apache.dolphinscheduler.common.utils.LogUtils.getLocalLogBaseDir;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RemoteLogUtils {

    /**
     * Marker of the per-download temporary files created next to a log archive
     * ({@code <archive>.download-<uuid>}): the staging file a remote handler downloads into before
     * publishing it, and the private streaming snapshot of {@code RemoteLogClient}. The API
     * server's startup sweep recognizes leftovers of both kinds (a JVM killed mid-transfer cannot
     * run its cleanup) by this marker.
     */
    public static final String DOWNLOAD_TEMP_FILE_MARKER = ".download-";

    private static RemoteLogService remoteLogService;

    @Autowired
    private RemoteLogService autowiredRemoteLogService;

    @PostConstruct
    private void init() {
        remoteLogService = autowiredRemoteLogService;
    }

    public static void sendRemoteLog(String logPath) {
        if (isRemoteLoggingEnable()) {
            // send task logs to remote storage asynchronously
            remoteLogService.asyncSendRemoteLog(logPath);
        }
    }

    /**
     * Downloads the remote archived log to its local path.
     *
     * <p>A failed download propagates as {@link IOException} — a failed (possibly truncated)
     * transfer must never be silently treated as a usable local log. The archive is only replaced
     * once a download completed in full, see
     * {@link #downloadToLocalFileAtomically(String, RemoteLogDownloader)}.
     */
    public static void getRemoteLog(String logPath) throws IOException {
        if (isRemoteLoggingEnable()) {
            log.info("Start to get log {} from remote target {}", logPath,
                    PropertyUtils.getString(Constants.REMOTE_LOGGING_TARGET));

            mkdirOfLog(logPath);
            RemoteLogHandler remoteLogHandler = RemoteLogHandlerFactory.getRemoteLogHandler();
            if (remoteLogHandler == null) {
                return;
            }
            try {
                remoteLogHandler.getRemoteLog(logPath);
            } catch (IOException e) {
                log.error("Failed to get log {} from remote target {}", logPath,
                        PropertyUtils.getString(Constants.REMOTE_LOGGING_TARGET), e);
                throw e;
            }
            log.info("End get log {} from remote target {}", logPath,
                    PropertyUtils.getString(Constants.REMOTE_LOGGING_TARGET));
        }
    }

    /**
     * Provider-specific part of a remote log download: write the complete remote object to
     * {@code stagingFile}. The staging file is private to the download — publishing it as the
     * local archive is done by {@link #downloadToLocalFileAtomically}.
     */
    @FunctionalInterface
    interface RemoteLogDownloader {

        void downloadTo(Path stagingFile) throws Exception;
    }

    /**
     * Downloads a remote log into a private staging file next to {@code logPath} and publishes it
     * with an atomic move, so the archive is only ever replaced by a COMPLETE download:
     * <ul>
     * <li>the staging file never appears at {@code logPath} — a reader observes the previously
     * published complete archive or the new one, never a partial transfer;</li>
     * <li>a failed download removes the staging file, leaves an existing cached archive
     * untouched, and propagates as {@link IOException}.</li>
     * </ul>
     * A staging file orphaned by a JVM dying mid-transfer is recognized by
     * {@link #DOWNLOAD_TEMP_FILE_MARKER} and swept at API server startup.
     */
    static void downloadToLocalFileAtomically(final String logPath,
                                              final RemoteLogDownloader downloader) throws IOException {
        final Path archive = Paths.get(logPath);
        final Path staging =
                archive.resolveSibling(archive.getFileName() + DOWNLOAD_TEMP_FILE_MARKER + UUID.randomUUID());
        try {
            downloader.downloadTo(staging);
            publishAtomically(staging, archive);
        } catch (Exception e) {
            // The archive is deliberately left as it was — a previously published complete
            // download, or nothing at all. A failed transfer must never be published.
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException("Failed to download remote log " + logPath
                    + "; the local archive was not modified", e);
        } finally {
            // After a successful publish the staging file no longer exists (it was moved), so this
            // only removes a partial download. Never throws — it runs in a finally block and must
            // not mask the download's own failure.
            deleteStagingQuietly(staging);
        }
    }

    private static void publishAtomically(final Path staging, final Path archive) throws IOException {
        try {
            Files.move(staging, archive, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException | FileAlreadyExistsException e) {
            // Some filesystems cannot move atomically, and on Windows an atomic move refuses to
            // replace an existing file (REPLACE_EXISTING is ignored). The staging file is a
            // sibling of the archive, so this fallback is still a same-filesystem replace, never
            // a cross-device copy that a reader could observe partially.
            Files.move(staging, archive, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteStagingQuietly(final Path staging) {
        try {
            Files.deleteIfExists(staging);
        } catch (Throwable e) {
            // Throwable, not IOException: this runs in a finally block and must never throw
            // through it (masking the download's own failure). A leftover file is swept at startup.
            log.warn("Failed to delete the log download staging file {}", staging, e);
        }
    }

    private static void mkdirOfLog(String logPath) {
        Path directory = Paths.get(logPath).getParent();
        directory.toFile().mkdirs();
    }

    public static boolean isRemoteLoggingEnable() {
        return PropertyUtils.getBoolean(Constants.REMOTE_LOGGING_ENABLE, Boolean.FALSE);
    }

    public static String getObjectNameFromLogPath(String logPath) {
        Path localLogBaseDirPath = Paths.get(getLocalLogBaseDir()).toAbsolutePath();

        Path path = Paths.get(logPath);
        int nameCount = path.getNameCount();

        String remoteLogBaseDir = PropertyUtils.getString(Constants.REMOTE_LOGGING_BASE_DIR);
        return Paths.get(remoteLogBaseDir, path.subpath(localLogBaseDirPath.getNameCount(), nameCount).toString())
                .toString();
    }
}
