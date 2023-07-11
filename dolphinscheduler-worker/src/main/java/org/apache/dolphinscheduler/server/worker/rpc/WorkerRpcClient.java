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

import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.processor.WorkerRpcProcessor;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This rpc client is only used to send message, will not receive message, all response message should send to {@link WorkerRpcServer}.
 */
@Component
@Slf4j
public class WorkerRpcClient implements AutoCloseable {

    @Autowired
    @Lazy
    private List<WorkerRpcProcessor> workerRpcProcessors;

    @Autowired
    private WorkerConfig workerConfig;

    private NettyRemotingClient nettyRemotingClient;

    public void start() {
        log.info("Worker rpc client starting");
        this.nettyRemotingClient = new NettyRemotingClient(workerConfig.getWorkerRpcClientConfig());
        // we only use the client to handle the ack message, we can optimize this, send ack to the nettyServer.
        for (WorkerRpcProcessor workerRpcProcessor : workerRpcProcessors) {
            this.nettyRemotingClient.registerProcessor(workerRpcProcessor);
        }
        log.info("Worker rpc client started");
    }

    public void send(Host host, Message message) throws RemotingException {
        nettyRemotingClient.send(host, message);
    }

    public void close() {
        log.info("Worker rpc client closing");
        nettyRemotingClient.close();
        log.info("Worker rpc client closed");
    }
}
