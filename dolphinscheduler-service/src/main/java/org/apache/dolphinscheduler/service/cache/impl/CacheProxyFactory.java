package org.apache.dolphinscheduler.service.cache.impl;

import org.apache.dolphinscheduler.common.enums.CacheType;
import org.apache.dolphinscheduler.service.cache.BaseCacheProxy;
import org.apache.dolphinscheduler.service.cache.TenantCacheProxy;
import org.apache.dolphinscheduler.service.cache.UserCacheProxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CacheProxyFactory {

    @Autowired
    private TenantCacheProxy tenantCacheProxy;

    @Autowired
    private UserCacheProxy userCacheProxy;

    Map<CacheType, BaseCacheProxy> cacheProxyMap = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        cacheProxyMap.put(CacheType.TENANT, tenantCacheProxy);
        cacheProxyMap.put(CacheType.USER, userCacheProxy);
    }

    public BaseCacheProxy getCacheProxy(CacheType cacheType) {
        return cacheProxyMap.get(cacheType);
    }
}
