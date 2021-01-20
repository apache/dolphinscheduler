package org.apache.dolphinscheduler.service.exceptions;

/**
 * Custom QuartzExecutorsException exception
 */
public class QuartzExecutorsException extends RuntimeException {

    /**
     * Construct a new runtime exception with the detail message
     *
     * @param errMsg message
     */
    public QuartzExecutorsException(String errMsg) {
        super(errMsg);
    }

    /**
     * Construct a new runtime exception with the detail message and cause
     *
     * @param errMsg message
     * @param cause cause
     */
    public QuartzExecutorsException(String errMsg, Throwable cause) {
        super(errMsg, cause);
    }
}
