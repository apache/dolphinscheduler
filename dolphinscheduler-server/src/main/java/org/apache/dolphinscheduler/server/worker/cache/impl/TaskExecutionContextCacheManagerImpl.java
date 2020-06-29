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

package org.apache.dolphinscheduler.server.worker.cache.impl;

import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.cache.TaskExecutionContextCacheManager;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  TaskExecutionContextCache
 */
@Service
public class TaskExecutionContextCacheManagerImpl implements TaskExecutionContextCacheManager {


    /**
     * taskInstance caceh
     */
    private Map<Integer,TaskExecutionContext> taskExecutionContextCache = new ConcurrentHashMap<>();

    /**
     * get taskInstance by taskInstance id
     *
     * @param taskInstanceId taskInstanceId
     * @return taskInstance
     */
    @Override
    public TaskExecutionContext getByTaskInstanceId(Integer taskInstanceId) {
        return taskExecutionContextCache.get(taskInstanceId);
    }

    /**
     * cache taskInstance
     *
     * @param taskExecutionContext taskExecutionContext
     */
    @Override
    public void cacheTaskExecutionContext(TaskExecutionContext taskExecutionContext) {
        taskExecutionContextCache.put(taskExecutionContext.getTaskInstanceId(),taskExecutionContext);
    }

    /**
     * remove taskInstance by taskInstanceId
     * @param taskInstanceId taskInstanceId
     */
    @Override
    public void removeByTaskInstanceId(Integer taskInstanceId) {
        taskExecutionContextCache.remove(taskInstanceId);
    }
}
