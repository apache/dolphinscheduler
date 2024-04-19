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
 * Audit Model type
 */
@Getter
public enum AuditModelType {

    PROJECT("Project", null),
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
    private final AuditModelType parentType;
    private final List<AuditModelType> child = new ArrayList<>();

    private static final HashMap<String, AuditModelType> AUDIT_MODEL_MAP = new HashMap<>();
    private static final List<AuditModelType> AUDIT_MODEL_TREE_LIST = new ArrayList<>();

    static {
        for (AuditModelType auditModelType : values()) {
            AUDIT_MODEL_MAP.put(auditModelType.name, auditModelType);
        }

        for (AuditModelType auditModelType : values()) {
            if (auditModelType.parentType != null) {
                of(auditModelType.parentType.name).child.add(auditModelType);
            } else {
                AUDIT_MODEL_TREE_LIST.add(auditModelType);
            }
        }
    }

    public static List<AuditModelType> getAuditModelTreeList() {
        return AUDIT_MODEL_TREE_LIST;
    }

    public static AuditModelType of(String name) {
        if (AUDIT_MODEL_MAP.containsKey(name)) {
            return AUDIT_MODEL_MAP.get(name);
        }

        throw new IllegalArgumentException("invalid audit operation type name " + name);
    }

    AuditModelType(String name, AuditModelType parentType) {
        this.name = name;
        this.parentType = parentType;
    }
}
