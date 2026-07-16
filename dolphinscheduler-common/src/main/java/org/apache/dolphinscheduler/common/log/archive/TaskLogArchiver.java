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

package org.apache.dolphinscheduler.common.log.archive;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.CompressionUtils;
import org.apache.dolphinscheduler.common.utils.LogUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Periodically archives old task-instance log day partitions into per-day zips and, optionally, deletes zips
 * that have aged past the expiry threshold. Runs as a plain daemon on both the master and the worker (each holds
 * task logs on its own local disk); it is <b>not</b> leader-gated, because every node must maintain the logs it
 * physically stored.
 * <p>
 * The bean is instantiated in every service that imports {@code CommonConfiguration}, but it does nothing until
 * {@link #start()} is invoked by a host server's lifecycle. API and alert servers therefore leave it idle.
 * {@link #start()} is idempotent so the standalone server (which boots master and worker in one JVM) can call it
 * from both without spawning two threads.
 */
@Slf4j
@Component
public class TaskLogArchiver implements AutoCloseable {

    private final TaskLogArchiveConfig archiveConfig;

    private volatile ScheduledExecutorService scheduler;

    @Autowired
    public TaskLogArchiver(TaskLogArchiveConfig archiveConfig) {
        this.archiveConfig = archiveConfig;
    }

    /**
     * Start the archiving daemon. No-op when archiving is disabled or the daemon is already running.
     */
    public synchronized void start() {
        if (!archiveConfig.isEnabled()) {
            log.info("Task log archiving is disabled, skip starting the archiver");
            return;
        }
        if (scheduler != null) {
            log.warn("Task log archiver is already started");
            return;
        }
        String logBaseDir = LogUtils.getLocalLogBaseDir();
        if (StringUtils.isBlank(logBaseDir)) {
            log.error("Cannot resolve the local log base directory, task log archiver will not start");
            return;
        }
        long intervalSeconds = Math.max(1, archiveConfig.getCheckInterval().getSeconds());
        // First pass runs shortly after boot (not one full interval later), otherwise a cluster that restarts
        // more often than the check interval would never archive anything.
        long initialDelaySeconds = Math.min(60, intervalSeconds);
        scheduler = ThreadUtils.newSingleDaemonScheduledExecutorService("TaskLogArchiveThread");
        scheduler.scheduleWithFixedDelay(this::runOnce, initialDelaySeconds, intervalSeconds, TimeUnit.SECONDS);
        log.info("Task log archiver started: logBaseDir={}, checkInterval={}, archiveThreshold={}, "
                + "expireEnabled={}, expireThreshold={}",
                logBaseDir, archiveConfig.getCheckInterval(), archiveConfig.getArchiveThreshold(),
                archiveConfig.isExpireEnabled(), archiveConfig.getExpireThreshold());
    }

    /**
     * One archiving pass. Wrapped so a failure never propagates into the scheduler and kills future runs.
     */
    void runOnce() {
        try {
            String logBaseDir = LogUtils.getLocalLogBaseDir();
            if (StringUtils.isBlank(logBaseDir)) {
                log.warn("Local log base directory is unavailable, skip this archiving pass");
                return;
            }
            Path logBasePath = Paths.get(logBaseDir).toAbsolutePath().normalize();
            archiveOldPartitions(logBasePath);
            if (archiveConfig.isExpireEnabled()) {
                deleteExpiredArchives(logBasePath);
            }
        } catch (Exception e) {
            log.error("Task log archiving pass failed", e);
        }
    }

    private void archiveOldPartitions(Path logBasePath) {
        LocalDate archiveBefore = LocalDate.now().minusDays(archiveConfig.getArchiveThreshold().toDays());
        List<Path> dayDirs = TaskLogArchivePathHelper.listArchivableDayDirs(logBasePath, archiveBefore);
        for (Path dayDir : dayDirs) {
            String dayName = dayDir.getFileName().toString();
            Path targetZip = TaskLogArchivePathHelper.resolveArchiveZip(logBasePath, dayName);
            try {
                CompressionUtils.zipDirectory(dayDir, targetZip);
                FileUtils.deleteDirectory(dayDir.toFile());
                log.info("Archived task log partition {} into {}", dayName, targetZip);
            } catch (IOException e) {
                // Leave the day dir in place so the next pass retries; drop a half-written zip to avoid a
                // corrupt archive masking the source.
                deleteQuietly(targetZip);
                log.error("Failed to archive task log partition {}, will retry next pass", dayName, e);
            }
        }
    }

    private void deleteExpiredArchives(Path logBasePath) {
        LocalDate expireBefore = LocalDate.now().minusDays(archiveConfig.getExpireThreshold().toDays());
        List<Path> expiredZips = TaskLogArchivePathHelper.listExpiredArchiveZips(logBasePath, expireBefore);
        for (Path zip : expiredZips) {
            if (deleteQuietly(zip)) {
                log.info("Deleted expired task log archive {}", zip);
            }
        }
    }

    private boolean deleteQuietly(Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Failed to delete task log archive file {}", path, e);
            return false;
        }
    }

    @Override
    public synchronized void close() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
            log.info("Task log archiver stopped");
        }
    }
}
