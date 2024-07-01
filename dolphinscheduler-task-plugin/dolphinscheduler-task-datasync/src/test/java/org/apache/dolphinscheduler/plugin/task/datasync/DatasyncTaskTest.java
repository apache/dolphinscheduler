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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.datasync.DataSyncClient;
import software.amazon.awssdk.services.datasync.model.CancelTaskExecutionRequest;
import software.amazon.awssdk.services.datasync.model.CancelTaskExecutionResponse;
import software.amazon.awssdk.services.datasync.model.CreateTaskRequest;
import software.amazon.awssdk.services.datasync.model.CreateTaskResponse;
import software.amazon.awssdk.services.datasync.model.DescribeTaskExecutionRequest;
import software.amazon.awssdk.services.datasync.model.DescribeTaskExecutionResponse;
import software.amazon.awssdk.services.datasync.model.DescribeTaskRequest;
import software.amazon.awssdk.services.datasync.model.DescribeTaskResponse;
import software.amazon.awssdk.services.datasync.model.StartTaskExecutionRequest;
import software.amazon.awssdk.services.datasync.model.StartTaskExecutionResponse;
import software.amazon.awssdk.services.datasync.model.TaskExecutionStatus;
import software.amazon.awssdk.services.datasync.model.TaskStatus;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DatasyncTaskTest {

    private static final String mockExeArn =
            "arn:aws:datasync:ap-northeast-3:523202806641:task/task-017642db08fdf6a55/execution/exec-0ac3607778dfc31f5";

    private static final String mockTaskArn =
            "arn:aws:datasync:ap-northeast-3:523202806641:task/task-071ca64ff4c2f0d4a";

    @InjectMocks
    @Spy
    DatasyncTask datasyncTask;

    @Mock
    TaskExecutionContext taskExecutionContext;

    @Spy
    @InjectMocks
    DatasyncHook datasyncHook;

    @Mock
    DataSyncClient client;

    @BeforeEach
    public void before() throws IllegalAccessException {
        String jsonData = "{\n" +
                "   \"CloudWatchLogGroupArn\": \"arn:aws:logs:ap-northeast-3:523202806641:log-group:/aws/datasync:*\",\n"
                +
                "   \"DestinationLocationArn\": \"arn:aws:datasync:ap-northeast-3:523202806641:location/loc-01cf61e102e58e365\",\n"
                +
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
                "   \"SourceLocationArn\": \"arn:aws:datasync:ap-northeast-3:523202806641:location/loc-04ceafb4aaf7a1a0d\",\n"
                +
                "   \"Tags\": [ \n" +
                "      { \n" +
                "         \"Key\": \"tagKey1\",\n" +
                "         \"Value\": \"tagValue1\"\n" +
                "      }\n" +
                "   ]\n" +
                "}";
        DatasyncParameters parameters = new DatasyncParameters();
        parameters.setJson(jsonData);
        parameters.setJsonFormat(true);
        datasyncTask = initTask(JSONUtils.toJsonString(parameters));
    }

    @Test
    public void testCreateTaskJson() {
        DatasyncParameters datasyncParameters = datasyncTask.getParameters();

        Assertions.assertEquals("arn:aws:logs:ap-northeast-3:523202806641:log-group:/aws/datasync:*",
                datasyncParameters.getCloudWatchLogGroupArn());
        Assertions.assertEquals("task001", datasyncParameters.getName());
        Assertions.assertEquals("arn:aws:datasync:ap-northeast-3:523202806641:location/loc-04ceafb4aaf7a1a0d",
                datasyncParameters.getSourceLocationArn());
        Assertions.assertEquals("arn:aws:datasync:ap-northeast-3:523202806641:location/loc-01cf61e102e58e365",
                datasyncParameters.getDestinationLocationArn());
        Assertions.assertEquals("inType1", datasyncParameters.getIncludes().get(0).getFilterType());
        Assertions.assertEquals("inValue1", datasyncParameters.getIncludes().get(0).getValue());
        Assertions.assertEquals("exType1", datasyncParameters.getExcludes().get(0).getFilterType());
        Assertions.assertEquals("exValue1", datasyncParameters.getExcludes().get(0).getValue());
        Assertions.assertEquals("tagKey1", datasyncParameters.getTags().get(0).getKey());
        Assertions.assertEquals("tagValue1", datasyncParameters.getTags().get(0).getValue());
        Assertions.assertEquals("* * * * * ?", datasyncParameters.getSchedule().getScheduleExpression());
        Assertions.assertEquals("aTime", datasyncParameters.getOptions().getAtime());
        Assertions.assertEquals(Long.valueOf(10), datasyncParameters.getOptions().getBytesPerSecond());
    }

    @Test
    public void testCheckCreateTask() {
        CreateTaskResponse response = mock(CreateTaskResponse.class);
        SdkHttpResponse sdkMock = mock(SdkHttpResponse.class);
        DescribeTaskResponse describeTaskResponse = mock(DescribeTaskResponse.class);
        when(client.createTask((CreateTaskRequest) any())).thenReturn(response);
        when(response.sdkHttpResponse()).thenReturn(sdkMock);
        when(describeTaskResponse.sdkHttpResponse()).thenReturn(sdkMock);
        when(sdkMock.isSuccessful()).thenReturn(true);
        when(response.taskArn()).thenReturn(mockTaskArn);
        when(client.describeTask((DescribeTaskRequest) any())).thenReturn(describeTaskResponse);
        when(describeTaskResponse.status()).thenReturn(TaskStatus.AVAILABLE);

        Boolean flag = datasyncHook.createDatasyncTask(datasyncTask.getParameters());

        Assertions.assertEquals(mockTaskArn, datasyncHook.getTaskArn());
        Assertions.assertTrue(flag);
    }

    @Test
    public void testStartTask() {
        StartTaskExecutionResponse response = mock(StartTaskExecutionResponse.class);
        SdkHttpResponse sdkMock = mock(SdkHttpResponse.class);
        DescribeTaskExecutionResponse describeTaskExecutionResponse = mock(DescribeTaskExecutionResponse.class);

        when(client.startTaskExecution((StartTaskExecutionRequest) any())).thenReturn(response);
        when(response.sdkHttpResponse()).thenReturn(sdkMock);
        when(sdkMock.isSuccessful()).thenReturn(true);
        when(response.taskExecutionArn()).thenReturn(mockExeArn);
        when(describeTaskExecutionResponse.sdkHttpResponse()).thenReturn(sdkMock);
        when(client.describeTaskExecution((DescribeTaskExecutionRequest) any()))
                .thenReturn(describeTaskExecutionResponse);
        when(describeTaskExecutionResponse.status()).thenReturn(TaskExecutionStatus.LAUNCHING);
        Boolean executionFlag = datasyncHook.startDatasyncTask();

        Assertions.assertEquals(mockExeArn, datasyncHook.getTaskExecArn());
        Assertions.assertTrue(executionFlag);
    }

    @Test
    public void testCancelTask() {
        CancelTaskExecutionResponse response = mock(CancelTaskExecutionResponse.class);
        SdkHttpResponse sdkMock = mock(SdkHttpResponse.class);
        when(client.cancelTaskExecution((CancelTaskExecutionRequest) any())).thenReturn(response);
        when(response.sdkHttpResponse()).thenReturn(sdkMock);
        when(sdkMock.isSuccessful()).thenReturn(true);
        Assertions.assertEquals(true, datasyncHook.cancelDatasyncTask());
    }

    @Test
    public void testDescribeTask() {
        SdkHttpResponse sdkMock = mock(SdkHttpResponse.class);
        DescribeTaskResponse failed = mock(DescribeTaskResponse.class);
        DescribeTaskResponse available = mock(DescribeTaskResponse.class);

        when(client.describeTask((DescribeTaskRequest) any())).thenReturn(failed);
        when(failed.sdkHttpResponse()).thenReturn(sdkMock);
        when(sdkMock.isSuccessful()).thenReturn(true);
        when(failed.status()).thenReturn(TaskStatus.UNKNOWN_TO_SDK_VERSION);
        Assertions.assertEquals(false,
                datasyncHook.doubleCheckTaskStatus(TaskStatus.AVAILABLE, DatasyncHook.taskFinishFlags));

        when(client.describeTask((DescribeTaskRequest) any())).thenReturn(available);
        when(available.sdkHttpResponse()).thenReturn(sdkMock);
        when(sdkMock.isSuccessful()).thenReturn(true);
        when(available.status()).thenReturn(TaskStatus.AVAILABLE);
        Assertions.assertEquals(true,
                datasyncHook.doubleCheckTaskStatus(TaskStatus.AVAILABLE, DatasyncHook.taskFinishFlags));
    }

    @Test
    public void testDescribeTaskExec() {
        SdkHttpResponse sdkMock = mock(SdkHttpResponse.class);
        DescribeTaskExecutionResponse failed = mock(DescribeTaskExecutionResponse.class);
        DescribeTaskExecutionResponse success = mock(DescribeTaskExecutionResponse.class);

        when(client.describeTaskExecution((DescribeTaskExecutionRequest) any())).thenReturn(failed);
        when(failed.sdkHttpResponse()).thenReturn(sdkMock);
        when(sdkMock.isSuccessful()).thenReturn(true);
        when(failed.status()).thenReturn(TaskExecutionStatus.UNKNOWN_TO_SDK_VERSION);
        Assertions.assertEquals(false,
                datasyncHook.doubleCheckExecStatus(TaskExecutionStatus.SUCCESS, DatasyncHook.doneStatus));

        when(client.describeTaskExecution((DescribeTaskExecutionRequest) any())).thenReturn(success);
        when(success.sdkHttpResponse()).thenReturn(sdkMock);
        when(sdkMock.isSuccessful()).thenReturn(true);
        when(success.status()).thenReturn(TaskExecutionStatus.SUCCESS);
        Assertions.assertEquals(true,
                datasyncHook.doubleCheckExecStatus(TaskExecutionStatus.SUCCESS, DatasyncHook.doneStatus));
    }

    private DatasyncTask initTask(String contextJson) {
        doReturn(contextJson).when(taskExecutionContext).getTaskParams();
        datasyncTask.init();
        return datasyncTask;
    }
}
