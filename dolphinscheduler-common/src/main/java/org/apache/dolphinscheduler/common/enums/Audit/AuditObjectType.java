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

import java.util.*;

/**
 * Audit Operation type
 */

public enum AuditObjectType {

    PROJECT(0, -1, "Project", true),
    RESOURCE(1,-1, "Resource", false),
    DATASOURCE(2,-1, "Datasource", true),
    SECURITY(3,-1, "Security", false),
    WORKFLOW(4,0, "Workflow", true),
    WORKFLOW_INSTANCE(5,4, "Workflow instance", true),
    TASK(6,5, "Workflow instance", true),
    FLINK(7,0, "Flink", true),
    ETL(8,0, "Etl", true);
    @Getter
    private final int code;
    @Getter
    private final int parentCode;
    @Getter
    private final String name;
    @Getter
    private final boolean hasLogs;
    @Getter
    private int level;

    private static final Map<Integer, List<AuditObjectType>> AUDIT_OBJECT_LEVEL_MAP = new HashMap<>();

    private static HashMap<Integer, AuditObjectType> AUDIT_OBJECT_MAP = new HashMap<>();


    static {
        for (AuditObjectType auditObjectType : values()) {
            int level = calculateLevel(auditObjectType);
            AUDIT_OBJECT_LEVEL_MAP.computeIfAbsent(level, k -> new ArrayList<>()).add(auditObjectType);
            auditObjectType.level = level;
            AUDIT_OBJECT_MAP.put(auditObjectType.code, auditObjectType);
        }
    }

    public static AuditObjectType of(int status) {
        if (AUDIT_OBJECT_MAP.containsKey(status)) {
            return AUDIT_OBJECT_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid audit operation type code " + status);
    }

    AuditObjectType(int code, int parentCode, String name, boolean hasLogs) {
        this.code = code;
        this.parentCode = parentCode;
        this.name = name;
        this.hasLogs = hasLogs;
    }

    private static int calculateLevel(AuditObjectType auditObjectType) {
        int level = 0;
        int parentCode = auditObjectType.parentCode;

        while (parentCode != -1) {
            level++;
            parentCode = getParentCode(parentCode);
        }

        return level;
    }

    private static int getParentCode(int code) {
        for (AuditObjectType objectType : values()) {
            if (objectType.code == code) {
                return objectType.parentCode;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        System.out.println(1);
    }
}
