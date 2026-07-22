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

import lombok.Getter;

import com.baomidou.mybatisplus.annotation.EnumValue;

@Getter
public enum WorkflowExecutionStatus {

    SUBMITTED_SUCCESS(0, false, false, false, false, false, false),
    RUNNING_EXECUTION(1, true, false, true, false, false, true),
    READY_PAUSE(2, true, false, true, false, false, true),
    PAUSE(3, false, false, false, false, true, false),
    READY_STOP(4, true, false, false, false, false, true),
    STOP(5, false, false, false, false, true, false),
    FAILURE(6, false, false, false, false, true, false),
    SUCCESS(7, false, false, false, false, true, false),
    SERIAL_WAIT(14, true, true, true, true, false, false),
    FAILOVER(18, false, false, false, false, false, false);

    /**
     * The unique code represent of the state which will be stored in database.
     */
    @EnumValue
    private final int code;

    /**
     * Whether the instance can be stopped, if the state is final state, it can't be stopped.
     * todo: Right now the SUBMITTED_SUCCESS state can't be stopped, we should support it in the future.
     */
    private final boolean canStop;

    /**
     * Whether the instance can be directly stopped in database, if true, the workflow instance will be directly set to STOP state in database.
     * Right now only serial_wait state support this.
     * todo: We should support SUBMITTED_SUCCESS state in the future.
     */
    private final boolean canDirectStopInDB;

    /**
     * Whether the instance can be paused, if the state is final state, it can't be paused.
     * todo: Right now the SUBMITTED_SUCCESS state can't be paused, we should support it in the future.
     */
    private final boolean canPause;

    /**
     * Whether the instance can be directly paused in database, if true, the workflow instance will be directly set to PAUSE state in database.
     * Right now only serial_wait state support this.
     * todo: We should support SUBMITTED_SUCCESS state in the future.
     */
    private final boolean canDirectPauseInDB;

    /**
     * Whether the instance need failover when the instance running on the master which is down.
     * It the instance is launched and not in final state, it should failover.
     */
    private final boolean needFailover;

    /**
     * Whether the state is a final state, if true, means the instance is finished and the state will not change unless the user retry/recover it.
     */
    private final boolean finalState;

    WorkflowExecutionStatus(int code,
                            boolean canStop,
                            boolean canDirectStopInDB,
                            boolean canPause,
                            boolean canDirectPauseInDB,
                            boolean finalState,
                            boolean needFailover) {
        this.code = code;
        this.canStop = canStop;
        this.canDirectStopInDB = canDirectStopInDB;
        this.canPause = canPause;
        this.canDirectPauseInDB = canDirectPauseInDB;
        this.finalState = finalState;
        this.needFailover = needFailover;
    }

    public static final int[] NEED_FAILOVER_STATES = Arrays.stream(WorkflowExecutionStatus.values())
            .filter(WorkflowExecutionStatus::isNeedFailover)
            .mapToInt(WorkflowExecutionStatus::getCode)
            .toArray();

    public static final int[] NOT_TERMINAL_STATES = Arrays.stream(WorkflowExecutionStatus.values())
            .filter(workflowExecutionStatus -> !workflowExecutionStatus.isFinalState())
            .mapToInt(WorkflowExecutionStatus::getCode)
            .toArray();

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isFailure() {
        return this == FAILURE;
    }

    public boolean isPaused() {
        return this == PAUSE;
    }

    public boolean isStopped() {
        return this == STOP;
    }

    @Override
    public String toString() {
        return name();
    }
}
