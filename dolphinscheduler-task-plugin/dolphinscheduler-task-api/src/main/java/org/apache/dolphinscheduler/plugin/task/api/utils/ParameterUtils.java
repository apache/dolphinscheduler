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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_DATETIME;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_FORMAT_TIME;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_SHECDULE_TIME;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parser.PlaceholderUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.TimePlaceholderUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * parameter parse utils
 */
public class ParameterUtils {

    private static final Pattern DATE_PARSE_PATTERN = Pattern.compile("\\$\\[([^\\$\\]]+)]");

    private static final Pattern DATE_START_PATTERN = Pattern.compile("^[0-9]");

    private static final char PARAM_REPLACE_CHAR = '?';

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
        if (parameterMap != null && null != parameterMap.get(PARAMETER_DATETIME)) {
            // Get current time, schedule execute time
            String cronTimeStr = parameterMap.get(PARAMETER_DATETIME);
            cronTime = DateUtils.parse(cronTimeStr, PARAMETER_FORMAT_TIME);
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
     * new
     * convert parameters place holders
     *
     * @param parameterString parameter
     * @param parameterMap    parameter map
     * @return convert parameters place holders
     */
    public static String convertParameterPlaceholders2(String parameterString, Map<String, String> parameterMap) {
        if (StringUtils.isEmpty(parameterString)) {
            return parameterString;
        }
        // Get current time, schedule execute time
        String cronTimeStr = parameterMap.get(PARAMETER_SHECDULE_TIME);
        Date cronTime = null;

        if (StringUtils.isNotEmpty(cronTimeStr)) {
            cronTime = DateUtils.parse(cronTimeStr, PARAMETER_FORMAT_TIME);

        } else {
            cronTime = new Date();
        }

        // replace variable ${} form,refers to the replacement of system variables and custom variables
        if (!parameterMap.isEmpty()) {
            parameterString = PlaceholderUtils.replacePlaceholders(parameterString, parameterMap, true);
        }

        // replace time $[...] form, eg. $[yyyyMMdd]
        if (cronTime != null) {
            return dateTemplateParse(parameterString, cronTime);
        }
        return parameterString;
    }

    /**
     * set in parameter
     *
     * @param index    index
     * @param stmt     preparedstatement
     * @param dataType data type
     * @param value    value
     * @throws Exception errors
     */
    public static void setInParameter(int index, PreparedStatement stmt, DataType dataType,
                                      String value) throws Exception {
        if (dataType.equals(DataType.VARCHAR)) {
            stmt.setString(index, value);
        } else if (dataType.equals(DataType.INTEGER)) {
            stmt.setInt(index, Integer.parseInt(value));
        } else if (dataType.equals(DataType.LONG)) {
            stmt.setLong(index, Long.parseLong(value));
        } else if (dataType.equals(DataType.FLOAT)) {
            stmt.setFloat(index, Float.parseFloat(value));
        } else if (dataType.equals(DataType.DOUBLE)) {
            stmt.setDouble(index, Double.parseDouble(value));
        } else if (dataType.equals(DataType.DATE)) {
            stmt.setDate(index, java.sql.Date.valueOf(value));
        } else if (dataType.equals(DataType.TIME)) {
            stmt.setTime(index, java.sql.Time.valueOf(value));
        } else if (dataType.equals(DataType.TIMESTAMP)) {
            stmt.setTimestamp(index, java.sql.Timestamp.valueOf(value));
        } else if (dataType.equals(DataType.BOOLEAN)) {
            stmt.setBoolean(index, Boolean.parseBoolean(value));
        }
    }

    public static Serializable getParameterValue(Property property) {
        if (property == null) {
            return null;
        }
        String value = property.getValue();
        switch (property.getType()) {
            case LONG:
                return Long.valueOf(value);
            case FLOAT:
                return Float.valueOf(value);
            case INTEGER:
                return Integer.valueOf(value);
            case DOUBLE:
                return Double.valueOf(value);
            case BOOLEAN:
                return Boolean.valueOf(value);
            // todo: add date type, list type....
            default:
                return value;
        }
    }

    public static boolean isNumber(Property property) {
        return property != null &&
                (DataType.INTEGER.equals(property.getType())
                        || DataType.LONG.equals(property.getType())
                        || DataType.FLOAT.equals(property.getType())
                        || DataType.DOUBLE.equals(property.getType()));
    }

    public static boolean isBoolean(Property property) {
        return property != null && DataType.BOOLEAN.equals(property.getType());
    }

    public static String expandListParameter(Map<Integer, Property> params, String sql) {
        Map<Integer, Property> expandMap = new HashMap<>();
        if (params == null || params.isEmpty()) {
            return sql;
        }
        String[] split = sql.split("\\?");
        if (split.length == 0) {
            return sql;
        }
        StringBuilder ret = new StringBuilder(split[0]);
        int index = 1;
        for (int i = 1; i < split.length; i++) {
            Property property = params.get(i);
            String value = property.getValue();
            if (DataType.LIST.equals(property.getType())) {
                List<Object> valueList = JSONUtils.toList(value, Object.class);
                if (valueList.isEmpty() && StringUtils.isNotBlank(value)) {
                    valueList.add(value);
                }
                for (int j = 0; j < valueList.size(); j++) {
                    ret.append(PARAM_REPLACE_CHAR);
                    if (j != valueList.size() - 1) {
                        ret.append(",");
                    }
                }
                for (Object v : valueList) {
                    Property newProperty = new Property();
                    if (v instanceof Integer) {
                        newProperty.setType(DataType.INTEGER);
                    } else if (v instanceof Long) {
                        newProperty.setType(DataType.LONG);
                    } else if (v instanceof Float) {
                        newProperty.setType(DataType.FLOAT);
                    } else if (v instanceof Double) {
                        newProperty.setType(DataType.DOUBLE);
                    } else {
                        newProperty.setType(DataType.VARCHAR);
                    }
                    newProperty.setValue(v.toString());
                    newProperty.setProp(property.getProp());
                    newProperty.setDirect(property.getDirect());
                    expandMap.put(index++, newProperty);
                }
            } else {
                ret.append(PARAM_REPLACE_CHAR);
                expandMap.put(index++, property);
            }
            ret.append(split[i]);
        }
        if (PARAM_REPLACE_CHAR == sql.charAt(sql.length() - 1)) {
            ret.append(PARAM_REPLACE_CHAR);
            expandMap.put(index, params.get(split.length));
        }
        params.clear();
        params.putAll(expandMap);
        return ret.toString();
    }

    /**
     * $[yyyyMMdd] replace schedule time
     */
    public static String replaceScheduleTime(String text, Date scheduleTime) {
        Map<String, Property> paramsMap = new HashMap<>();
        // if getScheduleTime null ,is current date
        if (null == scheduleTime) {
            scheduleTime = new Date();
        }

        String dateTime = DateUtils.format(scheduleTime, PARAMETER_FORMAT_TIME);
        Property p = new Property();
        p.setValue(dateTime);
        p.setProp(PARAMETER_SHECDULE_TIME);
        paramsMap.put(PARAMETER_SHECDULE_TIME, p);
        text = ParameterUtils.convertParameterPlaceholders2(text, convert(paramsMap));

        return text;
    }

    /**
     * format convert
     *
     * @param paramsMap params map
     * @return Map of converted
     * see org.apache.dolphinscheduler.server.utils.ParameterUtils.convert
     */
    public static Map<String, String> convert(Map<String, Property> paramsMap) {
        if (paramsMap == null) {
            return null;
        }

        Map<String, String> map = new HashMap<>();
        Iterator<Map.Entry<String, Property>> iter = paramsMap.entrySet().iterator();
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
            matcher.appendReplacement(newValue, value);
        }

        matcher.appendTail(newValue);

        return newValue.toString();
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
     * get parameters map
     *
     * @param definedParams definedParams
     * @return parameters map
     */
    public static Map<String, Property> getUserDefParamsMap(Map<String, String> definedParams) {
        Map<String, Property> userDefParamsMaps = new HashMap<>();
        if (definedParams != null) {
            Iterator<Map.Entry<String, String>> iter = definedParams.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> en = iter.next();
                Property property = new Property(en.getKey(), Direct.IN, DataType.VARCHAR, en.getValue());
                userDefParamsMaps.put(property.getProp(), property);
            }
        }
        return userDefParamsMaps;
    }

}
