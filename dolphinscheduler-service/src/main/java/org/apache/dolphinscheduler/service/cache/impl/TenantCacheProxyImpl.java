package org.apache.dolphinscheduler.service.cache.impl;

import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.cache.TenantCacheProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@CacheConfig(cacheResolver = "cacheResolver", cacheNames = "tenant")
public class TenantCacheProxyImpl implements TenantCacheProxy {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TenantMapper tenantMapper;

    @Override
    @CacheEvict
    public void update(int tenantId) {
        // just evict cache
    }

    @Override
    @Cacheable(sync = true)
    public Tenant queryById(int tenantId) {
        logger.info("tenantCacheProxy queryById:{}", tenantId);
        return tenantMapper.queryById(tenantId);
    }

    @Override
    public void cacheExpire(Object updateObj) {
        Tenant updateTenant = (Tenant) updateObj;
        SpringApplicationContext.getBean(TenantCacheProxy.class).update(updateTenant.getId());
    }
}
