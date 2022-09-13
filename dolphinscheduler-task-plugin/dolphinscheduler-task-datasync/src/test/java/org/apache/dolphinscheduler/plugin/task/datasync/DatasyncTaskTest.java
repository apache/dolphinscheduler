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

import static org.mockito.Mockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.any;

import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.datasync.DataSyncClient;
import software.amazon.awssdk.services.datasync.model.CancelTaskExecutionRequest;
import software.amazon.awssdk.services.datasync.model.CancelTaskExecutionResponse;
import software.amazon.awssdk.services.datasync.model.StartTaskExecutionRequest;
import software.amazon.awssdk.services.datasync.model.StartTaskExecutionResponse;
import software.amazon.awssdk.services.datasync.model.TaskExecutionStatus;
import software.amazon.awssdk.services.datasync.model.TaskStatus;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        JSONUtils.class,
        PropertyUtils.class,
        DatasyncHook.class
})
@PowerMockIgnore({"javax.*"})
public class DatasyncTaskTest {

    private static final String mockExeArn = "arn:aws:datasync:ap-northeast-3:523202806641:task/task-017642db08fdf6a55/execution/exec-0ac3607778dfc31f5";

    @Mock
    DatasyncHook datasyncHook;

    DatasyncTask datasyncTask;

    @Mock
    DataSyncClient client;

    @Before
    public void before() throws Exception {
        client = mock(DataSyncClient.class);
        mockStatic(DatasyncHook.class);
        when(DatasyncHook.createClient()).thenReturn(client);

        DatasyncParameters DatasyncParameters = new DatasyncParameters();
        datasyncTask = initTask(DatasyncParameters);
        MemberModifier.field(DatasyncTask.class, "hook").set(datasyncTask, datasyncHook);
    }

    @Test
    public void testCreateTaskJson() {
        String jsonData = "{\n" +
                "   \"CloudWatchLogGroupArn\": \"arn:aws:logs:ap-northeast-3:523202806641:log-group:/aws/datasync:*\",\n" +
                "   \"DestinationLocationArn\": \"arn:aws:datasync:ap-northeast-3:523202806641:location/loc-01cf61e102e58e365\",\n" +
                "   \"Excludes\": [ \n" +
                "      { \n" +
                "         \"FilterType\": \"exType1\",\n" +
                "         \"Value\": \"exValue1\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"Includes\": [ \n" +
                "      { \n" +
                "         \"FilterType\": \"inType1\",\n" +
                "         \"Value\": \"inValue1\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"Name\": \"task001\",\n" +
                "   \"Options\": { \n" +
                "      \"Atime\": \"aTime\",\n" +
                "      \"BytesPerSecond\": 10,\n" +
                "      \"Gid\": \"string\",\n" +
                "      \"LogLevel\": \"string\",\n" +
                "      \"Mtime\": \"string\",\n" +
                "      \"ObjectTags\": \"string\",\n" +
                "      \"OverwriteMode\": \"string\",\n" +
                "      \"PosixPermissions\": \"string\",\n" +
                "      \"PreserveDeletedFiles\": \"string\",\n" +
                "      \"PreserveDevices\": \"string\",\n" +
                "      \"SecurityDescriptorCopyFlags\": \"string\",\n" +
                "      \"TaskQueueing\": \"string\",\n" +
                "      \"TransferMode\": \"string\",\n" +
                "      \"Uid\": \"string\",\n" +
                "      \"VerifyMode\": \"string\"\n" +
                "   },\n" +
                "   \"Schedule\": { \n" +
                "      \"ScheduleExpression\": \"* * * * * ?\"\n" +
                "   },\n" +
                "   \"SourceLocationArn\": \"arn:aws:datasync:ap-northeast-3:523202806641:location/loc-04ceafb4aaf7a1a0d\",\n" +
                "   \"Tags\": [ \n" +
                "      { \n" +
                "         \"Key\": \"tagKey1\",\n" +
                "         \"Value\": \"tagValue1\"\n" +
                "      }\n" +
                "   ]\n" +
                "}";
        DatasyncParameters DatasyncParameters = new DatasyncParameters();
        DatasyncParameters.setJsonFormat(true);
        DatasyncParameters.setJson(jsonData);

        DatasyncTask DatasyncTask = initTask(DatasyncParameters);
        DatasyncParameters datasyncParameters = DatasyncTask.getParameters();
        Assert.assertEquals("arn:aws:logs:ap-northeast-3:523202806641:log-group:/aws/datasync:*", datasyncParameters.getCloudWatchLogGroupArn());
        Assert.assertEquals("task001", datasyncParameters.getName());
        Assert.assertEquals("arn:aws:datasync:ap-northeast-3:523202806641:location/loc-04ceafb4aaf7a1a0d", datasyncParameters.getSourceLocationArn());
        Assert.assertEquals("arn:aws:datasync:ap-northeast-3:523202806641:location/loc-01cf61e102e58e365", datasyncParameters.getDestinationLocationArn());
        Assert.assertEquals("inType1", datasyncParameters.getIncludes().get(0).getFilterType());
        Assert.assertEquals("inValue1", datasyncParameters.getIncludes().get(0).getValue());
        Assert.assertEquals("exType1", datasyncParameters.getExcludes().get(0).getFilterType());
        Assert.assertEquals("exValue1", datasyncParameters.getExcludes().get(0).getValue());
        Assert.assertEquals("tagKey1", datasyncParameters.getTags().get(0).getKey());
        Assert.assertEquals("tagValue1", datasyncParameters.getTags().get(0).getValue());
        Assert.assertEquals("* * * * * ?", datasyncParameters.getSchedule().getScheduleExpression());
        Assert.assertEquals("aTime", datasyncParameters.getOptions().getAtime());
        Assert.assertEquals(Long.valueOf(10), datasyncParameters.getOptions().getBytesPerSecond());
    }

    @Test
    public void testCheckCreateTask() {
        DatasyncParameters datasyncParameters = datasyncTask.getParameters();

        when(datasyncHook.createDatasyncTask(datasyncParameters)).thenReturn(true);
        Assert.assertEquals(TaskConstants.EXIT_CODE_SUCCESS, datasyncTask.checkCreateTask());

        when(datasyncHook.createDatasyncTask(datasyncParameters)).thenReturn(false);
        Assert.assertEquals(TaskConstants.EXIT_CODE_FAILURE, datasyncTask.checkCreateTask());
    }

    @Test
    public void testStartTask() {
        DatasyncHook hook = spy(new DatasyncHook());
        StartTaskExecutionResponse response = mock(StartTaskExecutionResponse.class);
        when(client.startTaskExecution((StartTaskExecutionRequest) any())).thenReturn(response);
        SdkHttpResponse sdkMock = mock(SdkHttpResponse.class);
        when(response.sdkHttpResponse()).thenReturn(sdkMock);
        when(sdkMock.isSuccessful()).thenReturn(true);
        when(response.taskExecutionArn()).thenReturn(mockExeArn);
        doReturn(true).when(hook).doubleCheckExecStatus(any(), any());
        hook.startDatasyncTask();
        Assert.assertEquals(mockExeArn, hook.getTaskExecArn());
    }

    @Test
    public void testCancelTask() {
        DatasyncHook hook = spy(new DatasyncHook());
        CancelTaskExecutionResponse response = mock(CancelTaskExecutionResponse.class);
        when(client.cancelTaskExecution((CancelTaskExecutionRequest) any())).thenReturn(response);
        SdkHttpResponse sdkMock = mock(SdkHttpResponse.class);
        when(response.sdkHttpResponse()).thenReturn(sdkMock);
        when(sdkMock.isSuccessful()).thenReturn(true);
        Assert.assertEquals(true, hook.cancelDatasyncTask());
    }

    @Test
    public void testDescribeTask() {
        DatasyncHook hook = spy(new DatasyncHook());
        doReturn(null).when(hook).queryDatasyncTaskStatus();
        Assert.assertEquals(false, hook.doubleCheckTaskStatus(TaskStatus.AVAILABLE, DatasyncHook.taskFinishFlags));

        doReturn(TaskStatus.AVAILABLE).when(hook).queryDatasyncTaskStatus();
        Assert.assertEquals(true, hook.doubleCheckTaskStatus(TaskStatus.AVAILABLE, DatasyncHook.taskFinishFlags));
    }

    @Test
    public void testDescribeTaskExec() {
        DatasyncHook hook = spy(new DatasyncHook());
        doReturn(null).when(hook).queryDatasyncTaskExecStatus();
        Assert.assertEquals(false, hook.doubleCheckExecStatus(TaskExecutionStatus.SUCCESS, DatasyncHook.doneStatus));

        doReturn(TaskExecutionStatus.SUCCESS).when(hook).queryDatasyncTaskExecStatus();
        Assert.assertEquals(true, hook.doubleCheckExecStatus(TaskExecutionStatus.SUCCESS, DatasyncHook.doneStatus));
    }

    private DatasyncTask initTask(DatasyncParameters DatasyncParameters) {
        TaskExecutionContext taskExecutionContext = createContext(DatasyncParameters);
        DatasyncTask datasyncTask = new DatasyncTask(taskExecutionContext);
        datasyncTask.init();
        return datasyncTask;
    }

    public TaskExecutionContext createContext(DatasyncParameters DatasyncParameters) {
        String parameters = JSONUtils.toJsonString(DatasyncParameters);
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp/dolphinscheduler_datasync_%s");
        return taskExecutionContext;
    }
}