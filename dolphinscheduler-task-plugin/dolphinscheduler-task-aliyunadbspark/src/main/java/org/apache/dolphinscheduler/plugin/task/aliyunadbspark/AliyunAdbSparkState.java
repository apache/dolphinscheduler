package org.apache.dolphinscheduler.plugin.task.aliyunadbspark;

import org.apache.commons.lang3.StringUtils;

public enum AliyunAdbSparkState {

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

    AliyunAdbSparkState(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static AliyunAdbSparkState fromValue(String value) {
        if (StringUtils.isNotBlank(value)) {
            for (AliyunAdbSparkState state : AliyunAdbSparkState.values()) {
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
