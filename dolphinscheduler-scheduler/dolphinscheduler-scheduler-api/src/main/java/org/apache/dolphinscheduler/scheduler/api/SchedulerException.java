package org.apache.dolphinscheduler.scheduler.api;

public class SchedulerException extends RuntimeException {

    public SchedulerException(String message) {
        super(message);
    }

    public SchedulerException(String message, Throwable cause) {
        super(message, cause);
    }

    public static SchedulerException of(String message) {
        return new SchedulerException(message);
    }

    public static SchedulerException of(String message, Throwable cause) {
        return new SchedulerException(message, cause);
    }
}
