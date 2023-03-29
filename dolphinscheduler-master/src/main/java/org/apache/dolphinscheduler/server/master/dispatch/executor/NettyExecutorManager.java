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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.WorkerGroupNotFoundException;
import org.apache.dolphinscheduler.server.master.registry.ServerNodeManager;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * netty executor manager
 */
@Service
@Slf4j
public class NettyExecutorManager extends AbstractExecutorManager<Boolean> {

    /**
     * server node manager
     */
    @Autowired
    private ServerNodeManager serverNodeManager;

    @Autowired
    private List<NettyRequestProcessor> nettyRequestProcessors;

    /**
     * netty remote client
     */
    private final NettyRemotingClient nettyRemotingClient;

    /**
     * constructor
     */
    public NettyExecutorManager() {
        final NettyClientConfig clientConfig = new NettyClientConfig();
        this.nettyRemotingClient = new NettyRemotingClient(clientConfig);
    }

    @PostConstruct
    public void init() {
        for (NettyRequestProcessor nettyRequestProcessor : nettyRequestProcessors) {
            this.nettyRemotingClient.registerProcessor(nettyRequestProcessor);
        }
    }

    /**
     * execute logic
     *
     * @param context context
     * @return result
     * @throws ExecuteException if error throws ExecuteException
     */
    @Override
    public void execute(ExecutionContext context) throws ExecuteException {
        // all nodes
        Set<String> allNodes = getAllNodes(context);
        // fail nodes
        Set<String> failNodeSet = new HashSet<>();
        // build command accord executeContext
        Message message = context.getMessage();
        // execute task host
        Host host = context.getHost();
        for (int i = 0; i < allNodes.size(); i++) {
            try {
                doExecute(host, message);
                context.setHost(host);
                // We set the host to taskInstance to avoid when the worker down, this taskInstance may not be
                // failovered, due to the taskInstance's host
                // is not belongs to the down worker ISSUE-10842.
                context.getTaskInstance().setHost(host.getAddress());
                return;
            } catch (ExecuteException ex) {
                log.error("Execute command {} error", message, ex);
                try {
                    failNodeSet.add(host.getAddress());
                    Set<String> tmpAllIps = new HashSet<>(allNodes);
                    Collection<String> remained = CollectionUtils.subtract(tmpAllIps, failNodeSet);
                    if (CollectionUtils.isNotEmpty(remained)) {
                        host = Host.of(remained.iterator().next());
                        log.error("retry execute command : {} host : {}", message, host);
                    } else {
                        throw new ExecuteException("fail after try all nodes");
                    }
                } catch (Throwable t) {
                    throw new ExecuteException("fail after try all nodes");
                }
            }
        }
    }

    @Override
    public void executeDirectly(ExecutionContext context) throws ExecuteException {
        Host host = context.getHost();
        doExecute(host, context.getMessage());
    }

    /**
     * execute logic
     *
     * @param host host
     * @param message command
     * @throws ExecuteException if error throws ExecuteException
     */
    public void doExecute(final Host host, final Message message) throws ExecuteException {
        // retry count，default retry 3
        int retryCount = 3;
        boolean success = false;
        do {
            try {
                nettyRemotingClient.send(host, message);
                success = true;
            } catch (Exception ex) {
                log.error("Send command to {} error, command: {}", host, message, ex);
                retryCount--;
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
            }
        } while (retryCount >= 0 && !success);

        if (!success) {
            throw new ExecuteException(String.format("send command : %s to %s error", message, host));
        }
    }

    /**
     * get all nodes
     *
     * @param context context
     * @return nodes
     */
    private Set<String> getAllNodes(ExecutionContext context) throws WorkerGroupNotFoundException {
        Set<String> nodes = Collections.emptySet();
        /**
         * executor type
         */
        ExecutorType executorType = context.getExecutorType();
        switch (executorType) {
            case WORKER:
                nodes = serverNodeManager.getWorkerGroupNodes(context.getWorkerGroup());
                break;
            case CLIENT:
                break;
            default:
                throw new IllegalArgumentException("invalid executor type : " + executorType);

        }
        return nodes;
    }

}
