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

package org.apache.dolphinscheduler.common.enums;

import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * condition type
 */
public enum ConditionType {

    /**
     * 0 none
     * 1 judge
     * 2 delay
     */
    NONE(0, "none"),
    JUDGE(1, "judge"),
    DELAY(2, "delay");

    ConditionType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @EnumValue
    private final int code;
    private final String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    private static final Map<String, ConditionType> CONDITION_TYPE_MAP = new HashMap<>();

    static {
        for (ConditionType conditionType : ConditionType.values()) {
            CONDITION_TYPE_MAP.put(conditionType.desc, conditionType);
        }
    }

    public static ConditionType of(String desc) {
        if (CONDITION_TYPE_MAP.containsKey(desc)) {
            return CONDITION_TYPE_MAP.get(desc);
        }
        throw new IllegalArgumentException("invalid type : " + desc);
    }
}
