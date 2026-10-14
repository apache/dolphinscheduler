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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.MDC;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Sensitive data log converter.
 * <p>
 * Static regex patterns (e.g. datasource password) are global.
 * Per-task sensitive parameter plaintext values are registered dynamically and
 * must be cleared when the task ends to avoid cross-task pollution.
 */
public class SensitiveDataConverter extends MessageConverter {

    private static Pattern multilinePattern;
    private static final Set<String> maskPatterns = new HashSet<>();

    /**
     * Skip very short literals to avoid over-masking common tokens (e.g. "1", "ab").
     */
    private static final int MIN_SENSITIVE_LITERAL_LENGTH = 3;

    /**
     * taskInstanceId -> sensitive plaintext values for that task only.
     * Values are stored longest-first so longer secrets are masked before their prefixes.
     */
    private static final ConcurrentHashMap<Integer, List<String>> TASK_SENSITIVE_VALUES = new ConcurrentHashMap<>();

    static {
        addMaskPattern(TaskConstants.DATASOURCE_PASSWORD_REGEX);
    }

    @Override
    public String convert(ILoggingEvent event) {
        String requestLogMsg = event.getFormattedMessage();
        Integer taskInstanceId = resolveTaskInstanceId(event);
        return maskSensitiveData(requestLogMsg, taskInstanceId);
    }

    public static synchronized void addMaskPattern(final String maskPattern) {
        if (maskPatterns.contains(maskPattern)) {
            return;
        }
        maskPatterns.add(maskPattern);
        multilinePattern = Pattern.compile(String.join("|", maskPatterns), Pattern.MULTILINE);
    }

    /**
     * Register sensitive plaintext values from this task's prepareParamsMap.
     * Empty / {@code ******} / values shorter than {@link #MIN_SENSITIVE_LITERAL_LENGTH} are skipped.
     * Safe to call multiple times (replaces prior set).
     */
    public static void registerTaskSensitiveValues(final int taskInstanceId,
                                                   final Map<String, Property> prepareParamsMap) {
        if (prepareParamsMap == null || prepareParamsMap.isEmpty()) {
            clearTaskSensitiveValues(taskInstanceId);
            return;
        }
        Set<String> values = new HashSet<>();
        for (Property property : prepareParamsMap.values()) {
            if (property == null || !property.isSensitive()) {
                continue;
            }
            String value = property.getValue();
            if (StringUtils.isEmpty(value)
                    || TaskConstants.SENSITIVE_DATA_MASK.equals(value)
                    || value.length() < MIN_SENSITIVE_LITERAL_LENGTH) {
                continue;
            }
            values.add(value);
        }
        if (values.isEmpty()) {
            clearTaskSensitiveValues(taskInstanceId);
            return;
        }
        List<String> ordered = new ArrayList<>(values);
        ordered.sort(Comparator.comparingInt(String::length).reversed());
        TASK_SENSITIVE_VALUES.put(taskInstanceId, Collections.unmodifiableList(ordered));
    }

    public static void clearTaskSensitiveValues(final int taskInstanceId) {
        TASK_SENSITIVE_VALUES.remove(taskInstanceId);
    }

    /** @return unmodifiable view for tests; empty when none registered */
    static Set<String> getTaskSensitiveValues(final int taskInstanceId) {
        List<String> values = TASK_SENSITIVE_VALUES.get(taskInstanceId);
        return values == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(values));
    }

    public static String maskSensitiveData(final String logMsg) {
        return maskSensitiveData(logMsg, resolveTaskInstanceIdFromMDC());
    }

    public static String maskSensitiveData(final String logMsg, final Integer taskInstanceId) {
        if (StringUtils.isEmpty(logMsg)) {
            return logMsg;
        }

        String masked = applyStaticMaskPatterns(logMsg);
        return applyTaskSensitiveLiteralMasks(masked, taskInstanceId);
    }

    private static String applyStaticMaskPatterns(final String logMsg) {
        if (multilinePattern == null) {
            return logMsg;
        }
        final StringBuffer sb = new StringBuffer(logMsg.length());
        final Matcher matcher = multilinePattern.matcher(logMsg);
        while (matcher.find()) {
            matcher.appendReplacement(sb, TaskConstants.SENSITIVE_DATA_MASK);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String applyTaskSensitiveLiteralMasks(final String logMsg, final Integer taskInstanceId) {
        if (taskInstanceId == null) {
            return logMsg;
        }
        List<String> values = TASK_SENSITIVE_VALUES.get(taskInstanceId);
        if (CollectionUtils.isEmpty(values)) {
            return logMsg;
        }
        String masked = logMsg;
        for (String value : values) {
            masked = StringUtils.replace(masked, value, TaskConstants.SENSITIVE_DATA_MASK);
        }
        return masked;
    }

    private static Integer resolveTaskInstanceId(final ILoggingEvent event) {
        if (event == null || event.getMDCPropertyMap() == null) {
            return resolveTaskInstanceIdFromMDC();
        }
        return parseTaskInstanceId(event.getMDCPropertyMap().get(TaskConstants.TASK_INSTANCE_ID_MDC_KEY));
    }

    private static Integer resolveTaskInstanceIdFromMDC() {
        return parseTaskInstanceId(MDC.get(TaskConstants.TASK_INSTANCE_ID_MDC_KEY));
    }

    private static Integer parseTaskInstanceId(final String raw) {
        if (StringUtils.isEmpty(raw)) {
            return null;
        }
        try {
            return Integer.valueOf(raw);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

}
