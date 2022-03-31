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
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.log.GetLogBytesRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.GetLogBytesResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.RemoveTaskLogRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.RemoveTaskLogResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.ViewLogRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.ViewLogResponseCommand;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.utils.Host;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * log client
 */
public class LogClientService implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(LogClientService.class);

    private final NettyClientConfig clientConfig;

    private final NettyRemotingClient client;

    private volatile boolean isRunning;

    /**
     * request time out
     */
    private static final long LOG_REQUEST_TIMEOUT = 10 * 1000L;

    /**
     * construct client
     */
    public LogClientService() {
        this.clientConfig = new NettyClientConfig();
        this.clientConfig.setWorkerThreads(4);
        this.client = new NettyRemotingClient(clientConfig);
        this.isRunning = true;
    }

    /**
     * close
     */
    @Override
    public void close() {
        this.client.close();
        this.isRunning = false;
        logger.info("logger client closed");
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
        logger.info("roll view log, host : {}, port : {}, path {}, skipLineNum {} ,limit {}", host, port, path, skipLineNum, limit);
        RollViewLogRequestCommand request = new RollViewLogRequestCommand(path, skipLineNum, limit);
        String result = "";
        final Host address = new Host(host, port);
        try {
            Command command = request.convert2Command();
            Command response = this.client.sendSync(address, command, LOG_REQUEST_TIMEOUT);
            if (response != null) {
                RollViewLogResponseCommand rollReviewLog = JSONUtils.parseObject(
                        response.getBody(), RollViewLogResponseCommand.class);
                return rollReviewLog.getMsg();
            }
        } catch (Exception e) {
            logger.error("roll view log error", e);
        } finally {
            this.client.closeChannel(address);
        }
        return result;
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
        logger.info("view log path {}", path);
        ViewLogRequestCommand request = new ViewLogRequestCommand(path);
        String result = "";
        final Host address = new Host(host, port);
        try {
            if (NetUtils.getHost().equals(host)) {
                result = LoggerUtils.readWholeFileContent(request.getPath());
            } else {
                Command command = request.convert2Command();
                Command response = this.client.sendSync(address, command, LOG_REQUEST_TIMEOUT);
                if (response != null) {
                    ViewLogResponseCommand viewLog = JSONUtils.parseObject(
                            response.getBody(), ViewLogResponseCommand.class);
                    result = viewLog.getMsg();
                }
            }
        } catch (Exception e) {
            logger.error("view log error", e);
        } finally {
            this.client.closeChannel(address);
        }
        return result;
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
        logger.info("log path {}", path);
        GetLogBytesRequestCommand request = new GetLogBytesRequestCommand(path);
        final Host address = new Host(host, port);
        try {
            Command command = request.convert2Command();
            Command response = this.client.sendSync(address, command, LOG_REQUEST_TIMEOUT);
            if (response != null) {
                GetLogBytesResponseCommand getLog = JSONUtils.parseObject(
                        response.getBody(), GetLogBytesResponseCommand.class);
                return getLog.getData() == null ? new byte[0] : getLog.getData();
            }
        } catch (Exception e) {
            logger.error("get log size error", e);
        } finally {
            this.client.closeChannel(address);
        }
        return new byte[0];
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
        logger.info("log path {}", path);
        RemoveTaskLogRequestCommand request = new RemoveTaskLogRequestCommand(path);
        Boolean result = false;
        final Host address = new Host(host, port);
        try {
            Command command = request.convert2Command();
            Command response = this.client.sendSync(address, command, LOG_REQUEST_TIMEOUT);
            if (response != null) {
                RemoveTaskLogResponseCommand taskLogResponse = JSONUtils.parseObject(
                        response.getBody(), RemoveTaskLogResponseCommand.class);
                return taskLogResponse.getStatus();
            }
        } catch (Exception e) {
            logger.error("remove task log error", e);
        } finally {
            this.client.closeChannel(address);
        }
        return result;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
