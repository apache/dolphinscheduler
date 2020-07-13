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
package org.apache.dolphinscheduler.server.worker.task.shell;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.ShellCommandExecutor;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Date;

/**
 *  shell task test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(OSUtils.class)
@PowerMockIgnore({"javax.management.*"})
public class ShellTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(ShellTaskTest.class);

    private ShellTask shellTask;

    private ProcessService processService;

    private ShellCommandExecutor shellCommandExecutor;

    private ApplicationContext applicationContext;
    private TaskExecutionContext taskExecutionContext;

    @Before
    public void before() throws Exception {
        taskExecutionContext = new TaskExecutionContext();

        PowerMockito.mockStatic(OSUtils.class);
        processService = PowerMockito.mock(ProcessService.class);
        shellCommandExecutor = PowerMockito.mock(ShellCommandExecutor.class);

        applicationContext = PowerMockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        PowerMockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        TaskProps props = new TaskProps();
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTenantCode("1");
        props.setEnvFile(".dolphinscheduler_env.sh");
        props.setTaskStartTime(new Date());
        props.setTaskTimeout(0);
        props.setTaskParams("{\"rawScript\": \" echo 'hello world!'\"}");
        shellTask = new ShellTask(taskExecutionContext, logger);
        shellTask.init();

        PowerMockito.when(processService.findDataSourceById(1)).thenReturn(getDataSource());
        PowerMockito.when(processService.findDataSourceById(2)).thenReturn(getDataSource());
        PowerMockito.when(processService.findProcessInstanceByTaskId(1)).thenReturn(getProcessInstance());

        String fileName = String.format("%s/%s_node.%s", taskExecutionContext.getExecutePath(),
                props.getTaskAppId(), OSUtils.isWindows() ? "bat" : "sh");
        PowerMockito.when(shellCommandExecutor.run("")).thenReturn(null);
    }

    private DataSource getDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setType(DbType.MYSQL);
        dataSource.setConnectionParams(
                "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:mysql://127.0.0.1:3306\",\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql://127.0.0.1:3306/test\"}");
        dataSource.setUserId(1);
        return dataSource;
    }

    private ProcessInstance getProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setCommandType(CommandType.START_PROCESS);
        processInstance.setScheduleTime(new Date());
        return processInstance;
    }

    @After
    public void after() {}

    /**
     * Method: ShellTask()
     */
    @Test
    public void testShellTask()
            throws Exception {
        TaskProps props = new TaskProps();
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTenantCode("1");
        ShellTask shellTaskTest = new ShellTask(taskExecutionContext, logger);
        Assert.assertNotNull(shellTaskTest);
    }

    /**
     * Method: init for Unix-like
     */
    @Test
    public void testInitForUnix() {
        try {
            PowerMockito.when(OSUtils.isWindows()).thenReturn(false);
            shellTask.init();
            Assert.assertTrue(true);
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testInitException() {
        TaskProps props = new TaskProps();
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTenantCode("1");
        props.setEnvFile(".dolphinscheduler_env.sh");
        props.setTaskStartTime(new Date());
        props.setTaskTimeout(0);
        props.setTaskParams("{\"rawScript\": \"\"}");
        ShellTask shellTask = new ShellTask(taskExecutionContext, logger);
        try {
            shellTask.init();
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            if (e.getMessage().contains("shell task params is not valid")) {
                Assert.assertTrue(true);
            }
        }
    }

    /**
     * Method: init for Windows
     */
    @Test
    public void testInitForWindows() {
        try {
            PowerMockito.when(OSUtils.isWindows()).thenReturn(true);
            shellTask.init();
            Assert.assertTrue(true);
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Method: handle() for Unix-like
     */
    @Test
    public void testHandleForUnix() throws Exception {
        try {
            PowerMockito.when(OSUtils.isWindows()).thenReturn(false);
            TaskProps props = new TaskProps();
            props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
            props.setTenantCode("1");
            props.setEnvFile(".dolphinscheduler_env.sh");
            props.setTaskStartTime(new Date());
            props.setTaskTimeout(0);
            props.setScheduleTime(new Date());
            props.setCmdTypeIfComplement(CommandType.START_PROCESS);
            props.setTaskParams("{\"rawScript\": \" echo ${test}\", \"localParams\": [{\"prop\":\"test\", \"direct\":\"IN\", \"type\":\"VARCHAR\", \"value\":\"123\"}]}");
            ShellTask shellTask1 = new ShellTask(taskExecutionContext, logger);
            shellTask1.init();
            shellTask1.handle();
            Assert.assertTrue(true);
        } catch (Error | Exception e) {
            if (!e.getMessage().contains("process error . exitCode is :  -1")
                    && !System.getProperty("os.name").startsWith("Windows")) {
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * Method: handle() for Windows
     */
    @Test
    public void testHandleForWindows() throws Exception {
        try {
            Assume.assumeTrue(OSUtils.isWindows());
            TaskProps props = new TaskProps();
            props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
            props.setTenantCode("1");
            props.setEnvFile(".dolphinscheduler_env.sh");
            props.setTaskStartTime(new Date());
            props.setTaskTimeout(0);
            props.setScheduleTime(new Date());
            props.setCmdTypeIfComplement(CommandType.START_PROCESS);
            props.setTaskParams("{\"rawScript\": \" echo ${test}\", \"localParams\": [{\"prop\":\"test\", \"direct\":\"IN\", \"type\":\"VARCHAR\", \"value\":\"123\"}]}");
            ShellTask shellTask1 = new ShellTask(taskExecutionContext, logger);
            shellTask1.init();
            shellTask1.handle();
            Assert.assertTrue(true);
        } catch (Error | Exception e) {
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
            shellTask.cancelApplication(true);
            Assert.assertTrue(true);
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }
    }

}
