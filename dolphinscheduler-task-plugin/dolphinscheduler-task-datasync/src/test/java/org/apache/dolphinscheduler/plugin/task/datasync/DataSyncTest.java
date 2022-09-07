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

package org.apache.dolphinscheduler.plugin.task.datasync;

import org.apache.commons.io.IOUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import software.amazon.awssdk.services.datasync.DataSyncClient;
import software.amazon.awssdk.services.datasync.model.CreateTaskRequest;
import software.amazon.awssdk.services.datasync.model.CreateTaskResponse;
import software.amazon.awssdk.services.datasync.model.DescribeTaskExecutionRequest;
import software.amazon.awssdk.services.datasync.model.DescribeTaskExecutionResponse;
import software.amazon.awssdk.services.datasync.model.FilterRule;
import software.amazon.awssdk.services.datasync.model.Options;
import software.amazon.awssdk.services.datasync.model.TagListEntry;
import software.amazon.awssdk.services.datasync.model.TaskSchedule;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_KILL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class DataSyncTest {

    private final String startingStatus = DatasyncHook.STATUS.READY;
    private final String runningStatus = DatasyncHook.STATUS.READY;
    private final String stopStatus = DatasyncHook.STATUS.READY;
    private final String canceledStatus = DatasyncHook.STATUS.READY;
    private final String successfulStatus = DatasyncHook.STATUS.READY;
    private final String finishedStatus = DatasyncHook.STATUS.READY;

    private DatasyncTask datasyncTask;
    private DataSyncClient client;
    private DatasyncHook hook;

    @Test
    public void testToString()   {

        String expected = "DatasyncParameters" +
                "(cloudWatchLogGroupArn=string, " +
                "destinationLocationArn=string, " +
                "sourceLocationArn=string, " +
                "name=string, " +
                "options=Options(VerifyMode=string, OverwriteMode=string, Atime=string, Mtime=string, Uid=string, Gid=string, PreserveDeletedFiles=string, PreserveDevices=string, PosixPermissions=string, BytesPerSecond=1, TaskQueueing=string, LogLevel=string, TransferMode=string, SecurityDescriptorCopyFlags=string, ObjectTags=string), " +
                "schedule=TaskSchedule(ScheduleExpression=string), " +
                "excludes=[FilterRule(FilterType=string, Value=string)], " +
                "includes=[FilterRule(FilterType=string, Value=string)], " +
                "tags=[TagListEntry(Key=string, Value=string)])";
       /* String expected = "DatasyncParameters"
                + "{"
                + "CloudWatchLogGroupArn='string', "
                + "DestinationLocationArn='string', "
                + "Name='string', "
                + "SourceLocationArn='string', "
                + "Excludes=[{\"FilterType\":\"string\",\"Value\":\"string\"}], "
                + "Includes=[{\"FilterType\":\"string\",\"Value\":\"string\"}], "
                + "Tags=[{\"Key\":\"string\",\"Value\":\"string\"}], "
                + "Schedule={\"ScheduleExpression\":\"string\"}, "
                + "Options={\"Atime\":\"string\",\"BytesPerSecond\":1,\"Gid\":\"string\",\"LogLevel\":\"string\",\"Mtime\":\"string\",\"ObjectTags\":\"string\",\"OverwriteMode\":\"string\",\"PosixPermissions\":\"string\",\"PreserveDeletedFiles\":\"string\",\"PreserveDevices\":\"string\",\"SecurityDescriptorCopyFlags\":\"string\",\"TaskQueueing\":\"string\",\"TransferMode\":\"string\",\"Uid\":\"string\",\"VerifyMode\":\"string\"} "
                + "}";*/
        Assert.assertEquals(expected,getDefaultParam());
    }



    private DatasyncParameters getDefaultParam(){
        DatasyncParameters datasyncParameters = new DatasyncParameters();
        datasyncParameters.setCloudWatchLogGroupArn("string");
        datasyncParameters.setDestinationLocationArn("string");
        datasyncParameters.setName("string");
        datasyncParameters.setSourceLocationArn("string");
        List<FilterRule> excludeList =new ArrayList<>();
        List<FilterRule> includeList =new ArrayList<>();
        List<TagListEntry> tags =new ArrayList<>();

        TagListEntry tagListEntry = TagListEntry.builder().key("string").value("string").build();
        FilterRule rule = FilterRule.builder().filterType("string").value("string").build();

        excludeList.add(rule);
        includeList.add(rule);
        tags.add(tagListEntry);
        datasyncParameters.setExcludes(excludeList);
        datasyncParameters.setIncludes(includeList);

        Options options = Options.builder().atime("string").bytesPerSecond(1l).gid("string").logLevel("string")
                .mtime("string").objectTags("string").overwriteMode("string").posixPermissions("string")
                .preserveDeletedFiles("string").preserveDevices("string").securityDescriptorCopyFlags("string")
                .taskQueueing("string").transferMode("string").uid("string").verifyMode("string").build();
        datasyncParameters.setOptions(options);
        datasyncParameters.setSchedule(TaskSchedule.builder().scheduleExpression("string").build());
        datasyncParameters.setTags(tags);
        return datasyncParameters;
    }

    @Before
    public void before() throws Exception {
        // mock DatasyncParameters and task
        DatasyncParameters DatasyncParameters = getDefaultParam();
        String DatasyncParametersString = JSONUtils.toJsonString(DatasyncParameters);
        TaskExecutionContext taskExecutionContext = PowerMockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(DatasyncParametersString);
        datasyncTask = spy(new DatasyncTask(taskExecutionContext));

        // mock client and behavior
        client = mock(DataSyncClient.class);
        hook = mock(DatasyncHook.class);
        CreateTaskResponse createTaskResponse = mock(CreateTaskResponse.class);
        when(client.createTask((CreateTaskRequest) any())).thenReturn(createTaskResponse);
        when(createTaskResponse.taskArn()).thenReturn("xx");
        doReturn(client).when(hook, "createClient");
        DescribeTaskExecutionResponse describeTaskExecutionResponse = mock(DescribeTaskExecutionResponse.class);
        when(client.describeTaskExecution((DescribeTaskExecutionRequest) any())).thenReturn(describeTaskExecutionResponse);

        datasyncTask.init();
    }

    /* @Test
    public void testHandle() throws Exception {
        when(hook.awaitReplicationTaskStatus(finishedStatus)).thenReturn(true);

        datasyncTask.handle();
        Assert.assertEquals(EXIT_CODE_SUCCESS, datasyncTask.getExitStatusCode());

    }

   @Test
    public void testHandleUserRequestTerminate() throws Exception {
        when(.getStatus()).thenReturn(canceledStatus);

        datasyncTask.handle();
        Assert.assertEquals(EXIT_CODE_KILL, datasyncTask.getExitStatusCode());
    }

    @Test
    public void testHandleTerminatedWithError() throws Exception {
        when(cluster.getStatus()).thenReturn(startingStatus, softwareConfigStatus, runningStatus, terminatedWithErrorsStatus);

        emrJobFlowTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrJobFlowTask.getExitStatusCode());
    }

    @Test
    public void testCanNotParseJson() throws Exception {
        mockStatic(JSONUtils.class);
        when(emrJobFlowTask, "createRunJobFlowRequest").thenThrow(new EmrTaskException("can not parse RunJobFlowRequest from json", new Exception("error")));
        emrJobFlowTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrJobFlowTask.getExitStatusCode());
    }

    @Test
    public void testClusterStatusNull() throws Exception {

        when(emrClient.describeCluster(any())).thenReturn(null);

        emrJobFlowTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrJobFlowTask.getExitStatusCode());
    }

    @Test
    public void testRunJobFlowError() throws Exception {

        when(emrClient.runJobFlow(any())).thenThrow(new AmazonElasticMapReduceException("error"), new EmrTaskException());
        emrJobFlowTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrJobFlowTask.getExitStatusCode());
        emrJobFlowTask.handle();
        Assert.assertEquals(EXIT_CODE_FAILURE, emrJobFlowTask.getExitStatusCode());

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
        emrParameters.setProgramType(ProgramType.RUN_JOB_FLOW);
        emrParameters.setJobFlowDefineJson(jobFlowDefineJson);

        return JSONUtils.toJsonString(emrParameters);
    }
*/
}
