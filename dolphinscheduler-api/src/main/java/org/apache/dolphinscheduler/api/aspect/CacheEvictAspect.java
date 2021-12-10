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

import org.apache.dolphinscheduler.common.enums.CacheType;
import org.apache.dolphinscheduler.remote.command.CacheExpireCommand;
import org.apache.dolphinscheduler.service.cache.CacheNotifyService;
import org.apache.dolphinscheduler.service.cache.impl.CacheKeyGenerator;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * aspect for cache evict
 */
@Aspect
@Component
public class CacheEvictAspect {

    private static final Logger logger = LoggerFactory.getLogger(CacheEvictAspect.class);

    /**
     * symbol of spring el
     */
    private static final String EL_SYMBOL = "#";

    @Autowired
    private CacheKeyGenerator cacheKeyGenerator;

    @Autowired
    private CacheNotifyService cacheNotifyService;

    @Pointcut("@annotation(org.springframework.cache.annotation.CacheEvict)")
    public void cacheEvictPointCut() {
        // Do nothing because of it's a pointcut
    }

    @Around("cacheEvictPointCut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature sign = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = sign.getMethod();
        Object target = proceedingJoinPoint.getTarget();
        Object[] args = proceedingJoinPoint.getArgs();

        Object result = proceedingJoinPoint.proceed();

        CacheConfig cacheConfig = method.getDeclaringClass().getAnnotation(CacheConfig.class);
        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);

        CacheType cacheType = getCacheType(cacheConfig, cacheEvict);
        if (cacheType != null) {
            String cacheKey;
            if (cacheEvict.key().isEmpty()) {
                cacheKey = (String) cacheKeyGenerator.generate(target, method, args);
            } else {
                cacheKey = cacheEvict.key();
                List<Name> paramsList = getParamAnnotationsByType(method, Name.class);
                if (cacheEvict.key().contains(EL_SYMBOL)) {
                    cacheKey = parseKey(cacheEvict.key(), paramsList.stream().map(o -> o.value()).collect(Collectors.toList()), Arrays.asList(args));
                }
            }
            if (StringUtils.isNotEmpty(cacheKey)) {
                cacheNotifyService.notifyMaster(new CacheExpireCommand(cacheType, cacheKey).convert2Command());
            }
        }

        return result;
    }

    private CacheType getCacheType(CacheConfig cacheConfig, CacheEvict cacheEvict) {
        String cacheName = null;
        if (cacheEvict.cacheNames().length > 0) {
            cacheName = cacheEvict.cacheNames()[0];
        }
        if (cacheConfig.cacheNames().length > 0) {
            cacheName = cacheConfig.cacheNames()[0];
        }
        if (cacheName == null) {
            return null;
        }
        for (CacheType cacheType : CacheType.values()) {
            if (cacheType.getCacheName().equals(cacheName)) {
                return cacheType;
            }
        }
        return null;
    }

    private String parseKey(String key, List<String> paramNameList, List<Object> paramList) {
        SpelExpressionParser spelParser = new SpelExpressionParser();
        EvaluationContext ctx = new StandardEvaluationContext();
        for (int i = 0; i < paramNameList.size(); i++) {
            ctx.setVariable("p" + i, paramList.get(i));
        }
        Object obj = spelParser.parseExpression(key).getValue(ctx);
        if (null == obj) {
            throw new RuntimeException("parseKey error");
        }
        return obj.toString();
    }

    private <T extends Annotation> List<T> getParamAnnotationsByType(Method method, Class<T> annotationClass) {
        List<T> annotationsList = new ArrayList<>();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation[] annotationsI = annotations[i];
            for (Annotation annotation : annotationsI) {
                if (annotation.annotationType().equals(annotationClass)) {
                    annotationsList.add((T) annotation);
                }
            }
        }
        return annotationsList;
    }
}
