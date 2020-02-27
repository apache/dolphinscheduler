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
import org.apache.dolphinscheduler.server.worker.task.ShellCommandExecutor;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.server.worker.task.datax.DataxTaskTest;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Date;

/**
 *  shell task test
 */
public class ShellTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(DataxTaskTest.class);

    private ShellTask shellTask;

    private ProcessService processService;

    private ShellCommandExecutor shellCommandExecutor;

    private ApplicationContext applicationContext;

    @Before
    public void before()
            throws Exception {
        processService = Mockito.mock(ProcessService.class);
        shellCommandExecutor = Mockito.mock(ShellCommandExecutor.class);

        applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        TaskProps props = new TaskProps();
        props.setTaskDir("/tmp");
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTaskInstId(1);
        props.setTenantCode("1");
        props.setEnvFile(".dolphinscheduler_env.sh");
        props.setTaskStartTime(new Date());
        props.setTaskTimeout(0);
        props.setTaskParams("{\"rawScript\": \" echo 'hello world!'\"}");
        shellTask = new ShellTask(props, logger);
        shellTask.init();

        Mockito.when(processService.findDataSourceById(1)).thenReturn(getDataSource());
        Mockito.when(processService.findDataSourceById(2)).thenReturn(getDataSource());
        Mockito.when(processService.findProcessInstanceByTaskId(1)).thenReturn(getProcessInstance());

        String fileName = String.format("%s/%s_node.%s", props.getTaskDir(), props.getTaskAppId(), OSUtils.isWindows() ? "bat" : "sh");
        Mockito.when(shellCommandExecutor.run(fileName, processService)).thenReturn(0);
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
    public void after()
            throws Exception {}

    /**
     * Method: ShellTask()
     */
    @Test
    public void testShellTask()
            throws Exception {
        TaskProps props = new TaskProps();
        props.setTaskDir("/tmp");
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTaskInstId(1);
        props.setTenantCode("1");
        Assert.assertNotNull(new ShellTask(props, logger));
    }

    /**
     * Method: init
     */
    @Test
    public void testInit()
            throws Exception {
        try {
            shellTask.init();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Method: handle()
     */
    @Test
    public void testHandle()
            throws Exception {
        try {
            shellTask.handle();
        } catch (RuntimeException e) {
            if (e.getMessage().indexOf("process error . exitCode is :  -1") < 0) {
                Assert.fail();
            }
        }
    }

    /**
     * Method: cancelApplication()
     */
    @Test
    public void testCancelApplication()
            throws Exception {
        try {
            shellTask.cancelApplication(true);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

}
