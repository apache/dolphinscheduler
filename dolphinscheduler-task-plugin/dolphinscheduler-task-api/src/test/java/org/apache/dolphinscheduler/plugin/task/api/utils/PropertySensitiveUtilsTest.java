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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

class PropertySensitiveUtilsTest {

    @Test
    void testDeserializePropertyWithoutSensitiveShouldUseDefaultFalse() {
        Property property = JSONUtils.parseObject(
                "{\"prop\":\"password\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"secret\"}",
                Property.class);

        Assertions.assertNotNull(property);
        Assertions.assertFalse(property.isSensitive());
    }

    @Test
    void testMaskSensitiveValuesShouldNotMutateSourceProperties() {
        Property sensitiveProperty = Property.builder()
                .prop("password")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value("secret")
                .sensitive(true)
                .build();
        Property normalProperty = new Property("env", Direct.IN, DataType.VARCHAR, "prod");

        List<Property> result = PropertySensitiveUtils.maskSensitiveValues(
                Lists.newArrayList(sensitiveProperty, normalProperty));

        Assertions.assertEquals(TaskConstants.SENSITIVE_DATA_MASK, result.get(0).getValue());
        Assertions.assertTrue(result.get(0).isSensitive());
        Assertions.assertEquals("prod", result.get(1).getValue());
        Assertions.assertFalse(result.get(1).isSensitive());
        Assertions.assertEquals("secret", sensitiveProperty.getValue());
    }

    @Test
    void testMergeSensitiveValuePlaceholdersShouldKeepExistingValue() {
        Property submittedProperty = Property.builder()
                .prop("password")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value(TaskConstants.SENSITIVE_DATA_MASK)
                .sensitive(true)
                .build();
        Property existingProperty = Property.builder()
                .prop("password")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value("encrypted-secret")
                .sensitive(true)
                .build();

        List<Property> result = PropertySensitiveUtils.mergeSensitiveValuePlaceholders(
                Lists.newArrayList(submittedProperty),
                Lists.newArrayList(existingProperty));

        Assertions.assertEquals("encrypted-secret", result.get(0).getValue());
        Assertions.assertEquals(TaskConstants.SENSITIVE_DATA_MASK, submittedProperty.getValue());
    }

    @Test
    void testMergeSensitiveValuePlaceholdersShouldKeepSubmittedValueWhenNotPlaceholder() {
        Property submittedProperty = Property.builder()
                .prop("password")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value("new-secret")
                .sensitive(true)
                .build();
        Property existingProperty = Property.builder()
                .prop("password")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value("old-secret")
                .sensitive(true)
                .build();

        List<Property> result = PropertySensitiveUtils.mergeSensitiveValuePlaceholders(
                Lists.newArrayList(submittedProperty),
                Lists.newArrayList(existingProperty));

        Assertions.assertEquals("new-secret", result.get(0).getValue());
    }
}
