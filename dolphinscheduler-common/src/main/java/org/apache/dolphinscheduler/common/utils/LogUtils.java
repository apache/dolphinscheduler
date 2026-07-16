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

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.log.archive.ArchivedLogLocation;
import org.apache.dolphinscheduler.common.log.archive.TaskLogArchivePathHelper;
import org.apache.dolphinscheduler.common.log.remote.RemoteLogUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

@Slf4j
public class LogUtils {

    public static byte[] getFileContentBytesFromLocal(String filePath) {
        if (!Files.exists(Paths.get(filePath))) {
            byte[] archivedBytes = readArchivedLogBytes(filePath);
            if (archivedBytes != null) {
                return archivedBytes;
            }
        }
        try (
                InputStream in = new FileInputStream(filePath);
                ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("get file bytes error", e);
        }
        return new byte[0];
    }

    public static byte[] getFileContentBytesFromRemote(String filePath) {
        RemoteLogUtils.getRemoteLog(filePath);
        return getFileContentBytesFromLocal(filePath);
    }

    public static List<String> readPartFileContentFromLocal(String filePath,
                                                            int skipLine,
                                                            int limit) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
                return stream.skip(skipLine).limit(limit).collect(Collectors.toList());
            } catch (IOException e) {
                log.error("read file error", e);
                throw new RuntimeException(String.format("Read file: %s error", filePath), e);
            }
        }
        List<String> archivedLines = readArchivedLogLines(filePath, skipLine, limit);
        if (archivedLines != null) {
            return archivedLines;
        }
        throw new RuntimeException("The file path: " + filePath + " not exists");
    }

    public static List<String> readPartFileContentFromRemote(String filePath,
                                                             int skipLine,
                                                             int limit) {
        RemoteLogUtils.getRemoteLog(filePath);
        return readPartFileContentFromLocal(filePath, skipLine, limit);
    }

    public static String rollViewLogLines(List<String> lines) {
        StringBuilder builder = new StringBuilder();
        final int MaxResponseLogSize = 65535;
        int totalLogByteSize = 0;
        for (String line : lines) {
            // If a single line of log is exceed max response size, cut off the line
            final int lineByteSize = line.getBytes(StandardCharsets.UTF_8).length;
            if (lineByteSize >= MaxResponseLogSize) {
                builder.append(line, 0, MaxResponseLogSize)
                        .append(" [this line's size ").append(lineByteSize).append(" bytes is exceed ")
                        .append(MaxResponseLogSize).append(" bytes, so only ")
                        .append(MaxResponseLogSize).append(" characters are reserved for performance reasons.]")
                        .append("\r\n");
            } else {
                builder.append(line).append("\r\n");
            }
            totalLogByteSize += lineByteSize;
            if (totalLogByteSize >= MaxResponseLogSize) {
                break;
            }
        }

        return builder.toString();
    }

    public static String getLocalLogBaseDir() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        return loggerContext.getProperty("log.base.ctx");
    }

    /**
     * Read the full bytes of a task log that has been archived into a day-partition zip. Returns {@code null}
     * when archiving is not in play for this path (unknown log base, path outside the base, or no matching
     * archive/entry) so the caller can fall through to its normal not-found handling.
     */
    private static byte[] readArchivedLogBytes(String filePath) {
        ArchivedLogLocation location = resolveArchivedLocation(filePath);
        if (location == null) {
            return null;
        }
        try {
            return CompressionUtils.readEntryBytes(location.getArchiveZip(), location.getEntryName());
        } catch (IOException e) {
            log.error("Failed to read archived task log {} from {}", filePath, location.getArchiveZip(), e);
            return null;
        }
    }

    /**
     * Read a line range of a task log that has been archived into a day-partition zip. Returns {@code null} when
     * archiving does not apply (see {@link #readArchivedLogBytes}).
     */
    private static List<String> readArchivedLogLines(String filePath, int skipLine, int limit) {
        ArchivedLogLocation location = resolveArchivedLocation(filePath);
        if (location == null) {
            return null;
        }
        try {
            return CompressionUtils.readEntryLines(location.getArchiveZip(), location.getEntryName(), skipLine,
                    limit);
        } catch (IOException e) {
            log.error("Failed to read archived task log {} from {}", filePath, location.getArchiveZip(), e);
            return null;
        }
    }

    private static ArchivedLogLocation resolveArchivedLocation(String filePath) {
        String logBaseDir = getLocalLogBaseDir();
        if (StringUtils.isBlank(logBaseDir)) {
            return null;
        }
        return TaskLogArchivePathHelper.resolveArchivedLocation(Paths.get(logBaseDir), filePath)
                .filter(location -> Files.exists(location.getArchiveZip()))
                .orElse(null);
    }

}
