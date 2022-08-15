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

package org.apache.dolphinscheduler.plugin.task.api.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum TaskExecutionStatus {

    SUBMITTED_SUCCESS(0, true, false, "submit success"),
    RUNNING_EXECUTION(1, true, false, "running"),
    PAUSE(3, false, true, "pause"),
    FAILURE(6, false, true, "failure"),
    SUCCESS(7, false, true, "success"),
    NEED_FAULT_TOLERANCE(8, false, true, "need fault tolerance"),
    KILL(9, false, true, "kill"),
    DELAY_EXECUTION(12, true, false, "delay execution"),
    FORCED_SUCCESS(13, false, true, "forced success"),
    DISPATCH(17, true, false, "dispatch"),

    ;

    private static final Map<Integer, TaskExecutionStatus> CODE_MAP = new HashMap<>();
    private static final int[] NEED_FAILOVER_STATES;

    static {
        for (TaskExecutionStatus executionStatus : TaskExecutionStatus.values()) {
            CODE_MAP.put(executionStatus.getCode(), executionStatus);
        }
        NEED_FAILOVER_STATES =
                Arrays.stream(TaskExecutionStatus.values())
                        .filter(TaskExecutionStatus::shouldFailover)
                        .mapToInt(TaskExecutionStatus::getCode)
                        .toArray();
    }

    /**
     * Get <code>TaskExecutionStatus</code> by code, if the code is invalidated will throw {@link IllegalArgumentException}.
     */
    public static TaskExecutionStatus of(int code) {
        TaskExecutionStatus taskExecutionStatus = CODE_MAP.get(code);
        if (taskExecutionStatus == null) {
            throw new IllegalArgumentException(String.format("The task execution status code: %s is invalidated",
                    code));
        }
        return taskExecutionStatus;
    }

    public boolean isRunning() {
        return this == RUNNING_EXECUTION;
    }

    public boolean isSuccess() {
        return this == TaskExecutionStatus.SUCCESS;
    }

    public boolean isForceSuccess() {
        return this == TaskExecutionStatus.FORCED_SUCCESS;
    }

    public boolean isKill() {
        return this == TaskExecutionStatus.KILL;
    }

    public boolean isFailure() {
        return this == TaskExecutionStatus.FAILURE;
    }

    public boolean isPause() {
        return this == TaskExecutionStatus.PAUSE;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isNeedFaultTolerance() {
        return this == NEED_FAULT_TOLERANCE;
    }

    public static int[] getNeedFailoverWorkflowInstanceState() {
        return NEED_FAILOVER_STATES;
    }

    public boolean shouldFailover() {
        return shouldBeFailover;
    }

    @EnumValue
    private final int code;

    private final boolean shouldBeFailover;
    private final String desc;

    private final boolean finished;

    TaskExecutionStatus(int code, boolean shouldBeFailover, boolean finished, String desc) {
        this.code = code;
        this.shouldBeFailover = shouldBeFailover;
        this.desc = desc;
        this.finished = finished;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
