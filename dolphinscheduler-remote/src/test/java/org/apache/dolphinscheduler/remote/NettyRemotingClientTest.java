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

package org.apache.dolphinscheduler.remote;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.remote.future.InvokeCallback;
import org.apache.dolphinscheduler.remote.future.ResponseFuture;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  netty remote client test
 */
public class NettyRemotingClientTest {

    /**
     *  test send sync
     */
    @Test
    public void testSendSync() {
        NettyServerConfig serverConfig = new NettyServerConfig();

        NettyRemotingServer server = new NettyRemotingServer(serverConfig);
        server.registerProcessor(CommandType.PING, new NettyRequestProcessor() {
            @Override
            public void process(Channel channel, Command command) {
                channel.writeAndFlush(Pong.create(command.getOpaque()));
            }
        });


        server.start();
        //
        final NettyClientConfig clientConfig = new NettyClientConfig();
        NettyRemotingClient client = new NettyRemotingClient(clientConfig);
        Command commandPing = Ping.create();
        try {
            Command response = client.sendSync(new Host("127.0.0.1", serverConfig.getListenPort()), commandPing, 2000);
            Assertions.assertEquals(commandPing.getOpaque(), response.getOpaque());
        } catch (Exception e) {
            e.printStackTrace();
        }
        server.close();
        client.close();
    }

    /**
     *  test sned async
     */
    @Test
    public void testSendAsync(){
        NettyServerConfig serverConfig = new NettyServerConfig();

        NettyRemotingServer server = new NettyRemotingServer(serverConfig);
        server.registerProcessor(CommandType.PING, new NettyRequestProcessor() {
            @Override
            public void process(Channel channel, Command command) {
                channel.writeAndFlush(Pong.create(command.getOpaque()));
            }
        });
        server.start();
        //
        final NettyClientConfig clientConfig = new NettyClientConfig();
        NettyRemotingClient client = new NettyRemotingClient(clientConfig);
        CountDownLatch latch = new CountDownLatch(1);
        Command commandPing = Ping.create();
        try {
            final AtomicLong opaque = new AtomicLong(0);
            client.sendAsync(new Host("127.0.0.1", serverConfig.getListenPort()), commandPing, 2000, new InvokeCallback() {
                @Override
                public void operationComplete(ResponseFuture responseFuture) {
                    opaque.set(responseFuture.getOpaque());
                    latch.countDown();
                }
            });
            latch.await();
            Assertions.assertEquals(commandPing.getOpaque(), opaque.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        server.close();
        client.close();
    }

    private static class Ping implements Serializable {

        /**
         * ping body
         */
        protected static final ByteBuf EMPTY_BODY = Unpooled.EMPTY_BUFFER;

        /**
         * request command body
         */
        private static final byte[] EMPTY_BODY_ARRAY = new byte[0];

        private static final ByteBuf PING_BUF;

        static {
            ByteBuf ping = Unpooled.buffer();
            ping.writeByte(Command.MAGIC);
            ping.writeByte(CommandType.PING.ordinal());
            ping.writeLong(0);
            ping.writeInt(0);
            ping.writeBytes(EMPTY_BODY);
            PING_BUF = Unpooled.unreleasableBuffer(ping).asReadOnly();
        }

        /**
         * ping content
         *
         * @return result
         */
        public static ByteBuf pingContent() {
            return PING_BUF.duplicate();
        }

        /**
         * create ping command
         *
         * @return command
         */
        public static Command create() {
            Command command = new Command();
            command.setType(CommandType.PING);
            command.setBody(EMPTY_BODY_ARRAY);
            return command;
        }
    }

    private static class Pong implements Serializable {

        /**
         * pong body
         */
        protected static final ByteBuf EMPTY_BODY = Unpooled.EMPTY_BUFFER;

        /**
         * pong command body
         */
        private static final byte[] EMPTY_BODY_ARRAY = new byte[0];

        /**
         * pong byte buffer
         */
        private static final ByteBuf PONG_BUF;

        static {
            ByteBuf ping = Unpooled.buffer();
            ping.writeByte(Command.MAGIC);
            ping.writeByte(CommandType.PONG.ordinal());
            ping.writeLong(0);
            ping.writeInt(0);
            ping.writeBytes(EMPTY_BODY);
            PONG_BUF = Unpooled.unreleasableBuffer(ping).asReadOnly();
        }

        /**
         * pong content
         *
         * @return result
         */
        public static ByteBuf pongContent() {
            return PONG_BUF.duplicate();
        }

        /**
         * package pong command
         *
         * @param opaque request unique identification
         * @return command
         */
        public static Command create(long opaque) {
            Command command = new Command(opaque);
            command.setType(CommandType.PONG);
            command.setBody(EMPTY_BODY_ARRAY);
            return command;
        }
    }
}
