package org.apache.dolphinscheduler.api.test.utils.enums;

/**
 * have_script
 * have_file
 * can_retry
 * have_arr_variables
 * have_map_variables
 * have_alert
 */
public enum Flag {
    /**
     * 0 no
     * 1 yes
     */
    NO(0, "no"),
    YES(1, "yes");

    Flag(int code, String desc) {
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