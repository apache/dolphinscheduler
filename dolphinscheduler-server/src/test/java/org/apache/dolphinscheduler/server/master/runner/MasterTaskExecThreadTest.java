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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.log.TaskLogDiscriminator;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Sets;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.sift.SiftingAppender;

@RunWith(MockitoJUnitRunner.Silent.class)
@PrepareForTest(MasterTaskExecThread.class)
public class MasterTaskExecThreadTest {

    private MasterTaskExecThread masterTaskExecThread;

    private SpringApplicationContext springApplicationContext;

    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    @Before
    public void setUp() {

        ApplicationContext applicationContext = PowerMockito.mock(ApplicationContext.class);
        this.springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        this.zookeeperRegistryCenter = PowerMockito.mock(ZookeeperRegistryCenter.class);
        PowerMockito.when(SpringApplicationContext.getBean(ZookeeperRegistryCenter.class))
                .thenReturn(this.zookeeperRegistryCenter);
        this.masterTaskExecThread = new MasterTaskExecThread(null);
    }

    @Test
    public void testExistsValidWorkerGroup1(){

        Mockito.when(zookeeperRegistryCenter.getWorkerGroupDirectly()).thenReturn(Sets.newHashSet());
        boolean b = masterTaskExecThread.existsValidWorkerGroup("default");
        Assert.assertFalse(b);
    }
    @Test
    public void testExistsValidWorkerGroup2(){
        Set<String> workerGorups = new HashSet<>();
        workerGorups.add("test1");
        workerGorups.add("test2");

        Mockito.when(zookeeperRegistryCenter.getWorkerGroupDirectly()).thenReturn(workerGorups);
        boolean b = masterTaskExecThread.existsValidWorkerGroup("default");
        Assert.assertFalse(b);
    }

    @Test
    public void testExistsValidWorkerGroup3(){
        Set<String> workerGorups = new HashSet<>();
        workerGorups.add("test1");

        Mockito.when(zookeeperRegistryCenter.getWorkerGroupDirectly()).thenReturn(workerGorups);
        Mockito.when(zookeeperRegistryCenter.getWorkerGroupNodesDirectly("test1")).thenReturn(workerGorups);
        boolean b = masterTaskExecThread.existsValidWorkerGroup("test1");
        Assert.assertTrue(b);
    }

    @Test
    public void testPauseTask(){


        ProcessService processService = Mockito.mock(ProcessService.class);
        Mockito.when(this.springApplicationContext.getBean(ProcessService.class))
                .thenReturn(processService);

        TaskInstance taskInstance = getTaskInstance();
        Mockito.when(processService.findTaskInstanceById(252612))
                .thenReturn(taskInstance);

        Mockito.when(processService.updateTaskInstance(taskInstance))
                .thenReturn(true);

        MasterTaskExecThread masterTaskExecThread = new MasterTaskExecThread(taskInstance);
        masterTaskExecThread.pauseTask();
        org.junit.Assert.assertEquals(ExecutionStatus.PAUSE, taskInstance.getState());
    }

    @Test
    public void testGetTaskLogPath() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setProcessDefinitionId(1);
        taskInstance.setProcessInstanceId(100);
        taskInstance.setId(1000);

        Logger rootLogger = (Logger) LoggerFactory.getILoggerFactory().getLogger("ROOT");
        Assert.assertNotNull(rootLogger);

        MasterTaskExecThread taskExecThread = new MasterTaskExecThread(taskInstance);

        Assert.assertEquals("/", Constants.SINGLE_SLASH);
        Assert.assertEquals("", taskExecThread.getTaskLogPath(taskInstance));

        SiftingAppender appender = Mockito.mock(SiftingAppender.class);
        // it's a trick to mock logger.getAppend("TASKLOGFILE")
        Mockito.when(appender.getName()).thenReturn("TASKLOGFILE");
        rootLogger.addAppender(appender);

        Path logBase = Paths.get("path").resolve("to").resolve("test");

        TaskLogDiscriminator taskLogDiscriminator = Mockito.mock(TaskLogDiscriminator.class);
        Mockito.when(taskLogDiscriminator.getLogBase()).thenReturn(logBase.toString());
        Mockito.when(appender.getDiscriminator()).thenReturn(taskLogDiscriminator);

        Path logPath = Paths.get(".").toAbsolutePath().getParent()
                .resolve(logBase)
                .resolve("1").resolve("100").resolve("1000.log");
        Assert.assertEquals(logPath.toString(), taskExecThread.getTaskLogPath(taskInstance));
    }

    private TaskInstance getTaskInstance(){
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskType("SHELL");
        taskInstance.setId(252612);
        taskInstance.setName("C");
        taskInstance.setProcessInstanceId(10111);
        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        return taskInstance;
    }

}
