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

package org.apache.dolphinscheduler.service.task;

import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TaskPluginManagerTest {

    @Autowired
    private TaskPluginManager taskPluginManager;

    @Before
    public void before() throws Exception {
        taskPluginManager = new TaskPluginManager();
        taskPluginManager.installPlugin(null);
        Assert.assertNotNull(taskPluginManager.getTaskChannelMap());
    }

    @Test
    public void testGetTaskChannel() {
        Assert.assertNotNull(taskPluginManager.getTaskChannel(TaskConstants.TASK_TYPE_BLOCKING));
        Assert.assertNotNull(taskPluginManager.getTaskChannel(TaskConstants.TASK_TYPE_CONDITIONS));
        Assert.assertNotNull(taskPluginManager.getTaskChannel(TaskConstants.TASK_TYPE_SQL));
        Assert.assertNull(taskPluginManager.getTaskChannel("NUlL_NAME"));
    }
}
