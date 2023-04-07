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
package org.apache.dolphinscheduler.api.resource;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * resource check test
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceCheckTest {

    private static final Logger logger = LoggerFactory.getLogger(ResourceCheckTest.class);

    @Mock
    private ProcessService processService;

    @Test
    public void testResourceCheckNull() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setResourceIds(null);
        taskDefinition.setName("taskDefinition");

        new ResourceCheck(ResourceType.FILE, processService, taskDefinition.getResourceIds(),
                taskDefinition.getName(), logger).checkAllExist();
    }

    @Test
    public void testResourceCheckNotExist() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setResourceIds("1");
        taskDefinition.setName("taskDefinition");
        String error = null;

        try {
            new ResourceCheck(ResourceType.FILE, processService, taskDefinition.getResourceIds(),
                    taskDefinition.getName(), logger).checkAllExist();
        } catch (ServiceException e) {
            error = e.getMessage();
        }

        Assertions.assertEquals(error,
                MessageFormat.format(Status.TASK_RESOURCE_NOT_EXIST.getMsg(), taskDefinition.getName()));
    }

    @Test
    public void testResourceCheckExist() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setResourceIds("1");
        taskDefinition.setName("taskDefinition");
        String error = null;

        Integer[] resourceArr = {1};
        List<Resource> list = new ArrayList<>();
        list.add(new Resource());

        Mockito.when(processService.listResourceByIds(resourceArr)).thenReturn(list);

        try {
            new ResourceCheck(ResourceType.FILE, processService, taskDefinition.getResourceIds(),
                    taskDefinition.getName(), logger).checkAllExist();
        } catch (ServiceException e) {
            error = e.getMessage();
        }

        Assertions.assertNull(error);
    }

    @Test
    public void testResourceCheckUDF() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setResourceIds(null);
        taskDefinition.setName("taskDefinition");
        String error = null;

        try {
            new ResourceCheck(ResourceType.UDF, processService, taskDefinition.getResourceIds(),
                    taskDefinition.getName(), logger).checkAllExist();
        } catch (ServiceException e) {
            error = e.getMessage();
        }

        Assertions.assertEquals(error,
                MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), ResourceType.UDF));
    }
}
