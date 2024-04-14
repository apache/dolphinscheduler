package org.apache.dolphinscheduler.api.utils;

import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class ServiceTestUtil {
    public static String randomStringWithLengthN(int n) {
        byte[] bitArray = new byte[n];
        new Random().nextBytes(bitArray);
        return new String(bitArray, StandardCharsets.UTF_8);
    }
}
