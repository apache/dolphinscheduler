package org.apache.dolphinscheduler.service.exceptions;

/**
 * Custom permission exception
 */
public class PermissionException extends RuntimeException {

    /**
     * Construct a new runtime exception with the detail message
     *
     * @param errMsg message
     */
    public PermissionException(String errMsg) {
        super(errMsg);
    }

    /**
     * Construct a new runtime exception with the detail message and cause
     *
     * @param errMsg message
     * @param cause cause
     */
    public PermissionException(String errMsg, Throwable cause) {
        super(errMsg, cause);
    }
}
