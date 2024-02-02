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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.service.AuditService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.AuditObjectType;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.dao.mapper.*;
import org.apache.dolphinscheduler.spi.enums.ResourceType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class OperatorLogAspect {

    @Autowired
    private AuditService auditService;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private UserMapper userMapper;

    @Pointcut("@annotation(OperatorLog)")
    public void logPointCut() {

    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        OperatorLog operatorLog = method.getAnnotation(OperatorLog.class);
        if (operatorLog == null) {
            return null;
        }

        AuditObjectType auditObjectType = operatorLog.objectType();
        AuditOperationType auditOperationType = operatorLog.operationType();

        Operation operation = method.getAnnotation(Operation.class);
        if (operation == null) {
            log.error("Operation is null");
            return null;
        }

        Object[] args = point.getArgs();
        String[] strings = signature.getParameterNames();

        User user = null;

        Map<String, Object> paramsMap = new HashMap<>();
        for (int i = 0; i < strings.length; i++) {
            paramsMap.put(strings[i], args[i]);

            if (args[i] instanceof User) {
                user = (User)args[i];
            }
        }

        if(user == null) {
            log.error("user is null");
            return null;
        }


        ResourceType resourceType = (ResourceType)paramsMap.get("type");

        switch (auditObjectType) {
            case FOLDER:
                if(resourceType != null && resourceType.equals(ResourceType.UDF))  auditObjectType = AuditObjectType.UDF_FOLDER;
                break;
            case FILE:
                if(resourceType != null && resourceType.equals(ResourceType.UDF))  auditObjectType = AuditObjectType.UDF_FILE;
                break;
            case WORKER_GROUP:
                if(auditOperationType == AuditOperationType.CREATE &&
                        !paramsMap.get("id").toString().equals("0"))  {
                    auditOperationType = AuditOperationType.UPDATE;
                }
                break;
            default:
                break;
        }

        if(auditOperationType.isIntermediateState()) {
            switch (auditOperationType) {
                case RELEASE:
                    ReleaseState releaseState = (ReleaseState)paramsMap.get("releaseState");
                    switch (releaseState) {
                        case ONLINE:
                            auditOperationType = AuditOperationType.ONLINE;
                            break;
                        case OFFLINE:
                            auditOperationType = AuditOperationType.OFFLINE;
                            break;
                        default:
                            break;
                    }
                    break;
                case EXECUTE:
                    ExecuteType executeType = (ExecuteType)paramsMap.get("executeType");
                    switch (executeType) {
                        case REPEAT_RUNNING:
                            auditOperationType = AuditOperationType.RERUN;
                            break;
                        case RECOVER_SUSPENDED_PROCESS:
                            auditOperationType = AuditOperationType.RESUME_PAUSE;
                            break;
                        case START_FAILURE_TASK_PROCESS:
                            auditOperationType = AuditOperationType.RESUME_FAILURE;
                            break;
                        case STOP:
                            auditOperationType = AuditOperationType.STOP;
                            break;
                        case PAUSE:
                            auditOperationType = AuditOperationType.PAUSE;
                            break;
                        case EXECUTE_TASK:
                            auditOperationType = AuditOperationType.EXECUTE;
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }

        List<AuditLog> auditLogList = new ArrayList<>();
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(user.getId());
        auditLog.setObjectType(auditObjectType.getCode());
        auditLog.setOperationType(auditOperationType.getCode());
        auditLog.setDescription(operation.description());
        auditLog.setTime(new Date());
        auditLogList.add(auditLog);

        setInformation(operatorLog, auditLogList, paramsMap);

        Result result = (Result)point.proceed();

        if (resultFail(result)) {
            log.error("request fail");
            return result;
        }

        if (operatorLog.returnObjectFieldName().length != 0) {
            auditLog.setObjectId(getObjectIfFromReturnObject(result.getData(), operatorLog.returnObjectFieldName()));
            auditLog.setObjectName(auditService.getObjectNameByObjectId(auditLog.getObjectId(), auditObjectType));
        }

        long duration = System.currentTimeMillis() - beginTime;

        auditService.addAudit(auditLogList, duration);

        return result;
    }

    private void setInformation(OperatorLog operatorLog, List<AuditLog> auditLogList, Map<String, Object> paramsMap) {

        String[] paramNameArr = operatorLog.requestParamName();

        if (paramNameArr.length == 0) {
            return;
        }

        modifyParma(paramNameArr, paramsMap, operatorLog.objectType());

        setObjectByParma(paramNameArr, paramsMap, operatorLog.objectType(), auditLogList);

        switch (operatorLog.operationType()) {
            case SWITCH_VERSION:
            case DELETE_VERSION:
                auditLogList.get(0).setObjectName(paramsMap.get("version").toString());
                break;
            default:
                break;
        }

        if(auditLogList.get(0).getObjectId() == null) {
            auditLogList.get(0).setObjectId(getObjectIdentityByParma(paramNameArr, paramsMap));
        }
    }

    private long getObjectIdentityByParma(String[] paramNameArr, Map<String, Object> paramsMap) {
        for (String name : paramNameArr) {
            if (paramsMap.get(name) instanceof String) {
                String param = (String)paramsMap.get(name);
                if (param.matches("\\d+")) {
                    return Long.parseLong(param);
                }
            }
        }

        return -1;
    }

    private void modifyParma(String[] paramNameArr, Map<String, Object> paramsMap, AuditObjectType objectType) {
        switch (objectType) {
            case SCHEDULE:
                for (int i = 0; i < paramNameArr.length; i++) {
                    if (paramNameArr[i].equals("id")) {
                        int id = (int)paramsMap.get(paramNameArr[i]);
                        Schedule schedule = scheduleMapper.selectById(id);
                        paramsMap.put("code", schedule.getProcessDefinitionCode());
                        paramNameArr[i] = "code";
                    }
                }
                break;
            default:
                break;
        }
    }


    private void setObjectByParma(String[] paramNameArr, Map<String, Object> paramsMap, AuditObjectType objectType, List<AuditLog> auditLogList) {
        for (String name : paramNameArr) {
            if (name.toLowerCase().contains("codes")) {
                String[] codes = ((String)paramsMap.get(name)).split(",");
                AuditLog auditLog = auditLogList.get(0);

                for(String code : codes) {
                    String detail = auditService.getObjectNameByObjectId(
                            Long.parseLong(code), objectType);

                    auditLog.setObjectId(Long.parseLong(code));
                    auditLog.setObjectName(detail);
                    auditLogList.add(auditLog);
                    auditLog = AuditLog.copyNewOne(auditLog);
                }

                auditLogList.remove(0);
            } else if (name.toLowerCase().contains("code")) {
                String detail = "";
                long code = (long)paramsMap.get(name);
                detail = auditService.getObjectNameByObjectId(code, objectType);
                auditLogList.get(0).setObjectName(detail);
                auditLogList.get(0).setObjectId(code);
            } else if (name.toLowerCase().contains("ids")) {
                String[] ids = ((String)paramsMap.get(name)).split(",");
                AuditLog auditLog = auditLogList.get(0);

                for(String id : ids) {
                    String detail = auditService.getObjectNameByObjectId(Long.parseLong(id), objectType);
                    auditLog.setObjectId(Long.parseLong(id));
                    auditLog.setObjectName(detail);
                    auditLogList.add(auditLog);
                    auditLog = AuditLog.copyNewOne(auditLog);
                }

                auditLogList.remove(0);
            } else if (name.toLowerCase().contains("userid")) {
                String detail = "";
                int id = (int)paramsMap.get(name);
                if(objectType.equals(AuditObjectType.UDP_FUNCTION)) {
                    User obj = userMapper.selectById(id);
                    detail =  obj.getEmail();
                }
                auditLogList.get(0).setObjectName(detail);
            }

            else if (name.toLowerCase().contains("id")) {
                int id = (int)paramsMap.get(name);
                auditLogList.get(0).setObjectId((long) id);
                String detail = auditService.getObjectNameByObjectId((long) id, objectType);
                auditLogList.get(0).setObjectName(detail);

            } else {
                auditLogList.get(0).setObjectName(paramsMap.get(name).toString());
            }
        }
    }

    private boolean resultFail(Result result){
        if(result != null && result.isFailed()) {
            return true;
        }

        return false;
    }

    public static long getObjectIfFromReturnObject(Object obj, String[] params) {
        try {
            Class<?> clazz = obj.getClass();

            if (clazz.equals(Long.class)) {
                return  (Long) obj;
            }

            while (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);

                    if (field.getName().equals(params[0])) {
                        return Long.parseLong(field.get(obj).toString());
                    }
                }

                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}