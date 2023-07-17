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
import org.apache.dolphinscheduler.remote.command.cache.CacheExpireRequest;
import org.apache.dolphinscheduler.service.cache.CacheNotifyService;
import org.apache.dolphinscheduler.service.cache.impl.CacheKeyGenerator;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
public class CacheEvictAspect {

    /**
     * symbol of spring el
     */
    private static final String EL_SYMBOL = "#";

    /**
     * prefix of spring el
     */
    private static final String P = "p";

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
                if (cacheEvict.key().contains(EL_SYMBOL)) {
                    cacheKey = parseKey(cacheEvict.key(), Arrays.asList(args));
                }
            }
            if (StringUtils.isNotEmpty(cacheKey)) {
                cacheNotifyService.notifyMaster(new CacheExpireRequest(cacheType, cacheKey).convert2Command());
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

    private String parseKey(String key, List<Object> paramList) {
        SpelExpressionParser spelParser = new SpelExpressionParser();
        EvaluationContext ctx = new StandardEvaluationContext();
        for (int i = 0; i < paramList.size(); i++) {
            ctx.setVariable(P + i, paramList.get(i));
        }
        Object obj = spelParser.parseExpression(key).getValue(ctx);
        if (null == obj) {
            throw new RuntimeException("parseKey error");
        }
        return obj.toString();
    }
}
