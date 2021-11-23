package org.apache.dolphinscheduler.service.cache;

public interface BaseCacheProxy {
    void cacheExpire(Object updateObj);
}
