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

package org.apache.dolphinscheduler.extract.base.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.apache.dolphinscheduler.extract.base.RpcMethod;
import org.apache.dolphinscheduler.extract.base.RpcService;
import org.apache.dolphinscheduler.extract.base.authentication.RpcAuthenticationConfig;
import org.apache.dolphinscheduler.extract.base.authentication.RpcAuthenticationConfiguration;
import org.apache.dolphinscheduler.extract.base.authentication.RpcAuthenticationHandler;
import org.apache.dolphinscheduler.extract.base.config.NettyClientConfig;
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;
import org.apache.dolphinscheduler.extract.base.exception.RemoteException;
import org.apache.dolphinscheduler.extract.base.server.RpcServer;
import org.apache.dolphinscheduler.extract.base.utils.Host;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ByteArrayResource;

import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.timeout.IdleStateEvent;

class RpcAuthenticationIntegrationTest {

    private final List<NettyRemotingClient> clients = new ArrayList<>();
    private final AtomicInteger invocations = new AtomicInteger();
    private RpcServer server;
    private Host host;
    private RpcAuthenticationConfig authentication;

    @BeforeEach
    void setUp() throws Exception {
        authentication = config((byte) 1);
    }

    @AfterEach
    void tearDown() {
        clients.forEach(NettyRemotingClient::close);
        if (server != null) {
            server.close();
        }
    }

    @Test
    void acceptsMatchingSecretsAndReauthenticatesAfterReconnect() throws Exception {
        startServer(authentication);
        NettyRemotingClient client = client(authentication);
        Echo proxy = proxy(client);
        assertEquals("first", proxy.echo("first"));
        Channel original = client.getOrCreateChannel(host);
        original.close().sync();
        assertEquals("second", proxy.echo("second"));
        assertNotSame(original, client.getOrCreateChannel(host));
        assertEquals(2, invocations.get());
    }

    @Test
    void rejectsWrongSecretWithoutInvokingBusinessMethod() throws Exception {
        startServer(authentication);
        Echo proxy = proxy(client(config((byte) 2)));
        assertThrows(RemoteException.class, () -> proxy.echo("forbidden"));
        assertEquals(0, invocations.get());
    }

    @Test
    void rejectsLegacyClientWhenServerRequiresAuthentication() throws Exception {
        startServer(authentication);
        Echo proxy = proxy(client(RpcAuthenticationConfig.disabled()));
        assertThrows(RemoteException.class, () -> proxy.echo("forbidden"));
        assertEquals(0, invocations.get());
    }

    @Test
    void refusesToDowngradeWhenServerDoesNotAuthenticate() throws Exception {
        startServer(RpcAuthenticationConfig.disabled());
        Echo proxy = proxy(client(authentication));
        assertThrows(RemoteException.class, () -> proxy.echo("forbidden"));
        assertEquals(0, invocations.get());
    }

    @Test
    void supportsLegacyTrafficWhenBothSidesDisableAuthentication() throws Exception {
        startServer(RpcAuthenticationConfig.disabled());
        assertEquals("legacy", proxy(client(RpcAuthenticationConfig.disabled())).echo("legacy"));
        assertEquals(1, invocations.get());
    }

    @Test
    void waitsForAuthenticationForConcurrentFirstRequests() throws Exception {
        startServer(authentication);
        Echo proxy = proxy(client(authentication));
        ExecutorService callers = Executors.newFixedThreadPool(12);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> results = new ArrayList<>();
        try {
            for (int i = 0; i < 12; i++) {
                results.add(callers.submit(() -> {
                    start.await();
                    return proxy.echo("concurrent");
                }));
            }
            start.countDown();
            for (Future<String> result : results) {
                assertEquals("concurrent", result.get(5, TimeUnit.SECONDS));
            }
            assertEquals(12, invocations.get());
        } finally {
            callers.shutdownNow();
        }
    }

    @Test
    void initializesStaticClientFromYamlBeforeApplicationBeanMakesRpc() throws Exception {
        startServer(authentication);
        try (
                AnnotationConfigApplicationContext context = context(
                        "rpc:\n  authentication:\n    enabled: true\n    secret: " + secret((byte) 1)
                                + "\n")) {
            context.registerBean("startupRpc", String.class,
                    () -> Clients.withService(Echo.class).withHost(host.getAddress()).echo("startup"));
            context.refresh();
            assertEquals("startup", context.getBean("startupRpc"));
            assertTrue(context.getBean(RpcAuthenticationConfig.class).isEnabled());
        }
        assertThrows(IllegalStateException.class,
                () -> Clients.withService(Echo.class).withHost(host.getAddress()));
    }

    @Test
    void failsClientOnlyServiceStartupWhenEnabledWithoutSecret() {
        try (AnnotationConfigApplicationContext context = context("rpc:\n  authentication:\n    enabled: true\n")) {
            assertThrows(BeansException.class, context::refresh);
        }
    }

    @Test
    void rejectsInvalidEnabledFlagInsteadOfSilentlyDisablingAuthentication() {
        try (AnnotationConfigApplicationContext context = context("rpc:\n  authentication:\n    enabled: typo\n")) {
            assertThrows(BeansException.class, context::refresh);
        }
    }

    @Test
    void defaultsToDisabledForExistingYamlConfigurations() throws Exception {
        startServer(RpcAuthenticationConfig.disabled());
        try (AnnotationConfigApplicationContext context = context("spring:\n  application:\n    name: test\n")) {
            context.refresh();
            assertFalse(context.getBean(RpcAuthenticationConfig.class).isEnabled());
            assertEquals("default", Clients.withService(Echo.class).withHost(host.getAddress()).echo("default"));
        }
    }

    @Test
    void suppressesHeartbeatsUntilAuthenticationCompletes() {
        EmbeddedChannel channel = new EmbeddedChannel(new RpcAuthenticationHandler(authentication, false),
                new NettyClientHandler(mock(NettyRemotingClient.class)));
        try {
            channel.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);
            assertNull(channel.readOutbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void doesNotReuseAProxyWithThePreviousContextsCredentials() throws Exception {
        startServer(authentication);
        String yaml = "rpc:\n  authentication:\n    enabled: true\n    secret: ";
        try (AnnotationConfigApplicationContext context = context(yaml + secret((byte) 1))) {
            context.refresh();
            assertEquals("allowed", Clients.withService(Echo.class).withHost(host.getAddress()).echo("allowed"));
        }
        try (AnnotationConfigApplicationContext context = context(yaml + secret((byte) 2))) {
            context.refresh();
            assertThrows(RemoteException.class,
                    () -> Clients.withService(Echo.class).withHost(host.getAddress()).echo("forbidden"));
        }
        assertEquals(1, invocations.get());
    }

    @Test
    void stalledAuthenticationDoesNotBlockCallsToOtherHosts() throws Exception {
        startServer(authentication);
        NettyRemotingClient client = client(RpcAuthenticationConfig.fromSecret(secret((byte) 1), 3000));
        ExecutorService callers = Executors.newFixedThreadPool(2);
        try (ServerSocket silentServer = new ServerSocket(0)) {
            silentServer.setSoTimeout(2000);
            Echo stalledProxy = new JdkDynamicRpcClientProxyFactory(client)
                    .getProxyClient("localhost:" + silentServer.getLocalPort(), Echo.class);
            Future<?> stalled = callers.submit(() -> assertThrows(RemoteException.class,
                    () -> stalledProxy.echo("stalled")));
            try (java.net.Socket accepted = silentServer.accept()) {
                Future<String> healthy = callers.submit(() -> proxy(client).echo("healthy"));
                assertEquals("healthy", healthy.get(2, TimeUnit.SECONDS));
                assertFalse(stalled.isDone());
            }
            stalled.get(2, TimeUnit.SECONDS);
        } finally {
            callers.shutdownNow();
        }
    }

    private AnnotationConfigApplicationContext context(String yaml) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ByteArrayResource(yaml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        context.getEnvironment().getPropertySources()
                .addFirst(new PropertiesPropertySource("yaml", factory.getObject()));
        context.register(RpcAuthenticationConfiguration.class);
        return context;
    }

    private RpcAuthenticationConfig config(byte value) {
        return RpcAuthenticationConfig.fromSecret(secret(value), 500);
    }

    private String secret(byte value) {
        byte[] secret = new byte[32];
        java.util.Arrays.fill(secret, value);
        return Base64.getEncoder().encodeToString(secret);
    }

    private void startServer(RpcAuthenticationConfig config) throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        host = Host.of("localhost:" + port);
        server = new RpcServer(NettyServerConfig.builder().serverName("AuthenticatedTestServer")
                .workerThread(1).listenPort(port).authentication(config).build());
        server.registerServerMethodInvokerProvider(new EchoService());
        server.start();
    }

    private NettyRemotingClient client(RpcAuthenticationConfig config) {
        NettyRemotingClient client = new NettyRemotingClient(NettyClientConfig.builder()
                .workerThreads(1).defaultRpcTimeoutMillis(1000).heartBeatIntervalMillis(50)
                .authentication(config).build());
        clients.add(client);
        return client;
    }

    private Echo proxy(NettyRemotingClient client) {
        return new JdkDynamicRpcClientProxyFactory(client).getProxyClient(host.getAddress(), Echo.class);
    }

    @RpcService
    public interface Echo {

        @RpcMethod(timeout = 1000)
        String echo(String value);
    }

    public class EchoService implements Echo {

        @Override
        public String echo(String value) {
            invocations.incrementAndGet();
            return value;
        }
    }
}
