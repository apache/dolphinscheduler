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

package org.apache.dolphinscheduler.server.utils;

import org.apache.dolphinscheduler.server.log.TaskLogDiscriminator;
import org.apache.dolphinscheduler.service.queue.entity.TaskExecutionContext;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.sift.SiftingAppender;

@RunWith(MockitoJUnitRunner.class)
public class LogUtilsTest {

    @Test
    public void testGetTaskLogPath() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setProcessInstanceId(100);
        taskExecutionContext.setTaskInstanceId(1000);
        taskExecutionContext.setProcessDefineCode(1L);
        taskExecutionContext.setProcessDefineVersion(1);

        Logger rootLogger = (Logger) LoggerFactory.getILoggerFactory().getLogger("ROOT");
        Assert.assertNotNull(rootLogger);

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
                .resolve("1_1").resolve("100").resolve("1000.log");
        Assert.assertEquals(logPath.toString(), LogUtils.getTaskLogPath(taskExecutionContext));
    }

}
