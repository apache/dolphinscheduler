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

package org.apache.dolphinscheduler.alert.rpc;

import org.apache.dolphinscheduler.alert.config.AlertConfig;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.factory.NettyRemotingServerFactory;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlertRpcServer implements AutoCloseable {

    @Autowired
    private List<NettyRequestProcessor> nettyRequestProcessors;
    @Autowired
    private AlertConfig alertConfig;

    private NettyRemotingServer nettyRemotingServer;

    public void start() {
        log.info("Starting alert rpc server...");
        nettyRemotingServer = NettyRemotingServerFactory.buildNettyRemotingServer(alertConfig.getPort());
        for (NettyRequestProcessor nettyRequestProcessor : nettyRequestProcessors) {
            nettyRemotingServer.registerProcessor(nettyRequestProcessor);
            log.info("Success register netty processor: {}", nettyRequestProcessor.getClass().getName());
        }
        nettyRemotingServer.start();
        log.info("Started alert rpc server...");
    }

    @Override
    public void close() throws Exception {
        log.info("Closing alert rpc server...");
        nettyRemotingServer.close();
        log.info("Closed alert rpc server...");
    }
}
