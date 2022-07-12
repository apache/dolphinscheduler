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

package org.apache.dolphinscheduler.plugin.task.sagemaker;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.sagemaker.model.StartPipelineExecutionRequest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JSONUtils.class, PropertyUtils.class,})
@PowerMockIgnore({"javax.*"})
@SuppressStaticInitializationFor("org.apache.dolphinscheduler.spi.utils.PropertyUtils")
public class SagemakerTaskTest {

    private SagemakerTask sagemakerTask;

    @Before
    public void before() {
        PowerMockito.mockStatic(PropertyUtils.class);
        String parameters = buildParameters();
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        sagemakerTask = new SagemakerTask(taskExecutionContext);
        sagemakerTask.init();
    }

    @Test
    public void testStartPipelineRequest() throws Exception {
        StartPipelineExecutionRequest request = sagemakerTask.createStartPipelineRequest();
        Assert.assertEquals("AbalonePipeline", request.getPipelineName());
        Assert.assertEquals("test Pipeline", request.getPipelineExecutionDescription());
        Assert.assertEquals("AbalonePipeline", request.getPipelineExecutionDisplayName());
        Assert.assertEquals("AbalonePipeline", request.getPipelineName());
        Assert.assertEquals(new Integer(1), request.getParallelismConfiguration().getMaxParallelExecutionSteps());
    }

    private String buildParameters() {
        SagemakerParameters parameters = new SagemakerParameters();
        String sagemakerRequestJson;
        try (InputStream i = this.getClass().getResourceAsStream("SagemakerRequestJson.json")) {
            assert i != null;
            sagemakerRequestJson = IOUtils.toString(i, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        parameters.setSagemakerRequestJson(sagemakerRequestJson);

        return JSONUtils.toJsonString(parameters);
    }
}
