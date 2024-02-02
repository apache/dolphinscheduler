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

import java.util.stream.Collectors;

import lombok.Getter;

/**
 * Audit Object type
 */
@Getter
public enum AuditObjectType {

    PROJECT(1000, -1, "Project", true),
    PROCESS(1001, 1000, "Process", true),
    PROCESS_INSTANCE(1002, 1001, "Process instance", true),
    TASK(1003, 1001, "Task", true),
    TASK_INSTANCE(1004, 1003, "Task instance", true),
    SCHEDULE(1005, 1001, "Schedule", true),

    RESOURCE(2000, -1, "Resource", false),
    FOLDER(2001, 2000, "Folder", true),
    FILE(2002, 2001, "File", true),
    UDF_FOLDER(2003, 2000, "UDF folder", true),
    UDF_FILE(2004, 2003, "UDF file", true),
    UDP_FUNCTION(2005, 2000, "UDF function", true),
    TASK_GROUP(2006, 2000, "Task group", true),
    TASK_GROUP_QUEUE(2007, 2000, "Task group queue", true),

    DATASOURCE(3000, -1, "Datasource", true),

    SECURITY(4000, -1, "Security", false),
    TENANT(4001, 4000, "Tenant", true),
    USER(4002, 4000, "User", true),
    ALARM_GROUP(4003, 4000, "Alarm group", true),
    ALARM_INSTANCE(4004, 4000, "Alarm instance", true),
    WORKER_GROUP(4005, 4000, "Worker group", true),
    YARN_QUEUE(4006, 4000, "Yarn queue", true),
    ENVIRONMENT(4007, 4000, "Environment", true),
    CLUSTER(4008, 4000, "Cluster", true),
    K8S_NAMESPACE(4009, 4000, "K8s namespace", true),
    TOKEN(4010, 4000, "Token", true),
    ;
    private final int code;
    private final int parentCode;
    private final String name;
    private final boolean hasLogs;
    private final List<AuditObjectType> child = new ArrayList<>();

    private static final HashMap<Integer, AuditObjectType> AUDIT_OBJECT_MAP = new HashMap<>();
    private static final List<AuditObjectType> AUDIT_OBJECT_TREE_LIST = new ArrayList<>();

    static {
        List<AuditObjectType> list = Arrays.stream(values()).sorted(Comparator.comparing(type -> type.parentCode))
                .collect(Collectors.toList());

        for (AuditObjectType auditObjectType : values()) {
            AUDIT_OBJECT_MAP.put(auditObjectType.code, auditObjectType);
        }

        for (AuditObjectType auditObjectType : list) {
            if (auditObjectType.parentCode != -1) {
                of(auditObjectType.parentCode).child.add(auditObjectType);
            } else {
                AUDIT_OBJECT_TREE_LIST.add(auditObjectType);
            }
        }
    }

    public static List<AuditObjectType> getAuditObjectTreeList() {
        return AUDIT_OBJECT_TREE_LIST;
    }

    public static AuditObjectType of(int code) {
        if (AUDIT_OBJECT_MAP.containsKey(code)) {
            return AUDIT_OBJECT_MAP.get(code);
        }

        throw new IllegalArgumentException("invalid audit operation type code " + code);
    }

    AuditObjectType(int code, int parentCode, String name, boolean hasLogs) {
        this.code = code;
        this.parentCode = parentCode;
        this.name = name;
        this.hasLogs = hasLogs;
    }
}
