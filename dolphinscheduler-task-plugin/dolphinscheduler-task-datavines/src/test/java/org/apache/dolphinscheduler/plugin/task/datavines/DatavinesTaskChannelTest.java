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

package org.apache.dolphinscheduler.plugin.task.datavines;

import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatavinesTaskChannelTest {

    @Mock
    private TaskExecutionContext taskExecutionContext;

    private DatavinesTaskChannel channel;

    @BeforeEach
    void setUp() {
        channel = new DatavinesTaskChannel();
    }

    @Test
    void createTaskReturnsDatavinesTask() {
        AbstractTask task = channel.createTask(taskExecutionContext);
        Assertions.assertNotNull(task);
        Assertions.assertInstanceOf(DatavinesTask.class, task);
    }

    @Test
    void parseParametersReturnsDatavinesParametersWithCorrectFields() {
        String taskParams =
                "{\"address\":\"http://datavines:9090\",\"jobId\":\"42\",\"token\":\"my-token\",\"failureBlock\":true}";
        AbstractParameters params = channel.parseParameters(taskParams);
        Assertions.assertNotNull(params);
        Assertions.assertInstanceOf(DatavinesParameters.class, params);
        DatavinesParameters datavinesParams = (DatavinesParameters) params;
        Assertions.assertEquals("http://datavines:9090", datavinesParams.getAddress());
        Assertions.assertEquals("42", datavinesParams.getJobId());
        Assertions.assertEquals("my-token", datavinesParams.getToken());
        Assertions.assertTrue(datavinesParams.isFailureBlock());
    }

    @Test
    void parseParametersWithEmptyJsonReturnsParametersObject() {
        AbstractParameters params = channel.parseParameters("{}");
        Assertions.assertNotNull(params);
        Assertions.assertInstanceOf(DatavinesParameters.class, params);
    }
}
