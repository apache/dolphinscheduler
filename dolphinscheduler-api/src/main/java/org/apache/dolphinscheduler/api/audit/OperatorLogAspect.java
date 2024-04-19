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
import org.apache.dolphinscheduler.api.audit.operator.AuditOperator;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;

@Aspect
@Slf4j
@Component
public class OperatorLogAspect {

    private static final ThreadLocal<AuditContext> auditThreadLocal = new ThreadLocal<>();

    @Pointcut("@annotation(org.apache.dolphinscheduler.api.audit.OperatorLog)")
    public void logPointCut() {
    }

    @Before("logPointCut()")
    public void before(JoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        OperatorLog operatorLog = method.getAnnotation(OperatorLog.class);
        Operation operation = method.getAnnotation(Operation.class);

        if (operation == null) {
            log.warn("Operation is null of method: {}", method.getName());
            return;
        }

        Map<String, Object> paramsMap = OperatorUtils.getParamsMap(point, signature);
        User user = OperatorUtils.getUser(paramsMap);
        if (user == null) {
            log.error("user is null");
            return;
        }

        AuditType auditType = operatorLog.auditType();

        try {
            AuditOperator operator = SpringApplicationContext.getBean(operatorLog.auditType().getOperatorClass());
            List<AuditLog> auditLogList = OperatorUtils.buildAuditLogList(operation.description(), auditType, user);
            operator.setRequestParam(auditType, auditLogList, paramsMap);
            AuditContext auditContext =
                    new AuditContext(auditLogList, paramsMap, operatorLog, System.currentTimeMillis(), operator);
            auditThreadLocal.set(auditContext);
        } catch (Throwable throwable) {
            log.error("Record audit log error", throwable);
        }
    }

    @AfterReturning(value = "logPointCut()", returning = "returnValue")
    public void afterReturning(Object returnValue) {
        try {
            AuditContext auditContext = auditThreadLocal.get();
            if (auditContext == null) {
                return;
            }
            auditContext.getOperator().recordAudit(auditContext, returnValue);
        } catch (Throwable throwable) {
            log.error("Record audit log error", throwable);
        } finally {
            auditThreadLocal.remove();
        }
    }

    @AfterThrowing("logPointCut()")
    public void afterThrowing() {
        auditThreadLocal.remove();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AuditContext {

        List<AuditLog> auditLogList;
        Map<String, Object> paramsMap;
        OperatorLog operatorLog;
        long beginTime;
        AuditOperator operator;
    }
}
