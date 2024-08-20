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

package org.apache.dolphinscheduler.api.task.hint;

import org.apache.dolphinscheduler.api.task.SqlTaskParsePluginLoader;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.plugin.task.api.enums.SqlType;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.google.common.collect.ImmutableMap;

@AllArgsConstructor
@Getter
public enum HintEnum {

    /**
     * plugin
     */
    PLUGIN("plugin", SqlTaskParsePluginLoader::getSqlTaskParsePlugin),

    /**
     * task name
     */
    NAME("name", s -> s),

    /**
     * datasource
     */
    DATASOURCE("datasource", s -> s),

    /**
     * upstream
     */
    UPSTREAM("upstream", s -> Arrays.stream(s.split(",")).map(String::trim)
            .filter(u -> !u.isEmpty()).collect(Collectors.toSet())),

    SQL_TYPE("sqlType", s -> {
        switch (s) {
            case "query":
                return SqlType.QUERY;
            case "non query":
                return SqlType.NON_QUERY;
            default:
                return null;
        }
    }),

    TASK_PRIORITY("taskPriority", Priority::valueOf),
    ;
    private final String key;
    private final Function<String, Object> converter;

    public static final Map<String, HintEnum> HINT_ENUM_MAP =
            ImmutableMap.copyOf(
                    Arrays.stream(HintEnum.values()).collect(Collectors.toMap(HintEnum::getKey, Function.identity())));
}
