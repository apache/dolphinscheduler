package org.apache.dolphinscheduler.api.v3.utils;

import org.hashids.Hashids;

public class IdObfuscator {
    private static final Hashids obfuscator;
    private static final String SALT = "DOLPHINSCHEDULER_ID_OBFUSCATOR_SALT!@#";

    static {
        obfuscator = new Hashids(SALT, 10);
    }

    public static String encode(int id) {
        return obfuscator.encode(id);
    }

    public static int decode(String encodedId) {
        return (int) obfuscator.decode(encodedId)[0];
    }
}
