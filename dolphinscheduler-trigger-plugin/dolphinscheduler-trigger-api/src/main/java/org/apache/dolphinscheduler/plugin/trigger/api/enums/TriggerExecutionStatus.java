package org.apache.dolphinscheduler.plugin.trigger.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

import java.util.HashMap;
import java.util.Map;

public enum TriggerExecutionStatus {

    SUBMITTED_SUCCESS(0, "submit success"),
    RUNNING_EXECUTION(1, "running"),
    PAUSE(3, "pause"),
    STOP(5, "stop"),
    FAILURE(6, "failure"),
    SUCCESS(7, "success"),
    NEED_FAULT_TOLERANCE(8, "need fault tolerance"),
    KILL(9, "kill"),
    DELAY_EXECUTION(12, "delay execution"),
    FORCED_SUCCESS(13, "forced success"),
    DISPATCH(17, "dispatch"),

    ;

    private static final Map<Integer, TriggerExecutionStatus> CODE_MAP = new HashMap<>();

    static {
        for (TriggerExecutionStatus executionStatus : TriggerExecutionStatus.values()) {
            CODE_MAP.put(executionStatus.getCode(), executionStatus);
        }
    }

    /**
     * Get <code>TaskExecutionStatus</code> by code, if the code is invalidated will throw {@link IllegalArgumentException}.
     */
    public static TriggerExecutionStatus of(int code) {
        TriggerExecutionStatus taskExecutionStatus = CODE_MAP.get(code);
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
        return this == TriggerExecutionStatus.SUCCESS;
    }

    public boolean isForceSuccess() {
        return this == TriggerExecutionStatus.FORCED_SUCCESS;
    }

    public boolean isKill() {
        return this == TriggerExecutionStatus.KILL;
    }

    public boolean isFailure() {
        return this == TriggerExecutionStatus.FAILURE || this == NEED_FAULT_TOLERANCE;
    }

    public boolean isPause() {
        return this == TriggerExecutionStatus.PAUSE;
    }

    public boolean isStop() {
        return this == TriggerExecutionStatus.STOP;
    }

    public boolean isFinished() {
        return isSuccess() || isKill() || isFailure() || isPause() || isStop() || isForceSuccess();
    }

    public boolean isNeedFaultTolerance() {
        return this == NEED_FAULT_TOLERANCE;
    }

    public boolean shouldFailover() {
        return SUBMITTED_SUCCESS == this
                || DISPATCH == this
                || RUNNING_EXECUTION == this
                || DELAY_EXECUTION == this;
    }

    @EnumValue
    private final int code;
    private final String desc;

    TriggerExecutionStatus(int code, String desc) {
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
        return "TaskExecutionStatus{" + "code=" + code + ", desc='" + desc + '\'' + '}';
    }

}
