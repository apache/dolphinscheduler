package org.apache.dolphinscheduler.service.exceptions;

/**
 * Custom CuratorZookeeperClientException exception
 */
public class CuratorZookeeperClientException extends RuntimeException {

    /**
     * Construct a new runtime exception with the detail message
     *
     * @param errMsg message
     */
    public CuratorZookeeperClientException(String errMsg) {
        super(errMsg);
    }

    /**
     * Construct a new runtime exception with the cause
     *
     * @param cause cause
     */
    public CuratorZookeeperClientException(Throwable cause) {
        super(cause);
    }

    /**
     * Construct a new runtime exception with the detail message and cause
     *
     * @param errMsg message
     * @param cause cause
     */
    public CuratorZookeeperClientException(String errMsg, Throwable cause) {
        super(errMsg, cause);
    }
}
