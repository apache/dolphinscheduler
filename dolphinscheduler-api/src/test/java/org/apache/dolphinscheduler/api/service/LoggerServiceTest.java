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
import org.apache.dolphinscheduler.api.log.LogClient;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.ProcessDao;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LoggerService.class})
public class LoggerServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(LoggerServiceTest.class);

    @InjectMocks
    private LoggerService loggerService;
    @Mock
    private ProcessDao processDao;
    @Mock
    private LogClient logClient;

    @Before
    public void setUp() {

        try {
            PowerMockito.whenNew(LogClient.class).withAnyArguments().thenReturn(logClient);
        } catch (Exception e) {
            logger.error("setUp error: {}",e.getMessage());
        }
    }

    @Test
    public void testQueryDataSourceList(){

        TaskInstance taskInstance = new TaskInstance();
        Mockito.when(processDao.findTaskInstanceById(1)).thenReturn(taskInstance);
        Result result = loggerService.queryLog(2,1,1);
        //TASK_INSTANCE_NOT_FOUND
        Assert.assertEquals(Status.TASK_INSTANCE_NOT_FOUND.getCode(),result.getCode().intValue());

        //HOST NOT FOUND
        result = loggerService.queryLog(1,1,1);
        Assert.assertEquals(Status.TASK_INSTANCE_NOT_FOUND.getCode(),result.getCode().intValue());

        //SUCCESS
        taskInstance.setHost("127.0.0.1");
        taskInstance.setLogPath("/temp/log");
        Mockito.when(logClient.rollViewLog("/temp/log",1,1 )).thenReturn("test");
        Mockito.when(processDao.findTaskInstanceById(1)).thenReturn(taskInstance);
        result = loggerService.queryLog(1,1,1);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
    }

    @Test
    public void testGetLogBytes(){

        TaskInstance taskInstance = new TaskInstance();
        Mockito.when(processDao.findTaskInstanceById(1)).thenReturn(taskInstance);

        //task instance is null
        try{
            loggerService.getLogBytes(2);
        }catch (Exception e){
            logger.error("testGetLogBytes error: {}","task instance is null");
        }

        //task instance host is null
        try{
            loggerService.getLogBytes(1);
        }catch (Exception e){
            logger.error("testGetLogBytes error: {}","task instance host is null");
        }

        //success
        Mockito.when(logClient.getLogBytes("/temp/log")).thenReturn(new byte[]{});
        taskInstance.setHost("127.0.0.1");
        taskInstance.setLogPath("/temp/log");
         byte []  result = loggerService.getLogBytes(1);
         Assert.assertEquals(0,result.length);
    }

}