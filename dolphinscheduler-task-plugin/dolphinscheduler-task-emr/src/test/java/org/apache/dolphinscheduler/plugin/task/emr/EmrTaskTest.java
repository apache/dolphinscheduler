package org.apache.dolphinscheduler.plugin.task.emr;

import static org.apache.dolphinscheduler.spi.task.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.EXIT_CODE_SUCCESS;

import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import org.apache.dolphinscheduler.spi.task.request.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

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
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.AmazonElasticMapReduceException;
import com.amazonaws.services.elasticmapreduce.model.Cluster;
import com.amazonaws.services.elasticmapreduce.model.ClusterStateChangeReason;
import com.amazonaws.services.elasticmapreduce.model.ClusterStatus;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterResult;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PropertyUtils.class,
    AmazonElasticMapReduceClientBuilder.class,
    EmrTask.class,
    AmazonElasticMapReduce.class,
    JSONUtils.class})
@PowerMockIgnore({"javax.*"})
@SuppressStaticInitializationFor("org.apache.dolphinscheduler.spi.utils.PropertyUtils")
public class EmrTaskTest {

    private final ClusterStatus startingState = new ClusterStatus().withState("STARTING").withStateChangeReason(new ClusterStateChangeReason());
    private final ClusterStatus startingStateSoftConfig = new ClusterStatus().withState("STARTING").withStateChangeReason(new ClusterStateChangeReason().withMessage("Configuring cluster software"));
    private final ClusterStatus runningState = new ClusterStatus().withState("RUNNING").withStateChangeReason(new ClusterStateChangeReason().withMessage("Running step"));
    private final ClusterStatus waitingState =
        new ClusterStatus().withState("TERMINATING").withStateChangeReason(new ClusterStateChangeReason().withCode("ALL_STEPS_COMPLETED").withMessage("Steps completed"));
    private EmrTask emrTask;
    private AmazonElasticMapReduce emrClient;

    @Before
    public void before() throws Exception {
        String emrParameters = buildEmrTaskParameters();
        TaskRequest taskExecutionContext = PowerMockito.mock(TaskRequest.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(emrParameters);
        emrTask = spy(new EmrTask(taskExecutionContext));

        // mock emrClient and behavior
        emrClient = mock(AmazonElasticMapReduce.class);
        RunJobFlowResult runJobFlowResult = mock(RunJobFlowResult.class);
        when(emrClient.runJobFlow(any())).thenReturn(runJobFlowResult);
        when(runJobFlowResult.getJobFlowId()).thenReturn("xx");
        doReturn(emrClient).when(emrTask, "getEmrClient");

        emrTask.init();
    }

    @Test
    public void testHandle()  {
        //mock cluster status

        DescribeClusterResult describeClusterResult = mock(DescribeClusterResult.class);
        when(emrClient.describeCluster(any())).thenReturn(describeClusterResult);
        Cluster cluster = mock(Cluster.class);
        when(describeClusterResult.getCluster()).thenReturn(cluster);
        when(cluster.getStatus()).thenReturn(startingState, startingStateSoftConfig, runningState, waitingState);

        emrTask.handle();
        Assert.assertEquals(EXIT_CODE_SUCCESS, emrTask.getExitStatusCode());

    }

    @Test
    public void testRunJobFlowRequestNull()  {
        mockStatic(JSONUtils.class);
        when(JSONUtils.parseObject(any())).thenReturn(null);
        emrTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrTask.getExitStatusCode());
    }

    @Test
    public void testClusterStatusNull()  {

        when(emrClient.describeCluster(any())).thenReturn(null);

        emrTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrTask.getExitStatusCode());
    }

    @Test
    public void testRunJobFlowError()  {

        when(emrClient.runJobFlow(any())).thenThrow(new AmazonElasticMapReduceException("error"));
        emrTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrTask.getExitStatusCode());
    }

    private String buildEmrTaskParameters() {
        EmrParameters emrParameters = new EmrParameters();
        emrParameters.setProfileName("default");
        emrParameters.setRegion("cn-north-1");
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