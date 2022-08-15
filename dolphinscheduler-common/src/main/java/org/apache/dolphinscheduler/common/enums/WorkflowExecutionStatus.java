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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum WorkflowExecutionStatus {

    // This class is split from <code>ExecutionStatus</code> #11339.
    // In order to compatible with the old value, the code is not consecutive
    SUBMITTED_SUCCESS(0, true, false, "submit success"),
    RUNNING_EXECUTION(1, true, false, "running"),
    READY_PAUSE(2, true, false, "ready pause"),
    PAUSE(3, false, true, "pause"),
    READY_STOP(4, true, false, "ready stop"),
    STOP(5, false, true, "stop"),
    FAILURE(6, false, true, "failure"),
    SUCCESS(7, false, true, "success"),
    DELAY_EXECUTION(12, true, false, "delay execution"),
    SERIAL_WAIT(14, false, false, "serial wait"),
    READY_BLOCK(15, true, false, "ready block"),
    BLOCK(16, false, true, "block"),
    ;

    private static final Map<Integer, WorkflowExecutionStatus> CODE_MAP = new HashMap<>();
    private static final int[] NEED_FAILOVER_STATES;

    private static final int[] NOT_TERMINATED_STATES = new int[]{
            WorkflowExecutionStatus.SUBMITTED_SUCCESS.getCode(),
            WorkflowExecutionStatus.RUNNING_EXECUTION.getCode(),
            WorkflowExecutionStatus.DELAY_EXECUTION.getCode(),
            WorkflowExecutionStatus.READY_PAUSE.getCode(),
            WorkflowExecutionStatus.READY_STOP.getCode(),
    };

    static {
        for (WorkflowExecutionStatus executionStatus : WorkflowExecutionStatus.values()) {
            CODE_MAP.put(executionStatus.getCode(), executionStatus);
        }
        NEED_FAILOVER_STATES = Arrays.stream(WorkflowExecutionStatus.values())
                .filter(WorkflowExecutionStatus::shouldFailover)
                .mapToInt(WorkflowExecutionStatus::getCode)
                .toArray();
    }

    /**
     * Get <code>WorkflowExecutionStatus</code> by code, if the code is invalidated will throw {@link IllegalArgumentException}.
     */
    public static @NonNull WorkflowExecutionStatus of(int code) {
        WorkflowExecutionStatus workflowExecutionStatus = CODE_MAP.get(code);
        if (workflowExecutionStatus == null) {
            throw new IllegalArgumentException(String.format("The workflow execution status code: %s is invalidated",
                    code));
        }
        return workflowExecutionStatus;
    }

    public boolean isRunning() {
        return this == RUNNING_EXECUTION;
    }

    public boolean canStop() {
        return this == RUNNING_EXECUTION || this == READY_PAUSE;
    }

    public boolean isFinished() {
        return finished;
    }

    /**
     * status is success
     *
     * @return status
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isFailure() {
        return this == FAILURE;
    }

    public boolean isPause() {
        return this == PAUSE;
    }

    public boolean isReadyStop() {
        return this == READY_STOP;
    }

    public boolean isStop() {
        return this == STOP;
    }

    public boolean isBlock() {
        return this == BLOCK;
    }

    public static int[] getNeedFailoverWorkflowInstanceState() {
        return NEED_FAILOVER_STATES;
    }

    @EnumValue
    private final int code;

    private final boolean shouldBeFailover;

    private final boolean finished;

    private final String desc;

    WorkflowExecutionStatus(int code, boolean shouldBeFailover, boolean finished, String desc) {
        this.code = code;
        this.shouldBeFailover = shouldBeFailover;
        this.finished = finished;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public boolean shouldFailover() {
        return shouldBeFailover;
    }

}
