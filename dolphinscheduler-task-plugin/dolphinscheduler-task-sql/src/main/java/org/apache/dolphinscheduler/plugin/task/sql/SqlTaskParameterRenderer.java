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

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

final class SqlTaskParameterRenderer {

    private SqlTaskParameterRenderer() {
    }

    static String render(String sql, Map<String, Property> paramsMap, int taskInstanceId) {
        if (StringUtils.isEmpty(sql)) {
            return sql;
        }
        return renderParameters(sql, paramsMap, taskInstanceId);
    }

    private static String renderParameters(String sql, Map<String, Property> paramsMap, int taskInstanceId) {
        StringBuilder renderedSql = new StringBuilder(sql.length());
        int index = 0;
        while (index < sql.length()) {
            char marker = sql.charAt(index);
            if (!isPlaceholderStart(sql, index, marker)) {
                renderedSql.append(marker);
                index++;
                continue;
            }

            int end = sql.indexOf('}', index + 2);
            if (end < 0) {
                throw new TaskException(String.format(
                        "Unclosed SQL parameter placeholder in task instance with id: %s",
                        taskInstanceId));
            }

            String paramName = sql.substring(index + 2, end);
            Property property = getProperty(paramsMap, paramName, taskInstanceId);
            renderedSql.append(StringUtils.defaultString(property.getValue()));
            index = end + 1;
        }
        return renderedSql.toString();
    }

    private static boolean isPlaceholderStart(String sql, int index, char marker) {
        return (marker == '$' || marker == '!')
                && index + 1 < sql.length()
                && sql.charAt(index + 1) == '{';
    }

    private static Property getProperty(Map<String, Property> paramsMap, String paramName, int taskInstanceId) {
        Property property = paramsMap == null ? null : paramsMap.get(paramName);
        if (property == null) {
            throw new TaskException(String.format(
                    "No Property with paramName: %s is found in paramsMap of task instance with id: %s",
                    paramName,
                    taskInstanceId));
        }
        return property;
    }
}
