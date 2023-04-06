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
import java.util.List;
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
            try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
                return stream.skip(skipLine).limit(limit).collect(Collectors.toList());
            } catch (IOException e) {
                log.error("read file error", e);
                throw new RuntimeException(String.format("Read file: %s error", filePath), e);
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

}
