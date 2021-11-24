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

import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * cache config
 */
@Component
public class CacheConfig {

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

    public boolean isCacheEnable() {
        return cacheEnable;
    }

    public int getTenantExpire() {
        return tenantExpire;
    }

    public int getTenantMaxSize() {
        return tenantMaxSize;
    }

    public int getUserExpire() {
        return userExpire;
    }

    public int getUserMaxSize() {
        return userMaxSize;
    }
}
