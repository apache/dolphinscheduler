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

package org.apache.dolphinscheduler.plugin.task.sql;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class SqlTaskParameterRenderer {

    private SqlTaskParameterRenderer() {
    }

    static String render(String sql, Map<String, Property> paramsMap, int taskInstanceId) {
        if (StringUtils.isEmpty(sql) || paramsMap == null || paramsMap.isEmpty()) {
            return sql;
        }
        return renderParameters(sql, paramsMap, taskInstanceId);
    }

    private static String renderParameters(String sql, Map<String, Property> paramsMap, int taskInstanceId) {
        StringBuilder renderedSql = new StringBuilder(sql.length());
        boolean insideSingleQuotedString = false;
        int index = 0;
        while (index < sql.length()) {
            char current = sql.charAt(index);
            if (current == '\'' && insideSingleQuotedString) {
                renderedSql.append(current);
                if (index + 1 < sql.length() && sql.charAt(index + 1) == '\'') {
                    renderedSql.append('\'');
                    index += 2;
                    continue;
                }
                insideSingleQuotedString = false;
                index++;
                continue;
            }

            Placeholder rawQuotedPlaceholder = matchQuotedPlaceholder(sql, index, '!', insideSingleQuotedString);
            if (rawQuotedPlaceholder != null) {
                Property property = getProperty(paramsMap, rawQuotedPlaceholder.paramName, taskInstanceId);
                renderedSql.append(renderRawProperty(property));
                index = rawQuotedPlaceholder.endIndex;
                continue;
            }

            Placeholder sqlQuotedPlaceholder = matchQuotedPlaceholder(sql, index, '$', insideSingleQuotedString);
            if (sqlQuotedPlaceholder != null) {
                Property property = getProperty(paramsMap, sqlQuotedPlaceholder.paramName, taskInstanceId);
                renderedSql.append(renderProperty(property, false, false));
                index = sqlQuotedPlaceholder.endIndex;
                continue;
            }

            Placeholder rawPlaceholder = matchPlaceholder(sql, index, '!');
            if (rawPlaceholder != null) {
                Property property = getProperty(paramsMap, rawPlaceholder.paramName, taskInstanceId);
                renderedSql.append(renderRawProperty(property));
                index = rawPlaceholder.endIndex;
                continue;
            }

            Placeholder sqlPlaceholder = matchPlaceholder(sql, index, '$');
            if (sqlPlaceholder != null) {
                Property property = getProperty(paramsMap, sqlPlaceholder.paramName, taskInstanceId);
                boolean identifierContext = !insideSingleQuotedString
                        && isIdentifierContext(sql, index, sqlPlaceholder.endIndex);
                renderedSql.append(renderProperty(property, identifierContext, insideSingleQuotedString));
                index = sqlPlaceholder.endIndex;
                continue;
            }

            renderedSql.append(current);
            if (current == '\'') {
                insideSingleQuotedString = true;
            }
            index++;
        }
        return renderedSql.toString();
    }

    private static Placeholder matchQuotedPlaceholder(String sql, int start, char marker,
                                                      boolean insideSingleQuotedString) {
        if (insideSingleQuotedString || start >= sql.length()) {
            return null;
        }
        char quote = sql.charAt(start);
        if (quote != '\'' && quote != '"') {
            return null;
        }
        Placeholder placeholder = matchPlaceholder(sql, start + 1, marker);
        if (placeholder == null || placeholder.endIndex >= sql.length() || sql.charAt(placeholder.endIndex) != quote) {
            return null;
        }
        return new Placeholder(placeholder.paramName, placeholder.endIndex + 1);
    }

    private static Placeholder matchPlaceholder(String sql, int start, char marker) {
        if (start + 2 >= sql.length() || sql.charAt(start) != marker || sql.charAt(start + 1) != '{') {
            return null;
        }
        int end = sql.indexOf('}', start + 2);
        if (end < 0) {
            return null;
        }
        return new Placeholder(sql.substring(start + 2, end), end + 1);
    }

    private static Property getProperty(Map<String, Property> paramsMap, String paramName, int taskInstanceId) {
        Property property = paramsMap.get(paramName);
        if (property == null) {
            throw new TaskException(String.format(
                    "No Property with paramName: %s is found in paramsMap of task instance with id: %s",
                    paramName,
                    taskInstanceId));
        }
        return property;
    }

    private static String renderRawProperty(Property property) {
        return StringUtils.defaultString(property.getValue());
    }

    private static String renderProperty(Property property, boolean identifierContext, boolean insideStringLiteral) {
        String value = property.getValue();
        if (value == null) {
            return "null";
        }
        if (insideStringLiteral) {
            return escapeSqlString(value);
        }
        if (identifierContext) {
            if (!isValidIdentifierFragment(value)) {
                throw new TaskException(String.format(
                        "Invalid SQL identifier fragment for property %s",
                        property.getProp()));
            }
            return value;
        }
        DataType dataType = property.getType();
        if (DataType.LIST.equals(dataType)) {
            return renderList(value);
        }
        if (isTypedLiteral(dataType)) {
            return renderTypedLiteral(property);
        }
        return quoteSqlString(value);
    }

    private static String renderList(String value) {
        List<Object> valueList = JSONUtils.toList(value, Object.class);
        if (valueList.isEmpty() && StringUtils.isNotBlank(value)) {
            valueList.add(value);
        }
        return valueList.stream()
                .map(SqlTaskParameterRenderer::renderListValue)
                .collect(Collectors.joining(","));
    }

    private static String renderListValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return quoteSqlString(value.toString());
    }

    private static boolean isTypedLiteral(DataType dataType) {
        return DataType.INTEGER.equals(dataType)
                || DataType.LONG.equals(dataType)
                || DataType.FLOAT.equals(dataType)
                || DataType.DOUBLE.equals(dataType)
                || DataType.BOOLEAN.equals(dataType)
                || DataType.DATE.equals(dataType)
                || DataType.TIME.equals(dataType)
                || DataType.TIMESTAMP.equals(dataType);
    }

    private static String renderTypedLiteral(Property property) {
        DataType dataType = property.getType();
        String value = property.getValue().trim();
        try {
            if (DataType.INTEGER.equals(dataType)) {
                return Integer.toString(Integer.parseInt(value));
            }
            if (DataType.LONG.equals(dataType)) {
                return Long.toString(Long.parseLong(value));
            }
            if (DataType.FLOAT.equals(dataType)) {
                float parsed = Float.parseFloat(value);
                if (!Float.isFinite(parsed)) {
                    throw invalidPropertyException(property);
                }
                return Float.toString(parsed);
            }
            if (DataType.DOUBLE.equals(dataType)) {
                double parsed = Double.parseDouble(value);
                if (!Double.isFinite(parsed)) {
                    throw invalidPropertyException(property);
                }
                return Double.toString(parsed);
            }
            if (DataType.BOOLEAN.equals(dataType)) {
                if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                    return Boolean.toString(Boolean.parseBoolean(value));
                }
                throw invalidPropertyException(property);
            }
            if (DataType.DATE.equals(dataType)) {
                return quoteSqlString(java.sql.Date.valueOf(value).toString());
            }
            if (DataType.TIME.equals(dataType)) {
                return quoteSqlString(java.sql.Time.valueOf(value).toString());
            }
            if (DataType.TIMESTAMP.equals(dataType)) {
                return quoteSqlString(java.sql.Timestamp.valueOf(value).toString());
            }
        } catch (IllegalArgumentException ex) {
            throw invalidPropertyException(property);
        }
        return quoteSqlString(value);
    }

    private static TaskException invalidPropertyException(Property property) {
        return new TaskException(String.format(
                "Invalid SQL parameter value for type %s and property %s",
                property.getType(),
                property.getProp()));
    }

    private static String quoteSqlString(String value) {
        return "'" + escapeSqlString(value) + "'";
    }

    private static String escapeSqlString(String value) {
        return value.replace("'", "''");
    }

    private static boolean isIdentifierContext(String sql, int start, int end) {
        return isIdentifierContextChar(charBefore(sql, start)) || isIdentifierContextChar(charAfter(sql, end));
    }

    private static char charBefore(String sql, int start) {
        return start > 0 ? sql.charAt(start - 1) : '\0';
    }

    private static char charAfter(String sql, int end) {
        return end < sql.length() ? sql.charAt(end) : '\0';
    }

    private static boolean isIdentifierContextChar(char ch) {
        return isIdentifierFragmentChar(ch) || ch == '.';
    }

    private static boolean isValidIdentifierFragment(String value) {
        return StringUtils.isNotEmpty(value)
                && value.chars().allMatch(SqlTaskParameterRenderer::isIdentifierFragmentChar);
    }

    private static boolean isIdentifierFragmentChar(int ch) {
        return Character.isLetterOrDigit(ch) || ch == '_' || ch == '$';
    }

    private static final class Placeholder {

        private final String paramName;
        private final int endIndex;

        private Placeholder(String paramName, int endIndex) {
            this.paramName = paramName;
            this.endIndex = endIndex;
        }
    }
}
