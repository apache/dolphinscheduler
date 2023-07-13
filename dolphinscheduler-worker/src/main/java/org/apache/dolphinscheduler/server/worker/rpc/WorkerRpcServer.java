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

import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.remote.processor.WorkerRpcProcessor;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;

import java.io.Closeable;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WorkerRpcServer implements Closeable {

    @Autowired
    private List<WorkerRpcProcessor> workerRpcProcessors;

    @Autowired
    private WorkerConfig workerConfig;

    private NettyRemotingServer nettyRemotingServer;

    public void start() {
        log.info("Worker rpc server starting...");
        NettyServerConfig serverConfig = workerConfig.getWorkerRpcServerConfig();
        serverConfig.setListenPort(workerConfig.getListenPort());
        nettyRemotingServer = new NettyRemotingServer(serverConfig);
        for (WorkerRpcProcessor workerRpcProcessor : workerRpcProcessors) {
            nettyRemotingServer.registerProcessor(workerRpcProcessor);
            log.info("Success register WorkerRpcProcessor: {}", workerRpcProcessor.getClass().getName());
        }
        this.nettyRemotingServer.start();
        log.info("Worker rpc server started...");
    }

    @Override
    public void close() {
        log.info("Worker rpc server closing");
        this.nettyRemotingServer.close();
        log.info("Worker rpc server closed");
    }

}
