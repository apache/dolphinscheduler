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

package org.apache.dolphinscheduler.server.worker.processor;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.dolphinscheduler.common.Constants.SLEEP_TIME_MILLIS;

/**
 *  taks callback service
 */
@Service
public class TaskCallbackService {

    private final Logger logger = LoggerFactory.getLogger(TaskCallbackService.class);
    private static final int [] RETRY_BACKOFF = { 1, 2, 3, 5, 10, 20, 40, 100, 100, 100, 100, 200, 200, 200 };

    /**
     *  remote channels
     */
    private static final ConcurrentHashMap<Integer, NettyRemoteChannel> REMOTE_CHANNELS = new ConcurrentHashMap<>();

    /**
     * zookeeper register center
     */
    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;


    /**
     * netty remoting client
     */
    private final NettyRemotingClient nettyRemotingClient;


    public TaskCallbackService(){
        final NettyClientConfig clientConfig = new NettyClientConfig();
        this.nettyRemotingClient = new NettyRemotingClient(clientConfig);
        this.nettyRemotingClient.registerProcessor(CommandType.DB_TASK_ACK, new DBTaskAckProcessor());
        this.nettyRemotingClient.registerProcessor(CommandType.DB_TASK_RESPONSE, new DBTaskResponseProcessor());
    }

    /**
     *  add callback channel
     * @param taskInstanceId taskInstanceId
     * @param channel  channel
     */
    public void addRemoteChannel(int taskInstanceId, NettyRemoteChannel channel){
        REMOTE_CHANNELS.put(taskInstanceId, channel);
    }

    /**
     *  get callback channel
     * @param taskInstanceId taskInstanceId
     * @return callback channel
     */
    private NettyRemoteChannel getRemoteChannel(int taskInstanceId){
        Channel newChannel;
        NettyRemoteChannel nettyRemoteChannel = REMOTE_CHANNELS.get(taskInstanceId);
        if(nettyRemoteChannel != null){
            if(nettyRemoteChannel.isActive()){
                return nettyRemoteChannel;
            }
            newChannel = nettyRemotingClient.getChannel(nettyRemoteChannel.getHost());
            if(newChannel != null){
                return getRemoteChannel(newChannel, nettyRemoteChannel.getOpaque(), taskInstanceId);
            }

        }

        Set<String> masterNodes = null;
        int ntries = 0;
        while (Stopper.isRunning()) {
            masterNodes = zookeeperRegistryCenter.getMasterNodesDirectly();
            if (CollectionUtils.isEmpty(masterNodes)) {
                logger.info("try {} times but not find any master for task : {}.",
                        ntries + 1,
                        taskInstanceId);
                masterNodes = null;
                ThreadUtils.sleep(pause(ntries++));
                continue;
            }
            logger.info("try {} times to find {} masters for task : {}.",
                    ntries + 1,
                    masterNodes.size(),
                    taskInstanceId);
            for (String masterNode : masterNodes) {
                newChannel = nettyRemotingClient.getChannel(Host.of(masterNode));
                if (newChannel != null) {
                    return getRemoteChannel(newChannel,taskInstanceId);
                }
            }
            masterNodes = null;
            ThreadUtils.sleep(pause(ntries++));
        }

        throw new IllegalStateException(String.format("all available master nodes : %s are not reachable for task: {}", masterNodes, taskInstanceId));
    }


    public int pause(int ntries){
        return SLEEP_TIME_MILLIS * RETRY_BACKOFF[ntries % RETRY_BACKOFF.length];
    }


    private NettyRemoteChannel getRemoteChannel(Channel newChannel, long opaque, int taskInstanceId){
        NettyRemoteChannel remoteChannel = new NettyRemoteChannel(newChannel, opaque);
        addRemoteChannel(taskInstanceId, remoteChannel);
        return remoteChannel;
    }

    private NettyRemoteChannel getRemoteChannel(Channel newChannel, int taskInstanceId){
        NettyRemoteChannel remoteChannel = new NettyRemoteChannel(newChannel);
        addRemoteChannel(taskInstanceId, remoteChannel);
        return remoteChannel;
    }

    /**
     *  remove callback channels
     * @param taskInstanceId taskInstanceId
     */
    public void remove(int taskInstanceId){
        REMOTE_CHANNELS.remove(taskInstanceId);
    }

    /**
     *  send ack
     * @param taskInstanceId taskInstanceId
     * @param command command
     */
    public void sendAck(int taskInstanceId, Command command){
        NettyRemoteChannel nettyRemoteChannel = getRemoteChannel(taskInstanceId);
        nettyRemoteChannel.writeAndFlush(command);
    }

    /**
     *  send result
     *
     * @param taskInstanceId taskInstanceId
     * @param command command
     */
    public void sendResult(int taskInstanceId, Command command){
        NettyRemoteChannel nettyRemoteChannel = getRemoteChannel(taskInstanceId);
        nettyRemoteChannel.writeAndFlush(command).addListener(new ChannelFutureListener(){

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()){
                    remove(taskInstanceId);
                    return;
                }
            }
        });
    }
}
