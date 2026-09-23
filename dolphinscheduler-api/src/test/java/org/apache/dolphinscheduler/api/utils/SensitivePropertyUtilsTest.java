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

package org.apache.dolphinscheduler.api.utils;

import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ErrorCommand;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstanceDependentDetails;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.master.command.ICommandParam;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SensitivePropertyUtilsTest {

    @Test
    void requireNoPlaceholderRejectsMaskOnCreate() {
        Assertions.assertThrows(ServiceException.class,
                () -> SensitivePropertyUtils.requireNoPlaceholder(
                        Collections.singletonList(sensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK))));
    }

    @Test
    void mergeKeepsOriginalOnPlaceholder() {
        List<Property> merged = SensitivePropertyUtils.merge(
                Collections.singletonList(sensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK)),
                Collections.singletonList(sensitive("pwd", "Secret123")));

        Assertions.assertEquals("Secret123", merged.get(0).getValue());
        Assertions.assertTrue(merged.get(0).isSensitive());
    }

    @Test
    void mergeKeepsOriginalWhenFalseToTrueWithPlaceholder() {
        List<Property> merged = SensitivePropertyUtils.merge(
                Collections.singletonList(sensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK)),
                Collections.singletonList(nonSensitive("pwd", "plain")));

        Assertions.assertEquals("plain", merged.get(0).getValue());
        Assertions.assertTrue(merged.get(0).isSensitive());
    }

    @Test
    void mergeRejectsTrueToFalseWithPlaceholder() {
        Assertions.assertThrows(ServiceException.class,
                () -> SensitivePropertyUtils.merge(
                        Collections.singletonList(nonSensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK)),
                        Collections.singletonList(sensitive("pwd", "Secret123"))));
    }

    @Test
    void restoreStartParamsUsesGlobalPlaintext() {
        List<Property> restored = SensitivePropertyUtils.restoreStartParams(
                Collections.singletonList(sensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK)),
                Collections.singletonList(sensitive("pwd", "Secret123")));

        Assertions.assertEquals("Secret123", restored.get(0).getValue());
        Assertions.assertTrue(restored.get(0).isSensitive());
    }

    @Test
    void restoreStartParamsKeepsUserOverride() {
        List<Property> restored = SensitivePropertyUtils.restoreStartParams(
                Collections.singletonList(sensitive("pwd", "new-secret")),
                Collections.singletonList(sensitive("pwd", "Secret123")));

        Assertions.assertEquals("new-secret", restored.get(0).getValue());
        Assertions.assertTrue(restored.get(0).isSensitive());
    }

    @Test
    void restoreStartParamsInheritsGlobalAttributesForSameName() {
        // Map/start transform builds Property without sensitive; same-name must inherit from global.
        List<Property> restored = SensitivePropertyUtils.restoreStartParams(
                Collections.singletonList(nonSensitive("pwd", "new-secret")),
                Collections.singletonList(sensitive("pwd", "Secret123")));

        Assertions.assertEquals("new-secret", restored.get(0).getValue());
        Assertions.assertTrue(restored.get(0).isSensitive());
        Assertions.assertEquals(Direct.IN, restored.get(0).getDirect());
        Assertions.assertEquals(DataType.VARCHAR, restored.get(0).getType());
    }

    @Test
    void restoreStartParamsUsesGlobalWhenSensitiveFlagMissing() {
        List<Property> restored = SensitivePropertyUtils.restoreStartParams(
                Collections.singletonList(new Property("pwd", Direct.IN, DataType.VARCHAR,
                        TaskConstants.SENSITIVE_DATA_MASK)),
                Collections.singletonList(sensitive("pwd", "Secret123")));

        Assertions.assertEquals("Secret123", restored.get(0).getValue());
        Assertions.assertTrue(restored.get(0).isSensitive());
    }

    @Test
    void restoreStartParamsDropsPlaceholderWithoutGlobal() {
        List<Property> restored = SensitivePropertyUtils.restoreStartParams(
                Collections.singletonList(sensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK)),
                Collections.emptyList());

        Assertions.assertTrue(restored.isEmpty());
    }

    @Test
    void restoreStartParamsNonMatchingNameIsNonSensitive() {
        List<Property> restored = SensitivePropertyUtils.restoreStartParams(
                Collections.singletonList(sensitive("extra", "plain")),
                Collections.singletonList(sensitive("pwd", "Secret123")));

        Assertions.assertEquals(1, restored.size());
        Assertions.assertEquals("extra", restored.get(0).getProp());
        Assertions.assertEquals("plain", restored.get(0).getValue());
        Assertions.assertFalse(restored.get(0).isSensitive());
    }

    @Test
    void emptyStringIsPersistedAsEmpty() {
        List<Property> merged = SensitivePropertyUtils.merge(
                Collections.singletonList(sensitive("pwd", "")),
                Collections.singletonList(sensitive("pwd", "Secret123")));

        Assertions.assertEquals("", merged.get(0).getValue());
    }

    @Test
    void mergeLocalParamsRejectsPlaceholderWhenNoExisting() {
        String submitted =
                "{\"localParams\":[{\"prop\":\"token\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\""
                        + TaskConstants.SENSITIVE_DATA_MASK + "\",\"sensitive\":true}]}";

        Assertions.assertThrows(ServiceException.class,
                () -> SensitivePropertyUtils.mergeLocalParams(submitted, null));
    }

    @Test
    void mergeLocalParamsRejectsTrueToFalseWithPlaceholder() {
        String existing =
                "{\"localParams\":[{\"prop\":\"token\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"abc\",\"sensitive\":true}]}";
        String submitted =
                "{\"localParams\":[{\"prop\":\"token\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\""
                        + TaskConstants.SENSITIVE_DATA_MASK + "\",\"sensitive\":false}]}";

        Assertions.assertThrows(ServiceException.class,
                () -> SensitivePropertyUtils.mergeLocalParams(submitted, existing));
    }

    @Test
    @SuppressWarnings("unchecked")
    void maskDoesNotMutateLocalParamsMap() {
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put(TaskConstants.LOCAL_PARAMS_LIST, Collections.singletonList(sensitive("token", "abc")));
        Map<String, Map<String, Object>> localParams = new LinkedHashMap<>();
        localParams.put("shell-1", inner);

        Map<String, Map<String, Object>> masked = SensitivePropertyUtils.mask(localParams);

        Assertions.assertEquals("abc",
                ((List<Property>) localParams.get("shell-1").get(TaskConstants.LOCAL_PARAMS_LIST)).get(0).getValue());
        Assertions.assertEquals(TaskConstants.SENSITIVE_DATA_MASK,
                ((List<Property>) masked.get("shell-1").get(TaskConstants.LOCAL_PARAMS_LIST)).get(0).getValue());
    }

    @Test
    void maskDoesNotMutateTaskInstanceDependentDetails() {
        TaskInstanceDependentDetails<?> original = new TaskInstanceDependentDetails<>();
        original.setTaskParams("{\"localParams\":[{\"prop\":\"token\",\"direct\":\"IN\",\"type\":\"VARCHAR\","
                + "\"value\":\"abc\",\"sensitive\":true}]}");
        original.setVarPool("[{\"prop\":\"pwd\",\"direct\":\"IN\",\"type\":\"VARCHAR\","
                + "\"value\":\"Secret123\",\"sensitive\":true}]");

        TaskInstance masked = SensitivePropertyUtils.mask(original);

        Assertions.assertTrue(masked.getTaskParams().contains(TaskConstants.SENSITIVE_DATA_MASK));
        Assertions.assertTrue(masked.getVarPool().contains(TaskConstants.SENSITIVE_DATA_MASK));
        Assertions.assertTrue(original.getTaskParams().contains("abc"));
        Assertions.assertTrue(original.getVarPool().contains("Secret123"));
        Assertions.assertFalse(masked.getTaskParams().contains("\"abc\""));
        Assertions.assertNotSame(original, masked);
    }

    @Test
    void maskDoesNotMutateWorkflowDefinition() {
        WorkflowDefinition original = new WorkflowDefinition();
        original.setGlobalParams("[{\"prop\":\"pwd\",\"direct\":\"IN\",\"type\":\"VARCHAR\","
                + "\"value\":\"Secret123\",\"sensitive\":true}]");

        WorkflowDefinition masked = SensitivePropertyUtils.mask(original);

        Assertions.assertTrue(original.getGlobalParams().contains("Secret123"));
        Assertions.assertTrue(masked.getGlobalParams().contains(TaskConstants.SENSITIVE_DATA_MASK));
        Assertions.assertEquals(TaskConstants.SENSITIVE_DATA_MASK, masked.getGlobalParamMap().get("pwd"));
        Assertions.assertEquals("Secret123", original.getGlobalParamMap().get("pwd"));
    }

    @Test
    void maskWorkflowInstanceMasksCommandParam() {
        String plaintextCommandParam = JSONUtils.toJsonString(RunWorkflowCommandParam.builder()
                .commandParams(Collections.singletonList(sensitive("pwd", "Secret123")))
                .timeZone("UTC")
                .build());
        WorkflowInstance original = new WorkflowInstance();
        original.setCommandParam(plaintextCommandParam);
        original.setGlobalParams("[{\"prop\":\"pwd\",\"direct\":\"IN\",\"type\":\"VARCHAR\","
                + "\"value\":\"Secret123\",\"sensitive\":true}]");

        WorkflowInstance masked = SensitivePropertyUtils.mask(original);

        Assertions.assertEquals(plaintextCommandParam, original.getCommandParam());
        Assertions.assertTrue(original.getCommandParam().contains("Secret123"));
        Assertions.assertFalse(masked.getCommandParam().contains("Secret123"));
        ICommandParam maskedCommandParam =
                JSONUtils.parseObject(masked.getCommandParam(), ICommandParam.class);
        Assertions.assertNotNull(maskedCommandParam);
        Assertions.assertEquals(TaskConstants.SENSITIVE_DATA_MASK,
                maskedCommandParam.getCommandParams().get(0).getValue());
        Assertions.assertTrue(maskedCommandParam.getCommandParams().get(0).isSensitive());
        Assertions.assertTrue(masked.getGlobalParams().contains(TaskConstants.SENSITIVE_DATA_MASK));
        Assertions.assertNotSame(original, masked);
    }

    @Test
    void maskCommandMasksSensitiveCommandParam() {
        String plaintextCommandParam = JSONUtils.toJsonString(RunWorkflowCommandParam.builder()
                .commandParams(Collections.singletonList(sensitive("pwd", "Secret123")))
                .timeZone("UTC")
                .build());
        Command original = Command.builder().commandParam(plaintextCommandParam).build();

        Command masked = SensitivePropertyUtils.mask(original);

        Assertions.assertEquals(plaintextCommandParam, original.getCommandParam());
        Assertions.assertFalse(masked.getCommandParam().contains("Secret123"));
        Assertions.assertTrue(masked.getCommandParam().contains(TaskConstants.SENSITIVE_DATA_MASK));
        Assertions.assertNotSame(original, masked);
    }

    @Test
    void maskErrorCommandMasksSensitiveCommandParam() {
        String plaintextCommandParam = JSONUtils.toJsonString(RunWorkflowCommandParam.builder()
                .commandParams(Collections.singletonList(sensitive("pwd", "Secret123")))
                .timeZone("UTC")
                .build());
        ErrorCommand original = new ErrorCommand();
        original.setCommandParam(plaintextCommandParam);

        ErrorCommand masked = SensitivePropertyUtils.mask(original);

        Assertions.assertEquals(plaintextCommandParam, original.getCommandParam());
        Assertions.assertFalse(masked.getCommandParam().contains("Secret123"));
        Assertions.assertTrue(masked.getCommandParam().contains(TaskConstants.SENSITIVE_DATA_MASK));
        Assertions.assertNotSame(original, masked);
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
