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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

@Slf4j
public class LogUtils {

    public static byte[] getFileContentBytesFromLocal(String filePath) {
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

    public static byte[] getFileContentBytes(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return getFileContentBytesFromLocal(filePath);
        }
        if (RemoteLogUtils.isRemoteLoggingEnable()) {
            return getFileContentBytesFromRemote(filePath);
        }
        return getFileContentBytesFromLocal(filePath);
    }

    public static List<String> readPartFileContentFromLocal(String filePath,
                                                            int skipLine,
                                                            int limit) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            log.info("readPartFileContentFromLocal Reading log file");
            // Check if there are rolling log files
            List<File> logFiles = getRollingLogFiles(filePath);

            if (logFiles.size() > 1) {
                // Handle rolling log files
                return readFromRollingLogFiles(logFiles, skipLine, limit);
            } else {
                // Handle single log file
                try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
                    return stream.skip(skipLine).limit(limit).collect(Collectors.toList());
                } catch (IOException e) {
                    log.error("read file error", e);
                    throw new RuntimeException(String.format("Read file: %s error", filePath), e);
                }
            }
        } else {
            throw new RuntimeException("The file path: " + filePath + " not exists");
        }
    }

    public static List<String> readPartFileContentFromRemote(String filePath,
                                                             int skipLine,
                                                             int limit) {
        RemoteLogUtils.getRemoteLog(filePath);
        return readPartFileContentFromLocal(filePath, skipLine, limit);
    }

    public static List<String> readPartFileContent(String filePath,
                                                   int skipLine,
                                                   int limit) {
        File file = new File(filePath);
        if (file.exists()) {
            return readPartFileContentFromLocal(filePath, skipLine, limit);
        }
        if (RemoteLogUtils.isRemoteLoggingEnable()) {
            return readPartFileContentFromRemote(filePath, skipLine, limit);
        }
        return readPartFileContentFromLocal(filePath, skipLine, limit);
    }

    public static String readWholeFileContentFromRemote(String filePath) {
        RemoteLogUtils.getRemoteLog(filePath);
        return LogUtils.readWholeFileContentFromLocal(filePath);
    }

    public static String readWholeFileContentFromLocal(String filePath) {
        String line;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
            while ((line = br.readLine()) != null) {
                sb.append(line + "\r\n");
            }
            return sb.toString();
        } catch (IOException e) {
            log.error("read file error", e);
        }
        return "";
    }

    public static String readWholeFileContent(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return readWholeFileContentFromLocal(filePath);
        }
        if (RemoteLogUtils.isRemoteLoggingEnable()) {
            return readWholeFileContentFromRemote(filePath);
        }
        return readWholeFileContentFromLocal(filePath);
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
     * Get all rolling log files for a given base file path.
     * Returns a sorted list containing the base file and its rolled versions (e.g., .1, .2, etc.)
     * ordered from newest to oldest (base file first, then .1, .2, etc.)
     */
    private static List<File> getRollingLogFiles(String basePath) {
        List<File> allFiles = new ArrayList<>();

        File baseFile = new File(basePath);
        File parentDir = baseFile.getParentFile();
        String fileName = baseFile.getName();

        // Add the base file if it exists
        if (baseFile.exists()) {
            allFiles.add(baseFile);
        }

        // Look for rolling files with pattern: basePath.N
        if (parentDir != null) {
            File[] files = parentDir.listFiles((dir, name) -> name.startsWith(fileName + ".") &&
                    Pattern.matches(Pattern.quote(fileName) + "\\.\\d+", name));

            if (files != null) {
                allFiles.addAll(Arrays.asList(files));
            }
        }

        // Sort all files in reverse order based on rolling number
        // Base file (without number) is treated as having number 0, so it comes last
        // descending order (larger numbers first)
        allFiles.sort((file1, file2) -> {
            int num1 = isRollingFile(file1) ? extractRollingNumber(file1) : 0;
            int num2 = isRollingFile(file2) ? extractRollingNumber(file2) : 0;
            return Integer.compare(num2, num1);
        });

        return allFiles;
    }

    /**
     * Extract the rolling number from a file name (e.g., from "xxx.log.3" extract 3)
     */
    private static int extractRollingNumber(File file) {
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            try {
                return Integer.parseInt(fileName.substring(lastDotIndex + 1));
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE; // Put invalid files at the end
            }
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Check if the file is a rolling file (has a number suffix like .1, .2, etc.)
     */
    private static boolean isRollingFile(File file) {
        String fileName = file.getName();
        // Check if the filename matches the pattern of a rolling file (basename.number)
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            String suffix = fileName.substring(lastDotIndex + 1);
            return suffix.matches("\\d+");
        }
        return false;
    }

    /**
     * Read lines from multiple rolling log files in order
     */
    private static List<String> readFromRollingLogFiles(List<File> logFiles, int skipLine, int limit) {
        List<String> allLines = new ArrayList<>();

        // Read all lines from all log files in order
        for (File file : logFiles) {
            log.info("Reading log file: {}", file.getAbsolutePath());
            try (Stream<String> stream = Files.lines(file.toPath())) {
                List<String> fileLines = stream.collect(Collectors.toList());
                allLines.addAll(fileLines);
            } catch (IOException e) {
                log.error("Error reading file: " + file.getAbsolutePath(), e);
                throw new RuntimeException(String.format("Read file: %s error", file.getAbsolutePath()), e);
            }
        }

        // Apply skip and limit
        int startIndex = Math.min(skipLine, allLines.size());
        int endIndex = Math.min(startIndex + limit, allLines.size());

        return allLines.subList(startIndex, endIndex);
    }

    /**
     * Get content of multiple log files (including rolling log files) as byte array
     * Reads files in reverse order (xxx.log.n, xxx.log.n-1, ..., xxx.log)
     *
     * @param filePath base file path
     * @return byte array of all log files content
     */
    public static byte[] getFileContentBytesWithRollingLogs(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            // Check if there are rolling log files
            List<File> logFiles = getRollingLogFiles(filePath);

            if (logFiles.size() > 1) {
                // Handle multiple log files (base file + rolling files)
                return getBytesFromMultipleLogFiles(logFiles);
            } else {
                // Handle single log file
                return getFileContentBytesFromLocal(filePath);
            }
        } else {
            throw new RuntimeException("The file path: " + filePath + " not exists");
        }
    }

    /**
     * Read bytes from multiple log files in order
     */
    private static byte[] getBytesFromMultipleLogFiles(List<File> logFiles) {
        List<byte[]> allBytes = new ArrayList<>();

        // Read all bytes from all log files in order
        for (File file : logFiles) {
            log.info("Reading log file for download: {}", file.getAbsolutePath());
            byte[] fileBytes = getFileContentBytesFromLocal(file.getAbsolutePath());
            allBytes.add(fileBytes);
        }

        // Combine all bytes
        int totalLength = allBytes.stream().mapToInt(bytes -> bytes.length).sum();
        byte[] result = new byte[totalLength];
        int position = 0;

        for (byte[] bytes : allBytes) {
            System.arraycopy(bytes, 0, result, position, bytes.length);
            position += bytes.length;
        }

        return result;
    }
}
