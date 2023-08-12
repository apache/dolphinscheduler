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

package org.apache.dolphinscheduler.server.master.rpc;

import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.remote.processor.MasterRpcProcessor;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Master RPC Server, used to send/receive request to other system.
 */
@Service
@Slf4j
public class MasterRPCServer implements AutoCloseable {

    private NettyRemotingServer nettyRemotingServer;

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private List<MasterRpcProcessor> masterRpcProcessors;

    public void start() {
        log.info("Starting Master RPC Server...");
        // init remoting server
        NettyServerConfig serverConfig = masterConfig.getMasterRpcServerConfig();
        serverConfig.setListenPort(masterConfig.getListenPort());
        this.nettyRemotingServer = new NettyRemotingServer(serverConfig);
        for (MasterRpcProcessor masterRpcProcessor : masterRpcProcessors) {
            this.nettyRemotingServer.registerProcessor(masterRpcProcessor);
            log.info("Success register netty processor: {}", masterRpcProcessor.getClass().getName());
        }
        this.nettyRemotingServer.start();
        log.info("Started Master RPC Server...");
    }

    @Override
    public void close() {
        log.info("Closing Master RPC Server...");
        this.nettyRemotingServer.close();
        log.info("Closed Master RPC Server...");
    }

}
