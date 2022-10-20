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

import static org.mockito.ArgumentMatchers.any;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.sagemaker.AmazonSageMaker;
import com.amazonaws.services.sagemaker.model.DescribePipelineExecutionResult;
import com.amazonaws.services.sagemaker.model.StartPipelineExecutionRequest;
import com.amazonaws.services.sagemaker.model.StartPipelineExecutionResult;
import com.amazonaws.services.sagemaker.model.StopPipelineExecutionResult;

@ExtendWith(MockitoExtension.class)
public class SagemakerTaskTest {

    private final String pipelineExecutionArn = "test-pipeline-arn";
    private final String clientRequestToken = "test-pipeline-token";
    private SagemakerTask sagemakerTask;
    private AmazonSageMaker client;
    private PipelineUtils pipelineUtils = new PipelineUtils();

    @BeforeEach
    public void before() {
        String parameters = buildParameters();
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);

        client = Mockito.mock(AmazonSageMaker.class);
        sagemakerTask = new SagemakerTask(taskExecutionContext);
        sagemakerTask.init();

        StartPipelineExecutionResult startPipelineExecutionResult = Mockito.mock(StartPipelineExecutionResult.class);
        Mockito.lenient().when(startPipelineExecutionResult.getPipelineExecutionArn()).thenReturn(pipelineExecutionArn);

        StopPipelineExecutionResult stopPipelineExecutionResult = Mockito.mock(StopPipelineExecutionResult.class);
        Mockito.lenient().when(stopPipelineExecutionResult.getPipelineExecutionArn()).thenReturn(pipelineExecutionArn);

        DescribePipelineExecutionResult describePipelineExecutionResult =
                Mockito.mock(DescribePipelineExecutionResult.class);
        Mockito.lenient().when(describePipelineExecutionResult.getPipelineExecutionStatus()).thenReturn("Executing",
                "Succeeded");

        Mockito.lenient().when(client.startPipelineExecution(any())).thenReturn(startPipelineExecutionResult);
        Mockito.lenient().when(client.stopPipelineExecution(any())).thenReturn(stopPipelineExecutionResult);
        Mockito.lenient().when(client.describePipelineExecution(any())).thenReturn(describePipelineExecutionResult);
    }

    @Test
    public void testStartPipelineRequest() throws Exception {
        StartPipelineExecutionRequest request = sagemakerTask.createStartPipelineRequest();
        Assertions.assertEquals("AbalonePipeline", request.getPipelineName());
        Assertions.assertEquals("test Pipeline", request.getPipelineExecutionDescription());
        Assertions.assertEquals("AbalonePipeline", request.getPipelineExecutionDisplayName());
        Assertions.assertEquals("AbalonePipeline", request.getPipelineName());
        Assertions.assertEquals(Integer.valueOf(1),
                request.getParallelismConfiguration().getMaxParallelExecutionSteps());
    }

    @Test
    public void testPipelineExecution() throws Exception {
        PipelineUtils.PipelineId pipelineId =
                pipelineUtils.startPipelineExecution(client, sagemakerTask.createStartPipelineRequest());
        Assertions.assertEquals(pipelineExecutionArn, pipelineId.getPipelineExecutionArn());
        Assertions.assertEquals(0, pipelineUtils.checkPipelineExecutionStatus(client, pipelineId));
        pipelineUtils.stopPipelineExecution(client, pipelineId);
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
