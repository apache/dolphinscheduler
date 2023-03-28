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
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.remote.processor.LoggerRequestProcessor;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.processor.TaskDispatchProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskExecuteResultAckProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskExecuteRunningAckProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskKillProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskRejectAckProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskSavePointProcessor;
import org.apache.dolphinscheduler.server.worker.processor.TaskUpdatePidAckProcessor;
import org.apache.dolphinscheduler.server.worker.processor.WorkflowHostChangeProcessor;

import java.io.Closeable;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WorkerRpcServer implements Closeable {

    @Autowired
    private TaskDispatchProcessor taskDispatchProcessor;

    @Autowired
    private TaskKillProcessor taskKillProcessor;

    @Autowired
    private TaskRejectAckProcessor taskRejectAckProcessor;

    @Autowired
    private TaskSavePointProcessor taskSavePointProcessor;

    @Autowired
    private TaskExecuteRunningAckProcessor taskExecuteRunningAckProcessor;

    @Resource
    private TaskUpdatePidAckProcessor taskUpdatePidAckProcessor;

    @Autowired
    private TaskExecuteResultAckProcessor taskExecuteResultAckProcessor;

    @Autowired
    private WorkflowHostChangeProcessor workflowHostChangeProcessor;

    @Autowired
    private LoggerRequestProcessor loggerRequestProcessor;

    @Autowired
    private WorkerConfig workerConfig;

    private NettyRemotingServer nettyRemotingServer;

    public void start() {
        log.info("Worker rpc server starting");
        NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(workerConfig.getListenPort());
        this.nettyRemotingServer = new NettyRemotingServer(serverConfig);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_DISPATCH_REQUEST, taskDispatchProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_KILL_REQUEST, taskKillProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_RUNNING_ACK,
                taskExecuteRunningAckProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_UPDATE_PID_ACK,
                taskUpdatePidAckProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_EXECUTE_RESULT_ACK, taskExecuteResultAckProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_REJECT_ACK, taskRejectAckProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.WORKFLOW_HOST_CHANGE_REQUEST,
                workflowHostChangeProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.TASK_SAVEPOINT_REQUEST, taskSavePointProcessor);
        // log server
        this.nettyRemotingServer.registerProcessor(CommandType.GET_APP_ID_REQUEST, loggerRequestProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.GET_LOG_BYTES_REQUEST, loggerRequestProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.ROLL_VIEW_LOG_REQUEST, loggerRequestProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.VIEW_WHOLE_LOG_REQUEST, loggerRequestProcessor);
        this.nettyRemotingServer.registerProcessor(CommandType.REMOVE_TAK_LOG_REQUEST, loggerRequestProcessor);
        this.nettyRemotingServer.start();
        log.info("Worker rpc server started");
    }

    @Override
    public void close() {
        log.info("Worker rpc server closing");
        this.nettyRemotingServer.close();
        log.info("Worker rpc server closed");
    }

}
