package org.apache.dolphinscheduler.common.enums;

import java.util.HashMap;

/**
 * Audit Module type
 */
public enum AuditModuleType {
    // TODO: add other audit module enums
    DEFAULT(0, "default"),
    USER_MODULE(1, "user module"),
    PROJECT_MODULE(2, "project module");

    private final int code;
    private final String enMsg;

    private static HashMap<Integer, AuditModuleType> AUDIT_MODULE_MAP = new HashMap<>();

    static {
        for (AuditModuleType auditModuleType : AuditModuleType.values()) {
            AUDIT_MODULE_MAP.put(auditModuleType.code, auditModuleType);
        }
    }

    AuditModuleType(int code, String enMsg) {
        this.code = code;
        this.enMsg = enMsg;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.enMsg;
    }

    public static AuditModuleType of(int status) {
        if (AUDIT_MODULE_MAP.containsKey(status)) {
            return AUDIT_MODULE_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid audit module type " + status);
    }
}
