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

package org.apache.dolphinscheduler.service.utils;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.sift.SiftingAppender;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.service.log.TaskLogDiscriminator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class LogUtilsTest {

    @Test
    public void testGetTaskLogPath() {
        Date firstSubmitTime = new Date();
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setProcessInstanceId(100);
        taskExecutionContext.setTaskInstanceId(1000);
        taskExecutionContext.setProcessDefineCode(1L);
        taskExecutionContext.setProcessDefineVersion(1);
        taskExecutionContext.setFirstSubmitTime(firstSubmitTime.getTime());

        Logger rootLogger = (Logger) LoggerFactory.getILoggerFactory().getLogger("ROOT");
        Assertions.assertNotNull(rootLogger);

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
                .resolve(DateUtils.format(firstSubmitTime, Constants.YYYYMMDD, null))
                .resolve("1_1-100-1000.log");
        Assertions.assertEquals(logPath.toString(), LogUtils.getTaskLogPath(taskExecutionContext));
    }

}
