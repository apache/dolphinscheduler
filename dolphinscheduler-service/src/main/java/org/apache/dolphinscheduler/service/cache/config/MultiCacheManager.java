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

package org.apache.dolphinscheduler.service.cache.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * multi cache manager
 */
@Configuration
public class MultiCacheManager {

    @Autowired
    private CacheConfig cacheConfig;

    /**
     * cache manager for tenant
     */
    @Bean
    public CacheManager tenantCacheManager() {
        if (!cacheConfig.isCacheEnable()) {
            return null;
        }
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(cacheConfig.getTenantExpire(), TimeUnit.MINUTES)
                .maximumSize(cacheConfig.getTenantMaxSize()));
        return cacheManager;
    }

    /**
     * cache manager for user
     */
    @Bean
    public CacheManager userCacheManager() {
        if (!cacheConfig.isCacheEnable()) {
            return null;
        }
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(cacheConfig.getUserExpire(), TimeUnit.MINUTES)
                .maximumSize(cacheConfig.getUserMaxSize()));
        return cacheManager;
    }

    /**
     * cache resolver
     */
    @Bean
    public CacheResolver cacheResolver() {
        MultiCacheManagerResolver cacheResolver = new MultiCacheManagerResolver(cacheConfig.isCacheEnable());
        cacheResolver.setTenantCacheManager(tenantCacheManager());
        cacheResolver.setUserCacheManager(userCacheManager());
        return cacheResolver;
    }
}
