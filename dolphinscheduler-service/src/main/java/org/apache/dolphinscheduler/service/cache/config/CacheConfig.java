package org.apache.dolphinscheduler.service.cache.config;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.RegistryFactory;
import org.apache.dolphinscheduler.registry.api.RegistryFactoryLoader;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.SpringCacheAnnotationParser;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * cache config
 */
@Configuration
public class CacheConfig extends CachingConfigurerSupport {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String CACHE_PREFIX = "cache";
    private static final String CACHE_CONFIG_FILE_PATH = "/cache.properties";
    private static final int DEFAULT_EXPIRE_MIN = 1;
    private static final int DEFAULT_MAX_SIZE = 100;

    private boolean cacheEnable = false;
    private int tenantExpire = DEFAULT_EXPIRE_MIN;
    private int tenantMaxSize = DEFAULT_MAX_SIZE;
    private int userExpire = DEFAULT_EXPIRE_MIN;
    private int userMaxSize = DEFAULT_MAX_SIZE;

    private static final String CACHE_ENABLE = "enable";
    private static final String TENANT_EXPIRE = "tenant.expire";
    private static final String TENANT_MAX_SIZE = "tenant.max.size";
    private static final String USER_EXPIRE = "user.expire";
    private static final String USER_MAX_SIZE = "user.max.size";

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    @PostConstruct
    public void afterConstruct() {
        start();
    }

    /**
     * cache manager for tenant
     * @return
     */
    @Bean
    public CacheManager tenantCacheManager() {
        if (!cacheEnable) {
            return null;
        }
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(this.tenantExpire, TimeUnit.MINUTES)
                .maximumSize(this.tenantMaxSize));
        return cacheManager;
    }

    /**
     * cache manager for user
     * @return
     */
    @Bean
    public CacheManager userCacheManager() {
        if (!cacheEnable) {
            return null;
        }
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(this.userExpire, TimeUnit.MINUTES)
                .maximumSize(this.userMaxSize));
        return cacheManager;
    }

    /**
     * cache resolver
     * @return
     */
    @Bean
    public CacheResolver cacheResolver() {
        MultiCacheManagerResolver cacheResolver = new MultiCacheManagerResolver(this.cacheEnable);
        cacheResolver.setTenantCacheManager(tenantCacheManager());
        cacheResolver.setUserCacheManager(userCacheManager());
        return cacheResolver;
    }

    private void start() {
        if (isStarted.compareAndSet(false, true)) {
            PropertyUtils.loadPropertyFile(CACHE_CONFIG_FILE_PATH);
            Map<String, String> cacheConfig = PropertyUtils.getPropertiesByPrefix(CACHE_PREFIX);
            if (cacheConfig == null || cacheConfig.size() == 0) {
                return;
            }

            cacheConfig.forEach((k, v) -> logger.debug("cache config: {}:{}", k, v));

            this.cacheEnable = Boolean.parseBoolean(cacheConfig.get(CACHE_ENABLE));
            this.tenantExpire = Integer.parseInt(cacheConfig.get(TENANT_EXPIRE));
            this.tenantMaxSize = Integer.parseInt(cacheConfig.get(TENANT_MAX_SIZE));
            this.userExpire = Integer.parseInt(cacheConfig.get(USER_EXPIRE));
            this.userMaxSize = Integer.parseInt(cacheConfig.get(USER_MAX_SIZE));
        }
    }
}
