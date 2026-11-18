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

package org.apache.dolphinscheduler.dao.crypto;

import static org.apache.dolphinscheduler.plugin.datasource.api.constants.DataSourceConstants.DATASOURCE_ENCRYPTION_ENABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SensitiveDefinitionParamCodecTest {

    @Test
    void encryptAndDecryptGlobalParams() {
        withEncryptionEnabled(() -> {
            String plaintext = GlobalParameterUtils.serializeGlobalParameter(
                    Arrays.asList(sensitive("pwd", "secret-1"), plain("name", "alice")));
            String encrypted = SensitiveDefinitionParamCodec.encryptGlobalParams(plaintext);
            List<Property> stored = GlobalParameterUtils.deserializeGlobalParameter(encrypted);
            assertEquals(PasswordUtils.encodePassword("secret-1"), valueOf(stored, "pwd"));
            assertEquals("alice", valueOf(stored, "name"));
            assertNotEquals("secret-1", valueOf(stored, "pwd"));

            String decoded = SensitiveDefinitionParamCodec.decryptGlobalParams(encrypted);
            List<Property> loaded = GlobalParameterUtils.deserializeGlobalParameter(decoded);
            assertEquals("secret-1", valueOf(loaded, "pwd"));
            assertEquals("alice", valueOf(loaded, "name"));
        });
    }

    @Test
    void encryptIsIdempotentForAlreadyEncodedValues() {
        withEncryptionEnabled(() -> {
            String once = GlobalParameterUtils.serializeGlobalParameter(
                    Arrays.asList(sensitive("pwd", "secret-1")));
            String first = SensitiveDefinitionParamCodec.encryptGlobalParams(once);
            String second = SensitiveDefinitionParamCodec.encryptGlobalParams(first);
            assertEquals(first, second);
            assertEquals("secret-1", valueOf(
                    GlobalParameterUtils.deserializeGlobalParameter(
                            SensitiveDefinitionParamCodec.decryptGlobalParams(second)),
                    "pwd"));
        });
    }

    @Test
    void skipPlaceholderAndEmpty() {
        withEncryptionEnabled(() -> {
            String plaintext = GlobalParameterUtils.serializeGlobalParameter(Arrays.asList(
                    sensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK),
                    sensitive("empty", ""),
                    sensitive("keep", "plain-secret")));
            String encrypted = SensitiveDefinitionParamCodec.encryptGlobalParams(plaintext);
            List<Property> stored = GlobalParameterUtils.deserializeGlobalParameter(encrypted);
            assertEquals(TaskConstants.SENSITIVE_DATA_MASK, valueOf(stored, "pwd"));
            assertEquals("", valueOf(stored, "empty"));
            assertEquals(PasswordUtils.encodePassword("plain-secret"), valueOf(stored, "keep"));
        });
    }

    @Test
    void encryptionOffLeavesValuesUnchanged() {
        String plaintext = GlobalParameterUtils.serializeGlobalParameter(
                Arrays.asList(sensitive("pwd", "secret-1")));
        assertEquals(plaintext, SensitiveDefinitionParamCodec.encryptGlobalParams(plaintext));
        assertEquals(plaintext, SensitiveDefinitionParamCodec.decryptGlobalParams(plaintext));
        assertFalse(SensitiveDefinitionParamCodec.isEncryptionEnabled());
    }

    @Test
    void encryptAndDecryptTaskParamsLocalParams() {
        withEncryptionEnabled(() -> {
            String taskParams = "{\"localParams\":"
                    + GlobalParameterUtils.serializeGlobalParameter(
                            Arrays.asList(sensitive("token", "t-secret"), plain("path", "/tmp")))
                    + ",\"rawScript\":\"echo\"}";
            String encrypted = SensitiveDefinitionParamCodec.encryptTaskParams(taskParams);
            Property token = localParam(encrypted, "token");
            Property path = localParam(encrypted, "path");
            assertEquals(PasswordUtils.encodePassword("t-secret"), token.getValue());
            assertEquals("/tmp", path.getValue());
            assertTrue(JSONUtils.parseObject(encrypted).has("rawScript"));

            String decoded = SensitiveDefinitionParamCodec.decryptTaskParams(encrypted);
            assertEquals("t-secret", localParam(decoded, "token").getValue());
            assertEquals("/tmp", localParam(decoded, "path").getValue());
            assertEquals("echo", JSONUtils.parseObject(decoded).get("rawScript").asText());
        });
    }

    private static void withEncryptionEnabled(Runnable action) {
        try (
                MockedStatic<PropertyUtils> propertyUtils =
                        mockStatic(PropertyUtils.class, Mockito.CALLS_REAL_METHODS)) {
            propertyUtils.when(() -> PropertyUtils.getBoolean(DATASOURCE_ENCRYPTION_ENABLE, false))
                    .thenReturn(true);
            action.run();
        }
    }

    private static Property sensitive(String name, String value) {
        return Property.builder()
                .prop(name)
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value(value)
                .sensitive(true)
                .build();
    }

    private static Property plain(String name, String value) {
        return Property.builder()
                .prop(name)
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value(value)
                .sensitive(false)
                .build();
    }

    private static String valueOf(List<Property> properties, String name) {
        return properties.stream()
                .filter(property -> name.equals(property.getProp()))
                .findFirst()
                .map(Property::getValue)
                .orElse(null);
    }

    private static Property localParam(String taskParams, String name) {
        List<Property> properties = JSONUtils.toList(
                JSONUtils.parseObject(taskParams).get("localParams").toString(), Property.class);
        return properties.stream()
                .filter(property -> name.equals(property.getProp()))
                .findFirst()
                .orElseThrow();
    }
}
