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
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.worker.processor.TaskExecuteResultAckProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskExecuteRunningAckProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskRejectAckProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskUpdatePidAckProcessor;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This rpc client is only used to send message, will not receive message, all response message should send to {@link WorkerRpcServer}.
 */
@Component
@Slf4j
public class WorkerRpcClient implements AutoCloseable {

    @Autowired
    private TaskExecuteRunningAckProcessor taskExecuteRunningAckProcessor;

    @Autowired
    private TaskExecuteResultAckProcessor taskExecuteResultAckProcessor;

    @Resource
    private TaskUpdatePidAckProcessor taskUpdatePidAckProcessor;

    @Autowired
    private TaskRejectAckProcessor taskRejectAckProcessor;

    private NettyRemotingClient nettyRemotingClient;

    public void start() {
        log.info("Worker rpc client starting");
        NettyClientConfig nettyClientConfig = new NettyClientConfig();
        this.nettyRemotingClient = new NettyRemotingClient(nettyClientConfig);
        // we only use the client to handle the ack message, we can optimize this, send ack to the nettyServer.
        this.nettyRemotingClient.registerProcessor(CommandType.TASK_EXECUTE_RUNNING_ACK,
                taskExecuteRunningAckProcessor);
        this.nettyRemotingClient.registerProcessor(CommandType.TASK_UPDATE_PID_ACK,
                taskUpdatePidAckProcessor);
        this.nettyRemotingClient.registerProcessor(CommandType.TASK_EXECUTE_RESULT_ACK, taskExecuteResultAckProcessor);
        this.nettyRemotingClient.registerProcessor(CommandType.TASK_REJECT_ACK, taskRejectAckProcessor);
        log.info("Worker rpc client started");
    }

    public void send(Host host, Command command) throws RemotingException {
        nettyRemotingClient.send(host, command);
    }

    public void close() {
        log.info("Worker rpc client closing");
        nettyRemotingClient.close();
        log.info("Worker rpc client closed");
    }
}
