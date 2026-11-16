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

package org.apache.dolphinscheduler.plugin.task.dinky;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class DinkyTaskTest {

    private static final String VERSION_PATH = "/openapi/version";

    private static final String SUBMIT_PATH = "/openapi/submitTask";

    private HttpServer server;

    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    @Test
    public void testSubmitApplicationV0FailureDoesNotInterruptThread() throws Exception {
        assertSubmitFailureDoesNotInterruptThread("0.6.5");
    }

    @Test
    public void testSubmitApplicationV1FailureDoesNotInterruptThread() throws Exception {
        assertSubmitFailureDoesNotInterruptThread("1.0.0");
    }

    private void assertSubmitFailureDoesNotInterruptThread(String dinkyVersion) throws Exception {
        startServer(dinkyVersion);

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext
                .setTaskParams(String.format("{\"address\":\"%s\",\"taskId\":\"1\",\"online\":false}", baseUrl()));
        taskExecutionContext.setPrepareParamsMap(Collections.emptyMap());

        DinkyTask dinkyTask = new DinkyTask(taskExecutionContext);
        dinkyTask.init();

        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean interrupted = new AtomicBoolean();
        Thread workerThread = new Thread(() -> {
            try {
                dinkyTask.handle(null);
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                interrupted.set(Thread.currentThread().isInterrupted());
            }
        }, "dinky-task-test-worker");
        workerThread.start();
        workerThread.join(10000);

        assertFalse(workerThread.isAlive(), "the failing submit should abort the task");
        assertNotNull(failure.get(), "the failing submit should throw");
        assertTrue(failure.get() instanceof TaskException,
                "expected TaskException but got " + failure.get());
        assertFalse(interrupted.get(),
                "a failed submit must not leave the worker thread interrupted");
    }

    private void startServer(String dinkyVersion) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(VERSION_PATH, exchange -> respond(exchange, 200,
                String.format("{\"code\":0,\"data\":\"%s\"}", dinkyVersion)));
        // A 200 with a malformed body makes the submit path throw while checking the response,
        // which is the path that used to set the interrupt flag.
        server.createContext(SUBMIT_PATH, exchange -> respond(exchange, 200, "not-json"));
        server.start();
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}
