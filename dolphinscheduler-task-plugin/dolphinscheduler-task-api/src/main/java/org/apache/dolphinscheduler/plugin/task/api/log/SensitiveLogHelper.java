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

import static org.apache.dolphinscheduler.common.constants.Constants.LOCAL_PARAMS;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertySensitiveUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Redacts sensitive parameter values at a specific log statement.
 * <p>
 * String masking is literal {@link StringUtils#replace(String, String, String)} of known
 * sensitive values, longest first. It is only for the string about to be logged. Empty
 * values and the {@code ******} placeholder are skipped. Callers must keep the original
 * text for files, JDBC binds, and remote uploads.
 * <p>
 * {@link #toPrettyJson(Object)} pretty-prints a logging-only copy and masks sensitive
 * {@code localParams} / {@code varPool} values. The live object is not modified.
 */
@Slf4j
@UtilityClass
public class SensitiveLogHelper {

    private static final String VAR_POOL = "varPool";

    public String mask(final String text, final Map<String, Property> params) {
        return mask(text, sensitiveValues(params));
    }

    /**
     * Replace known sensitive plaintext values in {@code text}. Longer values are replaced
     * before shorter ones so a value that contains another is not left partially visible.
     */
    public String mask(final String text, final Collection<String> sensitiveValues) {
        if (StringUtils.isEmpty(text) || CollectionUtils.isEmpty(sensitiveValues)) {
            return text;
        }
        List<String> orderedValues = sensitiveValues.stream()
                .filter(SensitiveLogHelper::shouldMask)
                .distinct()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .collect(Collectors.toList());
        String masked = text;
        for (String value : orderedValues) {
            masked = StringUtils.replace(masked, value, TaskConstants.SENSITIVE_DATA_MASK);
        }
        return masked;
    }

    public Set<String> sensitiveValues(final Map<String, Property> params) {
        if (params == null || params.isEmpty()) {
            return Collections.emptySet();
        }
        return sensitiveValues(params.values());
    }

    public Set<String> sensitiveValues(final Collection<Property> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return Collections.emptySet();
        }
        return properties.stream()
                .filter(PropertySensitiveUtils::isSensitive)
                .map(Property::getValue)
                .filter(SensitiveLogHelper::shouldMask)
                .collect(Collectors.toSet());
    }

    /**
     * Pretty JSON for a log line. Sensitive {@code localParams} and {@code varPool} values
     * are masked on a deserialized copy. {@code value} itself is not modified.
     * A JSON object string is parsed the same way so raw {@code taskParams} logs can use this.
     */
    public String toPrettyJson(final Object value) {
        if (value == null) {
            return "null";
        }
        String json = value instanceof String ? (String) value : JSONUtils.toJsonString(value);
        if (StringUtils.isEmpty(json)) {
            return json;
        }
        ObjectNode node = JSONUtils.parseObject(json);
        if (node == null) {
            return value instanceof String ? (String) value : JSONUtils.toPrettyJsonString(value);
        }
        maskPropertyArray(node, LOCAL_PARAMS);
        maskPropertyArray(node, VAR_POOL);
        return JSONUtils.toPrettyJsonString(node);
    }

    private void maskPropertyArray(final ObjectNode node, final String fieldName) {
        JsonNode arrayNode = node.get(fieldName);
        if (arrayNode == null || arrayNode.isNull() || !arrayNode.isArray()) {
            return;
        }
        try {
            List<Property> properties = JSONUtils.toList(arrayNode.toString(), Property.class);
            JsonNode masked = JSONUtils.toJsonNode(PropertySensitiveUtils.maskSensitiveValues(properties));
            if (masked != null) {
                node.set(fieldName, masked);
            }
        } catch (RuntimeException ex) {
            log.debug("Skip masking {} for log because it is not a property list: {}", fieldName, ex.toString());
        }
    }

    private boolean shouldMask(final String value) {
        return StringUtils.isNotEmpty(value) && !Objects.equals(value, TaskConstants.SENSITIVE_DATA_MASK);
    }
}
