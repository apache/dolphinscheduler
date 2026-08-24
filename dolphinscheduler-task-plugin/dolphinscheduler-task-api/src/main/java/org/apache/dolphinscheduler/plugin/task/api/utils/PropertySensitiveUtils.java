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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

/**
 * Mask / merge helpers for {@link Property#isSensitive()} without crypto.
 * <p>
 * Keep-original marker is only {@link TaskConstants#SENSITIVE_DATA_MASK} ({@code ******}).
 * Empty / null means a real empty value and must not be treated as keep-original.
 */
@UtilityClass
public class PropertySensitiveUtils {

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

    /**
     * Replace keep-original placeholders with existing DB values.
     * Only {@code ******} is treated as keep-original; empty string is a real empty value.
     */
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

    /**
     * Validate that {@code ******} is only used when an existing sensitive value can be kept.
     * Covers: new sensitive prop with placeholder; {@code false → true} with placeholder.
     *
     * @return first invalid prop name, or null if all valid
     */
    public String findInvalidSensitivePlaceholderProp(List<Property> submittedProperties,
                                                      List<Property> existingProperties) {
        if (CollectionUtils.isEmpty(submittedProperties)) {
            return null;
        }
        Map<String, Property> existingPropertyMap = toPropMap(existingProperties);
        for (Property submitted : submittedProperties) {
            if (!isSensitive(submitted) || !isSensitiveValuePlaceholder(submitted.getValue())) {
                continue;
            }
            Property existing = existingPropertyMap.get(submitted.getProp());
            if (existing == null || !existing.isSensitive()) {
                return submitted.getProp();
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
