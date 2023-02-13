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
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.remote.processor.LoggerRequestProcessor;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.processor.*;

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
    private TaskExecuteRunningProcessor taskExecuteRunningProcessor;

    @Autowired
    private TaskExecuteResponseProcessor taskExecuteResponseProcessor;

    @Autowired
    private TaskEventProcessor taskEventProcessor;

    @Autowired
    private StateEventProcessor stateEventProcessor;

    @Autowired
    private CacheProcessor cacheProcessor;

    @Autowired
    private TaskKillResponseProcessor taskKillResponseProcessor;

    @Autowired
    private UpdatePidProcessor updatePidProcessor;

    @Autowired
    private TaskRecallProcessor taskRecallProcessor;

    @Autowired
    private LoggerRequestProcessor loggerRequestProcessor;

    @Autowired
    private WorkflowExecutingDataRequestProcessor workflowExecutingDataRequestProcessor;

    @Autowired
    private TaskExecuteStartProcessor taskExecuteStartProcessor;

    public void start() {
        log.info("Starting Master RPC Server...");
        // init remoting server
        NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(masterConfig.getListenPort());
        this.nettyRemotingServer = new NettyRemotingServer(serverConfig);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_RUNNING, taskExecuteRunningProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_UPDATE_PID, updatePidProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_RESULT, taskExecuteResponseProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_KILL_RESPONSE, taskKillResponseProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.STATE_EVENT_REQUEST, stateEventProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_FORCE_STATE_EVENT_REQUEST, taskEventProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_WAKEUP_EVENT_REQUEST, taskEventProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.CACHE_EXPIRE, cacheProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_REJECT, taskRecallProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.WORKFLOW_EXECUTING_DATA_REQUEST,
                workflowExecutingDataRequestProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_START, taskExecuteStartProcessor);

        // log server
        this.nettyRemotingServer.registerProcessor(CommandType.GET_LOG_BYTES_REQUEST, loggerRequestProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.ROLL_VIEW_LOG_REQUEST, loggerRequestProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.VIEW_WHOLE_LOG_REQUEST, loggerRequestProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.REMOVE_TAK_LOG_REQUEST, loggerRequestProcessor);

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
