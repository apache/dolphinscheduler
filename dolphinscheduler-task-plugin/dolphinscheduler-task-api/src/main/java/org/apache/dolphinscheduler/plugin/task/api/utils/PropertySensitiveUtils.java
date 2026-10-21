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
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

/**
 * Mask / merge helpers for {@link Property#isSensitive()}.
 * Keep-original marker is only {@code ******}; empty / null is a real empty value.
 */
@UtilityClass
public class PropertySensitiveUtils {

    /**
     * TODO: Optimize the sensitive-value identification so sensitive values can be recognized
     * accurately instead of relying on a regex / length heuristic; once that lands, this rule
     * restriction can be removed.
     * <p>
     * Regex rule for a valid sensitive property value. Kept as a regex so the rule can be
     * extended later. It is matched with {@link Pattern#DOTALL} so the whole value must match.
     * <p>
     * Currently it requires at least 6 characters ({@code .{6,}}).
     */
    public static final String SENSITIVE_FIELD_RULE = ".{6,}";

    private static final Pattern SENSITIVE_FIELD_RULE_PATTERN =
            Pattern.compile(SENSITIVE_FIELD_RULE, Pattern.DOTALL);

    public boolean isSensitive(Property property) {
        return property != null && property.isSensitive();
    }

    public boolean isSensitiveValuePlaceholder(String value) {
        return TaskConstants.SENSITIVE_DATA_MASK.equals(value);
    }

    public Property copy(Property property) {
        if (property == null) {
            return null;
        }
        return Property.builder()
                .prop(property.getProp())
                .direct(property.getDirect())
                .type(property.getType())
                .value(property.getValue())
                .sensitive(property.isSensitive())
                .build();
    }

    public List<Property> copy(List<Property> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return Collections.emptyList();
        }
        return properties.stream()
                .map(PropertySensitiveUtils::copy)
                .collect(Collectors.toList());
    }

    public Property maskSensitiveValue(Property property) {
        Property maskedProperty = copy(property);
        if (isSensitive(maskedProperty) && maskedProperty.getValue() != null) {
            maskedProperty.setValue(TaskConstants.SENSITIVE_DATA_MASK);
        }
        return maskedProperty;
    }

    public List<Property> maskSensitiveValues(List<Property> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return Collections.emptyList();
        }
        return properties.stream()
                .map(PropertySensitiveUtils::maskSensitiveValue)
                .collect(Collectors.toList());
    }

    public List<Property> mergeSensitiveValuePlaceholders(List<Property> submittedProperties,
                                                          List<Property> existingProperties) {
        if (CollectionUtils.isEmpty(submittedProperties)) {
            return Collections.emptyList();
        }
        Map<String, Property> existingPropertyMap = toPropMap(existingProperties);

        return submittedProperties.stream()
                .map(PropertySensitiveUtils::copy)
                .peek(property -> mergeSensitiveValuePlaceholder(property, existingPropertyMap))
                .collect(Collectors.toList());
    }

    /** Create: {@code ******} is never a real value. */
    public String findPlaceholderProp(List<Property> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return null;
        }
        for (Property property : properties) {
            if (property != null && isSensitiveValuePlaceholder(property.getValue())) {
                return property.getProp();
            }
        }
        return null;
    }

    /**
     * Update: {@code ******} is keep-original only when an existing property can be merged.
     * {@code false→true} + {@code ******} is allowed; {@code true→false} + {@code ******} is rejected.
     */
    public String findInvalidSensitivePlaceholderProp(List<Property> submittedProperties,
                                                      List<Property> existingProperties) {
        if (CollectionUtils.isEmpty(submittedProperties)) {
            return null;
        }
        Map<String, Property> existingPropertyMap = toPropMap(existingProperties);
        for (Property submitted : submittedProperties) {
            if (!isSensitiveValuePlaceholder(submitted.getValue())) {
                continue;
            }
            Property existing = existingPropertyMap.get(submitted.getProp());
            if (!isSensitive(submitted)) {
                if (existing != null && existing.isSensitive()) {
                    return submitted.getProp();
                }
                continue;
            }
            if (existing == null) {
                return submitted.getProp();
            }
        }
        return null;
    }

    /**
     * Check that every sensitive property has a value which satisfies {@link #SENSITIVE_FIELD_RULE}.
     * <p>
     * Empty / null values and the {@code ******} keep-original placeholder are skipped, since
     * they are not real plaintext inputs and are handled separately by the create / update logic.
     *
     * @return the prop name of the first sensitive property whose value violates the rule,
     *         or {@code null} when every sensitive value is valid.
     */
    public String findInvalidSensitiveField(List<Property> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return null;
        }
        for (Property property : properties) {
            if (property == null || !property.isSensitive()) {
                continue;
            }
            String value = property.getValue();
            if (StringUtils.isEmpty(value) || isSensitiveValuePlaceholder(value)) {
                continue;
            }
            if (!SENSITIVE_FIELD_RULE_PATTERN.matcher(value).matches()) {
                return property.getProp();
            }
        }
        return null;
    }

    private void mergeSensitiveValuePlaceholder(Property submittedProperty,
                                                Map<String, Property> existingPropertyMap) {
        if (!isSensitive(submittedProperty) || !isSensitiveValuePlaceholder(submittedProperty.getValue())) {
            return;
        }
        Property existingProperty = existingPropertyMap.get(submittedProperty.getProp());
        if (existingProperty != null) {
            submittedProperty.setValue(existingProperty.getValue());
        }
    }

    private Map<String, Property> toPropMap(List<Property> properties) {
        return CollectionUtils.emptyIfNull(properties)
                .stream()
                .filter(Objects::nonNull)
                .filter(property -> property.getProp() != null)
                .collect(Collectors.toMap(Property::getProp, Function.identity(), (left, right) -> right));
    }
}
