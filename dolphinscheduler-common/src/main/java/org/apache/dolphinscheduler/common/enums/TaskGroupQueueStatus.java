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

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * running status for task group queue
 */
public enum TaskGroupQueueStatus {

    WAIT_QUEUE(-1, "wait queue"),
    ACQUIRE_SUCCESS(1, "acquire success"),
    RELEASE(2, "release");

    @EnumValue
    private final int code;
    private final String descp;
    private static HashMap<Integer, TaskGroupQueueStatus> STATUS_MAP = new HashMap<>();

    static {
        for (TaskGroupQueueStatus taskGroupQueueStatus : TaskGroupQueueStatus.values()) {
            STATUS_MAP.put(taskGroupQueueStatus.code, taskGroupQueueStatus);
        }
    }

    TaskGroupQueueStatus(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    public static TaskGroupQueueStatus of(int status) {
        if (STATUS_MAP.containsKey(status)) {
            return STATUS_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid status : " + status);
    }

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }
}
