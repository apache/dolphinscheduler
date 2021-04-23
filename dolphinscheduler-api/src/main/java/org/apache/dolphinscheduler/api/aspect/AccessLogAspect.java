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

package org.apache.dolphinscheduler.api.aspect;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AccessLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(AccessLogAspect.class);

    @Pointcut("@annotation(org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation)")
    public void logPointCut(){
        // Do nothing because of it's a pointcut
    }

    @Around("logPointCut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // fetch AccessLogAnnotation
        MethodSignature sign =  (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = sign.getMethod();
        AccessLogAnnotation annotation = method.getAnnotation(AccessLogAnnotation.class);

        String tranceId = UUID.randomUUID().toString();

        // log request
        if (!annotation.ignoreRequest()) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // handle login info
                String userName = parseLoginInfo(request);

                // handle args
                String argsString = parseArgs(proceedingJoinPoint, annotation);
                logger.info("REQUEST TRANCE_ID:{}, LOGIN_USER:{}, URI:{}, METHOD:{}, HANDLER:{}, ARGS:{}",
                        tranceId,
                        userName,
                        request.getRequestURI(),
                        request.getMethod(),
                        proceedingJoinPoint.getSignature().getDeclaringTypeName() + "." + proceedingJoinPoint.getSignature().getName(),
                        argsString);

            }
        }

        Object ob = proceedingJoinPoint.proceed();

        // log response
        if (!annotation.ignoreResponse()) {
            logger.info("RESPONSE TRANCE_ID:{}, BODY:{}, REQUEST DURATION:{} milliseconds", tranceId, ob, (System.currentTimeMillis() - startTime));
        }

        return ob;
    }

    private String parseArgs(ProceedingJoinPoint proceedingJoinPoint, AccessLogAnnotation annotation) {
        Object[] args = proceedingJoinPoint.getArgs();
        String argsString = Arrays.toString(args);
        if (annotation.ignoreRequestArgs().length > 0) {
            String[] parameterNames = ((MethodSignature) proceedingJoinPoint.getSignature()).getParameterNames();
            if (parameterNames.length > 0) {
                List<String> ignoreList = Arrays.stream(annotation.ignoreRequestArgs()).collect(Collectors.toList());
                HashMap<String, Object> argsMap = new HashMap<>();

                for (int i = 0; i < parameterNames.length; i++) {
                    if (!ignoreList.contains(parameterNames[i])) {
                        argsMap.put(parameterNames[i], args[i]);
                    }
                }
                argsString = argsMap.toString();
            }
        }
        return argsString;
    }

    private String parseLoginInfo(HttpServletRequest request) {
        String userName = "NOT LOGIN";
        User loginUser = (User) (request.getAttribute(Constants.SESSION_USER));
        if (loginUser != null) {
            userName = loginUser.getUserName();
        }
        return userName;
    }

}
