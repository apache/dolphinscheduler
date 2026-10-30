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

package org.apache.dolphinscheduler.common.config;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class PropertySecretLoggingTest {

    private static final String KEY = "rpc.authentication.secret";
    private static final String SECRET = "rpc-logging-test-secret";
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private final Class<?>[] delegates = {ImmutablePropertyDelegate.class, ImmutableYamlDelegate.class,
            ImmutablePriorityPropertyDelegate.class};
    private final Level[] levels = new Level[delegates.length];

    @BeforeEach
    void setUp() throws Exception {
        appender.start();
        for (int i = 0; i < delegates.length; i++) {
            Logger logger = (Logger) LoggerFactory.getLogger(delegates[i]);
            levels[i] = logger.getLevel();
            logger.setLevel(Level.DEBUG);
            logger.addAppender(appender);
        }
        clearSecretCache();
    }

    @AfterEach
    void tearDown() throws Exception {
        clearSecretCache();
        for (int i = 0; i < delegates.length; i++) {
            Logger logger = (Logger) LoggerFactory.getLogger(delegates[i]);
            logger.detachAppender(appender);
            logger.setLevel(levels[i]);
        }
        appender.stop();
    }

    @Test
    void propertiesFileKeepsSecretReadableButMasksLogs() {
        ImmutablePropertyDelegate properties = new ImmutablePropertyDelegate("/rpc-logging-test.properties");
        assertEquals(SECRET, properties.get(KEY));
        assertEquals("visible-value", properties.get("rpc.logging.test.visible"));
        assertRedacted();
        assertTrue(logs().contains("visible-value"));
    }

    @Test
    void yamlFileKeepsSecretReadableButMasksLogs() {
        ImmutableYamlDelegate yaml = new ImmutableYamlDelegate("/rpc-logging-test.yaml");
        assertEquals(SECRET, yaml.get(KEY));
        assertRedacted();
    }

    @Test
    void priorityPropertiesAndYamlLogsAreMasked() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(KEY, SECRET);
        ImmutablePriorityPropertyDelegate delegate = new ImmutablePriorityPropertyDelegate(
                new ImmutablePropertyDelegate(properties), new ImmutableYamlDelegate(new Properties()));
        assertEquals(SECRET, delegate.get(KEY));
        assertRedacted();
        clearSecretCache();
        appender.list.clear();
        delegate = new ImmutablePriorityPropertyDelegate(new ImmutablePropertyDelegate(new Properties()),
                new ImmutableYamlDelegate(properties));
        assertEquals(SECRET, delegate.get(KEY));
        assertRedacted();
    }

    @Test
    void jvmOverrideLogsAreMasked() {
        String previous = System.getProperty(KEY);
        try {
            System.setProperty(KEY, SECRET);
            assertEquals(SECRET, emptyDelegate().get(KEY));
            assertRedacted();
        } finally {
            if (previous == null) {
                System.clearProperty(KEY);
            } else {
                System.setProperty(KEY, previous);
            }
        }
    }

    @Test
    void environmentOverrideLogsAreMaskedForBothKeySpellings() throws Exception {
        withEnvironmentVariable("RPC_AUTHENTICATION_SECRET", SECRET).execute(() -> {
            assertEquals(SECRET, emptyDelegate().get(KEY));
            assertEquals(SECRET, emptyDelegate().get("RPC_AUTHENTICATION_SECRET"));
            assertRedacted();
        });
    }

    private ImmutablePriorityPropertyDelegate emptyDelegate() {
        return new ImmutablePriorityPropertyDelegate(new ImmutablePropertyDelegate(new Properties()),
                new ImmutableYamlDelegate(new Properties()));
    }

    private void clearSecretCache() throws Exception {
        Field field = ImmutablePriorityPropertyDelegate.class.getDeclaredField("configValueMap");
        field.setAccessible(true);
        Map<?, ?> cache = (Map<?, ?>) field.get(null);
        cache.remove(KEY);
        cache.remove("RPC_AUTHENTICATION_SECRET");
    }

    private String logs() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));
    }

    private void assertRedacted() {
        assertTrue(logs().contains("******"));
        assertFalse(logs().contains(SECRET));
        for (ILoggingEvent event : appender.list) {
            if (event.getArgumentArray() != null) {
                for (Object argument : event.getArgumentArray()) {
                    assertFalse(String.valueOf(argument).contains(SECRET));
                }
            }
        }
    }
}
