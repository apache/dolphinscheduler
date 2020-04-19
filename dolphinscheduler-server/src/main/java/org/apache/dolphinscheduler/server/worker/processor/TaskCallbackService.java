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
        NettyRemoteChannel nettyRemoteChannel = REMOTE_CHANNELS.get(taskInstanceId);
        if(nettyRemoteChannel == null){
            throw new IllegalArgumentException("nettyRemoteChannel is empty, should call addRemoteChannel first");
        }
        if(nettyRemoteChannel.isActive()){
            return nettyRemoteChannel;
        }
        Channel newChannel = nettyRemotingClient.getChannel(nettyRemoteChannel.getHost());
        if(newChannel != null){
            return getRemoteChannel(newChannel, nettyRemoteChannel.getOpaque(), taskInstanceId);
        }
        logger.warn("original master : {} is not reachable, random select master", nettyRemoteChannel.getHost());
        Set<String> masterNodes = zookeeperRegistryCenter.getMasterNodesDirectly();
        while (Stopper.isRunning()) {
            if (CollectionUtils.isEmpty(masterNodes)) {
                logger.error("no available master node");
                ThreadUtils.sleep(SLEEP_TIME_MILLIS);
            }else {
                break;
            }
        }
        for(String masterNode : masterNodes){
            newChannel = nettyRemotingClient.getChannel(Host.of(masterNode));
            if(newChannel != null){
                return getRemoteChannel(newChannel, nettyRemoteChannel.getOpaque(), taskInstanceId);
            }
        }
        throw new IllegalStateException(String.format("all available master nodes : %s are not reachable", masterNodes));
    }

    private NettyRemoteChannel getRemoteChannel(Channel newChannel, long opaque, int taskInstanceId){
        NettyRemoteChannel remoteChannel = new NettyRemoteChannel(newChannel, opaque);
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
