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

import static org.apache.dolphinscheduler.common.constants.Constants.APPID_COLLECT;
import static org.apache.dolphinscheduler.common.constants.Constants.DEFAULT_COLLECT_WAY;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.log.GetAppIdRequest;
import org.apache.dolphinscheduler.remote.command.log.GetAppIdResponse;
import org.apache.dolphinscheduler.remote.command.log.GetLogBytesRequest;
import org.apache.dolphinscheduler.remote.command.log.GetLogBytesResponse;
import org.apache.dolphinscheduler.remote.command.log.RemoveTaskLogRequest;
import org.apache.dolphinscheduler.remote.command.log.RemoveTaskLogResponse;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogRequest;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogResponse;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.factory.NettyRemotingClientFactory;
import org.apache.dolphinscheduler.remote.utils.Host;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.annotation.Nullable;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogClient implements AutoCloseable {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private final NettyRemotingClient client;

    private static final long LOG_REQUEST_TIMEOUT = 10 * 1000L;

    public LogClient() {
        client = NettyRemotingClientFactory.buildNettyRemotingClient();
    }

    public RollViewLogResponse queryTaskInstanceLog(Host taskExecuteHost,
                                                    String taskInstanceLogPath,
                                                    int skipLineNum,
                                                    int limit) {
        return doRollingViewLog(taskExecuteHost, taskInstanceLogPath, skipLineNum, limit);
    }

    public byte[] queryWholeTaskInstanceLogBytes(Host taskExecuteHost, String workflowInstanceLogPath) {
        return doDownloadLog(taskExecuteHost, workflowInstanceLogPath);
    }

    public RollViewLogResponse queryWorkflowInstanceLog(Host workflowInstanceExecuteHost,
                                                        String workflowInstanceLogPath,
                                                        int skipLineNum,
                                                        int limit) {
        return doRollingViewLog(workflowInstanceExecuteHost, workflowInstanceLogPath, skipLineNum, limit);
    }

    public byte[] queryWholeWorkflowInstanceLogBytes(Host host, String workflowInstanceLogPath) {
        return doDownloadLog(host, workflowInstanceLogPath);
    }

    public void removeTaskLog(@NonNull Host host, String path) {
        log.info("Begin remove task log from host: {} logPath {}", host, path);
        RemoveTaskLogRequest request = new RemoveTaskLogRequest(path);
        try {
            Message message = request.convert2Command();
            client.sendAsync(host, message, LOG_REQUEST_TIMEOUT, responseFuture -> {
                if (responseFuture.getCause() != null) {
                    log.error("Remove task log from host: {} logPath {} error, meet an unknown exception", host,
                            path, responseFuture.getCause());
                    return;
                }
                Message response = responseFuture.getResponseCommand();
                if (response == null) {
                    log.error("Remove task log from host: {} logPath {} error, response is null", host, path);
                    return;
                }
                RemoveTaskLogResponse removeTaskLogResponse =
                        JSONUtils.parseObject(response.getBody(), RemoveTaskLogResponse.class);
                if (removeTaskLogResponse.getStatus()) {
                    log.info("Success remove task log from host: {} logPath {}", host, path);
                } else {
                    log.error("Remove task log from host: {} logPath {} error", host, path);
                }
            });
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            log.error("Remove task log from host: {} logPath {} error, the current thread has been interrupted",
                    host,
                    path, interruptedException);
        } catch (Exception e) {
            log.error("Remove task log from host: {},  logPath: {} error", host, path, e);
        }
    }

    public @Nullable List<String> getAppIds(@NonNull String host, int port, @NonNull String taskLogFilePath,
                                            @NonNull String taskAppInfoPath) throws RemotingException, InterruptedException {
        log.info("Begin to get appIds from worker: {}:{} taskLogPath: {}, taskAppInfoPath: {}", host, port,
                taskLogFilePath, taskAppInfoPath);
        final Host workerAddress = new Host(host, port);
        List<String> appIds = null;
        if (NetUtils.getHost().equals(host)) {
            appIds = LogUtils.getAppIds(taskLogFilePath, taskAppInfoPath,
                    PropertyUtils.getString(APPID_COLLECT, DEFAULT_COLLECT_WAY));
        } else {
            final Message message = new GetAppIdRequest(taskLogFilePath, taskAppInfoPath).convert2Command();
            Message response = this.client.sendSync(workerAddress, message, LOG_REQUEST_TIMEOUT);
            if (response != null) {
                GetAppIdResponse responseCommand =
                        JSONUtils.parseObject(response.getBody(), GetAppIdResponse.class);
                appIds = responseCommand.getAppIds();
            }
        }
        log.info("Get appIds: {} from worker: {}:{} taskLogPath: {}, taskAppInfoPath: {}", appIds, host, port,
                taskLogFilePath, taskAppInfoPath);
        return appIds;
    }

    private RollViewLogResponse doRollingViewLog(Host executeHost,
                                                 String logPath,
                                                 int skipLineNum,
                                                 int limit) {
        RollViewLogRequest request = new RollViewLogRequest(logPath, skipLineNum, limit);
        try {
            Message response = client.sendSync(executeHost, request.convert2Command(), LOG_REQUEST_TIMEOUT);
            if (response != null) {
                return JSONUtils.parseObject(response.getBody(), RollViewLogResponse.class);
            }
            log.error("Roll view log response is null, request: {}", request);
            return RollViewLogResponse.error(RollViewLogResponse.Status.UNKNOWN_ERROR);
        } catch (Exception e) {
            log.error("Roll view log failed, meet an unknown exception: {}", request, e);
            return RollViewLogResponse.error(RollViewLogResponse.Status.UNKNOWN_ERROR);
        }
    }

    private byte[] doDownloadLog(Host executeHost, String logPath) {
        log.info("Get log bytes from host: {}, logPath {}", executeHost, logPath);
        GetLogBytesRequest request = new GetLogBytesRequest(logPath);
        try {
            Message response = client.sendSync(executeHost, request.convert2Command(), LOG_REQUEST_TIMEOUT);
            if (response != null) {
                GetLogBytesResponse getLogBytesResponse =
                        JSONUtils.parseObject(response.getBody(), GetLogBytesResponse.class);
                GetLogBytesResponse.Status responseStatus = getLogBytesResponse.getResponseStatus();
                if (responseStatus == GetLogBytesResponse.Status.SUCCESS) {
                    return getLogBytesResponse.getData();
                }
                return getLogBytesResponse.getResponseStatus().getDesc().getBytes(StandardCharsets.UTF_8);
            }
            log.error("Get logByte from host: {}, logPath: {} error, the response is null", executeHost, logPath);
            return EMPTY_BYTE_ARRAY;
        } catch (Exception e) {
            log.error("Get logByte from host: {}, logPath: {} error", executeHost, logPath, e);
            return GetLogBytesResponse.Status.UNKNOWN_ERROR.getDesc().getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public void close() {
        this.client.close();
        log.info("LogClientService closed");
    }

}
