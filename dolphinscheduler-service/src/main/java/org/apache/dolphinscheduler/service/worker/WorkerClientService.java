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
package org.apache.dolphinscheduler.service.worker;

import io.netty.channel.Channel;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.log.*;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.Address;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;
import org.apache.dolphinscheduler.service.MasterResponseCommand;
import org.apache.dolphinscheduler.service.WorkerRequestCommand;
import org.apache.dolphinscheduler.service.log.LogPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * log client
 */
public class WorkerClientService implements NettyRequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(WorkerClientService.class);

    private final NettyClientConfig clientConfig;

    private final NettyRemotingClient client;

    private final Address address;

    /**
     *  request time out
     */
    private final long logRequestTimeout = 10 * 1000;

    /**
     * construct client
     * @param host host
     * @param port port
     */
    public WorkerClientService(String host, int port) {
        this.address = new Address(host, port);
        this.clientConfig = new NettyClientConfig();
        this.clientConfig.setWorkerThreads(1);
        this.client = new NettyRemotingClient(clientConfig);
        this.client.registerProcessor(CommandType.MASTER_RESPONSE, this);

    }

    /**
     * shutdown
     */
    public void shutdown()  {
        this.client.close();
        logger.info("logger client shutdown");
    }


    public String reportResult() {
        WorkerRequestCommand request = new WorkerRequestCommand();
        String result = "";
        try {
            Command command = request.convert2Command();
            this.client.send(address, command);
            LogPromise promise = new LogPromise(command.getOpaque(), logRequestTimeout);
            result = ((String)promise.getResult());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("roll view log error", e);
        }
        return result;
    }


    @Override
    public void process(Channel channel, Command command) {
        logger.info("received log response : {}", command);
        MasterResponseCommand masterResponseCommand = FastJsonSerializer.deserialize(
                command.getBody(), MasterResponseCommand.class);
        LogPromise.notify(command.getOpaque(), masterResponseCommand.getMsg());
    }

    public static void main(String[] args) throws Exception{
        WorkerClientService workerClientService = new WorkerClientService("192.168.220.247", 1128);
        String result = workerClientService.reportResult();
        System.out.println(result);

    }

}