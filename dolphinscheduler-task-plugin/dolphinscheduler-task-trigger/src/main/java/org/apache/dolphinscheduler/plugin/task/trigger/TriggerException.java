package org.apache.dolphinscheduler.plugin.task.trigger;

public class TriggerException extends RuntimeException {

    private static final long serialVersionUID = 8155449302457294758L;

    public TriggerException() {
        super();
    }

    public TriggerException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public TriggerException(String msg) {
        super(msg);
    }
}