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

package org.apache.dolphinscheduler.remote.utils;

import org.apache.dolphinscheduler.common.utils.NetUtils;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * channel utils
 */
public class ChannelUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelUtils.class);

    private ChannelUtils() {
        throw new IllegalStateException(ChannelUtils.class.getName());
    }

    /**
     * get local address
     *
     * @param channel channel
     * @return local address
     */
    public static String getLocalAddress(Channel channel) {
        return NetUtils.getHost(((InetSocketAddress) channel.localAddress()).getAddress());
    }

    /**
     * get remote address
     *
     * @param channel channel
     * @return remote address
     */
    public static String getRemoteAddress(Channel channel) {
        return toAddress(channel).getAddress();
    }

    /**
     * channel to address
     *
     * @param channel channel
     * @return address
     */
    public static Host toAddress(Channel channel) {
        InetSocketAddress socketAddress = ((InetSocketAddress) channel.remoteAddress());
        if (socketAddress == null) {
            // the remote channel already closed
            LOGGER.warn("The channel is already closed");
            return Host.EMPTY;
        }
        return new Host(NetUtils.getHost(socketAddress.getAddress()), socketAddress.getPort());
    }

}
