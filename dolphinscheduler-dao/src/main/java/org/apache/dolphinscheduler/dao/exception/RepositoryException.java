package org.apache.dolphinscheduler.dao.exception;

/**
 * This exception is used in dao module, when crud from repository failed,
 * need to throw {@link RepositoryException} instead of {@link RuntimeException}.
 */
public class RepositoryException extends Exception {

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
