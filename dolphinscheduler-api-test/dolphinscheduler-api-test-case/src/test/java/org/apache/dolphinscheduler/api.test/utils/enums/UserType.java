package org.apache.dolphinscheduler.api.test.utils.enums;

/**
 * user type
 */
public enum UserType {
    /**
     * 0 admin user; 1 general user
     */
    ADMIN_USER(0, "admin user"),
    GENERAL_USER(1, "general user");

    UserType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final int code;
    private final String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}


