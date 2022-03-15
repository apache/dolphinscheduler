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
import com.amazonaws.services.elasticmapreduce.model.AmazonElasticMapReduceException;
import com.amazonaws.services.elasticmapreduce.model.Cluster;
import com.amazonaws.services.elasticmapreduce.model.ClusterState;
import com.amazonaws.services.elasticmapreduce.model.ClusterStateChangeReason;
import com.amazonaws.services.elasticmapreduce.model.ClusterStateChangeReasonCode;
import com.amazonaws.services.elasticmapreduce.model.ClusterStatus;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterResult;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    AmazonElasticMapReduceClientBuilder.class,
    EmrTask.class,
    AmazonElasticMapReduce.class,
    JSONUtils.class
})
@PowerMockIgnore({"javax.*"})
public class EmrTaskTest {

    private final ClusterStatus startingStatus =
        new ClusterStatus().withState(ClusterState.STARTING)
            .withStateChangeReason(new ClusterStateChangeReason());

    private final ClusterStatus softwareConfigStatus =
        new ClusterStatus().withState(ClusterState.STARTING)
            .withStateChangeReason(
                new ClusterStateChangeReason()
                    .withMessage("Configuring cluster software")
            );

    private final ClusterStatus runningStatus =
        new ClusterStatus().withState(ClusterState.RUNNING)
            .withStateChangeReason(
                new ClusterStateChangeReason().withMessage("Running step")
            );

    private final ClusterStatus terminatingStatus =
        new ClusterStatus().withState(ClusterState.TERMINATING.toString())
            .withStateChangeReason(
                new ClusterStateChangeReason()
                    .withCode(ClusterStateChangeReasonCode.ALL_STEPS_COMPLETED)
                    .withMessage("Steps completed")
            );

    private final ClusterStatus waitingStatus =
        new ClusterStatus().withState(ClusterState.WAITING)
            .withStateChangeReason(
                new ClusterStateChangeReason()
                    .withMessage("Cluster ready after last step completed.")
            );

    private final ClusterStatus userRequestTerminateStatus =
        new ClusterStatus().withState(ClusterState.TERMINATING)
            .withStateChangeReason(
                new ClusterStateChangeReason()
                    .withCode(ClusterStateChangeReasonCode.USER_REQUEST)
                    .withMessage("Terminated by user request")
            );


    private final ClusterStatus terminatedWithErrorsStatus =
        new ClusterStatus().withState(ClusterState.TERMINATED_WITH_ERRORS)
            .withStateChangeReason(
                new ClusterStateChangeReason()
                    .withCode(ClusterStateChangeReasonCode.STEP_FAILURE)
            );

    private EmrTask emrTask;
    private AmazonElasticMapReduce emrClient;
    private Cluster cluster;

    @Before
    public void before() throws Exception {
        String emrParameters = buildEmrTaskParameters();
        TaskExecutionContext taskExecutionContext = PowerMockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(emrParameters);
        emrTask = spy(new EmrTask(taskExecutionContext));

        // mock emrClient and behavior
        emrClient = mock(AmazonElasticMapReduce.class);
        RunJobFlowResult runJobFlowResult = mock(RunJobFlowResult.class);
        when(emrClient.runJobFlow(any())).thenReturn(runJobFlowResult);
        when(runJobFlowResult.getJobFlowId()).thenReturn("xx");
        doReturn(emrClient).when(emrTask, "createEmrClient");
        DescribeClusterResult describeClusterResult = mock(DescribeClusterResult.class);
        when(emrClient.describeCluster(any())).thenReturn(describeClusterResult);

        // mock cluster
        cluster = mock(Cluster.class);
        when(describeClusterResult.getCluster()).thenReturn(cluster);

        emrTask.init();
    }

    @Test
    public void testHandle() throws Exception {

        when(cluster.getStatus()).thenReturn(startingStatus, softwareConfigStatus, runningStatus, terminatingStatus);

        emrTask.handle();
        Assert.assertEquals(EXIT_CODE_SUCCESS, emrTask.getExitStatusCode());

    }

    @Test
    public void testHandleAliveWhenNoSteps() throws Exception {
        when(cluster.getStatus()).thenReturn(startingStatus, softwareConfigStatus, runningStatus, waitingStatus);

        emrTask.handle();
        Assert.assertEquals(EXIT_CODE_SUCCESS, emrTask.getExitStatusCode());
    }

    @Test
    public void testHandleUserRequestTerminate() throws Exception {
        when(cluster.getStatus()).thenReturn(startingStatus, userRequestTerminateStatus);

        emrTask.handle();
        Assert.assertEquals(EXIT_CODE_KILL, emrTask.getExitStatusCode());
    }

    @Test
    public void testHandleTerminatedWithError() throws Exception {
        when(cluster.getStatus()).thenReturn(startingStatus, softwareConfigStatus, runningStatus, terminatedWithErrorsStatus);

        emrTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrTask.getExitStatusCode());
    }

    @Test
    public void testCanNotParseJson() throws Exception {
        mockStatic(JSONUtils.class);
        when(emrTask, "createRunJobFlowRequest").thenThrow(new EmrTaskException("can not parse RunJobFlowRequest from json", new Exception("error")));
        emrTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrTask.getExitStatusCode());
    }

    @Test
    public void testClusterStatusNull() throws Exception {

        when(emrClient.describeCluster(any())).thenReturn(null);

        emrTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrTask.getExitStatusCode());
    }

    @Test
    public void testRunJobFlowError() throws Exception {

        when(emrClient.runJobFlow(any())).thenThrow(new AmazonElasticMapReduceException("error"), new EmrTaskException());
        emrTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrTask.getExitStatusCode());
        emrTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrTask.getExitStatusCode());

    }

    private String buildEmrTaskParameters() {
        EmrParameters emrParameters = new EmrParameters();
        String jobFlowDefineJson;
        try (InputStream i = this.getClass().getResourceAsStream("EmrJobFlowDefine.json")) {
            assert i != null;
            jobFlowDefineJson = IOUtils.toString(i, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        emrParameters.setJobFlowDefineJson(jobFlowDefineJson);

        return JSONUtils.toJsonString(emrParameters);
    }
}