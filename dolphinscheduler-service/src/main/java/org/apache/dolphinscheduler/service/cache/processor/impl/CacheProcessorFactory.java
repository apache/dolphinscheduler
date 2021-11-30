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

package org.apache.dolphinscheduler.service.cache.processor.impl;

import org.apache.dolphinscheduler.common.enums.CacheType;
import org.apache.dolphinscheduler.service.cache.processor.BaseCacheProcessor;
import org.apache.dolphinscheduler.service.cache.processor.ProcessDefinitionCacheProcessor;
import org.apache.dolphinscheduler.service.cache.processor.ProcessTaskRelationCacheProcessor;
import org.apache.dolphinscheduler.service.cache.processor.QueueCacheProcessor;
import org.apache.dolphinscheduler.service.cache.processor.TaskDefinitionCacheProcessor;
import org.apache.dolphinscheduler.service.cache.processor.TenantCacheProcessor;
import org.apache.dolphinscheduler.service.cache.processor.UserCacheProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CacheProcessorFactory {

    @Autowired
    private TenantCacheProcessor tenantCacheProcessor;

    @Autowired
    private UserCacheProcessor userCacheProcessor;

    @Autowired
    private QueueCacheProcessor queueCacheProcessor;

    @Autowired
    private ProcessDefinitionCacheProcessor processDefinitionCacheProcessor;

    @Autowired
    private ProcessTaskRelationCacheProcessor processTaskRelationCacheProcessor;

    @Autowired
    private TaskDefinitionCacheProcessor taskDefinitionCacheProcessor;

    Map<CacheType, BaseCacheProcessor> cacheProcessorMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        cacheProcessorMap.put(CacheType.TENANT, tenantCacheProcessor);
        cacheProcessorMap.put(CacheType.USER, userCacheProcessor);
        cacheProcessorMap.put(CacheType.QUEUE, queueCacheProcessor);
        cacheProcessorMap.put(CacheType.PROCESS_DEFINITION, processDefinitionCacheProcessor);
        cacheProcessorMap.put(CacheType.PROCESS_TASK_RELATION, processTaskRelationCacheProcessor);
        cacheProcessorMap.put(CacheType.TASK_DEFINITION, taskDefinitionCacheProcessor);
    }

    public BaseCacheProcessor getCacheProcessor(CacheType cacheType) {
        return cacheProcessorMap.get(cacheType);
    }
}
