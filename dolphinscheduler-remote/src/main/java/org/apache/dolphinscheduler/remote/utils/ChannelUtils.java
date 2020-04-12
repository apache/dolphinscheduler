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

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 *  channel utils
 */
public class ChannelUtils {

    /**
     *  get local address
     *
     * @param channel channel
     * @return local address
     */
    public static String getLocalAddress(Channel channel){
        return ((InetSocketAddress)channel.localAddress()).getAddress().getHostAddress();
    }

    /**
     *  get remote address
     * @param channel channel
     * @return remote address
     */
    public static String getRemoteAddress(Channel channel){
        return ((InetSocketAddress)channel.remoteAddress()).getAddress().getHostAddress();
    }

    /**
     *  channel to address
     * @param channel channel
     * @return address
     */
    public static Host toAddress(Channel channel){
        InetSocketAddress socketAddress = ((InetSocketAddress)channel.remoteAddress());
        return new Host(socketAddress.getAddress().getHostAddress(), socketAddress.getPort());
    }

}
