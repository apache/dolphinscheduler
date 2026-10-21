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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    void findPlaceholderPropOnCreate() {
        Property submitted = sensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK);

        Assertions.assertEquals("pwd",
                PropertySensitiveUtils.findPlaceholderProp(Collections.singletonList(submitted)));
    }

    @Test
    void falseToTrueWithPlaceholderIsKeepOriginal() {
        Property submitted = sensitive("pwd", TaskConstants.SENSITIVE_DATA_MASK);
        Property existing = nonSensitive("pwd", "plain");

        Assertions.assertNull(PropertySensitiveUtils.findInvalidSensitivePlaceholderProp(
                Collections.singletonList(submitted), Collections.singletonList(existing)));
        List<Property> merged = PropertySensitiveUtils.mergeSensitiveValuePlaceholders(
                Collections.singletonList(submitted), Collections.singletonList(existing));
        Assertions.assertEquals("plain", merged.get(0).getValue());
        Assertions.assertTrue(merged.get(0).isSensitive());
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
    void findInvalidSensitiveFieldIsNullWhenAllValuesMatch() {
        List<Property> properties = Arrays.asList(
                sensitive("pwd", "Secret123"),
                sensitive("token", "long-token-value"),
                nonSensitive("name", "a"));

        Assertions.assertNull(PropertySensitiveUtils.findInvalidSensitiveField(properties));
    }

    @Test
    void findInvalidSensitiveFieldReportsPropWhenTooShort() {
        Assertions.assertEquals("pwd",
                PropertySensitiveUtils.findInvalidSensitiveField(
                        Collections.singletonList(sensitive("pwd", "abc"))));
        Assertions.assertEquals("pwd",
                PropertySensitiveUtils.findInvalidSensitiveField(
                        Collections.singletonList(sensitive("pwd", "12345"))));
    }

    @Test
    void findInvalidSensitiveFieldAcceptsLengthGreaterThanOrEqualToSix() {
        // Boundary: exactly 6 characters is the minimum and passes.
        Assertions.assertNull(PropertySensitiveUtils.findInvalidSensitiveField(
                Collections.singletonList(sensitive("pwd", "123456"))));
        Assertions.assertNull(PropertySensitiveUtils.findInvalidSensitiveField(
                Collections.singletonList(sensitive("pwd", "1234567"))));
    }

    @Test
    void findInvalidSensitiveFieldSkipsPlaceholderAndEmpty() {
        List<Property> properties = Arrays.asList(
                sensitive("mask", TaskConstants.SENSITIVE_DATA_MASK),
                sensitive("empty", ""),
                sensitive("null", null));

        Assertions.assertNull(PropertySensitiveUtils.findInvalidSensitiveField(properties));
    }

    @Test
    void findInvalidSensitiveFieldIgnoresNonSensitiveShortValue() {
        List<Property> properties = Arrays.asList(
                nonSensitive("short", "a"),
                sensitive("pwd", "Secret123"));

        Assertions.assertNull(PropertySensitiveUtils.findInvalidSensitiveField(properties));
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

    @Test
    void extractSensitiveValuesReturnsOnlySensitivePlaintext() {
        Map<String, Property> paramsMap = new HashMap<>();
        paramsMap.put("pwd", sensitive("pwd", "Secret123"));
        paramsMap.put("name", nonSensitive("name", "alice"));
        paramsMap.put("empty", sensitive("empty", ""));
        paramsMap.put("placeholder", sensitive("placeholder", TaskConstants.SENSITIVE_DATA_MASK));

        Set<String> sensitiveValues = PropertySensitiveUtils.extractSensitiveValues(paramsMap);

        Assertions.assertEquals(Collections.singleton("Secret123"), sensitiveValues);
    }

    @Test
    void extractSensitiveValuesReturnsEmptyWhenMapIsNullOrEmpty() {
        Assertions.assertTrue(PropertySensitiveUtils.extractSensitiveValues(null).isEmpty());
        Assertions.assertTrue(PropertySensitiveUtils.extractSensitiveValues(new HashMap<>()).isEmpty());
    }

    @Test
    void maskSensitiveValuesReplacesEveryOccurrenceOfKnownValues() {
        String masked = PropertySensitiveUtils.maskSensitiveValues(
                "cmd SuperSecret123; echo SuperSecret123; normal",
                Collections.singleton("SuperSecret123"));

        Assertions.assertEquals("cmd ******; echo ******; normal", masked);
    }

    @Test
    void maskSensitiveValuesMasksLongerValueBeforeContainedOne() {
        // "secret" must not be masked first, otherwise "secret-token" would
        // partially survive as "******-token"
        String masked = PropertySensitiveUtils.maskSensitiveValues(
                "v=secret-token", new HashSet<>(Arrays.asList("secret", "secret-token")));

        Assertions.assertEquals("v=******", masked);
    }

    @Test
    void maskSensitiveValuesHasNoMinimumLengthThreshold() {
        // The values come from the caller's own sensitive params, no heuristic
        // length restriction is applied to them
        String masked = PropertySensitiveUtils.maskSensitiveValues(
                "v=abc", Collections.singleton("abc"));

        Assertions.assertEquals("v=******", masked);
    }

    @Test
    void maskSensitiveValuesKeepsMessageWhenNothingToMask() {
        Assertions.assertEquals("plain message",
                PropertySensitiveUtils.maskSensitiveValues("plain message", Collections.emptySet()));
        Assertions.assertNull(
                PropertySensitiveUtils.maskSensitiveValues(null, Collections.singleton("x")));
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
