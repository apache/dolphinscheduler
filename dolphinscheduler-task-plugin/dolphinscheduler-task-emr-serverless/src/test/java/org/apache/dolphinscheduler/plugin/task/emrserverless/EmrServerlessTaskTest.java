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

package org.apache.dolphinscheduler.plugin.task.emrserverless;

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.amazonaws.services.emrserverless.AWSEMRServerless;
import com.amazonaws.services.emrserverless.model.AWSEMRServerlessException;
import com.amazonaws.services.emrserverless.model.CancelJobRunResult;
import com.amazonaws.services.emrserverless.model.GetJobRunResult;
import com.amazonaws.services.emrserverless.model.JobRun;
import com.amazonaws.services.emrserverless.model.JobRunState;
import com.amazonaws.services.emrserverless.model.StartJobRunResult;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmrServerlessTaskTest {

    private static final String APPLICATION_ID = "00abcdefgh123456";
    private static final String JOB_RUN_ID = "00abcdefgh123456-jobrun-001";
    private static final String EXECUTION_ROLE_ARN = "arn:aws:iam::123456789012:role/EMRServerlessRole";

    private EmrServerlessTask emrServerlessTask;
    private AWSEMRServerless emrServerlessClient;

    private final TaskCallBack taskCallBack = new TaskCallBack() {

        @Override
        public void updateRemoteApplicationInfo(int taskInstanceId, ApplicationInfo applicationInfo) {
            // No-op: not needed for unit tests
        }

        @Override
        public void updateTaskInstanceInfo(int taskInstanceId) {
            // No-op: not needed for unit tests
        }
    };

    @BeforeEach
    void before() throws Exception {
        String taskParams = buildEmrServerlessTaskParameters();
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(taskParams);
        Mockito.lenient().when(taskExecutionContext.getTaskName()).thenReturn("test-emr-serverless-task");
        Mockito.lenient().when(taskExecutionContext.getTaskInstanceId()).thenReturn(1);

        emrServerlessTask = Mockito.spy(new EmrServerlessTask(taskExecutionContext));

        // mock emrServerlessClient
        emrServerlessClient = Mockito.mock(AWSEMRServerless.class);

        // mock startJobRun
        StartJobRunResult startJobRunResult = Mockito.mock(StartJobRunResult.class);
        Mockito.lenient().when(emrServerlessClient.startJobRun(any())).thenReturn(startJobRunResult);
        Mockito.lenient().when(startJobRunResult.getJobRunId()).thenReturn(JOB_RUN_ID);

        // inject mock client
        Mockito.doReturn(emrServerlessClient).when(emrServerlessTask).createEmrServerlessClient();

        emrServerlessTask.init();
    }

    @Test
    void testHandleSuccess() {
        // Job goes: SUBMITTED -> RUNNING -> SUCCESS
        mockJobRunStates(JobRunState.SUBMITTED.toString(), JobRunState.RUNNING.toString(),
                JobRunState.SUCCESS.toString());

        emrServerlessTask.handle(taskCallBack);
        Assertions.assertEquals(EXIT_CODE_SUCCESS, emrServerlessTask.getExitStatusCode());
    }

    @Test
    void testHandleFailed() {
        // Job goes: SUBMITTED -> RUNNING -> FAILED
        mockJobRunStates(JobRunState.SUBMITTED.toString(), JobRunState.RUNNING.toString(),
                JobRunState.FAILED.toString());

        emrServerlessTask.handle(taskCallBack);
        Assertions.assertEquals(EXIT_CODE_FAILURE, emrServerlessTask.getExitStatusCode());
    }

    @Test
    void testHandleCancelled() {
        // Job goes: SUBMITTED -> RUNNING -> CANCELLED
        mockJobRunStates(JobRunState.SUBMITTED.toString(), JobRunState.RUNNING.toString(),
                JobRunState.CANCELLED.toString());

        emrServerlessTask.handle(taskCallBack);
        Assertions.assertEquals(EXIT_CODE_KILL, emrServerlessTask.getExitStatusCode());
    }

    @Test
    void testHandleFullLifecycle() {
        // Job goes through all intermediate states: SUBMITTED -> PENDING -> SCHEDULED -> RUNNING -> SUCCESS
        mockJobRunStates(JobRunState.SUBMITTED.toString(), JobRunState.PENDING.toString(),
                JobRunState.SCHEDULED.toString(), JobRunState.RUNNING.toString(), JobRunState.SUCCESS.toString());

        emrServerlessTask.handle(taskCallBack);
        Assertions.assertEquals(EXIT_CODE_SUCCESS, emrServerlessTask.getExitStatusCode());
    }

    @Test
    void testSubmitError() {
        Mockito.when(emrServerlessClient.startJobRun(any()))
                .thenThrow(new AWSEMRServerlessException("Access denied"));

        Assertions.assertThrows(TaskException.class, () -> {
            emrServerlessTask.handle(taskCallBack);
        });
    }

    @Test
    void testGetJobRunReturnsNull() {
        // First call for submit (need a valid startJobRun response)
        StartJobRunResult startResult = Mockito.mock(StartJobRunResult.class);
        Mockito.when(emrServerlessClient.startJobRun(any())).thenReturn(startResult);
        Mockito.when(startResult.getJobRunId()).thenReturn(JOB_RUN_ID);

        // getJobRun returns null
        Mockito.when(emrServerlessClient.getJobRun(any())).thenReturn(null);

        emrServerlessTask.handle(taskCallBack);
        Assertions.assertEquals(EXIT_CODE_FAILURE, emrServerlessTask.getExitStatusCode());
    }

    @Test
    void testCancelApplication() {
        // Submit first so we have a jobRunId
        mockJobRunStates(JobRunState.SUBMITTED.toString(), JobRunState.RUNNING.toString(),
                JobRunState.SUCCESS.toString());
        emrServerlessTask.handle(taskCallBack);

        // Now test cancel
        CancelJobRunResult cancelResult = Mockito.mock(CancelJobRunResult.class);
        Mockito.when(emrServerlessClient.cancelJobRun(any())).thenReturn(cancelResult);
        emrServerlessTask.cancelApplication();

        Mockito.verify(emrServerlessClient).cancelJobRun(any());
    }

    @Test
    void testCancelWithEmptyJobRunId() {
        // Don't submit, so jobRunId is empty — cancel should be a no-op
        emrServerlessTask.cancelApplication();
        Mockito.verify(emrServerlessClient, Mockito.never()).cancelJobRun(any());
    }

    @Test
    void testFailoverRecovery() {
        // Simulate failover: submit the job first
        mockJobRunStates(JobRunState.SUBMITTED.toString(), JobRunState.RUNNING.toString(),
                JobRunState.SUCCESS.toString());
        emrServerlessTask.handle(taskCallBack);

        // Now create a new task instance simulating failover
        String taskParams = buildEmrServerlessTaskParameters();
        TaskExecutionContext failoverContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(failoverContext.getTaskParams()).thenReturn(taskParams);
        Mockito.lenient().when(failoverContext.getTaskName()).thenReturn("test-emr-serverless-failover");
        Mockito.lenient().when(failoverContext.getTaskInstanceId()).thenReturn(2);
        // Simulate that appIds was persisted from previous run
        Mockito.when(failoverContext.getAppIds()).thenReturn(JOB_RUN_ID);

        EmrServerlessTask failoverTask = Mockito.spy(new EmrServerlessTask(failoverContext));
        Mockito.doReturn(emrServerlessClient).when(failoverTask).createEmrServerlessClient();

        // Reset getJobRun mock for the recovery path — job already completed
        mockJobRunStates(JobRunState.SUCCESS.toString());

        failoverTask.init();
        failoverTask.handle(taskCallBack);

        // Should recover and succeed without calling startJobRun again
        Assertions.assertEquals(EXIT_CODE_SUCCESS, failoverTask.getExitStatusCode());
    }

    @Test
    void testInit() {
        String taskParams = buildEmrServerlessTaskParameters();
        TaskExecutionContext ctx = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(ctx.getTaskParams()).thenReturn(taskParams);
        Mockito.lenient().when(ctx.getTaskName()).thenReturn("test-init-task");
        Mockito.lenient().when(ctx.getTaskInstanceId()).thenReturn(100);

        EmrServerlessTask task = Mockito.spy(new EmrServerlessTask(ctx));
        AWSEMRServerless mockClient = Mockito.mock(AWSEMRServerless.class);
        Mockito.doReturn(mockClient).when(task).createEmrServerlessClient();

        task.init();

        // Verify initialization
        Assertions.assertNotNull(task.getParameters());
        Assertions.assertTrue(task.getParameters() instanceof EmrServerlessParameters);
        EmrServerlessParameters params = (EmrServerlessParameters) task.getParameters();
        Assertions.assertEquals(APPLICATION_ID, params.getApplicationId());
        Assertions.assertEquals(EXECUTION_ROLE_ARN, params.getExecutionRoleArn());
        Mockito.verify(task).createEmrServerlessClient();
    }

    @Test
    void testParametersCheck() {
        EmrServerlessParameters params = new EmrServerlessParameters();

        // All empty — should fail
        Assertions.assertFalse(params.checkParameters());

        // Only applicationId — should fail
        params.setApplicationId(APPLICATION_ID);
        Assertions.assertFalse(params.checkParameters());

        // applicationId + executionRoleArn — should fail (no JSON)
        params.setExecutionRoleArn(EXECUTION_ROLE_ARN);
        Assertions.assertFalse(params.checkParameters());

        // All three — should pass
        params.setStartJobRunRequestJson("{}");
        Assertions.assertTrue(params.checkParameters());
    }

    @Test
    void testInvalidJson() {
        // Build params with invalid JSON
        EmrServerlessParameters params = new EmrServerlessParameters();
        params.setApplicationId(APPLICATION_ID);
        params.setExecutionRoleArn(EXECUTION_ROLE_ARN);
        params.setStartJobRunRequestJson("{invalid json!!!}");

        TaskExecutionContext ctx = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(ctx.getTaskParams()).thenReturn(JSONUtils.toJsonString(params));
        Mockito.lenient().when(ctx.getTaskName()).thenReturn("test-bad-json");
        Mockito.lenient().when(ctx.getTaskInstanceId()).thenReturn(99);

        EmrServerlessTask badJsonTask = Mockito.spy(new EmrServerlessTask(ctx));
        Mockito.doReturn(emrServerlessClient).when(badJsonTask).createEmrServerlessClient();

        badJsonTask.init();
        Assertions.assertThrows(TaskException.class, () -> {
            badJsonTask.handle(taskCallBack);
        });
    }

    @Test
    void testHandle_PollingFailure() {
        // Submit succeeds
        StartJobRunResult startResult = Mockito.mock(StartJobRunResult.class);
        Mockito.when(emrServerlessClient.startJobRun(any())).thenReturn(startResult);
        Mockito.when(startResult.getJobRunId()).thenReturn(JOB_RUN_ID);

        // First poll returns RUNNING, second poll throws exception
        GetJobRunResult runningResult = Mockito.mock(GetJobRunResult.class);
        JobRun runningJobRun = Mockito.mock(JobRun.class);
        Mockito.when(runningResult.getJobRun()).thenReturn(runningJobRun);
        Mockito.when(runningJobRun.getState()).thenReturn(JobRunState.RUNNING.toString());

        Mockito.when(emrServerlessClient.getJobRun(any()))
                .thenReturn(runningResult)
                .thenThrow(new AWSEMRServerlessException("Network error"));

        emrServerlessTask.handle(taskCallBack);
        // When polling fails, task should set exit code to FAILURE
        Assertions.assertEquals(EXIT_CODE_FAILURE, emrServerlessTask.getExitStatusCode());
    }

    @Test
    void testMapStateToExitCode() throws Exception {
        // Test SUCCESS state
        mockJobRunStates(JobRunState.SUCCESS.toString());
        emrServerlessTask.handle(taskCallBack);
        Assertions.assertEquals(EXIT_CODE_SUCCESS, emrServerlessTask.getExitStatusCode());

        // Re-init for next test
        before();
        mockJobRunStates(JobRunState.FAILED.toString());
        emrServerlessTask.handle(taskCallBack);
        Assertions.assertEquals(EXIT_CODE_FAILURE, emrServerlessTask.getExitStatusCode());

        // Re-init for next test
        before();
        mockJobRunStates(JobRunState.CANCELLED.toString());
        emrServerlessTask.handle(taskCallBack);
        Assertions.assertEquals(EXIT_CODE_KILL, emrServerlessTask.getExitStatusCode());

        // Re-init for unknown state (should default to FAILURE)
        before();
        mockJobRunStates("UNKNOWN_STATE");
        emrServerlessTask.handle(taskCallBack);
        Assertions.assertEquals(EXIT_CODE_FAILURE, emrServerlessTask.getExitStatusCode());
    }

    @Test
    void testGetApplicationIds() {
        Assertions.assertTrue(emrServerlessTask.getApplicationIds().isEmpty());
    }

    // --- Helper methods ---

    private void mockJobRunStates(String... states) {
        if (states.length == 0) {
            return;
        }

        GetJobRunResult[] results = new GetJobRunResult[states.length];
        for (int i = 0; i < states.length; i++) {
            GetJobRunResult result = Mockito.mock(GetJobRunResult.class);
            JobRun jobRun = Mockito.mock(JobRun.class);
            Mockito.when(result.getJobRun()).thenReturn(jobRun);
            Mockito.when(jobRun.getState()).thenReturn(states[i]);
            results[i] = result;
        }

        if (results.length == 1) {
            Mockito.when(emrServerlessClient.getJobRun(any())).thenReturn(results[0]);
        } else {
            GetJobRunResult first = results[0];
            GetJobRunResult[] rest = new GetJobRunResult[results.length - 1];
            System.arraycopy(results, 1, rest, 0, rest.length);
            Mockito.when(emrServerlessClient.getJobRun(any())).thenReturn(first, rest);
        }
    }

    private String buildEmrServerlessTaskParameters() {
        EmrServerlessParameters params = new EmrServerlessParameters();
        params.setApplicationId(APPLICATION_ID);
        params.setExecutionRoleArn(EXECUTION_ROLE_ARN);

        String startJobRunRequestJson;
        try (InputStream is = this.getClass().getResourceAsStream("StartJobRunRequest.json")) {
            assert is != null;
            startJobRunRequestJson = IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        params.setStartJobRunRequestJson(startJobRunRequestJson);

        return JSONUtils.toJsonString(params);
    }
}
