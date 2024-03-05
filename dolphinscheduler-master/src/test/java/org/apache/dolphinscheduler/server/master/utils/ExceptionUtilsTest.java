package org.apache.dolphinscheduler.server.master.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

class ExceptionUtilsTest {

    @Test
    void isDatabaseConnectedFailedException() {
        // todo: Directly connect to database
        assertTrue(ExceptionUtils.isDatabaseConnectedFailedException(
                new DataAccessResourceFailureException("Database connection failed")));
        assertFalse(ExceptionUtils.isDatabaseConnectedFailedException(new RuntimeException("runtime exception")));
    }
}
