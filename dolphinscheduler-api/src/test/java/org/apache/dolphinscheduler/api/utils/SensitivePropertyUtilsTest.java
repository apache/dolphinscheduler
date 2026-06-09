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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.plugin.datasource.api.constants.DataSourceConstants;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

class SensitivePropertyUtilsTest {

    @Test
    void testEncryptSensitiveValuesShouldEncryptOnlySensitiveNonPlaceholder() {
        List<Property> properties = Lists.newArrayList(
                sensitiveProperty("secret_a"),
                new Property("env", Direct.IN, DataType.VARCHAR, "prod"),
                sensitiveProperty(TaskConstants.SENSITIVE_DATA_MASK));

        try (MockedStatic<PropertyUtils> ignored = mockEncryptionEnabled()) {
            List<Property> encrypted = SensitivePropertyUtils.encryptSensitiveValues(properties);

            Assertions.assertNotEquals("secret_a", encrypted.get(0).getValue());
            Assertions.assertEquals("prod", encrypted.get(1).getValue());
            Assertions.assertEquals(TaskConstants.SENSITIVE_DATA_MASK, encrypted.get(2).getValue());
            Assertions.assertEquals("secret_a", PasswordUtils.decodePassword(encrypted.get(0).getValue()));
        }
    }

    @Test
    void testMergeAndEncryptGlobalParamsShouldAvoidDoubleEncryption() {
        String existingGlobalParams;
        try (MockedStatic<PropertyUtils> ignored = mockEncryptionEnabled()) {
            existingGlobalParams = GlobalParameterUtils.serializeGlobalParameter(
                    SensitivePropertyUtils.encryptSensitiveValues(
                            Lists.newArrayList(sensitiveProperty("old_secret"))));
        }

        String submittedGlobalParams = GlobalParameterUtils.serializeGlobalParameter(
                Lists.newArrayList(sensitiveProperty(TaskConstants.SENSITIVE_DATA_MASK)));

        try (MockedStatic<PropertyUtils> ignored = mockEncryptionEnabled()) {
            String merged = SensitivePropertyUtils.mergeAndEncryptGlobalParams(submittedGlobalParams,
                    existingGlobalParams);
            Property saved = GlobalParameterUtils.deserializeGlobalParameter(merged).get(0);

            Assertions.assertNotEquals(TaskConstants.SENSITIVE_DATA_MASK, saved.getValue());
            Assertions.assertEquals("old_secret", PasswordUtils.decodePassword(saved.getValue()));
        }
    }

    @Test
    void testDecryptAndMaskSensitiveValuesShouldReturnPlaceholder() {
        String encryptedValue;
        try (MockedStatic<PropertyUtils> ignored = mockEncryptionEnabled()) {
            encryptedValue = PasswordUtils.encodePassword("db_secret");
        }

        List<Property> properties = Lists.newArrayList(sensitiveProperty(encryptedValue));

        try (MockedStatic<PropertyUtils> ignored = mockEncryptionEnabled()) {
            List<Property> masked = SensitivePropertyUtils.decryptAndMaskSensitiveValues(properties);

            Assertions.assertEquals(TaskConstants.SENSITIVE_DATA_MASK, masked.get(0).getValue());
            Assertions.assertTrue(masked.get(0).isSensitive());
        }
    }

    @Test
    void testDecryptAndMaskDagDataShouldMaskGlobalAndLocalParams() {
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        TaskDefinition taskDefinition = new TaskDefinition();
        String encryptedGlobal;
        String encryptedLocal;
        try (MockedStatic<PropertyUtils> ignored = mockEncryptionEnabled()) {
            encryptedGlobal = PasswordUtils.encodePassword("global_secret");
            encryptedLocal = PasswordUtils.encodePassword("local_secret");
        }

        workflowDefinition.setGlobalParams(GlobalParameterUtils.serializeGlobalParameter(
                Lists.newArrayList(sensitiveProperty(encryptedGlobal))));
        taskDefinition.setTaskParams("{\"localParams\":[{\"prop\":\"secret_local\",\"direct\":\"IN\","
                + "\"type\":\"VARCHAR\",\"value\":\"" + encryptedLocal + "\",\"sensitive\":true}],"
                + "\"rawScript\":\"echo 1\"}");

        DagData dagData = new DagData(workflowDefinition, Collections.emptyList(),
                Collections.singletonList(taskDefinition));

        try (MockedStatic<PropertyUtils> ignored = mockEncryptionEnabled()) {
            SensitivePropertyUtils.decryptAndMaskDagData(dagData);
        }

        Property maskedGlobal = workflowDefinition.getGlobalParamList().get(0);
        Property maskedLocal = SensitivePropertyUtils.getLocalParams(taskDefinition.getTaskParams()).get(0);
        Assertions.assertEquals(TaskConstants.SENSITIVE_DATA_MASK, maskedGlobal.getValue());
        Assertions.assertEquals(TaskConstants.SENSITIVE_DATA_MASK, maskedLocal.getValue());
        Assertions.assertFalse(workflowDefinition.getGlobalParams().contains("global_secret"));
        Assertions.assertFalse(taskDefinition.getTaskParams().contains("local_secret"));
    }

    @Test
    void testMergeAndEncryptLocalParamsInTaskParamsShouldKeepExistingValue() {
        String existingTaskParams;
        try (MockedStatic<PropertyUtils> ignored = mockEncryptionEnabled()) {
            existingTaskParams = "{\"localParams\":[{\"prop\":\"secret_local\",\"direct\":\"IN\","
                    + "\"type\":\"VARCHAR\",\"value\":\""
                    + PasswordUtils.encodePassword("old_local_secret")
                    + "\",\"sensitive\":true}],\"rawScript\":\"echo 1\"}";
        }

        String submittedTaskParams = "{\"localParams\":[{\"prop\":\"secret_local\",\"direct\":\"IN\","
                + "\"type\":\"VARCHAR\",\"value\":\"" + TaskConstants.SENSITIVE_DATA_MASK
                + "\",\"sensitive\":true}],\"rawScript\":\"echo 1\"}";

        try (MockedStatic<PropertyUtils> ignored = mockEncryptionEnabled()) {
            String merged = SensitivePropertyUtils.mergeAndEncryptLocalParamsInTaskParams(submittedTaskParams,
                    existingTaskParams);
            Property saved = SensitivePropertyUtils.getLocalParams(merged).get(0);

            Assertions.assertNotEquals(TaskConstants.SENSITIVE_DATA_MASK, saved.getValue());
            Assertions.assertEquals("old_local_secret", PasswordUtils.decodePassword(saved.getValue()));
            Assertions.assertEquals("echo 1", JSONUtils.getNodeString(merged, "rawScript"));
        }
    }

    private Property sensitiveProperty(String value) {
        return Property.builder()
                .prop("secret")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value(value)
                .sensitive(true)
                .build();
    }

    private MockedStatic<PropertyUtils> mockEncryptionEnabled() {
        MockedStatic<PropertyUtils> propertyUtilsMockedStatic = Mockito.mockStatic(PropertyUtils.class);
        propertyUtilsMockedStatic.when(() -> PropertyUtils.getBoolean(
                DataSourceConstants.DATASOURCE_ENCRYPTION_ENABLE, false)).thenReturn(Boolean.TRUE);
        propertyUtilsMockedStatic.when(() -> PropertyUtils.getString(
                DataSourceConstants.DATASOURCE_ENCRYPTION_SALT,
                DataSourceConstants.DATASOURCE_ENCRYPTION_SALT_DEFAULT))
                .thenReturn(DataSourceConstants.DATASOURCE_ENCRYPTION_SALT_DEFAULT);
        return propertyUtilsMockedStatic;
    }
}
