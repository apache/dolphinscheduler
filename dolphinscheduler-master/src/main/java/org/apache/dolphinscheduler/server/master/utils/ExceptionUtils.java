package org.apache.dolphinscheduler.server.master.utils;

import lombok.experimental.UtilityClass;

import org.springframework.dao.DataAccessResourceFailureException;

@UtilityClass
public class ExceptionUtils {

    public boolean isDatabaseConnectedFailedException(Throwable e) {
        return e instanceof DataAccessResourceFailureException;
    }

}
