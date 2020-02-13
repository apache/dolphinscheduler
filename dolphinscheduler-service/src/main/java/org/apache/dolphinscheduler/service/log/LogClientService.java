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

import io.netty.channel.Channel;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.log.*;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.Address;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * log client
 */
public class LogClientService implements NettyRequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(LogClientService.class);

    private final NettyClientConfig clientConfig;

    private final NettyRemotingClient client;

    /**
     *  request time out
     */
    private final long logRequestTimeout = 10 * 1000;

    /**
     * construct client
     */
    public LogClientService() {
        this.clientConfig = new NettyClientConfig();
        this.clientConfig.setWorkerThreads(4);
        this.client = new NettyRemotingClient(clientConfig);
        this.client.registerProcessor(CommandType.ROLL_VIEW_LOG_RESPONSE,this);
        this.client.registerProcessor(CommandType.VIEW_WHOLE_LOG_RESPONSE, this);
        this.client.registerProcessor(CommandType.GET_LOG_BYTES_RESPONSE, this);
    }

    /**
     * close
     */
    public void close()  {
        this.client.close();
        logger.info("logger client closed");
    }

    /**
     * roll view log
     * @param host host
     * @param port port
     * @param path path
     * @param skipLineNum skip line number
     * @param limit limit
     * @return log content
     */
    public String rollViewLog(String host, int port, String path,int skipLineNum,int limit) {
        logger.info("roll view log, host : {}, port : {}, path {}, skipLineNum {} ,limit {}", host, port, path, skipLineNum, limit);
        RollViewLogRequestCommand request = new RollViewLogRequestCommand(path, skipLineNum, limit);
        String result = "";
        final Address address = new Address(host, port);
        try {
            Command command = request.convert2Command();
            this.client.send(address, command);
            LogPromise promise = new LogPromise(command.getOpaque(), logRequestTimeout);
            result = ((String)promise.getResult());
        } catch (Exception e) {
            logger.error("roll view log error", e);
        } finally {
            this.client.closeChannel(address);
        }
        return result;
    }

    /**
     * view log
     * @param host host
     * @param port port
     * @param path path
     * @return log content
     */
    public String viewLog(String host, int port, String path) {
        logger.info("view log path {}", path);
        ViewLogRequestCommand request = new ViewLogRequestCommand(path);
        String result = "";
        final Address address = new Address(host, port);
        try {
            Command command = request.convert2Command();
            this.client.send(address, command);
            LogPromise promise = new LogPromise(command.getOpaque(), logRequestTimeout);
            result = ((String)promise.getResult());
        } catch (Exception e) {
            logger.error("view log error", e);
        } finally {
            this.client.closeChannel(address);
        }
        return result;
    }

    /**
     * get log size
     * @param host host
     * @param port port
     * @param path log path
     * @return log content bytes
     */
    public byte[] getLogBytes(String host, int port, String path) {
        logger.info("log path {}", path);
        GetLogBytesRequestCommand request = new GetLogBytesRequestCommand(path);
        byte[] result = null;
        final Address address = new Address(host, port);
        try {
            Command command = request.convert2Command();
            this.client.send(address, command);
            LogPromise promise = new LogPromise(command.getOpaque(), logRequestTimeout);
            result = (byte[])promise.getResult();
        } catch (Exception e) {
            logger.error("get log size error", e);
        } finally {
            this.client.closeChannel(address);
        }
        return result;
    }

    @Override
    public void process(Channel channel, Command command) {
        logger.info("received log response : {}", command);
        switch (command.getType()){
            case ROLL_VIEW_LOG_RESPONSE:
                RollViewLogResponseCommand rollReviewLog = FastJsonSerializer.deserialize(
                        command.getBody(), RollViewLogResponseCommand.class);
                LogPromise.notify(command.getOpaque(), rollReviewLog.getMsg());
                break;
            case VIEW_WHOLE_LOG_RESPONSE:
                ViewLogResponseCommand viewLog = FastJsonSerializer.deserialize(
                        command.getBody(), ViewLogResponseCommand.class);
                LogPromise.notify(command.getOpaque(), viewLog.getMsg());
                break;
            case GET_LOG_BYTES_RESPONSE:
                GetLogBytesResponseCommand getLog = FastJsonSerializer.deserialize(
                        command.getBody(), GetLogBytesResponseCommand.class);
                LogPromise.notify(command.getOpaque(), getLog.getData());
                break;
            default:
                throw new UnsupportedOperationException(String.format("command type : %s is not supported ", command.getType()));
        }
    }
}