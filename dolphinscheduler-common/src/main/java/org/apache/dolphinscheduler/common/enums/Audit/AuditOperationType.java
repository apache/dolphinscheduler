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

package org.apache.dolphinscheduler.common.enums.Audit;

import lombok.Getter;

import java.util.HashMap;

/**
 * Audit Operation type
 */

public enum AuditOperationType {

    CREATE(0, "CREATE"),
    READ(1, "READ"),
    UPDATE(2, "UPDATE"),
    ONLINE(3, "ONLINE"),
    START(4, "ONLINE"),
    SCHEDULE_RUN(6, "SCHEDULE_RUN"),
    RUN(6, "RUN"),
    STOP(7, "STOP"),
    DELETE(8, "DELETE");

    @Getter
    private final int code;
    @Getter
    private final String name;

    AuditOperationType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    private static HashMap<Integer, AuditOperationType> AUDIT_OPERATION_MAP = new HashMap<>();

    static {
        for (AuditOperationType operationType : AuditOperationType.values()) {
            AUDIT_OPERATION_MAP.put(operationType.code, operationType);
        }
    }

    public static AuditOperationType of(int status) {
        if (AUDIT_OPERATION_MAP.containsKey(status)) {
            return AUDIT_OPERATION_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid audit operation type code " + status);
    }
}
