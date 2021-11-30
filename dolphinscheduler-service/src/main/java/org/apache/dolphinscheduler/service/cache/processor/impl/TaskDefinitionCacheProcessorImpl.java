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
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.service.cache.processor.ProcessDefinitionCacheProcessor;
import org.apache.dolphinscheduler.service.cache.processor.TaskDefinitionCacheProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@CacheConfig(cacheNames = "taskDefinition")
public class TaskDefinitionCacheProcessorImpl implements TaskDefinitionCacheProcessor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Override
    @Cacheable(sync = true, key = "#taskCode + '_' + #taskDefinitionVersion")
    public TaskDefinition queryByDefinitionCodeAndVersion(long taskCode, int taskDefinitionVersion) {
        return taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskCode, taskDefinitionVersion);
    }

    @Override
    public void cacheExpire(Class updateObjClass, String updateObjJson) {
        ProcessDefinition updateQueue = (ProcessDefinition) JSONUtils.parseObject(updateObjJson, updateObjClass);
        if (updateQueue == null) {
            return;
        }

    }
}
