package org.apache.dolphinscheduler.remote.exceptions;

/**
 * Custom runtime exception
 */
public class RemoteException extends RuntimeException {

    /**
     * Construct a new runtime exception with the detail message
     *
     * @param   message  detail message
     */
    public RemoteException(String message) {
        super(message);
    }

    /**
     * Construct a new runtime exception with the detail message and cause
     *
     * @param   message  detail message
     */
    public RemoteException(String message, Throwable cause) {
        super(message, cause);
    }
}
