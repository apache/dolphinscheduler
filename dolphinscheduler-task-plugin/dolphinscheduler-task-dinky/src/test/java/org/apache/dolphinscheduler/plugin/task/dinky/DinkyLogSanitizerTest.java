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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

class DinkyLogSanitizerTest {

    private static final String URI_USERNAME = "alice";
    private static final String URI_PASSWORD = "s3cr3t";
    private static final String QUERY_TOKEN = "TOKEN";
    private static final String QUERY_USERNAME = "QUERY_USER";
    private static final String QUERY_PASSWORD = "QUERY_PASSWORD";
    private static final String CREDENTIAL_ADDRESS =
            "unknown://" + URI_USERNAME + ":" + URI_PASSWORD
                    + "@dinky:8888?token=" + QUERY_TOKEN
                    + "&username=" + QUERY_USERNAME
                    + "&password=" + QUERY_PASSWORD
                    + "&mode=test";

    @Test
    void testSummarizeVariablesDoesNotExposeValues() {
        Map<String, String> variables = new HashMap<>();
        variables.put("accessKeyId", "AKIA_TEST");
        variables.put("accessKeySecret", "SECRET_TEST");
        variables.put("businessDate", "2026-07-06");

        String summary = DinkyLogSanitizer.summarizeVariables(variables);

        assertTrue(summary.contains("size=3"));
        assertTrue(summary.contains("accessKeyId"));
        assertTrue(summary.contains("accessKeySecret"));
        assertTrue(summary.contains("businessDate"));
        assertFalse(summary.contains("AKIA_TEST"));
        assertFalse(summary.contains("SECRET_TEST"));
        assertFalse(summary.contains("2026-07-06"));
    }

    @Test
    void testSummarizeParametersDoesNotExposeLocalParamValues() {
        DinkyParameters parameters = new DinkyParameters();
        parameters.setAddress("http://dinky:8888");
        parameters.setTaskId("1001");
        parameters.setOnline(true);
        parameters.setLocalParams(Arrays.asList(
                new Property("accessKeyId", null, null, "AKIA_TEST"),
                new Property("businessDate", null, null, "2026-07-06")));

        String summary = DinkyLogSanitizer.summarizeParameters(parameters);

        assertTrue(summary.contains("address=http://dinky:8888"));
        assertTrue(summary.contains("taskId=1001"));
        assertTrue(summary.contains("online=true"));
        assertTrue(summary.contains("localParamKeys=[accessKeyId, businessDate]"));
        assertFalse(summary.contains("AKIA_TEST"));
        assertFalse(summary.contains("2026-07-06"));
    }

    @Test
    void testSummarizeParametersDoesNotExposeAddressCredentials() {
        DinkyParameters parameters = credentialParameters();

        String summary = DinkyLogSanitizer.summarizeParameters(parameters);

        assertAddressCredentialsMasked(summary);
        assertTrue(summary.contains("dinky:8888"));
        assertTrue(summary.contains("mode=test"));
    }

    @Test
    void testApplicationIdDoesNotExposeAddressCredentials() throws Exception {
        DinkyTask task = new DinkyTask(new TaskExecutionContext());
        setField(task, "dinkyParameters", credentialParameters());
        setField(task, "status", true);

        task.trackApplicationStatusV0();

        String applicationId = task.getAppIds();
        assertAddressCredentialsMasked(applicationId);
        assertTrue(applicationId.contains("dinky:8888"));
        assertTrue(applicationId.endsWith("-1001"));
    }

    @Test
    void testGetRequestUrlLogDoesNotExposeAddressCredentials() throws Exception {
        DinkyTask task = new DinkyTask(new TaskExecutionContext());
        Logger logger = (Logger) LoggerFactory.getLogger(DinkyTask.class);
        ListAppender<ILoggingEvent> appender = attachListAppender(logger);
        try {
            Method doGet = DinkyTask.class.getDeclaredMethod("doGet", String.class, Map.class);
            doGet.setAccessible(true);

            doGet.invoke(task, CREDENTIAL_ADDRESS, Collections.emptyMap());

            String messages = formattedMessages(appender);
            assertAddressCredentialsMasked(messages);
            assertTrue(messages.contains("access url:"));
            assertTrue(messages.contains("mode=test"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void testCancelLogsDoNotExposeAddressCredentials() throws Exception {
        DinkyTask task = new DinkyTask(new TaskExecutionContext());
        setField(task, "dinkyParameters", credentialParameters());
        Logger logger = (Logger) LoggerFactory.getLogger(DinkyTask.class);
        ListAppender<ILoggingEvent> appender = attachListAppender(logger);
        try {
            task.cancelApplication();

            String messages = formattedMessages(appender);
            assertAddressCredentialsMasked(messages);
            assertTrue(messages.contains("trying terminate dinky task"));
            assertTrue(messages.contains("dinky task terminated"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void testSanitizeMessageMasksSensitiveValues() {
        String message = "{\"accessKeyId\":\"AKIA_TEST\",\"accessKeySecret\":\"SECRET_TEST\"}";

        String sanitized = DinkyLogSanitizer.sanitizeMessage(message);

        assertTrue(sanitized.contains("accessKeyId"));
        assertTrue(sanitized.contains("accessKeySecret"));
        assertFalse(sanitized.contains("AKIA_TEST"));
        assertFalse(sanitized.contains("SECRET_TEST"));
    }

    @Test
    void testSanitizeMessageMasksSensitiveValuesInJsonNodeMessage() {
        String message = "{\"accessKeyId\":\"AKIA_TEST\",\"accessKeySecret\":\"SECRET_TEST\"}";

        String sanitized = DinkyLogSanitizer.sanitizeMessage(JsonNodeFactory.instance.textNode(message));

        assertTrue(sanitized.contains("accessKeyId"));
        assertTrue(sanitized.contains("accessKeySecret"));
        assertFalse(sanitized.contains("AKIA_TEST"));
        assertFalse(sanitized.contains("SECRET_TEST"));
    }

    @Test
    void testParseMalformedResponseFailsExplicitlyWithoutExposingRawResponse() throws Exception {
        DinkyTask task = new DinkyTask(new TaskExecutionContext());
        Method parseMethod = DinkyTask.class.getDeclaredMethod("parse", String.class);
        parseMethod.setAccessible(true);
        String response = "malformed response accessKeyId=AKIA_TEST accessKeySecret=SECRET_TEST";

        InvocationTargetException exception =
                assertThrows(InvocationTargetException.class, () -> parseMethod.invoke(task, response));

        DinkyTaskException cause = assertInstanceOf(DinkyTaskException.class, exception.getCause());
        assertTrue(cause.getMessage().contains("dinky task response parse failed"));
        assertFalse(cause.getMessage().contains("AKIA_TEST"));
        assertFalse(cause.getMessage().contains("SECRET_TEST"));
    }

    private static DinkyParameters credentialParameters() {
        DinkyParameters parameters = new DinkyParameters();
        parameters.setAddress(CREDENTIAL_ADDRESS);
        parameters.setTaskId("1001");
        return parameters;
    }

    private static void setField(DinkyTask task, String fieldName, Object value) throws Exception {
        Field field = DinkyTask.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(task, value);
    }

    private static ListAppender<ILoggingEvent> attachListAppender(Logger logger) {
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private static String formattedMessages(ListAppender<ILoggingEvent> appender) {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));
    }

    private static void assertAddressCredentialsMasked(String value) {
        assertFalse(value.contains(URI_USERNAME));
        assertFalse(value.contains(URI_PASSWORD));
        assertFalse(value.contains(QUERY_TOKEN));
        assertFalse(value.contains(QUERY_USERNAME));
        assertFalse(value.contains(QUERY_PASSWORD));
    }
}
