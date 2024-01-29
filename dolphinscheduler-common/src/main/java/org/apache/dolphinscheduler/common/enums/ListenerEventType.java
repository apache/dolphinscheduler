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

import lombok.Getter;

import com.baomidou.mybatisplus.annotation.EnumValue;

@Getter
public enum ListenerEventType {

    SERVER_DOWN(0, "SERVER_DOWN"),
    PROCESS_DEFINITION_CREATED(1, "PROCESS_DEFINITION_CREATED"),
    PROCESS_DEFINITION_UPDATED(2, "PROCESS_DEFINITION_UPDATED"),
    PROCESS_DEFINITION_DELETED(3, "PROCESS_DEFINITION_DELETED"),
    PROCESS_START(4, "PROCESS_START"),
    PROCESS_END(5, "PROCESS_INSTANCE_END"),
    PROCESS_FAIL(6, "PROCESS_FAIL"),
    TASK_START(10, "TASK_START"),
    TASK_END(11, "TASK_END"),
    TASK_FAIL(12, "TASK_FAIL");

    private static final Map<Integer, ListenerEventType> CODE_MAP = new HashMap<>();

    static {
        for (ListenerEventType listenerEventType : ListenerEventType.values()) {
            CODE_MAP.put(listenerEventType.getCode(), listenerEventType);
        }
    }

    @EnumValue
    private final int code;
    private final String descp;

    ListenerEventType(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    public static ListenerEventType of(int code) {
        ListenerEventType listenerEventType = CODE_MAP.get(code);
        if (listenerEventType == null) {
            throw new IllegalArgumentException(String.format("The task execution status code: %s is invalid",
                    code));
        }
        return listenerEventType;
    }
}
