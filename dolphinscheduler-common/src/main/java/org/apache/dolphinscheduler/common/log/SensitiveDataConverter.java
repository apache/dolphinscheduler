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

package org.apache.dolphinscheduler.common.log;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.constants.DataSourceConstants;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import com.google.common.base.Strings;

/**
 * sensitive data log converter
 */
public class SensitiveDataConverter extends MessageConverter {

    private static Pattern multilinePattern;
    private static HashSet<String> maskPatterns =
            new HashSet<>(Arrays.asList(DataSourceConstants.DATASOURCE_PASSWORD_REGEX));

    @Override
    public String convert(ILoggingEvent event) {

        // get original log
        String requestLogMsg = event.getFormattedMessage();

        // desensitization log
        return maskSensitiveData(requestLogMsg);
    }

    public static void addMaskPattern(String maskPattern) {
        maskPatterns.add(maskPattern);
    }

    public static String maskSensitiveData(final String logMsg) {
        if (StringUtils.isEmpty(logMsg)) {
            return logMsg;
        }
        multilinePattern = Pattern.compile(String.join("|", maskPatterns), Pattern.MULTILINE);

        StringBuffer sb = new StringBuffer(logMsg.length());
        Matcher matcher = multilinePattern.matcher(logMsg);

        while (matcher.find()) {
            String password = matcher.group();
            String maskPassword = Strings.repeat(Constants.STAR, password.length());
            matcher.appendReplacement(sb, maskPassword);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

}
