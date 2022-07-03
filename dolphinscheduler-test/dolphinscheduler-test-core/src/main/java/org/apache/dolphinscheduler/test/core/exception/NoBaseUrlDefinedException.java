package org.apache.dolphinscheduler.test.core.exception;

public class NoBaseUrlDefinedException extends DefaultException {

    public NoBaseUrlDefinedException() {
        super("There is no base URL configured and it was requested.");
    }
}
