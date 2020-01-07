package org.apache.dolphinscheduler.remote.processor;

import io.netty.channel.Channel;
import org.apache.dolphinscheduler.remote.command.Command;

/**
 * @Author: Tboy
 */
public interface NettyRequestProcessor {

    void process(final Channel channel, final Command command);
}
