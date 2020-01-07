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
package org.apache.dolphinscheduler.api.log;

import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.log.GetLogRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.ViewLogRequestCommand;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.utils.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * log client
 */
public class LoggerClient {

    private static final Logger logger = LoggerFactory.getLogger(LoggerClient.class);

    private final NettyRemotingClient client;

    /**
     * construct client
     * @param host host
     * @param port port
     */
    public LoggerClient(String host, int port) {
        NettyClientConfig clientConfig = new NettyClientConfig();
        this.client = new NettyRemotingClient(clientConfig);
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
    //TODO result
    public String rollViewLog(String path,int skipLineNum,int limit) {
        logger.info("roll view log : path {},skipLineNum {} ,limit {}", path, skipLineNum, limit);
        RollViewLogRequestCommand request = new RollViewLogRequestCommand(path, skipLineNum, limit);
        try {
            this.client.send(new Address(), request.convert2Command());

            return "";
        } catch (Exception e) {
            logger.error("roll view log error", e);
            return null;
        }
    }

    /**
     * view log
     * @param path path
     * @return log content
     */
    //TODO result
    public String viewLog(String path) {
        logger.info("view log path {}",path);
        ViewLogRequestCommand request = new ViewLogRequestCommand(path);
        try {
            this.client.send(new Address(), request.convert2Command());
            return "";
        } catch (Exception e) {
            logger.error("view log error", e);
            return null;
        }
    }

    /**
     * get log size
     * @param path log path
     * @return log content bytes
     */
    //TODO result
    public byte[] getLogBytes(String path) {
        logger.info("log path {}",path);
        GetLogRequestCommand request = new GetLogRequestCommand(path);
        try {
            this.client.send(new Address(), request.convert2Command());
            return null;
        } catch (Exception e) {
            logger.error("get log size error", e);
            return null;
        }
    }

}