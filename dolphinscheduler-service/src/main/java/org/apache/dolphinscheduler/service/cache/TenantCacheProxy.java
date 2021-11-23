package org.apache.dolphinscheduler.service.cache;

import org.apache.dolphinscheduler.dao.entity.Tenant;

public interface TenantCacheProxy extends BaseCacheProxy{
    void update(int tenantId);

    Tenant queryById(int tenantId);
}
