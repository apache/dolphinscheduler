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

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.placeholder.BusinessTimeUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.PlaceholderUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.TimePlaceholderUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
            //Get current time, schedule execute time
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
     * curing user define parameters
     *
     * @param globalParamMap  global param map
     * @param globalParamList global param list
     * @param commandType     command type
     * @param scheduleTime    schedule time
     * @return curing user define parameters
     */
    public static String curingGlobalParams(Map<String, String> globalParamMap, List<Property> globalParamList,
                                            CommandType commandType, Date scheduleTime, String timezone) {

        if (globalParamList == null || globalParamList.isEmpty()) {
            return null;
        }

        Map<String, String> globalMap = new HashMap<>();
        if (globalParamMap != null) {
            globalMap.putAll(globalParamMap);
        }
        Map<String, String> allParamMap = new HashMap<>();
        //If it is a complement, a complement time needs to be passed in, according to the task type
        Map<String, String> timeParams = BusinessTimeUtils.
            getBusinessTime(commandType, scheduleTime, timezone);

        if (timeParams != null) {
            allParamMap.putAll(timeParams);
        }

        allParamMap.putAll(globalMap);

        Set<Map.Entry<String, String>> entries = allParamMap.entrySet();

        Map<String, String> resolveMap = new HashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            String val = entry.getValue();
            if (val.startsWith("$")) {
                String str = ParameterUtils.convertParameterPlaceholders(val, allParamMap);
                resolveMap.put(entry.getKey(), str);
            }
        }
        globalMap.putAll(resolveMap);

        for (Property property : globalParamList) {
            String val = globalMap.get(property.getProp());
            if (val != null) {
                property.setValue(val);
            }
        }
        return JSONUtils.toJsonString(globalParamList);
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
