package org.apache.dolphinscheduler.api.test.utils.enums;

public enum ReleaseState {

    /**
     * 0 offline
     * 1 on line
     */
    OFFLINE(0, "offline"),
    ONLINE(1, "online");

    ReleaseState(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final int code;
    private final String desc;

    public static ReleaseState getEnum(int value) {
        for (ReleaseState e : ReleaseState.values()) {
            if (e.ordinal() == value) {
                return e;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
