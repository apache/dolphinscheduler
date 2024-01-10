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

package org.apache.dolphinscheduler.server.worker.rpc;

import org.apache.dolphinscheduler.extract.base.NettyRemotingServerFactory;
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;
import org.apache.dolphinscheduler.extract.base.server.SpringServerMethodInvokerDiscovery;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;

import java.io.Closeable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkerRpcServer extends SpringServerMethodInvokerDiscovery implements Closeable {

    public WorkerRpcServer(WorkerConfig workerConfig) {
        super(NettyRemotingServerFactory.buildNettyRemotingServer(NettyServerConfig.builder()
                .serverName("WorkerRpcServer").listenPort(workerConfig.getListenPort()).build()));
    }

    public void start() {
        log.info("WorkerRpcServer starting...");
        nettyRemotingServer.start();
        log.info("WorkerRpcServer started...");
    }

    @Override
    public void close() {
        log.info("WorkerRpcServer closing");
        nettyRemotingServer.close();
        log.info("WorkerRpcServer closed");
    }

}
