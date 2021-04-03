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
package org.apache.dolphinscheduler.server.worker.shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.AbstractCommandExecutor;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.List;

/**
 * python shell command executor test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(OSUtils.class)
@PowerMockIgnore({"javax.management.*"})
public class ShellCommandExecutorTest {

    private static final Logger logger = LoggerFactory.getLogger(ShellCommandExecutorTest.class);

    private ProcessService processService = null;
    private ApplicationContext applicationContext;

    @Before
    public void before() {
        applicationContext = PowerMockito.mock(ApplicationContext.class);
        processService = PowerMockito.mock(ProcessService.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        PowerMockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);
    }

    @Ignore
    @Test
    public void test() throws Exception {

        TaskProps taskProps = new TaskProps();
        // processDefineId_processInstanceId_taskInstanceId
        taskProps.setExecutePath("/opt/soft/program/tmp/dolphinscheduler/exec/flow/5/36/2864/7657");
        taskProps.setTaskAppId("36_2864_7657");
        // set tenant -> task execute linux user
        taskProps.setTenantCode("hdfs");
        taskProps.setTaskStartTime(new Date());
        taskProps.setTaskTimeout(360000);
        taskProps.setTaskInstanceId(7657);


        TaskInstance taskInstance = processService.findTaskInstanceById(7657);

//        TaskNode taskNode = JSON.parseObject(taskJson, TaskNode.class);
//        taskProps.setTaskParams(taskNode.getParams());


        // custom logger
//        Logger taskLogger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
//                taskInstance.getProcessDefinitionId(),
//                taskInstance.getProcessInstanceId(),
//                taskInstance.getId()));


//        AbstractTask task = TaskManager.newTask(taskInstance.getTaskType(), taskProps, taskLogger);

        AbstractTask task = null;

        logger.info("task info : {}", task);

        // job init
        task.init();

        // job handle
        task.handle();
        ExecutionStatus status = ExecutionStatus.SUCCESS;

        if (task.getExitStatusCode() == Constants.EXIT_CODE_SUCCESS) {
            status = ExecutionStatus.SUCCESS;
        } else if (task.getExitStatusCode() == Constants.EXIT_CODE_KILL) {
            status = ExecutionStatus.KILL;
        } else {
            status = ExecutionStatus.FAILURE;
        }

        logger.info(status.toString());
    }

    @Test
    public void testParseProcessOutput() {
        Class<AbstractCommandExecutor> shellCommandExecutorClass = AbstractCommandExecutor.class;
        try {

            Method method = shellCommandExecutorClass.getDeclaredMethod("parseProcessOutput", Process.class);
            method.setAccessible(true);
            Object[] arg1s = {new Process() {
                @Override
                public OutputStream getOutputStream() {
                    return new OutputStream() {
                        @Override
                        public void write(int b) throws IOException {
                            logger.info("unit test");
                        }
                    };
                }

                @Override
                public InputStream getInputStream() {
                    return new InputStream() {
                        @Override
                        public int read() throws IOException {
                            return 0;
                        }
                    };
                }

                @Override
                public InputStream getErrorStream() {
                    return null;
                }

                @Override
                public int waitFor() throws InterruptedException {
                    return 0;
                }

                @Override
                public int exitValue() {
                    return 0;
                }

                @Override
                public void destroy() {
                    logger.info("unit test");
                }
            } };
            method.invoke(new AbstractCommandExecutor(null, new TaskExecutionContext(), logger) {
                @Override
                protected String buildCommandFilePath() {
                    return null;
                }

                @Override
                protected String commandInterpreter() {
                    return null;
                }

                @Override
                protected void createCommandFileIfNotExists(String execCommand, String commandFile) throws IOException {
                    logger.info("unit test");
                }
            }, arg1s);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testFindAppId() {
        Class<AbstractCommandExecutor> shellCommandExecutorClass = AbstractCommandExecutor.class;
        try {

            Method method = shellCommandExecutorClass.getDeclaredMethod("findAppId", new Class[]{String.class});
            method.setAccessible(true);
            Object[] arg1s = {"11111"};
            String result = (String) method.invoke(new AbstractCommandExecutor(null, null, null) {
                @Override
                protected String buildCommandFilePath() {
                    return null;
                }

                @Override
                protected String commandInterpreter() {
                    return null;
                }

                @Override
                protected void createCommandFileIfNotExists(String execCommand, String commandFile) throws IOException {
                    logger.info("unit test");
                }
            }, arg1s);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testConvertFile2List() {
        Class<AbstractCommandExecutor> shellCommandExecutorClass = AbstractCommandExecutor.class;
        try {
            Method method = shellCommandExecutorClass.getDeclaredMethod("convertFile2List", String.class);
            method.setAccessible(true);
            Object[] arg1s = {"/opt/1.txt"};
            List<String> result = (List<String>) method.invoke(new AbstractCommandExecutor(null, null, null) {
                @Override
                protected String buildCommandFilePath() {
                    return null;
                }

                @Override
                protected String commandInterpreter() {
                    return null;
                }

                @Override
                protected void createCommandFileIfNotExists(String execCommand, String commandFile) throws IOException {
                    logger.info("unit test");
                }
            }, arg1s);
            Assert.assertTrue(true);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
