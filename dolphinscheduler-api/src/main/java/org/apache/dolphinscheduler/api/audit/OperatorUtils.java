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

package org.apache.dolphinscheduler.api.audit;

import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuditModelType;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

@Slf4j
public class OperatorUtils {

    public static boolean resultFail(Result<?> result) {
        return result != null && result.isFailed();
    }

    public static List<AuditLog> buildAuditLogList(String apiDescription, AuditType auditType, User user) {
        List<AuditLog> auditLogList = new ArrayList<>();
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(user.getId());
        auditLog.setModelType(auditType.getAuditModelType().getName());
        auditLog.setOperationType(auditType.getAuditOperationType().getName());
        auditLog.setDescription(apiDescription);
        auditLog.setCreateTime(new Date());
        auditLogList.add(auditLog);
        return auditLogList;
    }

    public static User getUser(Map<String, Object> paramsMap) {
        for (Object object : paramsMap.values()) {
            if (object instanceof User) {
                return (User) object;
            }
        }

        return null;
    }

    public static Map<String, Object> getParamsMap(JoinPoint point, MethodSignature signature) {
        Object[] args = point.getArgs();
        String[] strings = signature.getParameterNames();

        Map<String, Object> paramsMap = new HashMap<>();
        for (int i = 0; i < strings.length; i++) {
            paramsMap.put(strings[i], args[i]);
        }

        return paramsMap;
    }

    public static AuditOperationType modifyReleaseOperationType(AuditType auditType, Map<String, Object> paramsMap) {
        switch (auditType.getAuditOperationType()) {
            case RELEASE:
                ReleaseState releaseState = (ReleaseState) paramsMap.get(Constants.RELEASE_STATE);
                if (releaseState == null) {
                    break;
                }
                switch (releaseState) {
                    case ONLINE:
                        return AuditOperationType.ONLINE;
                    case OFFLINE:
                        return AuditOperationType.OFFLINE;
                    default:
                        break;
                }
                break;
            case EXECUTE:
                ExecuteType executeType = (ExecuteType) paramsMap.get(Constants.EXECUTE_TYPE);
                if (executeType == null) {
                    break;
                }
                switch (executeType) {
                    case REPEAT_RUNNING:
                        return AuditOperationType.RERUN;
                    case RECOVER_SUSPENDED_PROCESS:
                        return AuditOperationType.RESUME_PAUSE;
                    case START_FAILURE_TASK_PROCESS:
                        return AuditOperationType.RESUME_FAILURE;
                    case STOP:
                        return AuditOperationType.STOP;
                    case PAUSE:
                        return AuditOperationType.PAUSE;
                    case EXECUTE_TASK:
                        return AuditOperationType.EXECUTE;
                    default:
                        break;
                }
                break;
            default:
                break;
        }

        return auditType.getAuditOperationType();
    }

    public static long getObjectIdentityByParam(String[] paramNameArr, Map<String, Object> paramsMap) {
        for (String name : paramNameArr) {
            if (paramsMap.get(name) instanceof String) {
                String param = (String) paramsMap.get(name);
                try {
                    if (param.matches("\\d+")) {
                        return Long.parseLong(param);
                    }
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }

        return -1;
    }

    public static Map<String, Object> getObjectIfFromReturnObject(Object obj, String[] params) {
        Map<String, Object> map = new HashMap<>();

        try {
            Class<?> clazz = obj.getClass();

            if (clazz.equals(Long.class)) {
                map.put(params[0], obj);
            }

            while (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);

                    if (field.getName().equals(params[0])) {
                        map.put(params[0], field.get(obj));
                    }
                }

                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            log.error("get object if from return object error", e);
        }

        return map;
    }

    public static boolean isUdfResource(Map<String, Object> paramsMap) {
        ResourceType resourceType = (ResourceType) paramsMap.get(Constants.STRING_PLUGIN_PARAM_TYPE);
        return resourceType != null && resourceType.equals(ResourceType.UDF);
    }

    public static boolean isFolder(String name) {
        return name != null && name.endsWith("/");
    }

    public static String getFileAuditObject(AuditType auditType, Map<String, Object> paramsMap, String name) {
        boolean isUdfResource = isUdfResource(paramsMap);
        boolean isFolder = auditType == AuditType.FOLDER_CREATE || isFolder(name);
        if (isUdfResource) {
            return isFolder ? AuditModelType.UDF_FOLDER.getName() : AuditModelType.UDF_FILE.getName();
        } else {
            return isFolder ? AuditModelType.FOLDER.getName() : AuditModelType.FILE.getName();
        }
    }

}
