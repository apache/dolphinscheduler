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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;

/**
 * Audit Operation type
 */
@Getter
public enum AuditOperationType {

    CREATE("Create"),
    UPDATE("Update"),
    BATCH_DELETE("Batch Delete"),
    BATCH_START("Batch Start"),
    DELETE("Delete"),
    CLOSE("Close"), // 多条日志

    RELEASE("Release"), // 中间状态
    ONLINE("Online"),
    OFFLINE("Offline"),

    RESUME_PAUSE("Resume pause"),
    RESUME_FAILURE("Resume failure"),

    IMPORT("Import"),
    EXPORT("Export"),

    EXECUTE("Execute"),
    START("Start"),
    MODIFY("Modify"),
    RUN("Run"),
    RERUN("Rerun"),
    BATCH_RERUN("Batch Rerun"),
    STOP("Stop"),
    KILL("Kill"),
    PAUSE("Pause"),
    MOVE("Move"),

    SWITCH_STATUS("Switch status"),
    SWITCH_VERSION("Switch version"),
    DELETE_VERSION("Delete version"),
    FORCE_SUCCESS("Force success"),
    RENAME("Rename"),
    UPLOAD("Upload"),
    AUTHORIZE("Authorize"),
    UN_AUTHORIZE("Un authorize"),
    COPY("Copy"), // 多条日志
    ;

    private final String name;

    AuditOperationType(String name) {
        this.name = name;
    }

    private static final HashMap<String, AuditOperationType> AUDIT_OPERATION_MAP = new HashMap<>();

    static {
        for (AuditOperationType operationType : AuditOperationType.values()) {
            AUDIT_OPERATION_MAP.put(operationType.name, operationType);
        }
    }

    public static List<AuditOperationType> getOperationList() {
        return new ArrayList<>(AUDIT_OPERATION_MAP.values());
    }

    public static HashMap<String, AuditOperationType> getAuditOperationMap() {
        return AUDIT_OPERATION_MAP;
    }

    public static AuditOperationType of(String name) {
        if (AUDIT_OPERATION_MAP.containsKey(name)) {
            return AUDIT_OPERATION_MAP.get(name);
        }

        throw new IllegalArgumentException("invalid audit operation type code " + name);
    }
}
