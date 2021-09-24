package org.apache.dolphinscheduler.common.enums;

import java.util.HashMap;

/**
 * Audit Operation type
 */
public enum AuditOperationType {

    // TODO: add other audit operation enums
    DEFAULT(0, "default" ),
    CREATE_USER(1, "create user"),
    CREATE_PROJECT(2, "create project");

    private final int code;
    private final String enMsg;

    private static HashMap<Integer, AuditOperationType> AUDIT_OPERATION_MAP = new HashMap<>();

    static {
        for (AuditOperationType operationType : AuditOperationType.values()) {
            AUDIT_OPERATION_MAP.put(operationType.code, operationType);
        }
    }

    AuditOperationType(int code, String enMsg) {
        this.code = code;
        this.enMsg = enMsg;
    }

    public static AuditOperationType of(int status) {
        if (AUDIT_OPERATION_MAP.containsKey(status)) {
            return AUDIT_OPERATION_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid audit operation type code " + status);
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return enMsg;
    }
}
