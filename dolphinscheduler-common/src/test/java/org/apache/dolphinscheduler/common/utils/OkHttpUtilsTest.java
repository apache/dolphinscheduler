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

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaders;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.OkHttpClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

class OkHttpUtilsTest {

    private static final int TIMEOUT = 10_000;

    @Test
    void testKeepAliveAndPlainClientsAreSeparated() throws Exception {
        OkHttpClient plainClient = OkHttpUtils.getHttpClient(TIMEOUT, TIMEOUT, TIMEOUT, false);
        OkHttpClient keepAliveClient = OkHttpUtils.getHttpClient(TIMEOUT, TIMEOUT, TIMEOUT, true);

        Assertions.assertNotSame(plainClient.connectionPool(), keepAliveClient.connectionPool(),
                "the two settings must not share a connection pool");
        try (Socket plainSocket = plainClient.socketFactory().createSocket()) {
            Assertions.assertFalse(plainSocket.getKeepAlive(), "the plain client must not enable TCP keepalive");
        }
        try (Socket keepAliveSocket = keepAliveClient.socketFactory().createSocket()) {
            Assertions.assertTrue(keepAliveSocket.getKeepAlive(), "the keepalive client must enable TCP keepalive");
        }
    }

    @Test
    void testKeepAliveRequestDoesNotReusePlainConnection() throws Exception {
        resetConnectionPools();
        List<Integer> remotePorts = new CopyOnWriteArrayList<>();
        HttpServer server = startServer(remotePorts);
        try {
            String url = urlOf(server);
            OkHttpUtils.get(url, new OkHttpRequestHeaders(), null, TIMEOUT, TIMEOUT, TIMEOUT, false);
            assertPooledConnectionCount(false, 1);
            assertPooledConnectionCount(true, 0);

            OkHttpUtils.get(url, new OkHttpRequestHeaders(), null, TIMEOUT, TIMEOUT, TIMEOUT, true);
            assertPooledConnectionCount(false, 1);
            assertPooledConnectionCount(true, 1);
            assertRequestsUseDistinctConnections(remotePorts);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void testPlainRequestDoesNotReuseKeepAliveConnection() throws Exception {
        resetConnectionPools();
        List<Integer> remotePorts = new CopyOnWriteArrayList<>();
        HttpServer server = startServer(remotePorts);
        try {
            String url = urlOf(server);
            OkHttpUtils.get(url, new OkHttpRequestHeaders(), null, TIMEOUT, TIMEOUT, TIMEOUT, true);
            assertPooledConnectionCount(true, 1);
            assertPooledConnectionCount(false, 0);

            OkHttpUtils.get(url, new OkHttpRequestHeaders(), null, TIMEOUT, TIMEOUT, TIMEOUT, false);
            assertPooledConnectionCount(true, 1);
            assertPooledConnectionCount(false, 1);
            assertRequestsUseDistinctConnections(remotePorts);
        } finally {
            server.stop(0);
        }
    }

    private static void assertRequestsUseDistinctConnections(List<Integer> remotePorts) {
        Assertions.assertEquals(2, remotePorts.size(), "both requests should reach the server");
        Assertions.assertEquals(2, new HashSet<>(remotePorts).size(),
                "the two requests reused one TCP connection, remote ports: " + remotePorts);
    }

    private static void assertPooledConnectionCount(boolean keepAlive, int expected) {
        int actual = clientOf(keepAlive).connectionPool().connectionCount();
        Assertions.assertEquals(expected, actual,
                "unexpected pooled connection count of the keepAlive=" + keepAlive + " client");
    }

    private static void resetConnectionPools() {
        clientOf(false).connectionPool().evictAll();
        clientOf(true).connectionPool().evictAll();
    }

    private static OkHttpClient clientOf(boolean keepAlive) {
        return OkHttpUtils.getHttpClient(TIMEOUT, TIMEOUT, TIMEOUT, keepAlive);
    }

    private static HttpServer startServer(List<Integer> remotePorts) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/keep-alive", exchange -> {
            remotePorts.add(exchange.getRemoteAddress().getPort());
            byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(body);
            }
        });
        server.start();
        return server;
    }

    private static String urlOf(HttpServer server) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/keep-alive";
    }
}
