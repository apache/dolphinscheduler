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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.cache.processor.QueueCacheProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

@Component
public class QueueCacheProcessorImpl implements QueueCacheProcessor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    @CacheEvict(cacheNames = "user", allEntries = true)
    public void expireAllUserCache() {
        // just evict cache
        logger.debug("expire all user cache");
    }

    @Override
    public void cacheExpire(Class updateObjClass, String updateObjJson) {
        Queue updateQueue = (Queue) JSONUtils.parseObject(updateObjJson, updateObjClass);
        if (updateQueue == null) {
            return;
        }
        SpringApplicationContext.getBean(QueueCacheProcessor.class).expireAllUserCache();
    }
}
