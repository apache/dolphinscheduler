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

package org.apache.dolphinscheduler.plugin.task.datafactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.azure.resourcemanager.datafactory.DataFactoryManager;
import com.azure.resourcemanager.datafactory.models.CreateRunResponse;
import com.azure.resourcemanager.datafactory.models.PipelineResource;
import com.azure.resourcemanager.datafactory.models.Pipelines;

@ExtendWith(MockitoExtension.class)
public class DatafactoryTaskTest {

    private static final String mockRunId =
            "3c7182f4-d107-43c7-af2a-8c7b3ed1d614";

    DatafactoryTask datafactoryTask;

    @Mock
    DataFactoryManager client;
    MockedStatic<DatafactoryHook> datafactoryHookMockedStatic;

    @BeforeEach
    public void before() throws IllegalAccessException {
        client = mock(DataFactoryManager.class);
        datafactoryHookMockedStatic = mockStatic(DatafactoryHook.class);
        when(DatafactoryHook.createClient()).thenReturn(client);

        DatafactoryParameters datafactoryParameters = new DatafactoryParameters();
        datafactoryTask = initTask(datafactoryParameters);
    }

    @Test
    public void testConvertParam() {
        DatafactoryParameters parameters = castParam();
        Assertions.assertEquals("whale1", parameters.getFactoryName());
        Assertions.assertEquals("factory1", parameters.getResourceGroupName());
        Assertions.assertEquals("CopyPipeline_ps8", parameters.getPipelineName());
        datafactoryHookMockedStatic.close();
    }

    @Test
    public void testCheckCreateTask() {
        DatafactoryHook hook = new DatafactoryHook();
        DataFactoryManager client1 = DatafactoryHook.createClient();
        hook.setClient(client1);

        PipelineResource pipelineResource = mock(PipelineResource.class);
        Pipelines pipelines = mock(Pipelines.class);
        when(client1.pipelines()).thenReturn(pipelines);
        when(pipelines.get(any(), any(), any())).thenReturn(pipelineResource);

        CreateRunResponse response = mock(CreateRunResponse.class);
        when(pipelineResource.createRun()).thenReturn(response);
        when(response.runId()).thenReturn(mockRunId);
        hook.startDatafactoryTask(castParam());
        Assertions.assertEquals(mockRunId, hook.getRunId());
        datafactoryHookMockedStatic.close();
    }

    @Test
    public void testCancelTask() {
        datafactoryTask.cancelApplication();
        Assertions.assertEquals(datafactoryTask.getExitStatusCode(), TaskConstants.EXIT_CODE_KILL);
        datafactoryHookMockedStatic.close();
    }

    private DatafactoryParameters castParam() {
        String input =
                "{\"localParams\":[],\"resourceList\":[],\"factoryName\":\"whale1\",\"resourceGroupName\":\"factory1\",\"pipelineName\":\"CopyPipeline_ps8\"}";
        return JSONUtils.parseObject(input, DatafactoryParameters.class);
    }

    private DatafactoryTask initTask(DatafactoryParameters DatasyncParameters) {
        TaskExecutionContext taskExecutionContext = createContext(DatasyncParameters);
        DatafactoryTask task = new DatafactoryTask(taskExecutionContext);
        task.init();
        task.getHook().setRunId(mockRunId);
        return task;
    }

    public TaskExecutionContext createContext(DatafactoryParameters datafactoryParameters) {
        String parameters = JSONUtils.toJsonString(datafactoryParameters);
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        return taskExecutionContext;
    }
}
