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
 * operator type
 */
public enum OperatorType {

    /**
     * 0-equal
     * 1-little than
     * 2-little and equal
     * 3-great than
     * 4-great and equal
     * 5-not equal
     */
    EQ(0, "equal"),
    LT(1, "little than"),
    LE(2, "little and equal"),
    GT(3, "great than"),
    GE(4, "great and equal"),
    NE(5, "not equal");

    OperatorType(int code, String description) {
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

    private static final Map<Integer, OperatorType> VALUES_MAP = new HashMap<>();

    static {
        for (OperatorType type : OperatorType.values()) {
            VALUES_MAP.put(type.code, type);
        }
    }

    public static OperatorType of(Integer status) {
        if (VALUES_MAP.containsKey(status)) {
            return VALUES_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid code : " + status);
    }
}
