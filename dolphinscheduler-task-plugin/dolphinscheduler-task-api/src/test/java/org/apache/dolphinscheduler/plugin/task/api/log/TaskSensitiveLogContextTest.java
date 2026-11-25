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

package org.apache.dolphinscheduler.plugin.task.api.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;

class TaskSensitiveLogContextTest {

    @Test
    void maskReplacesLiteralJsonEscapeAndNestedSecret() {
        String raw = "sec\"ret";
        TaskExecutionContext context = TaskExecutionContext.builder()
                .prepareParamsMap(params("pwd", "SuperSecret123", "nested", "Secret123", "quoted", raw))
                .build();

        try (TaskSensitiveLogContext ignored = TaskSensitiveLogContext.open(context)) {
            assertEquals("cmd ******", TaskSensitiveLogContext.mask("cmd SuperSecret123"));
            assertEquals("cmd ******", TaskSensitiveLogContext.mask("cmd Secret123"));
            assertFalse(TaskSensitiveLogContext.mask("cmd SuperSecret123").contains("Super"));
            String escaped = JSONUtils.toJsonString(raw);
            assertTrue(TaskSensitiveLogContext.mask("body " + escaped).contains(TaskConstants.SENSITIVE_DATA_MASK));
            assertFalse(TaskSensitiveLogContext.mask("body " + escaped).contains("sec\\\"ret"));
        }
    }

    @Test
    void shortPlaceholderAndMissingScopeAreNotMasked() {
        TaskExecutionContext context = TaskExecutionContext.builder()
                .prepareParamsMap(params("short", "abc", "marker", TaskConstants.SENSITIVE_DATA_MASK))
                .globalParams("[{\"prop\":\"token\",\"value\":\"global-token\",\"direct\":\"IN\",\"type\":\"VARCHAR\","
                        + "\"sensitive\":true}]")
                .taskParams("{\"localParams\":[{\"prop\":\"sqlPwd\",\"value\":\"local-token\",\"direct\":\"IN\","
                        + "\"type\":\"VARCHAR\",\"sensitive\":true}]}")
                .build();

        assertEquals("abc global-token", TaskSensitiveLogContext.mask("abc global-token"));

        try (TaskSensitiveLogContext ignored = TaskSensitiveLogContext.open(context)) {
            assertEquals("abc stays", TaskSensitiveLogContext.mask("abc stays"));
            assertEquals(TaskConstants.SENSITIVE_DATA_MASK,
                    TaskSensitiveLogContext.mask(TaskConstants.SENSITIVE_DATA_MASK));
            assertEquals("g=****** l=******",
                    TaskSensitiveLogContext.mask("g=global-token l=local-token"));
        }
        assertEquals("global-token", TaskSensitiveLogContext.mask("global-token"));
    }

    @Test
    void layoutMasksThrowableButNotTaskOutput() {
        LoggerContext loggerContext = new LoggerContext();
        loggerContext.start();
        Logger logger = loggerContext.getLogger(TaskSensitiveLogContextTest.class);
        SensitivePatternLayout layout = new SensitivePatternLayout();
        layout.setContext(loggerContext);
        layout.setPattern("%message%n");
        layout.start();

        TaskExecutionContext context = TaskExecutionContext.builder()
                .prepareParamsMap(params("pwd", "SuperSecret123"))
                .build();
        try (TaskSensitiveLogContext ignored = TaskSensitiveLogContext.open(context)) {
            LoggingEvent failure = new LoggingEvent("test", logger, Level.ERROR, "sql failed",
                    new RuntimeException("connect SuperSecret123 refused"), null);
            String rendered = layout.doLayout(failure);
            assertFalse(rendered.contains("SuperSecret123"));
            assertTrue(rendered.contains(TaskConstants.SENSITIVE_DATA_MASK));

            LoggingEvent output = new LoggingEvent("test", logger, Level.INFO, "line {}", null,
                    new Object[]{"SuperSecret123"});
            output.setMarker(TaskLogMarkers.taskOutput());
            String taskOutput = layout.doLayout(output);
            assertTrue(taskOutput.contains("SuperSecret123"));
        } finally {
            layout.stop();
            loggerContext.stop();
        }
    }

    private static Map<String, Property> params(String... propAndValue) {
        Map<String, Property> params = new HashMap<>();
        for (int i = 0; i < propAndValue.length; i += 2) {
            params.put(propAndValue[i], Property.builder()
                    .prop(propAndValue[i])
                    .value(propAndValue[i + 1])
                    .sensitive(true)
                    .build());
        }
        return params;
    }
}
