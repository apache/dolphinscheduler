package org.apache.dolphinscheduler.test.core.exception;

public class DefaultException extends Exception{
    protected DefaultException(Object message) {
        this(message, null);
    }

    protected DefaultException(Object message, Throwable cause) {
        super(message.toString(), cause);
    }

    protected DefaultException(Throwable cause) {
        this(null, cause);
    }
}
