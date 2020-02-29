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

import org.apache.commons.collections.CollectionUtils;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskRequestCommand;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.server.master.processor.TaskAckProcessor;
import org.apache.dolphinscheduler.server.master.processor.TaskResponseProcessor;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    public NettyExecutorManager(){
        final NettyClientConfig clientConfig = new NettyClientConfig();
        this.nettyRemotingClient = new NettyRemotingClient(clientConfig);
        /**
         * register EXECUTE_TASK_RESPONSE command type TaskResponseProcessor
         * register EXECUTE_TASK_ACK command type TaskAckProcessor
         */
        this.nettyRemotingClient.registerProcessor(CommandType.EXECUTE_TASK_RESPONSE, new TaskResponseProcessor());
        this.nettyRemotingClient.registerProcessor(CommandType.EXECUTE_TASK_ACK, new TaskAckProcessor());
    }

    /**
     *  execute logic
     * @param context context
     * @throws ExecuteException
     */
    @Override
    public Boolean execute(ExecutionContext context) throws ExecuteException {

        /**
         *  all nodes
         */
        Set<String> allNodes = getAllNodes(context);

        /**
         * fail nodes
         */
        Set<String> failNodeSet = new HashSet<>();

        /**
         *  build command accord executeContext
         */
        Command command = buildCommand(context);

        /**
         * execute task host
         */
        Host host = context.getHost();
        boolean success = false;
        while (!success) {
            try {
                doExecute(host,command);
                success = true;
                context.setHost(host);
            } catch (ExecuteException ex) {
                logger.error(String.format("execute context : %s error", context.getContext()), ex);
                try {
                    failNodeSet.add(host.getAddress());
                    Set<String> tmpAllIps = new HashSet<>(allNodes);
                    Collection<String> remained = CollectionUtils.subtract(tmpAllIps, failNodeSet);
                    if (remained != null && remained.size() > 0) {
                        host = Host.of(remained.iterator().next());
                        logger.error("retry execute context : {} host : {}", context.getContext(), host);
                    } else {
                        throw new ExecuteException("fail after try all nodes");
                    }
                } catch (Throwable t) {
                    throw new ExecuteException("fail after try all nodes");
                }
            }
        }

        return success;
    }

    /**
     *  build command
     * @param context context
     * @return command
     */
    private Command buildCommand(ExecutionContext context) {
        ExecuteTaskRequestCommand requestCommand = new ExecuteTaskRequestCommand();
        ExecutorType executorType = context.getExecutorType();
        switch (executorType){
            case WORKER:
                TaskExecutionContext taskExecutionContext = context.getContext();
                requestCommand.setTaskExecutionContext(FastJsonSerializer.serializeToString(taskExecutionContext));
                break;
            case CLIENT:
                break;
            default:
                throw new IllegalArgumentException("invalid executor type : " + executorType);

        }
        return requestCommand.convert2Command();
    }

    /**
     *  execute logic
     * @param host host
     * @param command command
     * @throws ExecuteException
     */
    private void doExecute(final Host host, final Command command) throws ExecuteException {
        /**
         * retry count，default retry 3
         */
        int retryCount = 3;
        boolean success = false;
        do {
            try {
                nettyRemotingClient.send(host, command);
                success = true;
            } catch (Exception ex) {
                logger.error(String.format("send command : %s to %s error", command, host), ex);
                retryCount--;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {}
            }
        } while (retryCount >= 0 && !success);

        if (!success) {
            throw new ExecuteException(String.format("send command : %s to %s error", command, host));
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
}
