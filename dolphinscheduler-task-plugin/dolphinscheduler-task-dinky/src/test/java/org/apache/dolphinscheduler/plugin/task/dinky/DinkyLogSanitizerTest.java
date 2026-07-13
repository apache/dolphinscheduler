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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

class DinkyLogSanitizerTest {

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
}
