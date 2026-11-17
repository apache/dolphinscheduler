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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import org.apache.dolphinscheduler.extract.base.protocal.HeartBeatTransporter;
import org.apache.dolphinscheduler.extract.base.protocal.Transporter;
import org.apache.dolphinscheduler.extract.base.protocal.TransporterDecoder;
import org.apache.dolphinscheduler.extract.base.protocal.TransporterEncoder;

import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;

class RpcAuthenticationHandlerTest {

    private EmbeddedChannel server;
    private EmbeddedChannel client;
    private RpcAuthenticationConfig config;

    @BeforeEach
    void setUp() throws Exception {
        config = RpcAuthenticationConfig.fromSecret(Base64.getEncoder().encodeToString(new byte[32]), 1000);
        server = new EmbeddedChannel(new RpcAuthenticationHandler(config, true), new TransporterDecoder());
        client = new EmbeddedChannel(new RpcAuthenticationHandler(config, false));
    }

    @AfterEach
    void tearDown() {
        server.finishAndReleaseAll();
        client.finishAndReleaseAll();
    }

    @Test
    void authenticatesBothPeersBeforePassingBusinessTraffic() {
        assertFalse(RpcAuthenticationHandler.isAuthenticated(server));
        assertFalse(RpcAuthenticationHandler.isAuthenticated(client));
        client.writeInbound((ByteBuf) server.readOutbound());
        server.writeInbound((ByteBuf) client.readOutbound());
        assertTrue(RpcAuthenticationHandler.isAuthenticated(server));
        assertFalse(RpcAuthenticationHandler.isAuthenticated(client));
        client.writeInbound((ByteBuf) server.readOutbound());
        assertTrue(RpcAuthenticationHandler.isAuthenticated(client));
        server.writeInbound(heartbeat());
        Transporter message = server.readInbound();
        assertEquals(HeartBeatTransporter.METHOD_IDENTIFY, message.getHeader().getMethodIdentifier());
    }

    @Test
    void acceptsFragmentedHandshakeAndCoalescedBusinessFrame() {
        ByteBuf challenge = server.readOutbound();
        while (challenge.isReadable()) {
            client.writeInbound(challenge.readRetainedSlice(1));
        }
        challenge.release();
        ByteBuf proof = client.readOutbound();
        ByteBuf combined = Unpooled.wrappedBuffer(proof, heartbeat());
        server.writeInbound(combined);
        assertTrue(RpcAuthenticationHandler.isAuthenticated(server));
        Transporter message = server.readInbound();
        assertEquals(HeartBeatTransporter.METHOD_IDENTIFY, message.getHeader().getMethodIdentifier());
        client.writeInbound((ByteBuf) server.readOutbound());
        assertTrue(RpcAuthenticationHandler.isAuthenticated(client));
    }

    @Test
    void rejectsBusinessFrameBeforeAuthentication() {
        server.writeInbound(heartbeat());
        assertFalse(server.isActive());
        assertFalse(RpcAuthenticationHandler.isAuthenticated(server));
        assertNull(server.readInbound());
    }

    @Test
    void rejectsTamperedClientProof() {
        client.writeInbound((ByteBuf) server.readOutbound());
        ByteBuf proof = client.readOutbound();
        proof.setByte(proof.writerIndex() - 1, proof.getByte(proof.writerIndex() - 1) ^ 1);
        server.writeInbound(proof);
        assertFalse(server.isActive());
        assertFalse(RpcAuthenticationHandler.isAuthenticated(server));
    }

    @Test
    void rejectsTamperedServerProof() {
        client.writeInbound((ByteBuf) server.readOutbound());
        server.writeInbound((ByteBuf) client.readOutbound());
        ByteBuf proof = server.readOutbound();
        proof.setByte(proof.writerIndex() - 1, proof.getByte(proof.writerIndex() - 1) ^ 1);
        client.writeInbound(proof);
        assertFalse(client.isActive());
        assertFalse(RpcAuthenticationHandler.isAuthenticated(client));
    }

    @Test
    void rejectsProofReplayedOnAnotherConnection() {
        client.writeInbound((ByteBuf) server.readOutbound());
        ByteBuf proof = client.readOutbound();
        EmbeddedChannel anotherServer = new EmbeddedChannel(new RpcAuthenticationHandler(config, true));
        try {
            ReferenceCountUtil.release(anotherServer.readOutbound());
            anotherServer.writeInbound(proof);
            assertFalse(anotherServer.isActive());
            assertFalse(RpcAuthenticationHandler.isAuthenticated(anotherServer));
        } finally {
            anotherServer.finishAndReleaseAll();
        }
    }

    @Test
    void rejectsServerProofReplayedToAnotherClient() {
        client.writeInbound((ByteBuf) server.readOutbound());
        server.writeInbound((ByteBuf) client.readOutbound());
        ByteBuf oldProof = server.readOutbound();
        EmbeddedChannel anotherServer = new EmbeddedChannel(new RpcAuthenticationHandler(config, true));
        EmbeddedChannel anotherClient = new EmbeddedChannel(new RpcAuthenticationHandler(config, false));
        try {
            anotherClient.writeInbound((ByteBuf) anotherServer.readOutbound());
            ReferenceCountUtil.release(anotherClient.readOutbound());
            anotherClient.writeInbound(oldProof);
            assertFalse(anotherClient.isActive());
            assertFalse(RpcAuthenticationHandler.isAuthenticated(anotherClient));
        } finally {
            anotherServer.finishAndReleaseAll();
            anotherClient.finishAndReleaseAll();
        }
    }

    @Test
    void rejectsReflectedProofWithWrongRole() throws Exception {
        ByteBuf challenge = server.readOutbound();
        byte[] serverNonce = new byte[32];
        challenge.getBytes(6, serverNonce);
        client.writeInbound(challenge);
        ByteBuf proof = client.readOutbound();
        byte[] clientNonce = new byte[32];
        proof.getBytes(6, clientNonce);
        javax.crypto.Mac mac = config.newMac();
        mac.update("DolphinScheduler RPC authentication v1".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        mac.update((byte) 3);
        mac.update(serverNonce);
        proof.setBytes(38, mac.doFinal(clientNonce));
        server.writeInbound(proof);
        assertFalse(server.isActive());
        assertFalse(RpcAuthenticationHandler.isAuthenticated(server));
    }

    @Test
    void rejectsUnsupportedVersion() {
        ByteBuf challenge = server.readOutbound();
        challenge.setByte(4, 2);
        client.writeInbound(challenge);
        assertFalse(client.isActive());
        assertFalse(RpcAuthenticationHandler.isAuthenticated(client));
    }

    @Test
    void logsLocalClientAuthenticationException() throws Exception {
        assertLocalAuthenticationExceptionIsLogged(false);
    }

    @Test
    void logsLocalServerAuthenticationException() throws Exception {
        assertLocalAuthenticationExceptionIsLogged(true);
    }

    private void assertLocalAuthenticationExceptionIsLogged(boolean serverRole) throws Exception {
        RpcAuthenticationConfig failingConfig = spy(config);
        GeneralSecurityException cause = new GeneralSecurityException("HMAC provider failure");
        doThrow(cause).when(failingConfig).newMac();
        Logger logger = (Logger) LoggerFactory.getLogger(RpcAuthenticationHandler.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        EmbeddedChannel failingChannel = new EmbeddedChannel(new RpcAuthenticationHandler(failingConfig, serverRole));
        try {
            if (serverRole) {
                client.writeInbound((ByteBuf) failingChannel.readOutbound());
                failingChannel.writeInbound((ByteBuf) client.readOutbound());
            } else {
                failingChannel.writeInbound((ByteBuf) server.readOutbound());
            }
            assertFalse(failingChannel.isActive());
            assertFalse(RpcAuthenticationHandler.isAuthenticated(failingChannel));
            Throwable failure = RpcAuthenticationHandler.authenticationFuture(failingChannel).cause();
            assertEquals("RPC authentication failed", failure.getMessage());
            assertNull(failure.getCause());
            assertNull(failingChannel.readInbound());
            assertNull(failingChannel.readOutbound());
            assertEquals(1, appender.list.size());
            ILoggingEvent event = appender.list.get(0);
            assertEquals("RPC authentication failed for embedded (role=" + (serverRole ? "server" : "client") + ")",
                    event.getFormattedMessage());
            assertSame(cause, ((ThrowableProxy) event.getThrowableProxy()).getThrowable().getCause());
            assertTrue(event.getThrowableProxy().getStackTraceElementProxyArray().length > 0);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
            failingChannel.finishAndReleaseAll();
        }
    }

    @Test
    void closesIdleUnauthenticatedConnections() throws Exception {
        TimeUnit.MILLISECONDS.sleep(1100);
        server.runScheduledPendingTasks();
        client.runScheduledPendingTasks();
        assertFalse(server.isActive());
        assertFalse(client.isActive());
    }

    @Test
    void cancelsAuthenticationDeadlineAfterSuccess() throws Exception {
        client.writeInbound((ByteBuf) server.readOutbound());
        server.writeInbound((ByteBuf) client.readOutbound());
        client.writeInbound((ByteBuf) server.readOutbound());
        TimeUnit.MILLISECONDS.sleep(1100);
        server.runScheduledPendingTasks();
        client.runScheduledPendingTasks();
        assertTrue(server.isActive());
        assertTrue(client.isActive());
    }

    private ByteBuf heartbeat() {
        EmbeddedChannel encoder = new EmbeddedChannel(new TransporterEncoder());
        try {
            encoder.writeOutbound(HeartBeatTransporter.getHeartBeatTransporter());
            return encoder.readOutbound();
        } finally {
            encoder.finishAndReleaseAll();
        }
    }

}
