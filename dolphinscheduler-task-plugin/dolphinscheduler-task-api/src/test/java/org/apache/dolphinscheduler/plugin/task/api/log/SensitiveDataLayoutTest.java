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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;

/**
 * Layout-level tests: masking runs after the full log event (message + throwable) is rendered.
 */
class SensitiveDataLayoutTest {

    private static final int TASK_ID = 501;

    private SensitiveDataLayout layout;

    @BeforeEach
    void setUp() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        layout = new SensitiveDataLayout();
        layout.setContext(context);
        layout.setPattern("%msg%n");
        layout.start();

        SensitiveDataConverter.registerTaskSensitiveValues(TASK_ID,
                Collections.singletonMap("token", sensitive("token", "SuperSecretToken123")));
    }

    @AfterEach
    void tearDown() {
        SensitiveDataConverter.clearTaskSensitiveValues(TASK_ID);
        if (layout != null) {
            layout.stop();
        }
    }

    @Test
    void masksSecretInMessageViaRealLayout() {
        LoggingEvent event = baseEvent("worker dump contains SuperSecretToken123");
        String rendered = layout.doLayout(event);
        assertFalse(rendered.contains("SuperSecretToken123"));
        assertTrue(rendered.contains(TaskConstants.SENSITIVE_DATA_MASK));
    }

    @Test
    void masksSecretInThrowableStackTrace() {
        RuntimeException ex = new RuntimeException("HTTP body [\"SuperSecretToken123\"]");
        LoggingEvent event = baseEvent("task failed");
        event.setThrowableProxy(new ThrowableProxy(ex));

        String rendered = layout.doLayout(event);
        assertFalse(rendered.contains("SuperSecretToken123"));
        assertTrue(rendered.contains(TaskConstants.SENSITIVE_DATA_MASK));
    }

    @Test
    void processOutputMarkerKeepsTaskParamPlaintext() {
        LoggingEvent event = baseEvent(" -> SuperSecretToken123");
        event.setMarker(TaskLogMarkers.skipTaskParamMask());

        String rendered = layout.doLayout(event);
        assertTrue(rendered.contains("SuperSecretToken123"));
    }

    @Test
    void masksJsonEscapedSecretForm() {
        String secret = "quote\"and\\slash\nline";
        SensitiveDataConverter.registerTaskSensitiveValues(TASK_ID,
                Collections.singletonMap("token", sensitive("token", secret)));

        String escaped = SensitiveDataConverter.jsonEscape(secret);
        LoggingEvent event = baseEvent("{\"value\":\"" + escaped + "\"}");
        String rendered = layout.doLayout(event);
        assertFalse(rendered.contains(escaped));
        assertFalse(rendered.contains(secret));
        assertTrue(rendered.contains(TaskConstants.SENSITIVE_DATA_MASK));
    }

    private LoggingEvent baseEvent(String message) {
        LoggingEvent event = new LoggingEvent();
        event.setLoggerName(SensitiveDataLayoutTest.class.getName());
        event.setLevel(Level.INFO);
        event.setMessage(message);
        Map<String, String> mdc = new HashMap<>();
        mdc.put(TaskConstants.TASK_INSTANCE_ID_MDC_KEY, String.valueOf(TASK_ID));
        event.setMDCPropertyMap(mdc);
        return event;
    }

    private static Property sensitive(String prop, String value) {
        return Property.builder()
                .prop(prop)
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value(value)
                .sensitive(true)
                .build();
    }
}
