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

package org.apache.dolphinscheduler.remote.processor;

import org.apache.dolphinscheduler.common.log.remote.RemoteLogUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseLogProcessor {

    /**
     * get files content bytes for download file
     *
     * @param filePath file path
     * @return byte array of file
     */
    protected byte[] getFileContentBytesFromLocal(String filePath) {
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

    protected byte[] getFileContentBytesFromRemote(String filePath) {
        RemoteLogUtils.getRemoteLog(filePath);
        return getFileContentBytesFromLocal(filePath);
    }

    protected byte[] getFileContentBytes(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return getFileContentBytesFromLocal(filePath);
        }
        if (RemoteLogUtils.isRemoteLoggingEnable()) {
            return getFileContentBytesFromRemote(filePath);
        }
        return getFileContentBytesFromLocal(filePath);
    }

    /**
     * read part file content，can skip any line and read some lines
     *
     * @param filePath file path
     * @param skipLine skip line
     * @param limit read lines limit
     * @return part file content
     */
    protected List<String> readPartFileContentFromLocal(String filePath,
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

    protected List<String> readPartFileContentFromRemote(String filePath,
                                                         int skipLine,
                                                         int limit) {
        RemoteLogUtils.getRemoteLog(filePath);
        return readPartFileContentFromLocal(filePath, skipLine, limit);
    }

    protected List<String> readPartFileContent(String filePath,
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

    protected String readWholeFileContentFromRemote(String filePath) {
        RemoteLogUtils.getRemoteLog(filePath);
        return LogUtils.readWholeFileContentFromLocal(filePath);
    }

    protected String readWholeFileContent(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return LogUtils.readWholeFileContentFromLocal(filePath);
        }
        if (RemoteLogUtils.isRemoteLoggingEnable()) {
            return readWholeFileContentFromRemote(filePath);
        }
        return LogUtils.readWholeFileContentFromLocal(filePath);
    }

}
