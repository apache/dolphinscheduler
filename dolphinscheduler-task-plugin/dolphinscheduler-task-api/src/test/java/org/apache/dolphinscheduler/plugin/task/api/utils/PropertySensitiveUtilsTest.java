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

import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PropertySensitiveUtilsTest {

    @Test
    void maskSensitiveValuesShouldDeepCopyAndMask() {
        Property original = sensitive("pwd", "Secret123");
        List<Property> masked = PropertySensitiveUtils.maskSensitiveValues(Collections.singletonList(original));

        Assertions.assertEquals(TaskConstants.SENSITIVE_DATA_MASK, masked.get(0).getValue());
        Assertions.assertEquals("Secret123", original.getValue());
        Assertions.assertTrue(masked.get(0).isSensitive());
    }

    @Test
    void maskShouldNotChangeNonSensitive() {
        Property original = nonSensitive("name", "alice");
        List<Property> masked = PropertySensitiveUtils.maskSensitiveValues(Collections.singletonList(original));
        Assertions.assertEquals("alice", masked.get(0).getValue());
        Assertions.assertFalse(masked.get(0).isSensitive());
    }

    @Test
    void mergeShouldReplacePlaceholderWithExistingValue() {
        Property submitted = sensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK);
        Property existing = sensitive("pwd", "Secret123");

        List<Property> merged = PropertySensitiveUtils.mergeSensitiveValuePlaceholders(
                Collections.singletonList(submitted), Collections.singletonList(existing));

        Assertions.assertEquals("Secret123", merged.get(0).getValue());
        Assertions.assertEquals(TaskConstants.SENSITIVE_DATA_MASK, submitted.getValue());
    }

    @Test
    void emptyStringIsRealEmptyValueNotKeepOriginal() {
        Property submitted = sensitive("pwd", "");
        Property existing = sensitive("pwd", "Secret123");

        List<Property> merged = PropertySensitiveUtils.mergeSensitiveValuePlaceholders(
                Collections.singletonList(submitted), Collections.singletonList(existing));

        Assertions.assertEquals("", merged.get(0).getValue());
    }

    @Test
    void findInvalidPlaceholderWhenFalseToTrue() {
        Property submitted = sensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK);
        Property existing = nonSensitive("pwd", "plain");

        Assertions.assertEquals("pwd",
                PropertySensitiveUtils.findInvalidSensitivePlaceholderProp(
                        Collections.singletonList(submitted), Collections.singletonList(existing)));
    }

    @Test
    void findInvalidPlaceholderWhenNewSensitiveWithPlaceholder() {
        Property submitted = sensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK);

        Assertions.assertEquals("pwd",
                PropertySensitiveUtils.findInvalidSensitivePlaceholderProp(
                        Collections.singletonList(submitted), Collections.emptyList()));
    }

    @Test
    void validPlaceholderWhenTrueToTrue() {
        Property submitted = sensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK);
        Property existing = sensitive("pwd", "Secret123");

        Assertions.assertNull(PropertySensitiveUtils.findInvalidSensitivePlaceholderProp(
                Collections.singletonList(submitted), Collections.singletonList(existing)));
    }

    @Test
    void findInvalidPlaceholderWhenTrueToFalse() {
        Property submitted = nonSensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK);
        Property existing = sensitive("pwd", "Secret123");

        Assertions.assertEquals("pwd",
                PropertySensitiveUtils.findInvalidSensitivePlaceholderProp(
                        Collections.singletonList(submitted), Collections.singletonList(existing)));
    }

    @Test
    void validPlaceholderAfterToggleBackToSensitive() {
        // Uncheck then check again: final submit is still sensitive + ****** (keep-original).
        Property submitted = sensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK);
        Property existing = sensitive("pwd", "Secret123");

        Assertions.assertNull(PropertySensitiveUtils.findInvalidSensitivePlaceholderProp(
                Collections.singletonList(submitted), Collections.singletonList(existing)));
        List<Property> merged = PropertySensitiveUtils.mergeSensitiveValuePlaceholders(
                Collections.singletonList(submitted), Collections.singletonList(existing));
        Assertions.assertEquals("Secret123", merged.get(0).getValue());
        Assertions.assertTrue(merged.get(0).isSensitive());
    }

    @Test
    void mergeStartParamsSkipsMaskFromMapFormatWithoutSensitiveFlag() {
        Property startParam = nonSensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK);
        Property global = sensitive("pwd", "Secret123");

        List<Property> merged = PropertySensitiveUtils.mergeStartParamsWithGlobalParams(
                Collections.singletonList(startParam), Collections.singletonList(global));

        Assertions.assertEquals(1, merged.size());
        Assertions.assertEquals("Secret123", merged.get(0).getValue());
        Assertions.assertTrue(merged.get(0).isSensitive());
    }

    @Test
    void mergeStartParamsSkipsMaskFromListFormatWithSensitiveFlag() {
        Property startParam = sensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK);
        Property global = sensitive("pwd", "Secret123");

        List<Property> merged = PropertySensitiveUtils.mergeStartParamsWithGlobalParams(
                Collections.singletonList(startParam), Collections.singletonList(global));

        Assertions.assertEquals("Secret123", merged.get(0).getValue());
        Assertions.assertTrue(merged.get(0).isSensitive());
    }

    @Test
    void mergeStartParamsAppliesRealOverride() {
        Property startParam = nonSensitive("pwd", "new-secret");
        Property global = sensitive("pwd", "Secret123");

        List<Property> merged = PropertySensitiveUtils.mergeStartParamsWithGlobalParams(
                Collections.singletonList(startParam), Collections.singletonList(global));

        Assertions.assertEquals("new-secret", merged.get(0).getValue());
    }

    @Test
    void mergeStartParamsDoesNotAddMaskAsNewParam() {
        Property startParam = nonSensitive("token", TaskConstants.SENSITIVE_DATA_MASK);

        List<Property> merged = PropertySensitiveUtils.mergeStartParamsWithGlobalParams(
                Collections.singletonList(startParam), Collections.emptyList());

        Assertions.assertTrue(merged.isEmpty());
    }

    @Test
    void mergeStartParamsEmptyStringIsRealOverride() {
        Property startParam = sensitive("pwd", "");
        Property global = sensitive("pwd", "Secret123");

        List<Property> merged = PropertySensitiveUtils.mergeStartParamsWithGlobalParams(
                Collections.singletonList(startParam), Collections.singletonList(global));

        Assertions.assertEquals("", merged.get(0).getValue());
    }

    @Test
    void serializationRoundTripKeepsSensitiveFlag() {
        Property property = sensitive("token", "abc");
        String json = org.apache.dolphinscheduler.common.utils.JSONUtils.toJsonString(
                Arrays.asList(property));
        List<Property> parsed = org.apache.dolphinscheduler.common.utils.JSONUtils.toList(json, Property.class);
        Assertions.assertTrue(parsed.get(0).isSensitive());
        Assertions.assertEquals("abc", parsed.get(0).getValue());
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
