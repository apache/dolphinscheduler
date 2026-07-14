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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * sensitive data log converter
 */
public class SensitiveDataConverter extends MessageConverter {

    private static Pattern multilinePattern;
    private static final Set<String> maskPatterns = new HashSet<>();

    private static final String KNOWN_SENSITIVE_CONFIGURATION_KEY_REGEX =
            "(?:password|access[._-]?key(?:[._-]?(?:id|secret))?|secret[._-]?access[._-]?key|secret[._-]?key)";

    private static final Pattern QUOTED_SENSITIVE_CONFIGURATION_PREFIX_PATTERN = Pattern.compile(
            "((?:\\\\?\\\"|')?" + KNOWN_SENSITIVE_CONFIGURATION_KEY_REGEX
                    + "(?:\\\\?\\\"|')?\\s*(?::|=)\\s*(\\\\?\\\"|'))",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static final Pattern UNQUOTED_SENSITIVE_CONFIGURATION_PATTERN = Pattern.compile(
            "(" + KNOWN_SENSITIVE_CONFIGURATION_KEY_REGEX + "\\s*(?::|=)\\s*)([^\\s,;&}'\\\"]+)()",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

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
        multilinePattern = Pattern.compile(String.join("|", maskPatterns), Pattern.MULTILINE);
    }

    public static String maskSensitiveData(final String logMsg) {
        if (StringUtils.isEmpty(logMsg)) {
            return logMsg;
        }

        String maskedLogMsg = maskKnownSensitiveConfiguration(logMsg);
        return maskByConfiguredPatterns(maskedLogMsg);
    }

    private static String maskByConfiguredPatterns(final String logMsg) {
        final StringBuffer sb = new StringBuffer(logMsg.length());
        final Matcher matcher = multilinePattern.matcher(logMsg);

        while (matcher.find()) {
            matcher.appendReplacement(sb, TaskConstants.SENSITIVE_DATA_MASK);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String maskKnownSensitiveConfiguration(final String logMsg) {
        String maskedLogMsg = maskQuotedSensitiveConfiguration(logMsg);
        return maskKnownSensitiveConfiguration(maskedLogMsg, UNQUOTED_SENSITIVE_CONFIGURATION_PATTERN);
    }

    private static String maskQuotedSensitiveConfiguration(final String logMsg) {
        final StringBuilder sb = new StringBuilder(logMsg.length());
        final Matcher matcher = QUOTED_SENSITIVE_CONFIGURATION_PREFIX_PATTERN.matcher(logMsg);
        int appendFrom = 0;
        int searchFrom = 0;
        while (matcher.find(searchFrom)) {
            final String quote = matcher.group(2);
            final int closingQuoteStart = findClosingQuote(logMsg, matcher.end(), quote);
            if (closingQuoteStart < 0) {
                searchFrom = matcher.end();
                continue;
            }
            final int closingQuoteEnd = closingQuoteStart + quote.length();
            sb.append(logMsg, appendFrom, matcher.end());
            sb.append(TaskConstants.SENSITIVE_DATA_MASK);
            sb.append(logMsg, closingQuoteStart, closingQuoteEnd);
            appendFrom = closingQuoteEnd;
            searchFrom = closingQuoteEnd;
        }
        sb.append(logMsg, appendFrom, logMsg.length());
        return sb.toString();
    }

    private static int findClosingQuote(final String logMsg, final int valueStart, final String quote) {
        final char quoteCharacter = quote.charAt(quote.length() - 1);
        final boolean escapedQuote = quote.length() == 2 && quote.charAt(0) == '\\';
        for (int i = valueStart; i < logMsg.length(); i++) {
            if (logMsg.charAt(i) != quoteCharacter) {
                continue;
            }
            int precedingBackslashes = 0;
            for (int j = i - 1; j >= valueStart && logMsg.charAt(j) == '\\'; j--) {
                precedingBackslashes++;
            }
            if (escapedQuote && precedingBackslashes % 4 == 1) {
                return i - 1;
            }
            if (!escapedQuote && precedingBackslashes % 2 == 0) {
                return i;
            }
        }
        return -1;
    }

    private static String maskKnownSensitiveConfiguration(final String logMsg, final Pattern pattern) {
        final StringBuffer sb = new StringBuffer(logMsg.length());
        final Matcher matcher = pattern.matcher(logMsg);
        while (matcher.find()) {
            matcher.appendReplacement(sb,
                    Matcher.quoteReplacement(matcher.group(1) + TaskConstants.SENSITIVE_DATA_MASK + matcher.group(3)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
