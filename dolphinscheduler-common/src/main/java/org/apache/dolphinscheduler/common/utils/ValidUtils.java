package org.apache.dolphinscheduler.common.utils;

public class ValidUtils {

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new RuntimeException(message);
        }
    }
}
