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

package org.apache.dolphinscheduler.server.master.rpc;

import org.apache.dolphinscheduler.common.enums.CacheType;
import org.apache.dolphinscheduler.extract.master.IMasterCacheService;
import org.apache.dolphinscheduler.extract.master.transportor.CacheExpireRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MasterCacheServiceImpl implements IMasterCacheService {

    @Autowired
    private CacheManager cacheManager;

    @Override
    public void cacheExpire(CacheExpireRequest cacheExpireRequest) {
        if (cacheExpireRequest.getCacheKey().isEmpty()) {
            return;
        }

        CacheType cacheType = cacheExpireRequest.getCacheType();
        Cache cache = cacheManager.getCache(cacheType.getCacheName());
        if (cache != null) {
            cache.evict(cacheExpireRequest.getCacheKey());
            log.info("cache evict, type:{}, key:{}", cacheType.getCacheName(), cacheExpireRequest.getCacheKey());
        }
    }

}
