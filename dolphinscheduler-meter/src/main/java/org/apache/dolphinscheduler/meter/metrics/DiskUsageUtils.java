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

package org.apache.dolphinscheduler.meter.metrics;

import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class DiskUsageUtils {

    @Data
    @Builder
    public static class DiskUsage {

        private String diskPath;
        private long usedBytes;
        private long totalBytes;
        /**
         * Used percentage in range [0,1].
         */
        private double usedPercentage;
    }

    public static Optional<DiskUsage> getDiskUsage(final String diskPath) {
        if (diskPath == null || diskPath.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            final Path path = Paths.get(diskPath);
            if (!Files.exists(path)) {
                return Optional.empty();
            }

            final FileStore fileStore = Files.getFileStore(path);
            final long total = fileStore.getTotalSpace();
            final long usable = fileStore.getUsableSpace();
            if (total <= 0) {
                return Optional.of(DiskUsage.builder()
                        .diskPath(diskPath)
                        .usedBytes(0)
                        .totalBytes(0)
                        .usedPercentage(0d)
                        .build());
            }
            final long used = Math.max(0L, total - usable);
            final double usedPercentage = (double) used / (double) total;
            return Optional.of(DiskUsage.builder()
                    .diskPath(diskPath)
                    .usedBytes(used)
                    .totalBytes(total)
                    .usedPercentage(usedPercentage)
                    .build());
        } catch (Exception ex) {
            log.warn("Failed to get disk usage for path: {}", diskPath, ex);
            return Optional.empty();
        }
    }
}
