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
package org.apache.dolphinscheduler.server.master.cache;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

/**
 * @author 离歌笑
 * @desc
 * @date 2020-05-22
 */
public class TaskInstanceCacheManagerTest {

    private TaskInstanceCacheManager taskInstanceCacheManager;

    @Before
    public void setup(){
        taskInstanceCacheManager= Mockito.mock(TaskInstanceCacheManager.class);
    }

    @Test
    public void getByTaskInstanceId() {
        // when
        TaskInstance taskInstance = taskInstance();
        Mockito.when(taskInstanceCacheManager.getByTaskInstanceId(1)).thenReturn(taskInstance);

        TaskInstance result = taskInstanceCacheManager.getByTaskInstanceId(1);
        // assert
        Assert.assertEquals(taskInstance,result);
    }

    @Test
    public void cacheTaskInstance() {
        TaskInstance taskInstance=taskInstance();
        Mockito.doNothing().when(taskInstanceCacheManager).cacheTaskInstance(taskInstance);
    }

    @Test
    public void removeByTaskInstanceId() {
        // when
        TaskInstance taskInstance=new TaskInstance();
        taskInstanceCacheManager.cacheTaskInstance(taskInstance);

        Mockito.doNothing().when(taskInstanceCacheManager).removeByTaskInstanceId(taskInstance.getId());
    }

    private TaskInstance taskInstance(){
        TaskInstance taskInstance=new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setStartTime(new Date());
        return taskInstance;
    }

}