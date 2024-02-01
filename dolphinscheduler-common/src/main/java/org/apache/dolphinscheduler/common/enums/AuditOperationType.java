/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.common.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Audit Operation type
 */
@Getter
public enum AuditOperationType {
    CREATE(0, "Create", false, false),
    UPDATE(1, "Update", false, false),
    DELETE(2, "Delete", false, true),
    CLOSE(2, "Close", false, true),

    RELEASE(3, "Release", true, false),
    ONLINE(4, "Online", false, false),
    OFFLINE(5, "Offline", false, false),

    RESUME_PAUSE(7, "Resume pause", false, false),
    RESUME_FAILURE(7, "Resume failure", false, false),

    IMPORT(6, "Import", false, false),
    EXPORT(7, "Export", false, false),


    EXECUTE(8, "Execute", true, false),
    START(8, "Start", false, false),
    RUN(9, "Run", false, false),
    RERUN(10, "Rerun", false, false),
    STOP(11, "Stop", false, false),
    KILL(12, "Kill", false, false),
    PAUSE(13, "Pause", false, false),
    MOVE(14, "Move", false, false),

    SWITCH_STATUS(15, "Switch_status", false, false),
    SWITCH_VERSION(16, "Switch_version", false, false),
    DELETE_VERSION(17, "Delete_version", false, false),
    FORCE_SUCCESS(18, "Force_success", false, false),
    RENAME(19, "Rename", false, false),
    UPLOAD(20, "Upload", false, false),
    AUTHORIZE(21, "Authorize", false, false),
    UN_AUTHORIZE(21, "UN_Authorize", false, false),
    COPY(22, "Copy", false, true),
    ;

    private final int code;
    private final String name;
    private final boolean isIntermediateState;
    private final boolean multiLog;

    AuditOperationType(int code, String name, boolean isIntermediateState, boolean mutliLog) {
        this.code = code;
        this.name = name;
        this.isIntermediateState = isIntermediateState;
        this.multiLog = mutliLog;
    }

    private static final HashMap<Integer, AuditOperationType> AUDIT_OPERATION_MAP = new HashMap<>();

    static {
        for (AuditOperationType operationType : AuditOperationType.values()) {
            AUDIT_OPERATION_MAP.put(operationType.code, operationType);
        }
    }

    public static List<AuditOperationType> getOperationList() {
        return new ArrayList<>(AUDIT_OPERATION_MAP.values());
    }

    public static HashMap<Integer, AuditOperationType> getAuditOperationMap() {
        return AUDIT_OPERATION_MAP;
    }

    public static AuditOperationType of(int code) {
        if (AUDIT_OPERATION_MAP.containsKey(code)) {
            return AUDIT_OPERATION_MAP.get(code);
        }

        throw new IllegalArgumentException("invalid audit operation type code " + code);
    }
}
