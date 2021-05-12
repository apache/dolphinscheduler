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

package org.apache.dolphinscheduler.common.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Test.None;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LoggerUtils.class})
public class LoggerUtilsTest {
    private Logger logger = LoggerFactory.getLogger(LoggerUtilsTest.class);

    @Test
    public void buildTaskId() {

        String taskId = LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX, 798L,1,4084, 15210);

        Assert.assertEquals(" - [taskAppId=TASK-798_1-4084-15210]", taskId);
    }

    @Test
    public void getAppIds() {
        List<String> appIdList = LoggerUtils.getAppIds("Running job: application_1_1", logger);
        Assert.assertEquals("application_1_1", appIdList.get(0));

    }

    @Test
    public void testReadWholeFileContent() throws Exception {
        BufferedReader bufferedReader = PowerMockito.mock(BufferedReader.class);
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);
        PowerMockito.when(bufferedReader.readLine()).thenReturn("").thenReturn(null);
        FileInputStream fileInputStream = PowerMockito.mock(FileInputStream.class);
        PowerMockito.whenNew(FileInputStream.class).withAnyArguments().thenReturn(fileInputStream);

        InputStreamReader inputStreamReader = PowerMockito.mock(InputStreamReader.class);
        PowerMockito.whenNew(InputStreamReader.class).withAnyArguments().thenReturn(inputStreamReader);

        String log = LoggerUtils.readWholeFileContent("/tmp/log");
        Assert.assertNotNull(log);

        PowerMockito.when(bufferedReader.readLine()).thenThrow(new IOException());
        log = LoggerUtils.readWholeFileContent("/tmp/log");
        Assert.assertNotNull(log);
    }

    @Test(expected = None.class)
    public void testLogError() {
        Optional<Logger> loggerOptional = Optional.of(this.logger);

        LoggerUtils.logError(loggerOptional, "error message");
        LoggerUtils.logError(loggerOptional, new RuntimeException("error message"));
        LoggerUtils.logError(loggerOptional, "error message", new RuntimeException("runtime exception"));
        LoggerUtils.logInfo(loggerOptional, "info message");
    }
}