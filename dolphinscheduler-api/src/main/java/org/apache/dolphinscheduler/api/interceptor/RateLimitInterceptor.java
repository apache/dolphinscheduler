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

package org.apache.dolphinscheduler.api.interceptor;

import org.apache.dolphinscheduler.api.configuration.ApiConfig;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;

/**
 * This interceptor is used to control the traffic, consists with global traffic control and tenant-leve traffic control.
 * If the current coming tenant reaches his tenant-level request quota, his request will be reject fast.
 * If the current system request number reaches the global request quota, all coming request will be reject fast.
 */
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private ApiConfig.TrafficConfiguration trafficConfiguration;

    private RateLimiter globalRateLimiter;

    private LoadingCache<String, RateLimiter> tenantRateLimiterCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, RateLimiter>() {

                @Override
                public RateLimiter load(String token) {
                    // use tenant customize rate limit
                    Map<String, Integer> customizeTenantQpsRate = trafficConfiguration.getCustomizeTenantQpsRate();
                    int tenantQuota = trafficConfiguration.getDefaultTenantQpsRate();
                    if (MapUtils.isNotEmpty(customizeTenantQpsRate)) {
                        tenantQuota = customizeTenantQpsRate.getOrDefault(token,
                                trafficConfiguration.getDefaultTenantQpsRate());
                    }
                    // use tenant default rate limit
                    return RateLimiter.create(tenantQuota, 1, TimeUnit.SECONDS);
                }
            });

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws ExecutionException {
        // tenant-level rate limit
        if (trafficConfiguration.isTenantSwitch()) {
            String token = request.getHeader("token");
            if (!StringUtils.isEmpty(token)) {
                RateLimiter tenantRateLimiter = tenantRateLimiterCache.get(token);
                if (!tenantRateLimiter.tryAcquire()) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    log.warn("Too many request, reach tenant rate limit, current tenant:{} qps is {}", token,
                            tenantRateLimiter.getRate());
                    return false;
                }
            }
        }
        // global rate limit
        if (trafficConfiguration.isGlobalSwitch()) {
            if (!globalRateLimiter.tryAcquire()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                log.warn("Too many request, reach global rate limit, current qps is {}",
                        globalRateLimiter.getRate());
                return false;
            }
        }
        return true;
    }

    public RateLimitInterceptor(ApiConfig.TrafficConfiguration trafficConfiguration) {
        this.trafficConfiguration = trafficConfiguration;
        if (trafficConfiguration.isGlobalSwitch()) {
            this.globalRateLimiter =
                    RateLimiter.create(trafficConfiguration.getMaxGlobalQpsRate(), 1, TimeUnit.SECONDS);
        }
    }

}
