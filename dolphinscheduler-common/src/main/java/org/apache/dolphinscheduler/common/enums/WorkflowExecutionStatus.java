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

import lombok.NonNull;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum WorkflowExecutionStatus {

    SUBMITTED_SUCCESS(0, "submitted"),
    RUNNING_EXECUTION(1, "running"),
    READY_PAUSE(2, "ready pause"),
    PAUSE(3, "pause"),
    READY_STOP(4, "ready stop"),
    STOP(5, "stop"),
    FAILURE(6, "failure"),
    SUCCESS(7, "success"),
    SERIAL_WAIT(14, "serial wait"),
    WAIT_TO_RUN(17, "wait to run"),
    FAILOVER(18, "failover");

    private static final Map<Integer, WorkflowExecutionStatus> CODE_MAP = new HashMap<>();
    private static final int[] NEED_FAILOVER_STATES = new int[]{
            RUNNING_EXECUTION.getCode(),
            READY_PAUSE.getCode(),
            READY_STOP.getCode()
    };

    private static final int[] NOT_TERMINAL_STATUS = new int[]{
            SUBMITTED_SUCCESS.getCode(),
            RUNNING_EXECUTION.getCode(),
            READY_PAUSE.getCode(),
            READY_STOP.getCode(),
            SERIAL_WAIT.getCode(),
            WAIT_TO_RUN.getCode()
    };

    static {
        for (WorkflowExecutionStatus executionStatus : WorkflowExecutionStatus.values()) {
            CODE_MAP.put(executionStatus.getCode(), executionStatus);
        }
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
        return this == RUNNING_EXECUTION
                || this == READY_PAUSE
                || this == READY_STOP
                || this == SERIAL_WAIT
                || this == WAIT_TO_RUN;
    }

    public boolean canDirectStopInDB() {
        return this == SERIAL_WAIT || this == WAIT_TO_RUN;
    }

    public boolean canPause() {
        return this == RUNNING_EXECUTION
                || this == READY_PAUSE
                || this == SERIAL_WAIT;
    }

    public boolean canDirectPauseInDB() {
        return this == SERIAL_WAIT || this == WAIT_TO_RUN;
    }

    public boolean isFinished() {
        return isSuccess() || isFailure() || isStop() || isPause();
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

    public static int[] getNeedFailoverWorkflowInstanceState() {
        return NEED_FAILOVER_STATES;
    }

    public static int[] getNotTerminalStatus() {
        return NOT_TERMINAL_STATUS;
    }

    @EnumValue
    private final int code;

    private final String desc;

    WorkflowExecutionStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return name();
    }
}
