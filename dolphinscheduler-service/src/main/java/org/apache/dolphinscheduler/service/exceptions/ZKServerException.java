package org.apache.dolphinscheduler.service.exceptions;

/**
 * Custom ZKServerException exception
 */
public class ZKServerException extends RuntimeException {

    /**
     * Construct a new runtime exception with the cause
     *
     * @param cause cause
     */
    public ZKServerException(Throwable cause) {
        super(cause);
    }

    /**
     * Construct a new runtime exception with the detail message and cause
     *
     * @param errMsg message
     * @param cause cause
     */
    public ZKServerException(String errMsg, Throwable cause) {
        super(errMsg, cause);
    }
}
