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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Pure path arithmetic for task-log archiving. Task instance logs are laid out under the log base directory as
 * <pre>{base}/{yyyyMMdd}/{workflowDefinitionCode}/{version}/{workflowInstanceId}/{taskInstanceId}.log</pre>
 * so the first path segment below the base is a per-day partition. Each day partition is archived into a single
 * zip at <pre>{base}/archive/{yyyyMMdd}.zip</pre> whose entries are the paths relative to that day directory.
 * This class only computes paths and names; it neither reads nor writes files, which keeps the archiving and
 * read-fallback logic (which do touch disk) small and independently testable.
 */
@Slf4j
@UtilityClass
public class TaskLogArchivePathHelper {

    /**
     * Directory (relative to the log base) that holds the archived zips. It must not collide with a real day
     * partition; day partitions are 8-digit dates, so a word directory name is safe.
     */
    public static final String ARCHIVE_DIR_NAME = "archive";

    private static final String ZIP_SUFFIX = ".zip";

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Whether a directory name is a task-log day partition (an 8-digit {@code yyyyMMdd} date). Used to skip the
     * {@link #ARCHIVE_DIR_NAME} directory and any other non-partition entries when scanning the log base.
     */
    public static boolean isDayPartitionName(String name) {
        return parseDay(name).isPresent();
    }

    /**
     * The zip file a given day partition is archived into, e.g. {@code {base}/archive/20240115.zip}.
     */
    public static Path resolveArchiveZip(@NonNull Path logBaseDir, @NonNull String dayName) {
        return logBaseDir.resolve(ARCHIVE_DIR_NAME).resolve(dayName + ZIP_SUFFIX);
    }

    /**
     * Resolve the archive location of a task-instance log path. Given an absolute or relative log path under the
     * log base, returns the zip that would hold it plus the '/'-separated entry name inside that zip, or
     * {@link Optional#empty()} when the path does not sit under a recognizable day partition (in which case there
     * is nothing to fall back to).
     *
     * @param logBaseDir the task-log base directory
     * @param logPath    the original task-instance log path (as stored on {@code t_ds_task_instance.log_path})
     */
    public static Optional<ArchivedLogLocation> resolveArchivedLocation(@NonNull Path logBaseDir,
                                                                        @NonNull String logPath) {
        Path base = logBaseDir.toAbsolutePath().normalize();
        Path log = Paths.get(logPath).toAbsolutePath().normalize();
        if (!log.startsWith(base)) {
            return Optional.empty();
        }
        Path relative = base.relativize(log);
        // relative must be {yyyyMMdd}/{code}/{version}/{wfInstId}/{taskId}.log => at least 2 segments.
        if (relative.getNameCount() < 2) {
            return Optional.empty();
        }
        String dayName = relative.getName(0).toString();
        if (!isDayPartitionName(dayName)) {
            return Optional.empty();
        }
        Path entryPath = relative.subpath(1, relative.getNameCount());
        String entryName = toEntryName(entryPath);
        Path archiveZip = resolveArchiveZip(base, dayName);
        return Optional.of(new ArchivedLogLocation(archiveZip, entryName));
    }

    /**
     * List day partitions under the log base whose date is on or before {@code archiveBefore} and which have not
     * yet been archived (no matching zip exists). The returned directories are the ones the archiver should pack.
     *
     * @param logBaseDir    the task-log base directory
     * @param archiveBefore inclusive upper bound: partitions dated on/before this day are eligible
     */
    public static List<Path> listArchivableDayDirs(@NonNull Path logBaseDir, @NonNull LocalDate archiveBefore) {
        Path base = logBaseDir.toAbsolutePath().normalize();
        if (!Files.isDirectory(base)) {
            return new ArrayList<>();
        }
        List<Path> result = new ArrayList<>();
        try (Stream<Path> children = Files.list(base)) {
            children.filter(Files::isDirectory).forEach(dir -> {
                String name = dir.getFileName().toString();
                Optional<LocalDate> day = parseDay(name);
                if (!day.isPresent()) {
                    return;
                }
                if (day.get().isAfter(archiveBefore)) {
                    return;
                }
                if (Files.exists(resolveArchiveZip(base, name))) {
                    // Already archived; a stale day dir left next to an existing zip is cleaned by the archiver.
                    return;
                }
                result.add(dir);
            });
        } catch (IOException e) {
            log.error("Failed to list task-log day partitions under {}", base, e);
        }
        return result;
    }

    /**
     * List archive zips whose day is on or before {@code expireBefore}. These are the archives eligible for
     * deletion when expiry is enabled.
     *
     * @param logBaseDir   the task-log base directory
     * @param expireBefore inclusive upper bound: archives dated on/before this day are eligible for removal
     */
    public static List<Path> listExpiredArchiveZips(@NonNull Path logBaseDir, @NonNull LocalDate expireBefore) {
        Path archiveDir = logBaseDir.toAbsolutePath().normalize().resolve(ARCHIVE_DIR_NAME);
        if (!Files.isDirectory(archiveDir)) {
            return new ArrayList<>();
        }
        List<Path> result = new ArrayList<>();
        try (Stream<Path> children = Files.list(archiveDir)) {
            children.filter(Files::isRegularFile).forEach(zip -> {
                String name = zip.getFileName().toString();
                if (!name.endsWith(ZIP_SUFFIX)) {
                    return;
                }
                String dayName = name.substring(0, name.length() - ZIP_SUFFIX.length());
                Optional<LocalDate> day = parseDay(dayName);
                if (!day.isPresent()) {
                    return;
                }
                if (!day.get().isAfter(expireBefore)) {
                    result.add(zip);
                }
            });
        } catch (IOException e) {
            log.error("Failed to list archive zips under {}", archiveDir, e);
        }
        return result;
    }

    private static Optional<LocalDate> parseDay(String name) {
        if (name == null || name.length() != 8) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(name, DAY_FORMATTER));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private static String toEntryName(Path relativePath) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < relativePath.getNameCount(); i++) {
            if (i > 0) {
                sb.append('/');
            }
            sb.append(relativePath.getName(i).toString());
        }
        return sb.toString();
    }
}
