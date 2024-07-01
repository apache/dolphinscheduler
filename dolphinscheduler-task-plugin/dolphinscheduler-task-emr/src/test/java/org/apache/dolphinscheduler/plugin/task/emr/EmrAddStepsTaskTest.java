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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.ApplicationInfo;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
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
@ExtendWith(MockitoExtension.class)
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
    private TaskCallBack taskCallBack = new TaskCallBack() {

        @Override
        public void updateRemoteApplicationInfo(int taskInstanceId, ApplicationInfo applicationInfo) {

        }

        @Override
        public void updateTaskInstanceInfo(int taskInstanceId) {

        }
    };

    @BeforeEach
    public void before() throws Exception {
        // mock EmrParameters and EmrAddStepsTask
        EmrParameters emrParameters = buildEmrTaskParameters();
        String emrParametersString = JSONUtils.toJsonString(emrParameters);
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(emrParametersString);
        emrAddStepsTask = Mockito.spy(new EmrAddStepsTask(taskExecutionContext));

        // mock emrClient and behavior
        emrClient = Mockito.mock(AmazonElasticMapReduce.class);

        AddJobFlowStepsResult addJobFlowStepsResult = Mockito.mock(AddJobFlowStepsResult.class);
        Mockito.lenient().when(emrClient.addJobFlowSteps(any())).thenReturn(addJobFlowStepsResult);
        Mockito.lenient().when(addJobFlowStepsResult.getStepIds()).thenReturn(Collections.singletonList("step-xx"));

        Mockito.doReturn(emrClient).when(emrAddStepsTask).createEmrClient();
        DescribeStepResult describeStepResult = Mockito.mock(DescribeStepResult.class);
        Mockito.lenient().when(emrClient.describeStep(any())).thenReturn(describeStepResult);

        // mock step
        step = Mockito.mock(Step.class);
        Mockito.lenient().when(describeStepResult.getStep()).thenReturn(step);

        emrAddStepsTask.init();
    }

    @Test
    public void testCanNotParseJson() throws Exception {
        Mockito.when(emrAddStepsTask.createAddJobFlowStepsRequest()).thenThrow(
                new EmrTaskException("can not parse AddJobFlowStepsRequest from json", new Exception("error")));
        Assertions.assertThrows(TaskException.class, () -> {
            emrAddStepsTask.handle(taskCallBack);
        });
    }

    @Test
    public void testDefineJsonStepNotOne() throws Exception {
        // mock EmrParameters and EmrAddStepsTask
        EmrParameters emrParameters = buildErrorEmrTaskParameters();
        String emrParametersString = JSONUtils.toJsonString(emrParameters);
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(emrParametersString);
        emrAddStepsTask = Mockito.spy(new EmrAddStepsTask(taskExecutionContext));
        Mockito.doReturn(emrClient).when(emrAddStepsTask).createEmrClient();
        Assertions.assertThrows(TaskException.class, () -> {
            emrAddStepsTask.init();
            emrAddStepsTask.handle(taskCallBack);
        });

    }

    @Test
    public void testHandle() throws Exception {
        Mockito.when(step.getStatus()).thenReturn(pendingState, runningState, completedState);

        emrAddStepsTask.handle(taskCallBack);
        Assertions.assertEquals(EXIT_CODE_SUCCESS, emrAddStepsTask.getExitStatusCode());
    }

    @Test
    public void testHandleUserRequestTerminate() throws Exception {
        Mockito.when(step.getStatus()).thenReturn(pendingState, runningState, cancelledState);

        emrAddStepsTask.handle(taskCallBack);
        Assertions.assertEquals(EXIT_CODE_KILL, emrAddStepsTask.getExitStatusCode());
    }

    @Test
    public void testHandleError() throws Exception {
        Mockito.when(step.getStatus()).thenReturn(pendingState, runningState, failedState);
        emrAddStepsTask.handle(taskCallBack);
        Assertions.assertEquals(EXIT_CODE_FAILURE, emrAddStepsTask.getExitStatusCode());

        Mockito.when(emrClient.addJobFlowSteps(any())).thenThrow(new AmazonElasticMapReduceException("error"),
                new EmrTaskException());
        Assertions.assertThrows(TaskException.class, () -> {
            emrAddStepsTask.handle(taskCallBack);
        });
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
