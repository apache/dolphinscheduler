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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaderContentType;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaders;
import org.apache.dolphinscheduler.common.model.OkHttpResponse;
import org.apache.dolphinscheduler.common.model.OkHttpResult;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class OkHttpUtilsTest {

    private MockWebServer mockWebServer;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        baseUrl = "http://localhost:" + mockWebServer.getPort();
    }

    @AfterEach
    void tearDown() throws IOException {
        try {
            // Try to consume ONE request (most tests send one)
            // Use short timeout and catch AssertionError (thrown when no request arrives)
            mockWebServer.takeRequest(50, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (AssertionError e) {
            // No request was received — that's OK.
            // This often happens if the test failed before sending the request.
        }

        // Force shutdown — but if a response is still pending (enqueued but not consumed),
        // this may STILL fail. So the real fix is in the test logic, not tearDown.
        try {
            mockWebServer.shutdown();
        } catch (IOException e) {
            log.warn("MockWebServer shutdown failed: " + e.getMessage());
        }
    }

    @Test
    void testGet_success() throws Exception {
        String responseBody = "{\"status\":\"ok\"}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody));

        Map<String, Object> params = Collections.singletonMap("id", 123);
        OkHttpResponse response = OkHttpUtils.get(
                baseUrl + "/api/test",
                null,
                params,
                5000, 5000, 5000);

        assertEquals(200, response.getStatusCode());
        assertEquals(responseBody, response.getBody());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/api/test?id=123"));
    }

    @Test
    void testGetWithCall_success() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        OkHttpResult result = OkHttpUtils.getWithCall(
                baseUrl + "/api/test",
                null,
                null,
                5000, 5000, 5000);

        assertNotNull(result.getCall());
        assertEquals(200, result.getResponse().getStatusCode());
        assertFalse(result.getCall().isCanceled());
    }

    @Test
    void testPost_success() throws Exception {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.SC_OK)
                        .setHeader("Content-Type", "application/json; charset=utf-8")
                        .setBody("{\"created\":true}"));

        Map<String, Object> body = new HashMap<>();
        body.put("name", "Alice");
        body.put("age", 30);

        OkHttpResponse response = OkHttpUtils.post(
                baseUrl + "/api/users",
                null,
                null,
                body,
                5000, 5000, 5000);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertTrue(response.getBody().contains("created"));

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getBody().readUtf8().contains("\"name\":\"Alice\""));
    }

    @Test
    void testPostWithCall_withHeaders() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        OkHttpRequestHeaders headers = new OkHttpRequestHeaders();
        headers.setHeaders(new HashMap<>());
        headers.getHeaders().put("Authorization", "Bearer token123");
        headers.setOkHttpRequestHeaderContentType(OkHttpRequestHeaderContentType.APPLICATION_JSON);

        OkHttpResult result = OkHttpUtils.postWithCall(
                baseUrl + "/api/data",
                headers,
                null,
                Collections.singletonMap("key", "value"),
                5000, 5000, 5000);

        assertEquals(200, result.getResponse().getStatusCode());

        RecordedRequest req = mockWebServer.takeRequest();
        assertEquals("Bearer token123", req.getHeader("Authorization"));

        // Robust assertion for Content-Type
        String contentType = req.getHeader("Content-Type");
        assertNotNull(contentType);
        assertTrue(contentType.startsWith("application/json"));
    }

    @Test
    void testPut_success() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"updated\":true}"));

        OkHttpResponse response = OkHttpUtils.put(
                baseUrl + "/api/item/1",
                null,
                Collections.singletonMap("status", "active"),
                5000, 5000, 5000);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("updated"));
    }

    @Test
    void testDelete_success() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(204)); // No Content

        OkHttpResponse response = OkHttpUtils.delete(
                baseUrl + "/api/item/1",
                null,
                5000, 5000, 5000);

        assertEquals(204, response.getStatusCode());
    }

    @Test
    void testGet_invalidUrl_throwsRuntimeException() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            OkHttpUtils.get("not-a-url", null, null, 1000, 1000, 1000);
        });
        assertTrue(ex.getMessage().contains("Expected URL scheme 'http' or 'https' but no scheme was found"));
    }

    @Test
    void testPostWithCall_requestTimeout_throwsRuntimeExceptionWithIOException() throws InterruptedException {
        // Arrange: mock server delays response for 10 seconds
        mockWebServer.enqueue(new MockResponse()
                .setBodyDelay(10, TimeUnit.SECONDS)
                .setBody("{}"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            OkHttpUtils.postWithCall(
                    mockWebServer.url(baseUrl + "/slow").toString(),
                    null,
                    null,
                    Collections.singletonMap("data", "test"),
                    100, // connect timeout = 100ms
                    100, // read timeout = 100ms
                    100 // write timeout = 100ms
            );
        });

        // Verify exception message and cause
        assertTrue(exception.getMessage().contains("POST request execution failed"));
        assertTrue(exception.getCause() instanceof IOException);

        // Optional: verify the request was actually sent (consumes enqueued response)
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/slow", request.getPath());
    }

}
