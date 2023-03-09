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

package org.apache.dolphinscheduler.plugin.task.api.enums.dp;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * rule type
 */
public enum RuleType {

    /**
     * 0-single_table
     * 1-single_table_custom_sql
     * 2-multi_table_accuracy
     * 3-multi_table_comparison
     */
    SINGLE_TABLE(0, "single_table"),
    SINGLE_TABLE_CUSTOM_SQL(1, "single_table_custom_sql"),
    MULTI_TABLE_ACCURACY(2, "multi_table_accuracy"),
    MULTI_TABLE_COMPARISON(3, "multi_table_comparison");

    RuleType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    private final int code;
    private final String description;

    @JsonValue
    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    private static final Map<Integer, RuleType> VALUES_MAP = new HashMap<>();

    static {
        for (RuleType type : RuleType.values()) {
            VALUES_MAP.put(type.code, type);
        }
    }

    public static RuleType of(Integer status) {
        if (VALUES_MAP.containsKey(status)) {
            return VALUES_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid code : " + status);
    }
}
