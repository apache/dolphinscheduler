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

package org.apache.dolphinscheduler.service.cache.impl;

import org.apache.dolphinscheduler.common.enums.CacheType;
import org.apache.dolphinscheduler.service.cache.BaseCacheProxy;
import org.apache.dolphinscheduler.service.cache.QueueCacheProxy;
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

    @Autowired
    private QueueCacheProxy queueCacheProxy;

    Map<CacheType, BaseCacheProxy> cacheProxyMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        cacheProxyMap.put(CacheType.TENANT, tenantCacheProxy);
        cacheProxyMap.put(CacheType.USER, userCacheProxy);
        cacheProxyMap.put(CacheType.QUEUE, queueCacheProxy);
    }

    public BaseCacheProxy getCacheProxy(CacheType cacheType) {
        return cacheProxyMap.get(cacheType);
    }
}
