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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private static final String TRACE_ID = "traceId";

    public static final String sensitiveDataRegEx = "(password=[\'\"]+)(\\S+)([\'\"]+)";

    private static final Pattern sensitiveDataPattern = Pattern.compile(sensitiveDataRegEx, Pattern.CASE_INSENSITIVE);

    @Pointcut("@annotation(org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation)")
    public void logPointCut() {
        // Do nothing because of it's a pointcut
    }

    @Around("logPointCut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // fetch AccessLogAnnotation
        MethodSignature sign = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = sign.getMethod();
        AccessLogAnnotation annotation = method.getAnnotation(AccessLogAnnotation.class);

        String traceId = UUID.randomUUID().toString();

        // log request
        if (!annotation.ignoreRequest()) {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String traceIdFromHeader = request.getHeader(TRACE_ID);
                if (!StringUtils.isEmpty(traceIdFromHeader)) {
                    traceId = traceIdFromHeader;
                }
                // handle login info
                String userName = parseLoginInfo(request);

                // handle args
                String argsString = parseArgs(proceedingJoinPoint, annotation);
                // handle sensitive data in the string
                argsString = handleSensitiveData(argsString);
                logger.info("REQUEST TRACE_ID:{}, LOGIN_USER:{}, URI:{}, METHOD:{}, HANDLER:{}, ARGS:{}",
                        traceId,
                        userName,
                        request.getRequestURI(),
                        request.getMethod(),
                        proceedingJoinPoint.getSignature().getDeclaringTypeName() + "."
                                + proceedingJoinPoint.getSignature().getName(),
                        argsString);

            }
        }

        Object ob = proceedingJoinPoint.proceed();

        // log response
        if (!annotation.ignoreResponse()) {
            logger.info("RESPONSE TRACE_ID:{}, BODY:{}, REQUEST DURATION:{} milliseconds", traceId, ob,
                    (System.currentTimeMillis() - startTime));
        }

        return ob;
    }

    private String parseArgs(ProceedingJoinPoint proceedingJoinPoint, AccessLogAnnotation annotation) {
        Object[] args = proceedingJoinPoint.getArgs();
        String argsString = Arrays.toString(args);
        if (annotation.ignoreRequestArgs().length > 0) {
            String[] parameterNames = ((MethodSignature) proceedingJoinPoint.getSignature()).getParameterNames();
            if (parameterNames.length > 0) {
                Set<String> ignoreSet = Arrays.stream(annotation.ignoreRequestArgs()).collect(Collectors.toSet());
                HashMap<String, Object> argsMap = new HashMap<>();

                for (int i = 0; i < parameterNames.length; i++) {
                    if (!ignoreSet.contains(parameterNames[i])) {
                        argsMap.put(parameterNames[i], args[i]);
                    }
                }
                argsString = argsMap.toString();
            }
        }
        return argsString;
    }

    protected String handleSensitiveData(String originalData) {
        Matcher matcher = sensitiveDataPattern.matcher(originalData.toLowerCase());
        IntStream stream = IntStream.builder().build();
        boolean exists = false;
        while (matcher.find()) {
            if (matcher.groupCount() == 3) {
                stream = IntStream.concat(stream, IntStream.range(matcher.end(1), matcher.end(2)));
                exists = true;
            }
        }

        if (exists) {
            char[] chars = originalData.toCharArray();
            stream.forEach(idx -> {
                chars[idx] = '*';
            });
            return new String(chars);
        }

        return originalData;
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
