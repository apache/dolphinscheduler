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

    private final Address address;

    private final long logRequestTimeout = 10 * 1000; //10s

    /**
     * construct client
     * @param host host
     * @param port port
     */
    public LogClientService(String host, int port) {
        this.address = new Address(host, port);
        this.clientConfig = new NettyClientConfig();
        this.clientConfig.setWorkerThreads(1);
        this.client = new NettyRemotingClient(clientConfig);
        this.client.registerProcessor(CommandType.ROLL_VIEW_LOG_RES,this);
        this.client.registerProcessor(CommandType.VIEW_LOG_RES, this);
        this.client.registerProcessor(CommandType.GET_LOG_RES, this);

    }

    /**
     * shutdown
     */
    public void shutdown()  {
        this.client.close();
        logger.info("logger client shutdown");
    }

    /**
     * roll view log
     * @param path path
     * @param skipLineNum skip line number
     * @param limit limit
     * @return log content
     */
    public String rollViewLog(String path,int skipLineNum,int limit) {
        logger.info("roll view log, path {}, skipLineNum {} ,limit {}", path, skipLineNum, limit);
        RollViewLogRequestCommand request = new RollViewLogRequestCommand(path, skipLineNum, limit);
        String result = "";
        try {
            Command command = request.convert2Command();
            this.client.send(address, command);
            LogPromise promise = new LogPromise(command.getOpaque(), logRequestTimeout);
            result = ((String)promise.getResult());
        } catch (Exception e) {
            logger.error("roll view log error", e);
        }
        return result;
    }

    /**
     * view log
     * @param path path
     * @return log content
     */
    public String viewLog(String path) {
        logger.info("view log path {}", path);
        ViewLogRequestCommand request = new ViewLogRequestCommand(path);
        String result = "";
        try {
            Command command = request.convert2Command();
            this.client.send(address, command);
            LogPromise promise = new LogPromise(command.getOpaque(), logRequestTimeout);
            result = ((String)promise.getResult());
        } catch (Exception e) {
            logger.error("view log error", e);
        }
        return result;
    }

    /**
     * get log size
     * @param path log path
     * @return log content bytes
     */
    public byte[] getLogBytes(String path) {
        logger.info("log path {}", path);
        GetLogRequestCommand request = new GetLogRequestCommand(path);
        byte[] result = null;
        try {
            Command command = request.convert2Command();
            this.client.send(address, command);
            LogPromise promise = new LogPromise(command.getOpaque(), logRequestTimeout);
            result = (byte[])promise.getResult();
        } catch (Exception e) {
            logger.error("get log size error", e);
        }
        return result;
    }

    @Override
    public void process(Channel channel, Command command) {
        logger.info("received log response : {}", command);
        switch (command.getType()){
            case ROLL_VIEW_LOG_RES:
                RollViewLogResponseCommand rollReviewLog = FastJsonSerializer.deserialize(command.getBody(), RollViewLogResponseCommand.class);
                LogPromise.notify(command.getOpaque(), rollReviewLog.getMsg());
                break;
            case VIEW_LOG_RES:
                ViewLogResponseCommand viewLog = FastJsonSerializer.deserialize(command.getBody(), ViewLogResponseCommand.class);
                LogPromise.notify(command.getOpaque(), viewLog.getMsg());
                break;
            case GET_LOG_RES:
                GetLogResponseCommand getLog = FastJsonSerializer.deserialize(command.getBody(), GetLogResponseCommand.class);
                LogPromise.notify(command.getOpaque(), getLog.getData());
                break;
            default:
                throw new UnsupportedOperationException(String.format("command type : %s is not supported ", command.getType()));
        }
    }

    public static void main(String[] args) throws Exception{
        LogClientService logClient = new LogClientService("192.168.220.247", 50051);
        String log = logClient.rollViewLog("/opt/program/incubator-dolphinscheduler/logs/1/463/540.log",0,1000);
        System.out.println(log);
    }
}