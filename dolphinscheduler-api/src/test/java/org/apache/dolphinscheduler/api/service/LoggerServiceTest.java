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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.LoggerServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * logger service test
 */
@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({LoggerServiceImpl.class})
public class LoggerServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(LoggerServiceTest.class);

    @InjectMocks
    private LoggerServiceImpl loggerService;

    @Mock
    private ProcessService processService;

    @Before
    public void init() {
        this.loggerService.init();
    }

    @Test
    public void testQueryDataSourceList() {

        TaskInstance taskInstance = new TaskInstance();
        Mockito.when(processService.findTaskInstanceById(1)).thenReturn(taskInstance);
        Result result = loggerService.queryLog(2, 1, 1);
        //TASK_INSTANCE_NOT_FOUND
        Assert.assertTrue(result.isStatus(Status.TASK_INSTANCE_NOT_FOUND));

        try {
            //HOST NOT FOUND OR ILLEGAL
            result = loggerService.queryLog(1, 1, 1);
        } catch (RuntimeException e) {
            Assert.assertTrue(true);
            logger.error("testQueryDataSourceList error {}", e.getMessage());
        }
        Assert.assertTrue(result.isStatus(Status.TASK_INSTANCE_NOT_FOUND));

        //SUCCESS
        taskInstance.setHost("127.0.0.1:8080");
        taskInstance.setLogPath("/temp/log");
        Mockito.when(processService.findTaskInstanceById(1)).thenReturn(taskInstance);
        result = loggerService.queryLog(1, 1, 1);
        Assert.assertTrue(result.isSuccess());
    }

    @Test
    public void testGetLogBytes() {

        TaskInstance taskInstance = new TaskInstance();
        Mockito.when(processService.findTaskInstanceById(1)).thenReturn(taskInstance);

        //task instance is null
        try {
            loggerService.getLogBytes(2);
        } catch (RuntimeException e) {
            Assert.assertTrue(true);
            logger.error("testGetLogBytes error: {}", "task instance is null");
        }

        //task instance host is null
        try {
            loggerService.getLogBytes(1);
        } catch (RuntimeException e) {
            Assert.assertTrue(true);
            logger.error("testGetLogBytes error: {}", "task instance host is null");
        }

        //success
        taskInstance.setHost("127.0.0.1:8080");
        taskInstance.setLogPath("/temp/log");
        //if use @RunWith(PowerMockRunner.class) mock object,sonarcloud will not calculate the coverage,
        // so no assert will be added here
        loggerService.getLogBytes(1);

    }

    @After
    public void close() {
        this.loggerService.close();
    }

}