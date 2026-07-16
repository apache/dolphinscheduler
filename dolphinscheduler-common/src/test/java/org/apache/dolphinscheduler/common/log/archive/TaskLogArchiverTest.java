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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.common.utils.CompressionUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * Exercises {@link TaskLogArchiver#runOnce()} against a real temp log base. The archiver resolves its base
 * directory from the logback context property {@code log.base.ctx}; the test points it at the temp dir and
 * restores the prior value afterward so parallel test classes in the same fork are unaffected.
 */
class TaskLogArchiverTest {

    private static final String LOG_BASE_PROPERTY = "log.base.ctx";

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @TempDir
    Path tempDir;

    private LoggerContext loggerContext;

    private String originalLogBase;

    @BeforeEach
    void setUp() {
        loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        originalLogBase = loggerContext.getProperty(LOG_BASE_PROPERTY);
        loggerContext.putProperty(LOG_BASE_PROPERTY, tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        if (originalLogBase == null) {
            loggerContext.putProperty(LOG_BASE_PROPERTY, "");
        } else {
            loggerContext.putProperty(LOG_BASE_PROPERTY, originalLogBase);
        }
    }

    @Test
    void runOnce_archivesOldPartitionAndKeepsRecentOne() throws IOException {
        Path oldDay = createDayPartition(LocalDate.now().minusDays(40));
        Path recentDay = createDayPartition(LocalDate.now().minusDays(1));

        TaskLogArchiver archiver = new TaskLogArchiver(config(true, false));
        archiver.runOnce();

        // Old partition packed into a zip and the source directory removed.
        assertFalse(Files.exists(oldDay), "old partition should be removed after archiving");
        Path oldZip = tempDir.resolve("archive").resolve(oldDay.getFileName() + ".zip");
        assertTrue(Files.exists(oldZip), "old partition should be archived into a zip");
        byte[] entry = CompressionUtils.readEntryBytes(oldZip, "7654321/1/42/100.log");
        assertTrue(entry != null && entry.length > 0, "archived entry should be readable");

        // Recent partition left untouched.
        assertTrue(Files.exists(recentDay), "recent partition should not be archived");
    }

    @Test
    void runOnce_deletesExpiredArchivesWhenExpiryEnabled() throws IOException {
        Path archiveDir = tempDir.resolve("archive");
        Files.createDirectories(archiveDir);
        String expiredDay = LocalDate.now().minusDays(100).format(DAY_FORMATTER);
        String freshDay = LocalDate.now().minusDays(40).format(DAY_FORMATTER);
        Path expiredZip = archiveDir.resolve(expiredDay + ".zip");
        Path freshZip = archiveDir.resolve(freshDay + ".zip");
        Files.createFile(expiredZip);
        Files.createFile(freshZip);

        TaskLogArchiver archiver = new TaskLogArchiver(config(true, true));
        archiver.runOnce();

        assertFalse(Files.exists(expiredZip), "archive older than expire threshold should be deleted");
        assertTrue(Files.exists(freshZip), "archive within expire threshold should be kept");
    }

    @Test
    void runOnce_leavesExpiredArchivesWhenExpiryDisabled() throws IOException {
        Path archiveDir = tempDir.resolve("archive");
        Files.createDirectories(archiveDir);
        String expiredDay = LocalDate.now().minusDays(100).format(DAY_FORMATTER);
        Path expiredZip = archiveDir.resolve(expiredDay + ".zip");
        Files.createFile(expiredZip);

        TaskLogArchiver archiver = new TaskLogArchiver(config(true, false));
        archiver.runOnce();

        assertTrue(Files.exists(expiredZip), "expiry disabled should never delete an archive");
    }

    private Path createDayPartition(LocalDate day) throws IOException {
        Path dayDir = tempDir.resolve(day.format(DAY_FORMATTER));
        Path taskDir = dayDir.resolve("7654321").resolve("1").resolve("42");
        Files.createDirectories(taskDir);
        Files.write(taskDir.resolve("100.log"), "some log content\n".getBytes(StandardCharsets.UTF_8));
        return dayDir;
    }

    private TaskLogArchiveConfig config(boolean enabled, boolean expireEnabled) {
        TaskLogArchiveConfig config = new TaskLogArchiveConfig();
        config.setEnabled(enabled);
        config.setCheckInterval(Duration.ofDays(1));
        config.setArchiveThreshold(Duration.ofDays(30));
        config.setExpireEnabled(expireEnabled);
        config.setExpireThreshold(Duration.ofDays(90));
        return config;
    }
}
