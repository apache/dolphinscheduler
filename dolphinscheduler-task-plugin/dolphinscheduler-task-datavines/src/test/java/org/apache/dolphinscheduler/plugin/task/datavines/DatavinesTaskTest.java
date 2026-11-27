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

package org.apache.dolphinscheduler.plugin.task.datavines;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_KILL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DatavinesTaskTest {

    private MockWebServer server;

    @BeforeEach
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void testExecuteAndResultSuccess() throws Exception {
        enqueueSuccessResponses();

        DatavinesTask task = createTask(server.url("/").toString(), 123L,
                "secret-token", 1, 5, 0);
        task.handle(null);

        Assertions.assertEquals(EXIT_CODE_SUCCESS, task.getExitStatusCode());

        RecordedRequest execute = server.takeRequest();
        Assertions.assertEquals("POST", execute.getMethod());
        Assertions.assertEquals("/api/v1/openapi/job/execute/123", execute.getPath());
        Assertions.assertEquals("Bearer secret-token", execute.getHeader("Authorization"));

        RecordedRequest runningStatus = server.takeRequest();
        Assertions.assertEquals("GET", runningStatus.getMethod());
        Assertions.assertEquals("/api/v1/openapi/job/execution/status/123", runningStatus.getPath());

        RecordedRequest successStatus = server.takeRequest();
        Assertions.assertEquals("GET", successStatus.getMethod());
        Assertions.assertEquals("/api/v1/openapi/job/execution/status/123", successStatus.getPath());

        RecordedRequest result = server.takeRequest();
        Assertions.assertEquals("GET", result.getMethod());
        Assertions.assertEquals("/api/v1/openapi/job/execution/result/123", result.getPath());

        DatavinesParameters parameters = (DatavinesParameters) task.getParameters();
        Assertions.assertTrue(parameters.getVarPool().stream()
                .anyMatch(property -> DatavinesConstants.OUT_EXECUTION_ID.equals(property.getProp())
                        && "123".equals(property.getValue())));
        Assertions.assertTrue(parameters.getVarPool().stream()
                .anyMatch(property -> DatavinesConstants.OUT_RESULT.equals(property.getProp())
                        && "SUCCESS".equals(property.getValue())));
    }

    @Test
    public void testRetryOnTransientFailure() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("temporary failure"));
        server.enqueue(successResponse("123"));
        server.enqueue(successResponse("\"SUCCESS\""));
        server.enqueue(successResponse("\"SUCCESS\""));

        DatavinesTask task = createTask(server.url("/").toString(), 123L,
                "secret-token", 1, 5, 1);
        task.handle(null);

        Assertions.assertEquals(EXIT_CODE_SUCCESS, task.getExitStatusCode());
        Assertions.assertEquals(4, server.getRequestCount());
    }

    @Test
    public void testExecuteBusinessFailure() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"code\":500,\"msg\":\"job not found\"}"));

        DatavinesTask task = createTask(server.url("/").toString(), 123L,
                "secret-token", 1, 5, 0);

        Assertions.assertThrows(TaskException.class, () -> task.handle(null));
        Assertions.assertEquals(EXIT_CODE_FAILURE, task.getExitStatusCode());
        Assertions.assertEquals(1, server.getRequestCount());
    }

    @Test
    public void testStatusFailure() throws Exception {
        server.enqueue(successResponse("123"));
        server.enqueue(successResponse("\"FAILURE\""));

        DatavinesTask task = createTask(server.url("/").toString(), 123L,
                "secret-token", 1, 5, 0);
        task.handle(null);

        Assertions.assertEquals(EXIT_CODE_FAILURE, task.getExitStatusCode());
        Assertions.assertEquals(2, server.getRequestCount());
    }

    @Test
    public void testResultFailure() throws Exception {
        server.enqueue(successResponse("123"));
        server.enqueue(successResponse("\"SUCCESS\""));
        server.enqueue(successResponse("\"FAILURE\""));

        DatavinesTask task = createTask(server.url("/").toString(), 123L,
                "secret-token", 1, 5, 0);
        task.handle(null);

        Assertions.assertEquals(EXIT_CODE_FAILURE, task.getExitStatusCode());
        Assertions.assertEquals(3, server.getRequestCount());
    }

    @Test
    public void testTimeout() throws Exception {
        server.enqueue(successResponse("123"));
        server.enqueue(successResponse("\"RUNNING\""));

        DatavinesTask task = createTask(server.url("/").toString(), 123L,
                "secret-token", 1, 1, 0);

        Assertions.assertThrows(TaskException.class, () -> task.handle(null));
        Assertions.assertEquals(EXIT_CODE_FAILURE, task.getExitStatusCode());
    }

    @Test
    public void testCancel() throws Exception {
        server.enqueue(successResponse("123"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"code\":200,\"data\":\"RUNNING\"}")
                .setBodyDelay(3, TimeUnit.SECONDS));

        DatavinesTask task = createTask(server.url("/").toString(), 123L,
                "secret-token", 1, 30, 0);

        Thread worker = new Thread(() -> {
            try {
                task.handle(null);
            } catch (Exception ignored) {
                // The task may exit with a TaskException after cancellation.
            }
        });
        worker.start();

        Thread.sleep(500);
        task.cancel();
        worker.join(5000);

        Assertions.assertFalse(worker.isAlive());
        Assertions.assertEquals(EXIT_CODE_KILL, task.getExitStatusCode());
    }

    @Test
    public void testTokenIsMaskedInToString() {
        DatavinesParameters parameters = new DatavinesParameters();
        parameters.setAddress("http://127.0.0.1:5600");
        parameters.setJobId(1L);
        parameters.setToken("secret-token");

        Assertions.assertFalse(parameters.toString().contains("secret-token"));
        Assertions.assertTrue(parameters.toString().contains("***"));
    }

    @Test
    public void testInvalidParameters() {
        DatavinesParameters parameters = new DatavinesParameters();
        parameters.setAddress("127.0.0.1:5600");
        parameters.setJobId(0L);
        parameters.setToken("");
        parameters.setInterval(0);
        parameters.setTimeout(0);
        parameters.setRetryTimes(-1);

        Assertions.assertFalse(parameters.checkParameters());
    }

    private DatavinesTask createTask(String address,
                                     long jobId,
                                     String token,
                                     int interval,
                                     int timeout,
                                     int retryTimes) {
        DatavinesParameters parameters = new DatavinesParameters();
        parameters.setAddress(address);
        parameters.setJobId(jobId);
        parameters.setToken(token);
        parameters.setInterval(interval);
        parameters.setTimeout(timeout);
        parameters.setRetryTimes(retryTimes);

        TaskExecutionContext context = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(context.getTaskParams()).thenReturn(JSONUtils.toJsonString(parameters));
        Mockito.when(context.getPrepareParamsMap())
                .thenReturn(Collections.<String, Property>emptyMap());

        DatavinesTask task = new DatavinesTask(context);
        task.init();
        return task;
    }

    private void enqueueSuccessResponses() {
        server.enqueue(successResponse("123"));
        server.enqueue(successResponse("\"RUNNING\""));
        server.enqueue(successResponse("\"SUCCESS\""));
        server.enqueue(successResponse("\"SUCCESS\""));
    }

    private MockResponse successResponse(String dataJson) {
        return new MockResponse()
                .setResponseCode(200)
                .setBody("{\"code\":200,\"data\":" + dataJson + "}");
    }
}
