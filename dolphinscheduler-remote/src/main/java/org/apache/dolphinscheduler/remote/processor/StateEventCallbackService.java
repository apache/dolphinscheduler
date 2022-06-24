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

package org.apache.dolphinscheduler.remote.processor;

import static org.apache.dolphinscheduler.common.Constants.SLEEP_TIME_MILLIS;

import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.utils.Host;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.netty.channel.Channel;

/**
 * task callback service
 */
@Service
public class StateEventCallbackService {

    private final Logger logger = LoggerFactory.getLogger(StateEventCallbackService.class);
    private static final int[] RETRY_BACKOFF = {1, 2, 3, 5, 10, 20, 40, 100, 100, 100, 100, 200, 200, 200};

    /**
     * remote channels
     */
    private static final ConcurrentHashMap<String, NettyRemoteChannel> REMOTE_CHANNELS = new ConcurrentHashMap<>();

    /**
     * netty remoting client
     */
    private final NettyRemotingClient nettyRemotingClient;

    public StateEventCallbackService() {
        final NettyClientConfig clientConfig = new NettyClientConfig();
        this.nettyRemotingClient = new NettyRemotingClient(clientConfig);
    }

    /**
     * add callback channel
     *
     * @param channel channel
     */
    public void addRemoteChannel(String host, NettyRemoteChannel channel) {
        REMOTE_CHANNELS.put(host, channel);
    }

    /**
     * get callback channel
     *
     * @param host
     * @return callback channel
     */
    private NettyRemoteChannel newRemoteChannel(Host host) {
        Channel newChannel;
        NettyRemoteChannel nettyRemoteChannel = REMOTE_CHANNELS.get(host.getAddress());
        if (nettyRemoteChannel != null) {
            if (nettyRemoteChannel.isActive()) {
                return nettyRemoteChannel;
            }
        }
        newChannel = nettyRemotingClient.getChannel(host);
        if (newChannel != null) {
            return newRemoteChannel(newChannel, host.getAddress());
        }
        return null;
    }

    public long pause(int ntries) {
        return SLEEP_TIME_MILLIS * RETRY_BACKOFF[ntries % RETRY_BACKOFF.length];
    }

    private NettyRemoteChannel newRemoteChannel(Channel newChannel, long opaque, String host) {
        NettyRemoteChannel remoteChannel = new NettyRemoteChannel(newChannel, opaque);
        addRemoteChannel(host, remoteChannel);
        return remoteChannel;
    }

    private NettyRemoteChannel newRemoteChannel(Channel newChannel, String host) {
        NettyRemoteChannel remoteChannel = new NettyRemoteChannel(newChannel);
        addRemoteChannel(host, remoteChannel);
        return remoteChannel;
    }

    /**
     * remove callback channels
     */
    public void remove(String host) {
        REMOTE_CHANNELS.remove(host);
    }

    /**
     * send result
     *
     * @param command command
     */
    public void sendResult(String address, int port, Command command) {
        logger.info("send result, host:{}, command:{}", address, command.toString());
        Host host = new Host(address, port);
        NettyRemoteChannel nettyRemoteChannel = newRemoteChannel(host);
        if (nettyRemoteChannel != null) {
            nettyRemoteChannel.writeAndFlush(command);
        }
    }

    public void sendResult(Host host, Command command) {
        logger.info("send result, host:{}, command:{}", host.getAddress(), command.toString());
        NettyRemoteChannel nettyRemoteChannel = newRemoteChannel(host);
        if (nettyRemoteChannel != null) {
            nettyRemoteChannel.writeAndFlush(command);
        }
    }
}
