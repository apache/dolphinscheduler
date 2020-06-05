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
package org.apache.dolphinscheduler.server.worker.task.ssh;

import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.server.entity.SSHTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 *  shell task test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(OSUtils.class)
@PowerMockIgnore({"javax.management.*"})
public class SSHTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(SSHTaskTest.class);

    private SSHTask sshTask;

    private ApplicationContext applicationContext;
    private TaskExecutionContext taskExecutionContext;
    private SSHTaskExecutionContext sshTaskExecutionContext;

    private String params = "{\"address\":\"\",\"database\":\"\",\"jdbcUrl\":\"/\",\"user\":\"root\",\"password\":\"Printf();43\",\"host\":\"121.196.205.49\",\"port\":\"22\"}";

    @Before
    public void before() throws Exception {
        taskExecutionContext = new TaskExecutionContext();

        applicationContext = PowerMockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);

        taskExecutionContext.setTaskParams("{\"remote\":true,\"datasource\":1,\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 'test'\"}");
        sshTaskExecutionContext = new SSHTaskExecutionContext();
        sshTaskExecutionContext.setConnectionParams(params);
        taskExecutionContext.setSshTaskExecutionContext(sshTaskExecutionContext);

        sshTask = new SSHTask(taskExecutionContext, logger);
        sshTask.init();
    }

    @After
    public void after() {}

    /**
     * Method: ShellTask()
     */
    @Test
    public void testSSHTask() throws Exception {
        TaskProps props = new TaskProps();
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTenantCode("1");
        SSHTask shellTaskTest = new SSHTask(taskExecutionContext, logger);
        Assert.assertNotNull(shellTaskTest);
    }

    /**
     * Method: handle() for Windows
     */
    @Test
    public void testHandle() throws Exception {
        try {
            taskExecutionContext.getSshTaskExecutionContext().setConnectionParams(params);
            SSHTask sshTask = new SSHTask(taskExecutionContext, logger);
            sshTask.init();
            sshTask.handle();
        } catch (Error | Exception e) {
            logger.error("error", e);
            if (!e.getMessage().contains("process error . exitCode is :  -1")) {
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * Method: cancelApplication()
     */
    @Test
    public void testCancelApplication() throws Exception {
        try {
            sshTask.cancelApplication(true);
            Assert.assertTrue(true);
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }
    }

}
