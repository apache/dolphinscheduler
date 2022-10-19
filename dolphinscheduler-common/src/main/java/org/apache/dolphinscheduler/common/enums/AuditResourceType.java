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

import java.util.HashMap;

/**
 * Audit Module type
 */
public enum AuditResourceType {

    // TODO: add other audit resource enums
    USER_MODULE(0, "USER"),
    PROJECT_MODULE(1, "PROJECT");

    private final int code;
    private final String enMsg;

    private static HashMap<Integer, AuditResourceType> AUDIT_RESOURCE_MAP = new HashMap<>();

    static {
        for (AuditResourceType auditResourceType : AuditResourceType.values()) {
            AUDIT_RESOURCE_MAP.put(auditResourceType.code, auditResourceType);
        }
    }

    AuditResourceType(int code, String enMsg) {
        this.code = code;
        this.enMsg = enMsg;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.enMsg;
    }

    public static AuditResourceType of(int status) {
        if (AUDIT_RESOURCE_MAP.containsKey(status)) {
            return AUDIT_RESOURCE_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid audit resource type code " + status);
    }
}
