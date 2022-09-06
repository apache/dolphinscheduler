package org.apache.dolphinscheduler.plugin.task.api.enums;

public enum TaskExecuteType {

    SYNC(0, "Will use SyncWorkerDelayTaskExecuteRunnable to execute the task"),
    ASYNC(1, "Will use AsyncWorkerDelayTaskExecuteRunnable to execute the task"),

    ;

    private final int code;
    private final String desc;

    TaskExecuteType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
