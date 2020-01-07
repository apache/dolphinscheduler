package org.apache.dolphinscheduler.remote.utils;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * @Author: Tboy
 */
public class ChannelUtils {

    public static String getLocalAddress(Channel channel){
        return ((InetSocketAddress)channel.localAddress()).getAddress().getHostAddress();
    }

    public static String getRemoteAddress(Channel channel){
        return ((InetSocketAddress)channel.remoteAddress()).getAddress().getHostAddress();
    }

    public static Address toAddress(Channel channel){
        InetSocketAddress socketAddress = ((InetSocketAddress)channel.remoteAddress());
        return new Address(socketAddress.getAddress().getHostAddress(), socketAddress.getPort());
    }

}
