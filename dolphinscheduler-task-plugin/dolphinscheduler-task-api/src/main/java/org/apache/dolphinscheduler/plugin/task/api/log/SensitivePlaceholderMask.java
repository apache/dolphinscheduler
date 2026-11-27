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

import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertySensitiveUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders {@code ${name}} for a log line. Sensitive properties become {@code ******}
 * at the placeholder only. The same characters elsewhere in the text are left as-is,
 * whatever their length.
 */
public final class SensitivePlaceholderMask {

    private SensitivePlaceholderMask() {
    }

    public static String forLog(final String template, final Map<String, Property> params) {
        return forLog(template, ParameterUtils.convert(params), params);
    }

    public static String forLog(final String template,
                                final Map<String, String> resolvedValues,
                                final Map<String, Property> params) {
        if (StringUtils.isEmpty(template)) {
            return template;
        }
        Map<String, String> logValues = new HashMap<>();
        if (resolvedValues != null) {
            logValues.putAll(resolvedValues);
        }
        if (params != null) {
            for (Property property : params.values()) {
                if (property == null || StringUtils.isEmpty(property.getProp())) {
                    continue;
                }
                if (!logValues.containsKey(property.getProp()) && property.getValue() != null) {
                    logValues.put(property.getProp(), property.getValue());
                }
                if (PropertySensitiveUtils.isSensitive(property)
                        && StringUtils.isNotEmpty(property.getValue())
                        && !TaskConstants.SENSITIVE_DATA_MASK.equals(property.getValue())) {
                    logValues.put(property.getProp(), TaskConstants.SENSITIVE_DATA_MASK);
                }
            }
        }
        if (logValues.isEmpty()) {
            return ParameterUtils.convertParameterPlaceholders(template, Collections.emptyMap());
        }
        return ParameterUtils.convertParameterPlaceholders(template, logValues);
    }
}
