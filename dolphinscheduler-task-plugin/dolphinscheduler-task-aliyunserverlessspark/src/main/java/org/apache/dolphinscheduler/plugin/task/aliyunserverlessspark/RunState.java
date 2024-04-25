package org.apache.dolphinscheduler.plugin.task.aliyunserverlessspark;

import java.util.Objects;

public enum RunState {

    Submitted,

    Pending,

    Running,

    Success,

    Failed,

    Cancelling,

    Cancelled,

    CancelFailed;

    public static boolean isFinal(RunState runState) {
        return Success == runState || Failed == runState || Cancelled == runState;
    }

    public static boolean hasLaunched(RunState runState) {
        return Objects.nonNull(runState) && runState != Submitted && runState != Pending;
    }

    public static boolean isCancelled(RunState runState) {
        return Cancelled == runState;
    }

}
