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
    CLOSE(3, "Close", false, true),

    RELEASE(4, "Release", true, false),
    ONLINE(5, "Online", false, false),
    OFFLINE(6, "Offline", false, false),

    RESUME_PAUSE(7, "Resume pause", false, false),
    RESUME_FAILURE(8, "Resume failure", false, false),

    IMPORT(9, "Import", false, false),
    EXPORT(10, "Export", false, false),

    EXECUTE(11, "Execute", true, false),
    START(12, "Start", false, false),
    RUN(13, "Run", false, false),
    RERUN(14, "Rerun", false, false),
    STOP(15, "Stop", false, false),
    KILL(16, "Kill", false, false),
    PAUSE(17, "Pause", false, false),
    MOVE(18, "Move", false, false),

    SWITCH_STATUS(19, "Switch status", false, false),
    SWITCH_VERSION(20, "Switch version", false, false),
    DELETE_VERSION(21, "Delete version", false, false),
    FORCE_SUCCESS(22, "Force success", false, false),
    RENAME(23, "Rename", false, false),
    UPLOAD(24, "Upload", false, false),
    AUTHORIZE(25, "Authorize", false, false),
    UN_AUTHORIZE(26, "Un authorize", false, false),
    COPY(27, "Copy", false, true),
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
