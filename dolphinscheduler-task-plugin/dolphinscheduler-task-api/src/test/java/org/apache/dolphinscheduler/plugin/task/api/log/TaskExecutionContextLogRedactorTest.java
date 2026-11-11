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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class TaskExecutionContextLogRedactorTest {

    @Test
    void redactsPrepareParamsAndGlobalParamsWithoutMutatingSource() {
        String globalSecret = "GlobalSecretValue";
        String localSecret = "LocalSecretValue";

        TaskExecutionContext source = new TaskExecutionContext();
        source.setTaskInstanceId(9);
        source.setGlobalParams(GlobalParameterUtils.serializeGlobalParameter(
                Collections.singletonList(sensitive("token", globalSecret))));

        Map<String, Property> prepare = new HashMap<>();
        // Local override: prepareParamsMap no longer holds the global plaintext,
        // but globalParams still does — redactor must mask both.
        prepare.put("token", sensitive("token", localSecret));
        prepare.put("name", nonSensitive("name", "alice"));
        source.setPrepareParamsMap(prepare);
        source.setTaskParams("{\"localParams\":[{\"prop\":\"pwd\",\"direct\":\"IN\","
                + "\"type\":\"VARCHAR\",\"value\":\"TaskParamSecret\",\"sensitive\":true}]}");
        source.setVarPool(Collections.singletonList(sensitive("pool", "VarPoolSecret")));

        TaskExecutionContext redacted = TaskExecutionContextLogRedactor.redactForLog(source);
        assertNotSame(source, redacted);

        assertEquals(localSecret, source.getPrepareParamsMap().get("token").getValue());
        assertTrue(source.getGlobalParams().contains(globalSecret));
        assertTrue(source.getTaskParams().contains("TaskParamSecret"));
        assertEquals("VarPoolSecret", source.getVarPool().get(0).getValue());

        assertEquals(TaskConstants.SENSITIVE_DATA_MASK,
                redacted.getPrepareParamsMap().get("token").getValue());
        assertEquals("alice", redacted.getPrepareParamsMap().get("name").getValue());

        List<Property> redactedGlobals =
                GlobalParameterUtils.deserializeGlobalParameter(redacted.getGlobalParams());
        assertEquals(TaskConstants.SENSITIVE_DATA_MASK, redactedGlobals.get(0).getValue());

        assertFalse(redacted.getTaskParams().contains("TaskParamSecret"));
        assertTrue(redacted.getTaskParams().contains(TaskConstants.SENSITIVE_DATA_MASK));
        assertEquals(TaskConstants.SENSITIVE_DATA_MASK, redacted.getVarPool().get(0).getValue());

        String json = JSONUtils.toPrettyJsonString(redacted);
        assertFalse(json.contains(globalSecret));
        assertFalse(json.contains(localSecret));
        assertFalse(json.contains("TaskParamSecret"));
        assertFalse(json.contains("VarPoolSecret"));
    }

    @Test
    void copyFailureReturnsSafeStubWithoutSensitivePayloads() {
        String secret = "CopyFailSecret";
        TaskExecutionContext source = new TaskExecutionContext();
        source.setTaskInstanceId(42);
        source.setTaskName("shell-task");
        source.setTaskType("SHELL");
        source.setWorkflowInstanceId(7);
        Map<String, Property> prepare = new HashMap<>();
        prepare.put("token", sensitive("token", secret));
        source.setPrepareParamsMap(prepare);
        source.setGlobalParams(GlobalParameterUtils.serializeGlobalParameter(
                Collections.singletonList(sensitive("token", secret))));
        source.setTaskParams("{\"localParams\":[{\"prop\":\"token\",\"value\":\"" + secret
                + "\",\"sensitive\":true}]}");
        source.setVarPool(Collections.singletonList(sensitive("token", secret)));

        TaskExecutionContext redacted = TaskExecutionContextLogRedactor.redactForLog(source, ignored -> null);

        assertNotSame(source, redacted);
        assertEquals(42, redacted.getTaskInstanceId());
        assertEquals("shell-task", redacted.getTaskName());
        assertEquals("SHELL", redacted.getTaskType());
        assertEquals(7, redacted.getWorkflowInstanceId());
        assertNull(redacted.getPrepareParamsMap());
        assertNull(redacted.getGlobalParams());
        assertNull(redacted.getTaskParams());
        assertNull(redacted.getVarPool());

        String json = JSONUtils.toPrettyJsonString(redacted);
        assertFalse(json.contains(secret));
        assertEquals(secret, source.getPrepareParamsMap().get("token").getValue());
    }

    @Test
    void copyExceptionReturnsSafeStubWithoutSensitivePayloads() {
        String secret = "CopyExceptionSecret";
        TaskExecutionContext source = new TaskExecutionContext();
        source.setTaskInstanceId(43);
        Map<String, Property> prepare = new HashMap<>();
        prepare.put("token", sensitive("token", secret));
        source.setPrepareParamsMap(prepare);

        TaskExecutionContext redacted = TaskExecutionContextLogRedactor.redactForLog(source, ignored -> {
            throw new IllegalStateException("boom");
        });

        assertNotSame(source, redacted);
        assertEquals(43, redacted.getTaskInstanceId());
        assertNull(redacted.getPrepareParamsMap());
        assertFalse(JSONUtils.toPrettyJsonString(redacted).contains(secret));
    }

    @Test
    void prettyJsonOfRedactedContextHidesDoubleEscapedSecret() {
        String secret = "quote\"and\\slash\nline";
        String oneLevel = SensitiveDataConverter.jsonEscape(secret);
        String doubleEscaped = SensitiveDataConverter.jsonEscape(oneLevel);

        TaskExecutionContext source = new TaskExecutionContext();
        source.setTaskInstanceId(11);
        source.setGlobalParams(GlobalParameterUtils.serializeGlobalParameter(
                Collections.singletonList(sensitive("token", secret))));
        Map<String, Property> prepare = new HashMap<>();
        prepare.put("token", sensitive("token", secret));
        source.setPrepareParamsMap(prepare);
        source.setTaskParams(JSONUtils.toJsonString(Collections.singletonMap(
                "localParams", Collections.singletonList(sensitive("token", secret)))));

        String raw = JSONUtils.toPrettyJsonString(source);
        assertTrue(raw.contains(doubleEscaped));

        String redactedJson =
                JSONUtils.toPrettyJsonString(TaskExecutionContextLogRedactor.redactForLog(source));
        assertFalse(redactedJson.contains(secret));
        assertFalse(redactedJson.contains(oneLevel));
        assertFalse(redactedJson.contains(doubleEscaped));
        assertTrue(redactedJson.contains(TaskConstants.SENSITIVE_DATA_MASK));
        assertEquals(secret, source.getPrepareParamsMap().get("token").getValue());
        assertTrue(source.getGlobalParams().contains(oneLevel));
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
