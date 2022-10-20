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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parser.PlaceholderUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.TimePlaceholderUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * parameter parse utils
 */
public class ParameterUtils {

    private static final Pattern DATE_PARSE_PATTERN = Pattern.compile("\\$\\[([^\\$\\]]+)]");
    private static final Pattern DATE_START_PATTERN = Pattern.compile("^[0-9]");

    private ParameterUtils() {
        throw new UnsupportedOperationException("Construct ParameterUtils");
    }

    /**
     * convert parameters place holders
     *
     * @param parameterString parameter
     * @param parameterMap    parameter map
     * @return convert parameters place holders
     */
    public static String convertParameterPlaceholders(String parameterString, Map<String, String> parameterMap) {
        if (StringUtils.isEmpty(parameterString)) {
            return parameterString;
        }
        Date cronTime;
        if (parameterMap != null && !parameterMap.isEmpty()) {
            // replace variable ${} form,refers to the replacement of system variables and custom variables
            parameterString = PlaceholderUtils.replacePlaceholders(parameterString, parameterMap, true);
        }
        if (parameterMap != null && null != parameterMap.get(Constants.PARAMETER_DATETIME)) {
            // Get current time, schedule execute time
            String cronTimeStr = parameterMap.get(Constants.PARAMETER_DATETIME);
            cronTime = DateUtils.parse(cronTimeStr, Constants.PARAMETER_FORMAT_TIME, null);
        } else {
            cronTime = new Date();
        }
        // replace time $[...] form, eg. $[yyyyMMdd]
        if (cronTime != null) {
            return dateTemplateParse(parameterString, cronTime);
        }
        return parameterString;
    }

    /**
     * handle escapes
     *
     * @param inputString input string
     * @return string filter escapes
     */
    public static String handleEscapes(String inputString) {

        if (!StringUtils.isEmpty(inputString)) {
            return inputString.replace("%", "////%").replaceAll("[\n|\r\t]", "_");
        }
        return inputString;
    }

    /**
     * format convert
     *
     * @param paramsMap params map
     * @return Map of converted
     * see org.apache.dolphinscheduler.server.utils.ParamUtils.convert
     */
    public static Map<String, String> convert(Map<String, Property> paramsMap) {
        Map<String, String> map = new HashMap<>();
        Iterator<Entry<String, Property>> iter = paramsMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Property> en = iter.next();
            map.put(en.getKey(), en.getValue().getValue());
        }
        return map;
    }

    private static String dateTemplateParse(String templateStr, Date date) {
        if (templateStr == null) {
            return null;
        }

        StringBuffer newValue = new StringBuffer(templateStr.length());

        Matcher matcher = DATE_PARSE_PATTERN.matcher(templateStr);

        while (matcher.find()) {
            String key = matcher.group(1);
            if (DATE_START_PATTERN.matcher(key).matches()) {
                continue;
            }
            String value = TimePlaceholderUtils.getPlaceHolderTime(key, date);
            assert value != null;
            matcher.appendReplacement(newValue, value);
        }

        matcher.appendTail(newValue);

        return newValue.toString();
    }

}
