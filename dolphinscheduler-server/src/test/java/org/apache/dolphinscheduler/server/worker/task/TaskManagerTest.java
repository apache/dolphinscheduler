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
package org.apache.dolphinscheduler.server.worker.task;

import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.server.entity.SSHTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.ssh.SSHTask;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 *  ssh task test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(OSUtils.class)
@PowerMockIgnore({"javax.management.*"})
public class TaskManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(TaskManagerTest.class);
    private String params = "{\"address\":\"\",\"database\":\"\",\"jdbcUrl\":\"/\",\"user\":\"root\",\"password\":\"Printf();43\",\"host\":\"121.196.205.49\",\"port\":\"22\"}";
    private TaskExecutionContext taskExecutionContext;
    private SSHTaskExecutionContext sshTaskExecutionContext;

    @Before
    public void before() throws Exception {
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams("{\"remote\":true,\"datasource\":1,\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 'test'\"}");
        taskExecutionContext.setTaskType("SHELL");
        sshTaskExecutionContext = new SSHTaskExecutionContext();
        sshTaskExecutionContext.setConnectionParams(params);
        taskExecutionContext.setSshTaskExecutionContext(sshTaskExecutionContext);
    }

    /**
     * Method: SSHTask()
     */
    @Test
    public void testNewTask() throws Exception {
        AbstractTask task = TaskManager.newTask(taskExecutionContext, logger);
        Assert.assertEquals( task.getClass().getSimpleName(),"SSHTask");
    }



}
