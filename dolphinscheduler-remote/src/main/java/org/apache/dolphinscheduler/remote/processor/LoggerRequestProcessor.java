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

import static org.apache.dolphinscheduler.common.constants.Constants.APPID_COLLECT;
import static org.apache.dolphinscheduler.common.constants.Constants.DEFAULT_COLLECT_WAY;

import org.apache.dolphinscheduler.common.log.remote.RemoteLogUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.log.GetAppIdRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.GetAppIdResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.GetLogBytesRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.GetLogBytesResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.RemoveTaskLogRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.RemoveTaskLogResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.ViewLogRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.ViewLogResponseCommand;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

/**
 * logger request process logic
 */
@Component
@Slf4j
public class LoggerRequestProcessor implements NettyRequestProcessor {

    @Override
    public void process(Channel channel, Command command) {
        log.info("received command : {}", command);

        // request task log command type
        final CommandType commandType = command.getType();
        switch (commandType) {
            case GET_LOG_BYTES_REQUEST:
                GetLogBytesRequestCommand getLogRequest = JSONUtils.parseObject(
                        command.getBody(), GetLogBytesRequestCommand.class);
                String path = getLogRequest.getPath();
                byte[] bytes = getFileContentBytes(path);
                GetLogBytesResponseCommand getLogResponse = new GetLogBytesResponseCommand(bytes);
                channel.writeAndFlush(getLogResponse.convert2Command(command.getOpaque()));
                break;
            case VIEW_WHOLE_LOG_REQUEST:
                ViewLogRequestCommand viewLogRequest = JSONUtils.parseObject(
                        command.getBody(), ViewLogRequestCommand.class);
                String viewLogPath = viewLogRequest.getPath();
                String msg = readWholeFileContent(viewLogPath);
                ViewLogResponseCommand viewLogResponse = new ViewLogResponseCommand(msg);
                channel.writeAndFlush(viewLogResponse.convert2Command(command.getOpaque()));
                break;
            case ROLL_VIEW_LOG_REQUEST:
                RollViewLogRequestCommand rollViewLogRequest = JSONUtils.parseObject(
                        command.getBody(), RollViewLogRequestCommand.class);

                String rollViewLogPath = rollViewLogRequest.getPath();

                List<String> lines = readPartFileContent(rollViewLogPath,
                        rollViewLogRequest.getSkipLineNum(), rollViewLogRequest.getLimit());
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
                RollViewLogResponseCommand rollViewLogRequestResponse =
                        new RollViewLogResponseCommand(builder.toString());
                channel.writeAndFlush(rollViewLogRequestResponse.convert2Command(command.getOpaque()));
                break;
            case REMOVE_TAK_LOG_REQUEST:
                RemoveTaskLogRequestCommand removeTaskLogRequest = JSONUtils.parseObject(
                        command.getBody(), RemoveTaskLogRequestCommand.class);

                String taskLogPath = removeTaskLogRequest.getPath();
                File taskLogFile = new File(taskLogPath);
                boolean status = true;
                try {
                    if (taskLogFile.exists()) {
                        status = taskLogFile.delete();
                    }
                } catch (Exception e) {
                    status = false;
                }

                RemoveTaskLogResponseCommand removeTaskLogResponse = new RemoveTaskLogResponseCommand(status);
                channel.writeAndFlush(removeTaskLogResponse.convert2Command(command.getOpaque()));
                break;
            case GET_APP_ID_REQUEST:
                GetAppIdRequestCommand getAppIdRequestCommand =
                        JSONUtils.parseObject(command.getBody(), GetAppIdRequestCommand.class);
                String appInfoPath = getAppIdRequestCommand.getAppInfoPath();
                String logPath = getAppIdRequestCommand.getLogPath();
                List<String> appIds = LogUtils.getAppIds(logPath, appInfoPath,
                        PropertyUtils.getString(APPID_COLLECT, DEFAULT_COLLECT_WAY));
                channel.writeAndFlush(
                        new GetAppIdResponseCommand(appIds).convert2Command(command.getOpaque()));
                break;
            default:
                throw new IllegalArgumentException("unknown commandType: " + commandType);
        }
    }

    /**
     * get files content bytes for download file
     *
     * @param filePath file path
     * @return byte array of file
     */
    private byte[] getFileContentBytesFromLocal(String filePath) {
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

    private byte[] getFileContentBytesFromRemote(String filePath) {
        RemoteLogUtils.getRemoteLog(filePath);
        return getFileContentBytesFromLocal(filePath);
    }

    private byte[] getFileContentBytes(String filePath) {
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
    private List<String> readPartFileContentFromLocal(String filePath,
                                                      int skipLine,
                                                      int limit) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
                return stream.skip(skipLine).limit(limit).collect(Collectors.toList());
            } catch (IOException e) {
                log.error("read file error", e);
            }
        } else {
            log.info("file path: {} not exists", filePath);
        }
        return Collections.emptyList();
    }

    private List<String> readPartFileContentFromRemote(String filePath,
                                                       int skipLine,
                                                       int limit) {
        RemoteLogUtils.getRemoteLog(filePath);
        return readPartFileContentFromLocal(filePath, skipLine, limit);
    }

    private List<String> readPartFileContent(String filePath,
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

    private String readWholeFileContentFromRemote(String filePath) {
        RemoteLogUtils.getRemoteLog(filePath);
        return LogUtils.readWholeFileContentFromLocal(filePath);
    }

    private String readWholeFileContent(String filePath) {
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
