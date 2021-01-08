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

package org.apache.dolphinscheduler.server.worker.cache;

import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.cache.impl.TaskExecutionContextCacheManagerImpl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TaskExecutionContextCacheManagerTest
 */
public class TaskExecutionContextCacheManagerTest {

    private TaskExecutionContextCacheManager taskExecutionContextCacheManager;
    private TaskExecutionContext taskExecutionContext;

    @Before
    public void before() {
        taskExecutionContextCacheManager = new TaskExecutionContextCacheManagerImpl();
    }

    @Test
    public void testGetByTaskInstanceId() {
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskInstanceId(2);
        taskExecutionContextCacheManager.cacheTaskExecutionContext(taskExecutionContext);
        Assert.assertEquals(2, taskExecutionContextCacheManager.getByTaskInstanceId(2).getTaskInstanceId());
    }

    @Test
    public void updateTaskExecutionContext() {
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskInstanceId(1);
        taskExecutionContextCacheManager.cacheTaskExecutionContext(taskExecutionContext);
        Assert.assertTrue(taskExecutionContextCacheManager.updateTaskExecutionContext(taskExecutionContext));
        taskExecutionContextCacheManager.removeByTaskInstanceId(1);
        Assert.assertFalse(taskExecutionContextCacheManager.updateTaskExecutionContext(taskExecutionContext));
    }

}
