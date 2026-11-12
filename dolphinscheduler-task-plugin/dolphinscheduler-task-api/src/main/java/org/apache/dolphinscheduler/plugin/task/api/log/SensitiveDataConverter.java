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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;

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
import org.slf4j.Marker;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Sensitive data masking helpers used by {@link SensitiveDataLayout} and by task plugins
 * that call {@link #maskSensitiveData(String)} directly (e.g. Sqoop / DataX scripts).
 * <p>
 * Static regex patterns (e.g. datasource password) are global.
 * Per-task sensitive parameter plaintext values (and their JSON-escaped forms) are
 * registered dynamically and must be cleared when the task ends.
 * <p>
 * Task process stdout / pod logs should use {@link TaskLogMarkers#skipTaskParamMask()} so
 * task-parameter literal masking is skipped; only worker-side logs are masked.
 * <p>
 * Literal replacement has no minimum length. A short value that also appears in timestamps,
 * log levels, or ordinary words is replaced everywhere on that rendered Worker log line.
 */
public class SensitiveDataConverter extends MessageConverter {

    private static Pattern multilinePattern;
    private static final Set<String> maskPatterns = new HashSet<>();

    /**
     * taskInstanceId -> sensitive plaintext / escaped values for that task only.
     * Values are stored longest-first so longer secrets are masked before their prefixes.
     */
    private static final ConcurrentHashMap<Integer, List<String>> TASK_SENSITIVE_VALUES = new ConcurrentHashMap<>();

    static {
        addMaskPattern(TaskConstants.DATASOURCE_PASSWORD_REGEX);
    }

    @Override
    public String convert(ILoggingEvent event) {
        // Kept for backward compatibility if a conversionRule still points here.
        return maskRenderedLog(event.getFormattedMessage(), event);
    }

    public static synchronized void addMaskPattern(final String maskPattern) {
        if (maskPatterns.contains(maskPattern)) {
            return;
        }
        maskPatterns.add(maskPattern);
        multilinePattern = Pattern.compile(String.join("|", maskPatterns), Pattern.MULTILINE);
    }

    /**
     * Register sensitive values from the full task context (prepareParams + globalParams + varPool).
     * Empty / {@code ******} placeholder values are skipped. JSON-escaped forms are also registered
     * so secrets containing quotes / backslashes / newlines still match in serialized logs.
     */
    public static void registerTaskSensitiveValues(final TaskExecutionContext taskExecutionContext) {
        if (taskExecutionContext == null) {
            return;
        }
        registerTaskSensitiveValues(
                taskExecutionContext.getTaskInstanceId(),
                taskExecutionContext.getPrepareParamsMap(),
                taskExecutionContext.getGlobalParams(),
                taskExecutionContext.getVarPool());
    }

    /**
     * Register sensitive plaintext values from this task's prepareParamsMap only.
     * Prefer {@link #registerTaskSensitiveValues(TaskExecutionContext)} when context is available.
     */
    public static void registerTaskSensitiveValues(final int taskInstanceId,
                                                   final Map<String, Property> prepareParamsMap) {
        registerTaskSensitiveValues(taskInstanceId, prepareParamsMap, null, null);
    }

    public static void registerTaskSensitiveValues(final int taskInstanceId,
                                                   final Map<String, Property> prepareParamsMap,
                                                   final String globalParams,
                                                   final List<Property> varPool) {
        Set<String> values = new HashSet<>();
        collectSensitiveValues(values, prepareParamsMap);
        collectSensitiveValues(values, GlobalParameterUtils.deserializeGlobalParameter(globalParams));
        collectSensitiveValues(values, varPool);

        if (values.isEmpty()) {
            clearTaskSensitiveValues(taskInstanceId);
            return;
        }

        Set<String> withEscapes = new HashSet<>();
        for (String value : values) {
            withEscapes.add(value);
            String jsonEscaped = jsonEscape(value);
            if (StringUtils.isNotEmpty(jsonEscaped) && !jsonEscaped.equals(value)) {
                withEscapes.add(jsonEscaped);
            }
        }

        List<String> ordered = new ArrayList<>(withEscapes);
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

    /**
     * Mask a fully rendered log line (message + throwable). Honors
     * {@link TaskLogMarkers#skipTaskParamMask()} for task-parameter literals.
     */
    public static String maskRenderedLog(final String renderedLog, final ILoggingEvent event) {
        if (StringUtils.isEmpty(renderedLog)) {
            return renderedLog;
        }
        String masked = applyStaticMaskPatterns(renderedLog);
        if (shouldSkipTaskParamMask(event)) {
            return masked;
        }
        Integer taskInstanceId = resolveTaskInstanceId(event);
        return applyTaskSensitiveLiteralMasks(masked, taskInstanceId);
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

    private static void collectSensitiveValues(final Set<String> values,
                                               final Map<String, Property> prepareParamsMap) {
        if (prepareParamsMap == null || prepareParamsMap.isEmpty()) {
            return;
        }
        collectSensitiveValues(values, prepareParamsMap.values());
    }

    private static void collectSensitiveValues(final Set<String> values,
                                               final Iterable<Property> properties) {
        if (properties == null) {
            return;
        }
        for (Property property : properties) {
            if (property == null || !property.isSensitive()) {
                continue;
            }
            String value = property.getValue();
            if (StringUtils.isEmpty(value)
                    || TaskConstants.SENSITIVE_DATA_MASK.equals(value)) {
                continue;
            }
            values.add(value);
        }
    }

    /**
     * JSON-string escape of {@code value} without surrounding quotes, matching how Jackson
     * embeds the string inside larger JSON documents.
     */
    static String jsonEscape(final String value) {
        if (value == null) {
            return null;
        }
        String quoted = JSONUtils.toJsonString(value);
        if (quoted != null && quoted.length() >= 2 && quoted.charAt(0) == '"'
                && quoted.charAt(quoted.length() - 1) == '"') {
            return quoted.substring(1, quoted.length() - 1);
        }
        return value;
    }

    private static boolean shouldSkipTaskParamMask(final ILoggingEvent event) {
        if (event == null) {
            return false;
        }
        Marker marker = event.getMarker();
        return marker != null && marker.contains(TaskLogMarkers.skipTaskParamMask());
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
