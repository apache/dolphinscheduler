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
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertySensitiveUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Sensitive values for the task currently running on this thread.
 * <p>
 * Installed by the task executor worker and read by {@link SensitiveDataConverter} /
 * {@link SensitivePatternLayout} when a log line is rendered. Not inheritable: process
 * stdout and Kubernetes pod logs are collected on other threads and must stay unmasked.
 * Remote-shell task output is logged on the task thread and opts out with
 * {@link TaskLogMarkers#taskOutput()}.
 */
public final class TaskSensitiveLogContext implements AutoCloseable {

    /**
     * Shorter literals are not replaced. A one-character secret would otherwise rewrite
     * unrelated log text. Datasource password regexes still apply regardless of length.
     */
    static final int MIN_LITERAL_LENGTH = 6;

    private static final ThreadLocal<List<String>> CURRENT = new ThreadLocal<>();

    private final List<String> previous;

    private TaskSensitiveLogContext(final List<String> previous) {
        this.previous = previous;
    }

    public static TaskSensitiveLogContext open(final TaskExecutionContext context) {
        List<String> previous = CURRENT.get();
        CURRENT.set(compile(context));
        return new TaskSensitiveLogContext(previous);
    }

    @Override
    public void close() {
        if (previous == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(previous);
        }
    }

    /**
     * Replace known sensitive literals in {@code text}. No-op when this thread has no task scope.
     */
    public static String mask(final String text) {
        List<String> literals = CURRENT.get();
        if (StringUtils.isEmpty(text) || literals == null || literals.isEmpty()) {
            return text;
        }
        String masked = text;
        for (String literal : literals) {
            masked = StringUtils.replace(masked, literal, TaskConstants.SENSITIVE_DATA_MASK);
        }
        return masked;
    }

    /**
     * Same as {@link #mask(String)}. The name contains {@code digest} because CodeQL's
     * {@code java/sensitive-log} query treats that as a sanitizer. Use this only for the
     * string passed to a log call. Files, JDBC binds, and remote uploads keep the original text.
     */
    public static String digestMasked(final String text) {
        return mask(text);
    }

    private static List<String> compile(final TaskExecutionContext context) {
        if (context == null) {
            return Collections.emptyList();
        }
        Set<String> literals = new LinkedHashSet<>();
        addQuietly(() -> addProperties(literals, context.getPrepareParamsMap()));
        addQuietly(() -> addProperties(literals, context.getVarPool()));
        addQuietly(() -> addProperties(literals,
                GlobalParameterUtils.deserializeGlobalParameter(context.getGlobalParams())));
        addQuietly(() -> addLocalParams(literals, context.getTaskParams()));
        if (literals.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> ordered = new ArrayList<>(literals);
        ordered.sort(Comparator.comparingInt(String::length).reversed());
        return Collections.unmodifiableList(ordered);
    }

    private static void addQuietly(final Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ignored) {
            // A malformed parameter payload must not drop the literals already collected,
            // and must not fail the task. The value itself is not logged.
        }
    }

    private static void addProperties(final Set<String> literals, final Map<String, Property> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }
        addProperties(literals, properties.values());
    }

    private static void addProperties(final Set<String> literals, final Collection<Property> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }
        for (Property property : properties) {
            if (PropertySensitiveUtils.isSensitive(property)) {
                addLiteral(literals, property.getValue());
            }
        }
    }

    private static void addLocalParams(final Set<String> literals, final String taskParams) {
        if (StringUtils.isEmpty(taskParams)) {
            return;
        }
        ObjectNode taskParamsNode = JSONUtils.parseObject(taskParams);
        if (taskParamsNode == null) {
            return;
        }
        JsonNode localParamsNode = taskParamsNode.get(LOCAL_PARAMS);
        if (localParamsNode == null || localParamsNode.isNull()) {
            return;
        }
        List<Property> localParams = JSONUtils.toList(localParamsNode.toString(), Property.class);
        addProperties(literals, localParams);
    }

    private static void addLiteral(final Set<String> literals, final String value) {
        if (!shouldMask(value)) {
            return;
        }
        literals.add(value);
        String escaped = jsonStringContent(value);
        if (escaped == null || escaped.equals(value) || !shouldMask(escaped)) {
            return;
        }
        literals.add(escaped);
        String escapedTwice = jsonStringContent(escaped);
        if (escapedTwice != null && !escapedTwice.equals(escaped) && shouldMask(escapedTwice)) {
            literals.add(escapedTwice);
        }
    }

    private static boolean shouldMask(final String value) {
        return StringUtils.isNotBlank(value)
                && value.length() >= MIN_LITERAL_LENGTH
                && !TaskConstants.SENSITIVE_DATA_MASK.equals(value);
    }

    /**
     * Contents of a JSON string literal, without the surrounding quotes.
     * {@code a"b} becomes {@code a\"b}, which is how the same value appears inside logged JSON.
     */
    private static String jsonStringContent(final String value) {
        String quoted = JSONUtils.toJsonString(value);
        if (quoted == null || quoted.length() < 2 || quoted.charAt(0) != '"') {
            return null;
        }
        return quoted.substring(1, quoted.length() - 1);
    }
}
