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

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.internal.connection.RealConnection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

class OkHttpUtilsTest {

    private static final int TIMEOUT = 10_000;

    @Test
    void testSocketKeepAliveIsAppliedPerSetting() throws Exception {
        HttpServer server = startServer();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/keep-alive";

            OkHttpUtils.get(url, new OkHttpRequestHeaders(), null, TIMEOUT, TIMEOUT, TIMEOUT, false);
            OkHttpUtils.get(url, new OkHttpRequestHeaders(), null, TIMEOUT, TIMEOUT, TIMEOUT, true);

            List<Socket> plainSockets = pooledSockets(baseClient("CLIENT"));
            List<Socket> keepAliveSockets = pooledSockets(baseClient("KEEP_ALIVE_CLIENT"));

            Assertions.assertFalse(plainSockets.isEmpty(), "the socket of the plain request should be pooled");
            Assertions.assertFalse(keepAliveSockets.isEmpty(),
                    "the keepalive request must not reuse the pooled socket of the plain request");
            for (Socket socket : plainSockets) {
                Assertions.assertFalse(socket.getKeepAlive(), "the plain connection should not enable TCP keepalive");
            }
            for (Socket socket : keepAliveSockets) {
                Assertions.assertTrue(socket.getKeepAlive(), "the keepalive connection should enable TCP keepalive");
            }
        } finally {
            server.stop(0);
        }
    }

    private static HttpServer startServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/keep-alive", exchange -> {
            byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(body);
            }
        });
        server.start();
        return server;
    }

    private static OkHttpClient baseClient(String fieldName) throws Exception {
        Field field = OkHttpUtils.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (OkHttpClient) field.get(null);
    }

    private static List<Socket> pooledSockets(OkHttpClient client) throws Exception {
        ConnectionPool pool = client.connectionPool();
        Field connectionsField = pool.getDelegate$okhttp().getClass().getDeclaredField("connections");
        connectionsField.setAccessible(true);
        Collection<?> connections = (Collection<?>) connectionsField.get(pool.getDelegate$okhttp());
        List<Socket> sockets = new ArrayList<>(connections.size());
        for (Object connection : connections) {
            sockets.add(((RealConnection) connection).socket());
        }
        return sockets;
    }
}
