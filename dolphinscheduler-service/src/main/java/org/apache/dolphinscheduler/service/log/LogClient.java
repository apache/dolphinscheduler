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

package org.apache.dolphinscheduler.service.log;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
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
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.factory.NettyRemotingClientFactory;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;

import java.util.List;

import javax.annotation.Nullable;

import lombok.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogClient implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(LogClient.class);

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private final NettyRemotingClient client;

    private static final long LOG_REQUEST_TIMEOUT = 10 * 1000L;

    public LogClient() {
        client = NettyRemotingClientFactory.buildNettyRemotingClient();
    }

    /**
     * roll view log
     *
     * @param host host
     * @param port port
     * @param path path
     * @param skipLineNum skip line number
     * @param limit limit
     * @return log content
     */
    public String rollViewLog(String host, int port, String path, int skipLineNum, int limit) {
        logger.info("Roll view log from host : {}, port : {}, path {}, skipLineNum {} ,limit {}", host, port, path,
                skipLineNum, limit);
        RollViewLogRequestCommand request = new RollViewLogRequestCommand(path, skipLineNum, limit);
        final Host address = new Host(host, port);
        try {
            Command command = request.convert2Command();
            Command response = client.sendSync(address, command, LOG_REQUEST_TIMEOUT);
            if (response != null) {
                RollViewLogResponseCommand rollReviewLog =
                        JSONUtils.parseObject(response.getBody(), RollViewLogResponseCommand.class);
                return rollReviewLog.getMsg();
            }
            return "Roll view log response is null";
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.error(
                    "Roll view log from host : {}, port : {}, path {}, skipLineNum {} ,limit {} error, the current thread has been interrupted",
                    host, port, path, skipLineNum, limit, ex);
            return "Roll view log error: " + ex.getMessage();
        } catch (Exception e) {
            logger.error("Roll view log from host : {}, port : {}, path {}, skipLineNum {} ,limit {} error", host, port,
                    path, skipLineNum, limit, e);
            return "Roll view log error: " + e.getMessage();
        }
    }

    /**
     * view log
     *
     * @param host host
     * @param port port
     * @param path path
     * @return log content
     */
    public String viewLog(String host, int port, String path) {
        logger.info("View log from host: {}, port: {}, logPath: {}", host, port, path);
        ViewLogRequestCommand request = new ViewLogRequestCommand(path);
        final Host address = new Host(host, port);
        try {
            if (NetUtils.getHost().equals(host)) {
                return LoggerUtils.readWholeFileContent(request.getPath());
            } else {
                Command command = request.convert2Command();
                Command response = this.client.sendSync(address, command, LOG_REQUEST_TIMEOUT);
                if (response != null) {
                    ViewLogResponseCommand viewLog =
                            JSONUtils.parseObject(response.getBody(), ViewLogResponseCommand.class);
                    return viewLog.getMsg();
                }
                return "View log response is null";
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.error("View log from host: {}, port: {}, logPath: {} error, the current thread has been interrupted",
                    host, port, path, ex);
            return "View log error: " + ex.getMessage();
        } catch (Exception e) {
            logger.error("View log from host: {}, port: {}, logPath: {} error", host, port, path, e);
            return "View log error: " + e.getMessage();
        }
    }

    /**
     * get log size
     *
     * @param host host
     * @param port port
     * @param path log path
     * @return log content bytes
     */
    public byte[] getLogBytes(String host, int port, String path) {
        logger.info("Get log bytes from host: {}, port: {}, logPath {}", host, port, path);
        GetLogBytesRequestCommand request = new GetLogBytesRequestCommand(path);
        final Host address = new Host(host, port);
        try {
            Command command = request.convert2Command();
            Command response = this.client.sendSync(address, command, LOG_REQUEST_TIMEOUT);
            if (response != null) {
                GetLogBytesResponseCommand getLog =
                        JSONUtils.parseObject(response.getBody(), GetLogBytesResponseCommand.class);
                return getLog.getData() == null ? EMPTY_BYTE_ARRAY : getLog.getData();
            }
            return EMPTY_BYTE_ARRAY;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.error(
                    "Get logSize from host: {}, port: {}, logPath: {} error, the current thread has been interrupted",
                    host, port, path, ex);
            return EMPTY_BYTE_ARRAY;
        } catch (Exception e) {
            logger.error("Get logSize from host: {}, port: {}, logPath: {} error", host, port, path, e);
            return EMPTY_BYTE_ARRAY;
        }
    }

    /**
     * remove task log
     *
     * @param host host
     * @param port port
     * @param path path
     * @return remove task status
     */
    public Boolean removeTaskLog(String host, int port, String path) {
        logger.info("Remove task log from host: {}, port: {}, logPath {}", host, port, path);
        RemoveTaskLogRequestCommand request = new RemoveTaskLogRequestCommand(path);
        final Host address = new Host(host, port);
        try {
            Command command = request.convert2Command();
            Command response = this.client.sendSync(address, command, LOG_REQUEST_TIMEOUT);
            if (response != null) {
                RemoveTaskLogResponseCommand taskLogResponse =
                        JSONUtils.parseObject(response.getBody(), RemoveTaskLogResponseCommand.class);
                return taskLogResponse.getStatus();
            }
            return false;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.error(
                    "Remove task log from host: {}, port: {} logPath: {} error, the current thread has been interrupted",
                    host, port, path, ex);
            return false;
        } catch (Exception e) {
            logger.error("Remove task log from host: {}, port: {} logPath: {} error", host, port, path, e);
            return false;
        }
    }

    public @Nullable List<String> getAppIds(@NonNull String host, int port,
                                            @NonNull String taskLogFilePath) throws RemotingException, InterruptedException {
        logger.info("Begin to get appIds from worker: {}:{} taskLogPath: {}", host, port, taskLogFilePath);
        final Host workerAddress = new Host(host, port);
        List<String> appIds = null;
        if (NetUtils.getHost().equals(host)) {
            appIds = LogUtils.getAppIdsFromLogFile(taskLogFilePath);
        } else {
            final Command command = new GetAppIdRequestCommand(taskLogFilePath).convert2Command();
            Command response = this.client.sendSync(workerAddress, command, LOG_REQUEST_TIMEOUT);
            if (response != null) {
                GetAppIdResponseCommand responseCommand =
                        JSONUtils.parseObject(response.getBody(), GetAppIdResponseCommand.class);
                appIds = responseCommand.getAppIds();
            }
        }
        logger.info("Get appIds: {} from worker: {}:{} taskLogPath: {}", appIds, host, port, taskLogFilePath);
        return appIds;
    }

    @Override
    public void close() {
        this.client.close();
        logger.info("LogClientService closed");
    }

}
