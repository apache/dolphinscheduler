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

import org.apache.dolphinscheduler.api.audit.operator.Operator;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import io.swagger.v3.oas.annotations.Operation;

@Aspect
@Slf4j
public class OperatorLogAspect {

    @Pointcut("@annotation(org.apache.dolphinscheduler.api.audit.OperatorLog)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        OperatorLog operatorLog = method.getAnnotation(OperatorLog.class);
        // Api don't need record log
        if (operatorLog == null) {
            return point.proceed();
        }

        Operation operation = method.getAnnotation(Operation.class);
        if (operation == null) {
            log.warn("Operation is null of method: {}", method.getName());
            return point.proceed();
        }

        Operator operator = SpringApplicationContext.getBean(operatorLog.auditType().getOperatorClass());
        return operator.recordAudit(point, operation.description(), operatorLog.auditType());
    }
}
