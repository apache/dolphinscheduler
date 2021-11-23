package org.apache.dolphinscheduler.service.cache.config;

import org.apache.dolphinscheduler.common.enums.CacheType;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
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

    public void setUserCacheManager(CacheManager userCacheManager) {
        this.userCacheManager = userCacheManager;
    }

    public void setTenantCacheManager(CacheManager tenantCacheManager) {
        this.tenantCacheManager = tenantCacheManager;
    }

    private CacheManager tenantCacheManager;

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

        logger.info("cacheNames:{}", Arrays.toString(cacheNames.toArray()));

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

        logger.info("resolveCaches, cacheType:{}", cacheType);

        List<Cache> result = new ArrayList<>(cacheNames.size());
        for (String name : cacheNames) {
            result.add(cacheManager.getCache(name));
        }
        return result;
    }
}
