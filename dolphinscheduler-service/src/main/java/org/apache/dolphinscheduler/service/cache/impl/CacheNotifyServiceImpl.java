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

package org.apache.dolphinscheduler.service.cache.impl;

import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.processor.NettyRemoteChannel;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.cache.CacheNotifyService;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.netty.channel.Channel;

/**
 * cache notify service
 */
@Service
public class CacheNotifyServiceImpl implements CacheNotifyService {

    private final Logger logger = LoggerFactory.getLogger(CacheNotifyServiceImpl.class);

    @Autowired
    private RegistryClient registryClient;

    /**
     * remote channels
     */
    private static final ConcurrentHashMap<Host, NettyRemoteChannel> REMOTE_CHANNELS = new ConcurrentHashMap<>();

    /**
     * netty remoting client
     */
    private final NettyRemotingClient nettyRemotingClient;

    public CacheNotifyServiceImpl() {
        final NettyClientConfig clientConfig = new NettyClientConfig();
        this.nettyRemotingClient = new NettyRemotingClient(clientConfig);
    }

    /**
     * add channel
     *
     * @param channel channel
     */
    private void cache(Host host, NettyRemoteChannel channel) {
        REMOTE_CHANNELS.put(host, channel);
    }

    /**
     * remove channel
     */
    private void remove(Host host) {
        REMOTE_CHANNELS.remove(host);
    }

    /**
     * get remote channel
     *
     * @return netty remote channel
     */
    private NettyRemoteChannel getRemoteChannel(Host host) {
        NettyRemoteChannel nettyRemoteChannel = REMOTE_CHANNELS.get(host);
        if (nettyRemoteChannel != null) {
            if (nettyRemoteChannel.isActive()) {
                return nettyRemoteChannel;
            } else {
                this.remove(host);
            }
        }

        Channel channel = nettyRemotingClient.getChannel(host);
        if (channel == null) {
            return null;
        }

        NettyRemoteChannel remoteChannel = new NettyRemoteChannel(channel);
        this.cache(host, remoteChannel);
        return remoteChannel;
    }

    /**
     * send result to master
     *
     * @param command command
     */
    @Override
    public void notifyMaster(Command command) {
        logger.info("send result, command:{}", command.toString());
        try {
            List<Server> serverList = registryClient.getServerList(NodeType.MASTER);
            if (CollectionUtils.isEmpty(serverList)) {
                return;
            }

            for (Server server : serverList) {
                Host host = new Host(server.getHost(), server.getPort());
                NettyRemoteChannel nettyRemoteChannel = getRemoteChannel(host);
                if (nettyRemoteChannel == null) {
                    continue;
                }
                nettyRemoteChannel.writeAndFlush(command);
            }
        } catch (Exception e) {
            logger.error("notify master error", e);
        }
    }
}
