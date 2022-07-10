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

package org.apache.dolphinscheduler.plugin.task.emr;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_KILL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;

import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsResult;
import com.amazonaws.services.elasticmapreduce.model.AmazonElasticMapReduceException;
import com.amazonaws.services.elasticmapreduce.model.DescribeStepResult;
import com.amazonaws.services.elasticmapreduce.model.Step;
import com.amazonaws.services.elasticmapreduce.model.StepState;
import com.amazonaws.services.elasticmapreduce.model.StepStatus;

/**
 * EmrAddStepsTask Test
 *
 * @since v3.1.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({
    AmazonElasticMapReduceClientBuilder.class,
    EmrAddStepsTask.class,
    AmazonElasticMapReduce.class,
    JSONUtils.class
})
@PowerMockIgnore({"javax.*"})
public class EmrAddStepsTaskTest {

    private final StepStatus pendingState =
        new StepStatus().withState(StepState.PENDING);

    private final StepStatus runningState =
        new StepStatus().withState(StepState.RUNNING);

    private final StepStatus completedState =
        new StepStatus().withState(StepState.COMPLETED);

    private final StepStatus cancelledState =
        new StepStatus().withState(StepState.CANCELLED);

    private final StepStatus failedState =
        new StepStatus().withState(StepState.FAILED);

    private EmrAddStepsTask emrAddStepsTask;
    private AmazonElasticMapReduce emrClient;
    private Step step;

    @Before
    public void before() throws Exception {
        // mock EmrParameters and EmrAddStepsTask
        EmrParameters emrParameters = buildEmrTaskParameters();
        String emrParametersString = JSONUtils.toJsonString(emrParameters);
        TaskExecutionContext taskExecutionContext = PowerMockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(emrParametersString);
        emrAddStepsTask = spy(new EmrAddStepsTask(taskExecutionContext));

        // mock emrClient and behavior
        emrClient = mock(AmazonElasticMapReduce.class);

        AddJobFlowStepsResult addJobFlowStepsResult = mock(AddJobFlowStepsResult.class);
        when(emrClient.addJobFlowSteps(any())).thenReturn(addJobFlowStepsResult);
        when(addJobFlowStepsResult.getStepIds()).thenReturn(Collections.singletonList("step-xx"));

        doReturn(emrClient).when(emrAddStepsTask, "createEmrClient");
        DescribeStepResult describeStepResult = mock(DescribeStepResult.class);
        when(emrClient.describeStep(any())).thenReturn(describeStepResult);

        // mock step
        step = mock(Step.class);
        when(describeStepResult.getStep()).thenReturn(step);

        emrAddStepsTask.init();
    }

    @Test
    public void testCanNotParseJson() throws Exception {
        mockStatic(JSONUtils.class);
        when(emrAddStepsTask, "createAddJobFlowStepsRequest").thenThrow(new EmrTaskException("can not parse AddJobFlowStepsRequest from json", new Exception("error")));
        emrAddStepsTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrAddStepsTask.getExitStatusCode());
    }

    @Test
    public void testDefineJsonStepNotOne() throws Exception {
        // mock EmrParameters and EmrAddStepsTask
        EmrParameters emrParameters = buildErrorEmrTaskParameters();
        String emrParametersString = JSONUtils.toJsonString(emrParameters);
        TaskExecutionContext taskExecutionContext = PowerMockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(emrParametersString);
        emrAddStepsTask = spy(new EmrAddStepsTask(taskExecutionContext));
        doReturn(emrClient).when(emrAddStepsTask, "createEmrClient");
        emrAddStepsTask.init();
        emrAddStepsTask.handle();

        Assert.assertEquals(EXIT_CODE_FAILURE, emrAddStepsTask.getExitStatusCode());
    }

    @Test
    public void testHandle() throws Exception {
        when(step.getStatus()).thenReturn(pendingState, runningState, completedState);

        emrAddStepsTask.handle();
        Assert.assertEquals(EXIT_CODE_SUCCESS, emrAddStepsTask.getExitStatusCode());
    }

    @Test
    public void testHandleUserRequestTerminate() throws Exception {
        when(step.getStatus()).thenReturn(pendingState, runningState, cancelledState);

        emrAddStepsTask.handle();
        Assert.assertEquals(EXIT_CODE_KILL, emrAddStepsTask.getExitStatusCode());
    }

    @Test
    public void testHandleError() throws Exception {
        when(step.getStatus()).thenReturn(pendingState, runningState, failedState);
        emrAddStepsTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrAddStepsTask.getExitStatusCode());

        when(emrClient.addJobFlowSteps(any())).thenThrow(new AmazonElasticMapReduceException("error"), new EmrTaskException());
        emrAddStepsTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrAddStepsTask.getExitStatusCode());
    }

    private EmrParameters buildEmrTaskParameters() {
        EmrParameters emrParameters = new EmrParameters();
        String stepsDefineJson;
        try (InputStream i = this.getClass().getResourceAsStream("EmrAddStepsDefine.json")) {
            assert i != null;
            stepsDefineJson = IOUtils.toString(i, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        emrParameters.setProgramType(ProgramType.ADD_JOB_FLOW_STEPS);
        emrParameters.setStepsDefineJson(stepsDefineJson);

        return emrParameters;
    }

    private EmrParameters buildErrorEmrTaskParameters() {
        EmrParameters emrParameters = new EmrParameters();
        String stepsDefineJson;
        try (InputStream i = this.getClass().getResourceAsStream("EmrErrorAddStepsDefine.json")) {
            assert i != null;
            stepsDefineJson = IOUtils.toString(i, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        emrParameters.setProgramType(ProgramType.ADD_JOB_FLOW_STEPS);
        emrParameters.setStepsDefineJson(stepsDefineJson);

        return emrParameters;
    }
}