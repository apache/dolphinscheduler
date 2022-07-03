package org.apache.dolphinscheduler.test.core.exception;

public class WebStorageNotSupportedException extends DefaultException {
    public WebStorageNotSupportedException() {
        super("Web storage is not supported by the driver");
    }
}
