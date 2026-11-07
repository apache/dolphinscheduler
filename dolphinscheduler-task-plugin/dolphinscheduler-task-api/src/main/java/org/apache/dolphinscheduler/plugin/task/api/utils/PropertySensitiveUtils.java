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
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Mask / merge helpers for {@link Property#isSensitive()}.
 * Keep-original marker is only {@code ******}; empty / null is a real empty value.
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
     * Deep-copy then apply {@code transformer} to each non-empty {@code sensitive=true} value.
     * Used for definition-time encode / execution-time decode without pulling crypto deps into task-api.
     */
    public List<Property> transformSensitiveValues(List<Property> properties,
                                                   Function<String, String> transformer) {
        if (CollectionUtils.isEmpty(properties)) {
            return Collections.emptyList();
        }
        return properties.stream()
                .map(property -> transformSensitiveValue(property, transformer))
                .collect(Collectors.toList());
    }

    public Property transformSensitiveValue(Property property, Function<String, String> transformer) {
        Property copied = copy(property);
        if (isSensitive(copied) && StringUtils.isNotEmpty(copied.getValue())) {
            copied.setValue(transformer.apply(copied.getValue()));
        }
        return copied;
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

    /**
     * Rewrite {@code localParams} inside a taskParams JSON string.
     * Returns the original string when missing / unparsable.
     */
    public String transformLocalParamsInTaskParams(String taskParams,
                                                   Function<List<Property>, List<Property>> transform) {
        if (StringUtils.isEmpty(taskParams) || transform == null) {
            return taskParams;
        }
        ObjectNode taskParamsNode = JSONUtils.parseObject(taskParams);
        if (taskParamsNode == null) {
            return taskParams;
        }
        JsonNode localParamsNode = taskParamsNode.findValue("localParams");
        if (localParamsNode == null || localParamsNode.isNull()) {
            return taskParams;
        }
        List<Property> localParams = JSONUtils.toList(localParamsNode.toString(), Property.class);
        taskParamsNode.set("localParams", JSONUtils.toJsonNode(transform.apply(localParams)));
        return JSONUtils.toJsonString(taskParamsNode);
    }
}
