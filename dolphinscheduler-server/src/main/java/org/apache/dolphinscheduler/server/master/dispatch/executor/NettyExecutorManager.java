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

package org.apache.dolphinscheduler.server.master.dispatch.executor;

import com.github.rholder.retry.RetryException;
import org.apache.dolphinscheduler.common.utils.RetryerUtils;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.server.master.processor.TaskAckProcessor;
import org.apache.dolphinscheduler.server.master.processor.TaskKillResponseProcessor;
import org.apache.dolphinscheduler.server.master.processor.TaskResponseProcessor;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 *  netty executor manager
 */
@Service
public class NettyExecutorManager extends AbstractExecutorManager<Boolean>{

    private final Logger logger = LoggerFactory.getLogger(NettyExecutorManager.class);

    /**
     * zookeeper node manager
     */
    @Autowired
    private ZookeeperNodeManager zookeeperNodeManager;

    /**
     * netty remote client
     */
    private final NettyRemotingClient nettyRemotingClient;

    /**
     * constructor
     */
    public NettyExecutorManager(){
        final NettyClientConfig clientConfig = new NettyClientConfig();
        this.nettyRemotingClient = new NettyRemotingClient(clientConfig);
    }

    @PostConstruct
    public void init(){
        /**
         * register EXECUTE_TASK_RESPONSE command type TaskResponseProcessor
         * register EXECUTE_TASK_ACK command type TaskAckProcessor
         */
        this.nettyRemotingClient.registerProcessor(CommandType.TASK_EXECUTE_RESPONSE, new TaskResponseProcessor());
        this.nettyRemotingClient.registerProcessor(CommandType.TASK_EXECUTE_ACK, new TaskAckProcessor());
        this.nettyRemotingClient.registerProcessor(CommandType.TASK_KILL_RESPONSE, new TaskKillResponseProcessor());
    }

    /**
     * execute logic
     * @param context context
     * @return result
     * @throws ExecuteException if error throws ExecuteException
     */
    @Override
    public Boolean execute(ExecutionContext context) throws ExecuteException {
        LinkedList<String> allNodes = new LinkedList<>();
        Set<String> nodes = getAllNodes(context);
        if (nodes != null) {
            allNodes.addAll(nodes);
        }
        /**
         *  build command accord executeContext
         */
        Command command = context.getCommand();

        /**
         * execute task host
         */
        String startHostAddress = context.getHost().getAddress();
        // remove start host address and add it to head
        allNodes.remove(startHostAddress);
        allNodes.addFirst(startHostAddress);
 
        boolean success = false;
        for (String address : allNodes) {
            try {
                Host host = Host.of(address);
                doExecute(host, command);
                success = true;
                context.setHost(host);
                break;
            } catch (ExecuteException ex) {
                logger.error("retry execute command : {} host : {}", command, address);
            }
        }
        if (!success) {
            throw new ExecuteException("fail after try all nodes");
        }
        
        return success;
    }

    @Override
    public void executeDirectly(ExecutionContext context) throws ExecuteException {
        Host host = context.getHost();
        doExecute(host, context.getCommand());
    }

    /**
     *  execute logic
     * @param host host
     * @param command command
     * @throws ExecuteException if error throws ExecuteException
     */
    private void doExecute(final Host host, final Command command) throws ExecuteException {
        try {
            RetryerUtils.retryCall(() -> {
                nettyRemotingClient.send(host, command);
                return Boolean.TRUE;
            });
        } catch (ExecutionException | RetryException e) {
            throw new ExecuteException(String.format("send command : %s to %s error", command, host), e);
        }
    }

    /**
     *  get all nodes
     * @param context context
     * @return nodes
     */
    private Set<String> getAllNodes(ExecutionContext context){
        Set<String> nodes = Collections.EMPTY_SET;
        /**
         * executor type
         */
        ExecutorType executorType = context.getExecutorType();
        switch (executorType){
            case WORKER:
                nodes = zookeeperNodeManager.getWorkerGroupNodes(context.getWorkerGroup());
                break;
            case CLIENT:
                break;
            default:
                throw new IllegalArgumentException("invalid executor type : " + executorType);

        }
        return nodes;
    }

    public NettyRemotingClient getNettyRemotingClient() {
        return nettyRemotingClient;
    }
}
