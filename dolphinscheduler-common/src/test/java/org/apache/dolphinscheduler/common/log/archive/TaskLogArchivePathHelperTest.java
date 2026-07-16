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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TaskLogArchivePathHelperTest {

    @TempDir
    Path tempDir;

    @Test
    void isDayPartitionName_acceptsOnlyValidDates() {
        assertTrue(TaskLogArchivePathHelper.isDayPartitionName("20240115"));
        assertFalse(TaskLogArchivePathHelper.isDayPartitionName("20241399"));
        assertFalse(TaskLogArchivePathHelper.isDayPartitionName("archive"));
        assertFalse(TaskLogArchivePathHelper.isDayPartitionName("2024011"));
        assertFalse(TaskLogArchivePathHelper.isDayPartitionName("202401150"));
    }

    @Test
    void resolveArchiveZip_buildsExpectedPath() {
        Path zip = TaskLogArchivePathHelper.resolveArchiveZip(tempDir, "20240115");
        assertEquals(tempDir.resolve("archive").resolve("20240115.zip"), zip);
    }

    @Test
    void resolveArchivedLocation_mapsLogPathToZipAndEntry() {
        String logPath = tempDir.resolve("20240115").resolve("7654321").resolve("1").resolve("42")
                .resolve("100.log").toString();

        Optional<ArchivedLogLocation> location =
                TaskLogArchivePathHelper.resolveArchivedLocation(tempDir, logPath);

        assertTrue(location.isPresent());
        assertEquals(tempDir.resolve("archive").resolve("20240115.zip"), location.get().getArchiveZip());
        assertEquals("7654321/1/42/100.log", location.get().getEntryName());
    }

    @Test
    void resolveArchivedLocation_rejectsPathOutsideBase() {
        Optional<ArchivedLogLocation> location =
                TaskLogArchivePathHelper.resolveArchivedLocation(tempDir, "/elsewhere/20240115/1/1/1/1.log");
        assertFalse(location.isPresent());
    }

    @Test
    void resolveArchivedLocation_rejectsNonDayPartition() {
        String logPath = tempDir.resolve("archive").resolve("something.log").toString();
        Optional<ArchivedLogLocation> location =
                TaskLogArchivePathHelper.resolveArchivedLocation(tempDir, logPath);
        assertFalse(location.isPresent());
    }

    @Test
    void listArchivableDayDirs_selectsOnlyOldUnarchivedPartitions() throws IOException {
        Files.createDirectories(tempDir.resolve("20240110"));
        Files.createDirectories(tempDir.resolve("20240120"));
        Files.createDirectories(tempDir.resolve("archive"));
        Files.createDirectories(tempDir.resolve("notaday"));
        // 20240105 is old but already has a zip => excluded
        Files.createDirectories(tempDir.resolve("20240105"));
        Files.createFile(tempDir.resolve("archive").resolve("20240105.zip"));

        List<Path> dirs = TaskLogArchivePathHelper.listArchivableDayDirs(tempDir, LocalDate.of(2024, 1, 15));

        assertEquals(1, dirs.size());
        assertEquals("20240110", dirs.get(0).getFileName().toString());
    }

    @Test
    void listArchivableDayDirs_emptyWhenBaseMissing() {
        List<Path> dirs = TaskLogArchivePathHelper.listArchivableDayDirs(
                tempDir.resolve("does-not-exist"), LocalDate.of(2024, 1, 15));
        assertTrue(dirs.isEmpty());
    }

    @Test
    void listExpiredArchiveZips_selectsOnlyOldZips() throws IOException {
        Path archiveDir = tempDir.resolve("archive");
        Files.createDirectories(archiveDir);
        Files.createFile(archiveDir.resolve("20240101.zip"));
        Files.createFile(archiveDir.resolve("20240120.zip"));
        Files.createFile(archiveDir.resolve("garbage.zip"));
        Files.createFile(archiveDir.resolve("20240102.txt"));

        List<Path> zips = TaskLogArchivePathHelper.listExpiredArchiveZips(tempDir, LocalDate.of(2024, 1, 10));

        assertEquals(1, zips.size());
        assertEquals("20240101.zip", zips.get(0).getFileName().toString());
    }
}
