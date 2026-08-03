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

import org.apache.dolphinscheduler.common.log.remote.RemoteLogUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

@Slf4j
public class LogUtils {

    /**
     * Maximum response log size in bytes, used to truncate log content to prevent OOM.
     */
    public static final int MAX_RESPONSE_LOG_SIZE = 65535;

    public static byte[] getFileContentBytesFromLocal(String filePath) {
        return getFileContentBytesFromLocal(filePath, Integer.MAX_VALUE);
    }

    /**
     * Read file content as byte array with a maximum size limit.
     * If the file exceeds maxSize, only the first maxSize bytes are returned.
     */
    public static byte[] getFileContentBytesFromLocal(String filePath, int maxSize) {
        try (
                InputStream in = new FileInputStream(filePath);
                ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int len;
            int totalRead = 0;
            while ((len = in.read(buf)) != -1) {
                int toWrite = Math.min(len, maxSize - totalRead);
                if (toWrite <= 0) {
                    break;
                }
                bos.write(buf, 0, toWrite);
                totalRead += toWrite;
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

    /**
     * Read part of a log file with early termination when accumulated byte size reaches MAX_RESPONSE_LOG_SIZE.
     * This prevents loading the entire file into memory when only ~64KB is needed.
     */
    public static List<String> readPartFileContentFromLocal(String filePath,
                                                            int skipLine,
                                                            int limit) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("The file path: " + filePath + " not exists");
        }
        List<String> result = new ArrayList<>();
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            // Skip lines
            int skipped = 0;
            while (skipped < skipLine && reader.readLine() != null) {
                skipped++;
            }
            // Read lines with early termination based on accumulated byte size
            int totalBytes = 0;
            int read = 0;
            String line;
            while (read < limit && (line = reader.readLine()) != null) {
                int lineBytes = line.getBytes(StandardCharsets.UTF_8).length;
                if (totalBytes + lineBytes > MAX_RESPONSE_LOG_SIZE && read > 0) {
                    break;
                }
                result.add(line);
                totalBytes += lineBytes;
                read++;
            }
        } catch (IOException e) {
            log.error("read file error", e);
            throw new RuntimeException(String.format("Read file: %s error", filePath), e);
        }
        return result;
    }

    public static List<String> readPartFileContentFromRemote(String filePath,
                                                             int skipLine,
                                                             int limit) {
        RemoteLogUtils.getRemoteLog(filePath);
        return readPartFileContentFromLocal(filePath, skipLine, limit);
    }

    public static String rollViewLogLines(List<String> lines) {
        StringBuilder builder = new StringBuilder();
        int totalLogByteSize = 0;
        for (String line : lines) {
            // If a single line of log is exceed max response size, cut off the line
            final int lineByteSize = line.getBytes(StandardCharsets.UTF_8).length;
            if (lineByteSize >= MAX_RESPONSE_LOG_SIZE) {
                builder.append(line, 0, MAX_RESPONSE_LOG_SIZE)
                        .append(" [this line's size ").append(lineByteSize).append(" bytes is exceed ")
                        .append(MAX_RESPONSE_LOG_SIZE).append(" bytes, so only ")
                        .append(MAX_RESPONSE_LOG_SIZE).append(" characters are reserved for performance reasons.]")
                        .append("\r\n");
            } else {
                builder.append(line).append("\r\n");
            }
            totalLogByteSize += lineByteSize;
            if (totalLogByteSize >= MAX_RESPONSE_LOG_SIZE) {
                break;
            }
        }

        return builder.toString();
    }

    public static String getLocalLogBaseDir() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        return loggerContext.getProperty("log.base.ctx");
    }

}
