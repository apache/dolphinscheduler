package org.apache.dolphinscheduler.test.core.extensions;

import org.apache.dolphinscheduler.test.core.exception.DefaultException;

public class IncorrectDriverTypeException extends DefaultException {
    public IncorrectDriverTypeException(String message) {
        super(message);
    }
}
