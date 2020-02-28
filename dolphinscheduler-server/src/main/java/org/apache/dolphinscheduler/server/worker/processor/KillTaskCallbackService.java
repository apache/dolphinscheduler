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
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskAckCommand;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskResponseCommand;
import org.apache.dolphinscheduler.remote.command.KillTaskResponseCommand;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 *  taks callback service
 */
public class KillTaskCallbackService {

    private final Logger logger = LoggerFactory.getLogger(KillTaskCallbackService.class);

    /**
     *  remote channels
     */
    private static final ConcurrentHashMap<Integer, NettyRemoteChannel> REMOTE_CHANNELS = new ConcurrentHashMap<>();

    /**
     * netty remoting client
     */
    private final NettyRemotingClient nettyRemotingClient;


    public KillTaskCallbackService(){
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
    public NettyRemoteChannel getRemoteChannel(int taskInstanceId){
        NettyRemoteChannel nettyRemoteChannel = REMOTE_CHANNELS.get(taskInstanceId);
        if(nettyRemoteChannel.isActive()){
            return nettyRemoteChannel;
        }
        Channel newChannel = nettyRemotingClient.getChannel(nettyRemoteChannel.getHost());
        if(newChannel != null){
            NettyRemoteChannel remoteChannel = new NettyRemoteChannel(newChannel, nettyRemoteChannel.getOpaque());
            addRemoteChannel(taskInstanceId, remoteChannel);
            return remoteChannel;
        }
        return null;
    }

    /**
     *  remove callback channels
     * @param taskInstanceId taskInstanceId
     */
    public void remove(int taskInstanceId){
        REMOTE_CHANNELS.remove(taskInstanceId);
    }

    /**
     *  send result
     *
     * @param taskInstanceId taskInstanceId
     * @param killTaskResponseCommand killTaskResponseCommand
     */
    public void sendKillResult(int taskInstanceId, KillTaskResponseCommand killTaskResponseCommand){
        NettyRemoteChannel nettyRemoteChannel = getRemoteChannel(taskInstanceId);
        if(nettyRemoteChannel == null){
            //TODO
        } else{
            nettyRemoteChannel.writeAndFlush(killTaskResponseCommand.convert2Command()).addListener(new ChannelFutureListener(){

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
}
