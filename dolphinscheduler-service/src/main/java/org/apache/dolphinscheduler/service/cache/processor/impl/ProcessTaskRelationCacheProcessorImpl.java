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
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.cache.processor.ProcessTaskRelationCacheProcessor;
import org.apache.dolphinscheduler.service.cache.processor.QueueCacheProcessor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@CacheConfig(cacheNames = "processTaskRelation")
public class ProcessTaskRelationCacheProcessorImpl implements ProcessTaskRelationCacheProcessor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Override
    @Cacheable(sync = true, key = "#projectCode + '_' + #processDefinitionCode")
    public List<ProcessTaskRelation> queryByProcessCode(long projectCode, long processDefinitionCode) {
        return processTaskRelationMapper.queryByProcessCode(projectCode, processDefinitionCode);
    }

    @Override
    public void cacheExpire(Class updateObjClass, String updateObjJson) {
        Queue updateQueue = (Queue) JSONUtils.parseObject(updateObjJson, updateObjClass);
        if (updateQueue == null) {
            return;
        }
    }
}
