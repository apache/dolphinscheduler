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

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * sensitive data log converter
 */
public class SensitiveDataConverter extends MessageConverter {

    private static volatile Pattern multilinePattern;
    private static final Set<String> maskPatterns = new HashSet<>();
    private static final Map<String, Integer> dynamicMaskPatternRefCount = new HashMap<>();

    static {
        addMaskPattern(TaskConstants.DATASOURCE_PASSWORD_REGEX);
    }

    @Override
    public String convert(ILoggingEvent event) {

        // get original log
        String requestLogMsg = event.getFormattedMessage();

        // desensitization log
        return maskSensitiveData(requestLogMsg);
    }

    public static synchronized void addMaskPattern(final String maskPattern) {
        if (maskPatterns.contains(maskPattern)) {
            return;
        }
        maskPatterns.add(maskPattern);
        refreshMultilinePattern();
    }

    public static synchronized void addDynamicMaskPattern(final String maskPattern) {
        if (StringUtils.isEmpty(maskPattern)) {
            return;
        }
        dynamicMaskPatternRefCount.put(maskPattern, dynamicMaskPatternRefCount.getOrDefault(maskPattern, 0) + 1);
        refreshMultilinePattern();
    }

    public static synchronized void removeDynamicMaskPattern(final String maskPattern) {
        if (StringUtils.isEmpty(maskPattern)) {
            return;
        }
        Integer refCount = dynamicMaskPatternRefCount.get(maskPattern);
        if (refCount == null) {
            return;
        }
        if (refCount <= 1) {
            dynamicMaskPatternRefCount.remove(maskPattern);
        } else {
            dynamicMaskPatternRefCount.put(maskPattern, refCount - 1);
        }
        refreshMultilinePattern();
    }

    public static String maskSensitiveData(final String logMsg) {
        if (StringUtils.isEmpty(logMsg)) {
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

    private static void refreshMultilinePattern() {
        Set<String> allMaskPatterns = new HashSet<>(maskPatterns);
        allMaskPatterns.addAll(dynamicMaskPatternRefCount.keySet());
        multilinePattern = Pattern.compile(String.join("|", allMaskPatterns), Pattern.MULTILINE);
    }

}
