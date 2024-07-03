package org.apache.dolphinscheduler.plugin.task.adbspark;

import org.apache.commons.lang3.StringUtils;

public enum AdbSparkState {

    SUBMITTED("SUBMITTED"),
    STARTING("STARTING"),
    RUNNING("RUNNING"),
    FAILING("FAILING"),
    SUCCEEDING("SUCCEEDING"),
    KILLING("KILLING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    KILLED("KILLED"),
    FATAL("FATAL"),
    UNKNOWN("UNKNOWN");

    private final String value;

    AdbSparkState(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static AdbSparkState fromValue(String value) {
        if (StringUtils.isNotBlank(value)) {
            for (AdbSparkState state : AdbSparkState.values()) {
                if (state.value.equalsIgnoreCase(value)) {
                    return state;
                }
            }

            throw new IllegalArgumentException("Cannot create enum from " + value + " value!");
        } else {
            throw new IllegalArgumentException("Value cannot be null or empty!");
        }
    }

}
