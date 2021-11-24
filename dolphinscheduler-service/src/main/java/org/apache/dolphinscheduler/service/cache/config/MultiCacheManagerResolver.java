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

import org.apache.dolphinscheduler.common.enums.CacheType;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.support.NoOpCache;

/**
 * multi cache manager resolver
 */
public class MultiCacheManagerResolver implements CacheResolver {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean enableCache = false;

    private CacheManager userCacheManager;

    private CacheManager tenantCacheManager;

    public void setUserCacheManager(CacheManager userCacheManager) {
        this.userCacheManager = userCacheManager;
    }

    public void setTenantCacheManager(CacheManager tenantCacheManager) {
        this.tenantCacheManager = tenantCacheManager;
    }

    MultiCacheManagerResolver(boolean enableCache) {
        this.enableCache = enableCache;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        if (!this.enableCache) {
            return Collections.singletonList(new NoOpCache("NoOpCache"));
        }

        Set<String> cacheNames = context.getOperation().getCacheNames();
        if (CollectionUtils.isEmpty(cacheNames)) {
            return Collections.emptyList();
        }

        Optional<String> cacheNameOptional = cacheNames.stream().findFirst();
        if (!cacheNameOptional.isPresent()) {
            return Collections.emptyList();
        }

        CacheType cacheType = CacheType.valueOf(cacheNameOptional.get().toUpperCase());

        CacheManager cacheManager;
        switch (cacheType) {
            case TENANT:
                cacheManager = tenantCacheManager;
                break;
            case USER:
                cacheManager = userCacheManager;
                break;
            default:
                throw new IllegalArgumentException("can not find cache manager for name:" + cacheNameOptional.get());
        }

        logger.debug("resolveCaches, cacheType:{}", cacheType);

        List<Cache> result = new ArrayList<>(cacheNames.size());
        for (String name : cacheNames) {
            result.add(cacheManager.getCache(name));
        }
        return result;
    }
}
