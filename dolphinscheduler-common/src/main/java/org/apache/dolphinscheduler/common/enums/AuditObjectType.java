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
 * Audit Object type
 */
@Getter
public enum AuditObjectType {

    PROJECT("Project", null), // 1
    PROCESS("Process", PROJECT),
    PROCESS_INSTANCE("ProcessInstance", PROCESS),
    TASK("Task", PROCESS),
    TASK_INSTANCE("TaskInstance", TASK),
    SCHEDULE("Schedule", PROCESS),

    RESOURCE("Resource", null),
    FOLDER("Folder", RESOURCE),
    FILE("File", FOLDER),
    UDF_FOLDER("UDFFolder", RESOURCE),
    UDF_FILE("UDFFile", UDF_FOLDER),
    UDF_FUNCTION("UDFFunction", RESOURCE),
    TASK_GROUP("TaskGroup", RESOURCE),

    DATASOURCE("Datasource", null),

    SECURITY("Security", null),
    TENANT("Tenant", SECURITY),
    USER("User", SECURITY),
    ALARM_GROUP("AlarmGroup", SECURITY),
    ALARM_INSTANCE("AlarmInstance", SECURITY),
    WORKER_GROUP("WorkerGroup", SECURITY),
    YARN_QUEUE("YarnQueue", SECURITY),
    ENVIRONMENT("Environment", SECURITY),
    CLUSTER("Cluster", SECURITY),
    K8S_NAMESPACE("K8sNamespace", SECURITY),
    TOKEN("Token", SECURITY),
    ;
    private final String name;
    private final AuditObjectType parentType;
    private final List<AuditObjectType> child = new ArrayList<>();

    private static final HashMap<String, AuditObjectType> AUDIT_OBJECT_MAP = new HashMap<>();
    private static final List<AuditObjectType> AUDIT_OBJECT_TREE_LIST = new ArrayList<>();

    static {
        for (AuditObjectType auditObjectType : values()) {
            AUDIT_OBJECT_MAP.put(auditObjectType.name, auditObjectType);
        }

        for (AuditObjectType auditObjectType : values()) {
            if (auditObjectType.parentType != null) {
                of(auditObjectType.parentType.name).child.add(auditObjectType);
            } else {
                AUDIT_OBJECT_TREE_LIST.add(auditObjectType);
            }
        }
    }

    public static List<AuditObjectType> getAuditObjectTreeList() {
        return AUDIT_OBJECT_TREE_LIST;
    }

    public static AuditObjectType of(String name) {
        if (AUDIT_OBJECT_MAP.containsKey(name)) {
            return AUDIT_OBJECT_MAP.get(name);
        }

        throw new IllegalArgumentException("invalid audit operation type name " + name);
    }

    AuditObjectType(String name, AuditObjectType parentType) {
        this.name = name;
        this.parentType = parentType;
    }
}
