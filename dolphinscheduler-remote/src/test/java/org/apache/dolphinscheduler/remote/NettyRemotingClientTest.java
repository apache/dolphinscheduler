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

import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.remote.future.InvokeCallback;
import org.apache.dolphinscheduler.remote.future.ResponseFuture;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.Host;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

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
        server.registerProcessor(new NettyRequestProcessor() {

            @Override
            public void process(Channel channel, Message message) {
                channel.writeAndFlush(Pong.create(message.getOpaque()));
            }

            @Override
            public MessageType getCommandType() {
                return MessageType.PING;
            }
        });

        server.start();
        //
        final NettyClientConfig clientConfig = new NettyClientConfig();
        NettyRemotingClient client = new NettyRemotingClient(clientConfig);
        Message messagePing = Ping.create();
        try {
            Message response = client.sendSync(new Host("127.0.0.1", serverConfig.getListenPort()), messagePing, 2000);
            Assertions.assertEquals(messagePing.getOpaque(), response.getOpaque());
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
    public void testSendAsync() {
        NettyServerConfig serverConfig = new NettyServerConfig();

        NettyRemotingServer server = new NettyRemotingServer(serverConfig);
        server.registerProcessor(new NettyRequestProcessor() {

            @Override
            public void process(Channel channel, Message message) {
                channel.writeAndFlush(Pong.create(message.getOpaque()));
            }

            @Override
            public MessageType getCommandType() {
                return MessageType.PING;
            }
        });
        server.start();
        //
        final NettyClientConfig clientConfig = new NettyClientConfig();
        NettyRemotingClient client = new NettyRemotingClient(clientConfig);
        CountDownLatch latch = new CountDownLatch(1);
        Message messagePing = Ping.create();
        try {
            final AtomicLong opaque = new AtomicLong(0);
            client.sendAsync(new Host("127.0.0.1", serverConfig.getListenPort()), messagePing, 2000,
                    new InvokeCallback() {

                        @Override
                        public void operationComplete(ResponseFuture responseFuture) {
                            opaque.set(responseFuture.getOpaque());
                            latch.countDown();
                        }
                    });
            latch.await();
            Assertions.assertEquals(messagePing.getOpaque(), opaque.get());
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
            ping.writeByte(Message.MAGIC);
            ping.writeByte(MessageType.PING.ordinal());
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
        public static Message create() {
            Message message = new Message();
            message.setType(MessageType.PING);
            message.setBody(EMPTY_BODY_ARRAY);
            return message;
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
            ping.writeByte(Message.MAGIC);
            ping.writeByte(MessageType.PONG.ordinal());
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
        public static Message create(long opaque) {
            Message message = new Message(opaque);
            message.setType(MessageType.PONG);
            message.setBody(EMPTY_BODY_ARRAY);
            return message;
        }
    }
}
