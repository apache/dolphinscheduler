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

import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class SensitiveLogHelperTest {

    @Test
    void maskReplacesSensitiveValueAndKeepsNonSensitive() {
        Map<String, Property> params = new HashMap<>();
        params.put("pwd", sensitive("pwd", "SuperSecret123"));
        params.put("name", nonSensitive("name", "hello"));

        String masked = SensitiveLogHelper.mask("echo SuperSecret123 and hello", params);

        assertEquals("echo ****** and hello", masked);
        assertEquals("SuperSecret123", params.get("pwd").getValue());
        assertEquals("hello", params.get("name").getValue());
    }

    @Test
    void maskSkipsEmptyAndPlaceholder() {
        Map<String, Property> params = new HashMap<>();
        params.put("empty", sensitive("empty", ""));
        params.put("blank", sensitive("blank", null));
        params.put("mask", sensitive("mask", TaskConstants.SENSITIVE_DATA_MASK));

        String text = "keep ****** and the rest";
        assertEquals(text, SensitiveLogHelper.mask(text, params));
        assertTrue(SensitiveLogHelper.sensitiveValues(params).isEmpty());
    }

    @Test
    void longerSecretReplacedBeforeItsPrefix() {
        Map<String, Property> params = new HashMap<>();
        params.put("short", sensitive("short", "secret"));
        params.put("long", sensitive("long", "secret-token"));

        String masked = SensitiveLogHelper.mask("secret-token", params);

        assertEquals(TaskConstants.SENSITIVE_DATA_MASK, masked);
        assertFalse(masked.contains("******-token"));
    }

    @Test
    void nonSensitivePrefixSurvivesWhenLongerSecretIsMasked() {
        Map<String, Property> params = new HashMap<>();
        params.put("long", sensitive("long", "secret-token"));
        params.put("short", nonSensitive("short", "secret"));

        assertEquals("secret and ******", SensitiveLogHelper.mask("secret and secret-token", params));
    }

    @Test
    void toPrettyJsonMasksLocalParamsAndVarPoolWithoutMutatingSource() {
        Property local = sensitive("token", "LocalSecretValue");
        Property plain = nonSensitive("user", "alice");
        Property pooled = sensitive("token", "PoolSecretValue");
        Map<String, Object> source = new HashMap<>();
        source.put("rawScript", "echo hello");
        source.put("localParams", Arrays.asList(local, plain));
        source.put("varPool", Collections.singletonList(pooled));

        String json = SensitiveLogHelper.toPrettyJson(source);

        assertTrue(json.contains(TaskConstants.SENSITIVE_DATA_MASK));
        assertTrue(json.contains("alice"));
        assertTrue(json.contains("echo hello"));
        assertFalse(json.contains("LocalSecretValue"));
        assertFalse(json.contains("PoolSecretValue"));
        assertEquals("LocalSecretValue", local.getValue());
        assertEquals("PoolSecretValue", pooled.getValue());
    }

    @Test
    void toPrettyJsonMasksTaskParamsJsonString() {
        String taskParams = "{\"rawScript\":\"echo 1\",\"localParams\":[{\"prop\":\"pwd\",\"direct\":\"IN\","
                + "\"type\":\"VARCHAR\",\"value\":\"db-secret\",\"sensitive\":true}]}";

        String json = SensitiveLogHelper.toPrettyJson(taskParams);

        assertFalse(json.contains("db-secret"));
        assertTrue(json.contains(TaskConstants.SENSITIVE_DATA_MASK));
        assertTrue(taskParams.contains("db-secret"));
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

    private static Property nonSensitive(String prop, String value) {
        return Property.builder()
                .prop(prop)
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value(value)
                .sensitive(false)
                .build();
    }
}
