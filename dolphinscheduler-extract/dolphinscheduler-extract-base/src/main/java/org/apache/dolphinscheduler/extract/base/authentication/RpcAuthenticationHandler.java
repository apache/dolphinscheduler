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

package org.apache.dolphinscheduler.extract.base.authentication;

import org.apache.dolphinscheduler.extract.base.exception.RemoteException;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;

import lombok.extern.slf4j.Slf4j;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;

/**
 * A fixed-size handshake before RPC framing. It authenticates cluster membership;
 * it does not encrypt or integrity-protect subsequent RPC traffic.
 * Frames contain a four-byte magic, a one-byte version and a one-byte type:
 * server challenge (32-byte nonce), client proof (32-byte nonce + 32-byte MAC),
 * server proof (32-byte MAC). The MAC binds the protocol context, direction and both nonces.
 */
@Slf4j
public final class RpcAuthenticationHandler extends ByteToMessageDecoder {

    private static final int MAGIC = 0x44534155;
    private static final byte VERSION = 1;
    private static final byte CHALLENGE = 1;
    private static final byte CLIENT_PROOF = 2;
    private static final byte SERVER_PROOF = 3;
    private static final int HEADER_LENGTH = 6;
    private static final int NONCE_LENGTH = 32;
    private static final byte[] CONTEXT = "DolphinScheduler RPC authentication v1".getBytes(StandardCharsets.US_ASCII);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AttributeKey<Promise<Void>> AUTHENTICATION = AttributeKey.valueOf("rpc.authentication");

    private final RpcAuthenticationConfig config;
    private final boolean server;
    private byte[] serverNonce;
    private byte[] clientNonce;
    private Promise<Void> authentication;
    private ScheduledFuture<?> timeout;

    public RpcAuthenticationHandler(RpcAuthenticationConfig config, boolean server) {
        this.config = config;
        this.server = server;
    }

    public static Future<Void> authenticationFuture(Channel channel) {
        return channel.attr(AUTHENTICATION).get();
    }

    public static boolean isAuthenticated(Channel channel) {
        Future<Void> future = authenticationFuture(channel);
        return future == null || future.isSuccess();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        authentication = ctx.executor().newPromise();
        ctx.channel().attr(AUTHENTICATION).set(authentication);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        timeout = ctx.executor().schedule(() -> fail(ctx, "RPC authentication timed out"),
                config.getTimeoutMillis(), TimeUnit.MILLISECONDS);
        if (server) {
            serverNonce = nonce();
            write(ctx, CHALLENGE, serverNonce);
        }
        ctx.fireChannelActive();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (authentication.isDone()) {
            return;
        }
        if (in.readableBytes() < HEADER_LENGTH) {
            return;
        }
        int index = in.readerIndex();
        byte expectedType = server ? CLIENT_PROOF : serverNonce == null ? CHALLENGE : SERVER_PROOF;
        if (in.getInt(index) != MAGIC || in.getByte(index + 4) != VERSION || in.getByte(index + 5) != expectedType) {
            fail(ctx, "RPC authentication failed");
            return;
        }
        int payloadLength = expectedType == CLIENT_PROOF ? NONCE_LENGTH * 2 : NONCE_LENGTH;
        if (in.readableBytes() < HEADER_LENGTH + payloadLength) {
            return;
        }
        in.skipBytes(HEADER_LENGTH);
        if (expectedType == CHALLENGE) {
            serverNonce = new byte[NONCE_LENGTH];
            in.readBytes(serverNonce);
            clientNonce = nonce();
            byte[] proof = proof(CLIENT_PROOF);
            ByteBuf payload = ctx.alloc().buffer(NONCE_LENGTH * 2);
            payload.writeBytes(clientNonce).writeBytes(proof);
            write(ctx, CLIENT_PROOF, payload);
        } else {
            if (server) {
                clientNonce = new byte[NONCE_LENGTH];
                in.readBytes(clientNonce);
            }
            byte[] receivedProof = new byte[NONCE_LENGTH];
            in.readBytes(receivedProof);
            if (!MessageDigest.isEqual(proof(expectedType), receivedProof)) {
                fail(ctx, "RPC authentication failed");
                return;
            }
            if (server) {
                write(ctx, SERVER_PROOF, proof(SERVER_PROOF));
            }
            timeout.cancel(false);
            authentication.setSuccess(null);
            // ByteToMessageDecoder forwards any remaining bytes to the RPC decoder on removal.
            ctx.pipeline().remove(this);
        }
    }

    private byte[] proof(byte role) throws GeneralSecurityException {
        Mac mac = config.newMac();
        mac.update(CONTEXT);
        mac.update(role);
        mac.update(serverNonce);
        return mac.doFinal(clientNonce);
    }

    private static byte[] nonce() {
        byte[] nonce = new byte[NONCE_LENGTH];
        RANDOM.nextBytes(nonce);
        return nonce;
    }

    private void write(ChannelHandlerContext ctx, byte type, byte[] payload) {
        write(ctx, type, ctx.alloc().buffer(payload.length).writeBytes(payload));
    }

    private void write(ChannelHandlerContext ctx, byte type, ByteBuf payload) {
        ByteBuf message = ctx.alloc().buffer(HEADER_LENGTH + payload.readableBytes());
        try {
            message.writeInt(MAGIC).writeByte(VERSION).writeByte(type).writeBytes(payload);
        } finally {
            payload.release();
        }
        ctx.writeAndFlush(message).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    private void fail(ChannelHandlerContext ctx, String message) {
        if (authentication.tryFailure(new RemoteException(message))) {
            log.warn("{} for {}", message, ctx.channel().remoteAddress());
        }
        if (timeout != null) {
            timeout.cancel(false);
        }
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        fail(ctx, "RPC connection closed before authentication completed");
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        fail(ctx, "RPC authentication failed");
    }
}
