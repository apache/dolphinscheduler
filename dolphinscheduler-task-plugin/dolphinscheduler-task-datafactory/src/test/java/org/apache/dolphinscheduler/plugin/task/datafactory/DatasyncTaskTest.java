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

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DatasyncTaskTest {

    private static final String mockExeArn =
            "arn:aws:datasync:ap-northeast-3:523202806641:task/task-017642db08fdf6a55/execution/exec-0ac3607778dfc31f5";

    private static final String mockTaskArn =
            "arn:aws:datasync:ap-northeast-3:523202806641:task/task-071ca64ff4c2f0d4a";

    DatafactoryHook datasyncHook;

    DatafactoryTask datasyncTask;

    /*@Mock
    DataSyncClient client;
    MockedStatic<DatafactoryHook> datasyncHookMockedStatic;
    @BeforeEach
    public void before() throws IllegalAccessException {
        client = mock(DataSyncClient.class);
        datasyncHookMockedStatic = mockStatic(DatafactoryHook.class);
        when(DatafactoryHook.createClient()).thenReturn(client);

        DatafactoryParameters DatasyncParameters = new DatafactoryParameters();
        datasyncTask = initTask(DatasyncParameters);
        datasyncTask.setHook(datasyncHook);
    }

    @Test
    public void testCreateTaskJson() {
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
        DatafactoryParameters DatasyncParameters = new DatafactoryParameters();
        DatasyncParameters.setJsonFormat(true);
        DatasyncParameters.setJson(jsonData);

        DatafactoryTask DatasyncTask = initTask(DatasyncParameters);
        DatafactoryParameters datasyncParameters = DatasyncTask.getParameters();
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
        datasyncHookMockedStatic.close();
    }

    @Test
    public void testCheckCreateTask() {
        DatafactoryHook hook = spy(new DatafactoryHook());
        CreateTaskResponse response = mock(CreateTaskResponse.class);
        when(client.createTask((CreateTaskRequest) any())).thenReturn(response);
        SdkHttpResponse sdkMock = mock(SdkHttpResponse.class);
        when(response.sdkHttpResponse()).thenReturn(sdkMock);
        when(sdkMock.isSuccessful()).thenReturn(true);
        when(response.taskArn()).thenReturn(mockTaskArn);

        doReturn(true).when(hook).doubleCheckTaskStatus(any(), any());
        hook.createDatasyncTask(datasyncTask.getParameters());
        Assertions.assertEquals(mockTaskArn, hook.getTaskArn());
        datasyncHookMockedStatic.close();
    }

    @Test
    public void testStartTask() {
        DatafactoryHook hook = spy(new DatafactoryHook());
        StartTaskExecutionResponse response = mock(StartTaskExecutionResponse.class);
        when(client.startTaskExecution((StartTaskExecutionRequest) any())).thenReturn(response);
        SdkHttpResponse sdkMock = mock(SdkHttpResponse.class);
        when(response.sdkHttpResponse()).thenReturn(sdkMock);
        when(sdkMock.isSuccessful()).thenReturn(true);
        when(response.taskExecutionArn()).thenReturn(mockExeArn);
        doReturn(true).when(hook).doubleCheckExecStatus(any(), any());
        hook.startDatasyncTask();
        Assertions.assertEquals(mockExeArn, hook.getTaskExecArn());
        datasyncHookMockedStatic.close();
    }

    @Test
    public void testCancelTask() {
        DatafactoryHook hook = spy(new DatafactoryHook());
        CancelTaskExecutionResponse response = mock(CancelTaskExecutionResponse.class);
        when(client.cancelTaskExecution((CancelTaskExecutionRequest) any())).thenReturn(response);
        SdkHttpResponse sdkMock = mock(SdkHttpResponse.class);
        when(response.sdkHttpResponse()).thenReturn(sdkMock);
        when(sdkMock.isSuccessful()).thenReturn(true);
        Assertions.assertEquals(true, hook.cancelDatasyncTask());
        datasyncHookMockedStatic.close();
    }

    @Test
    public void testDescribeTask() {
        DatafactoryHook hook = spy(new DatafactoryHook());
        doReturn(null).when(hook).queryDatasyncTaskStatus();
        Assertions.assertEquals(false, hook.doubleCheckTaskStatus(TaskStatus.AVAILABLE, DatafactoryHook.taskFinishFlags));

        doReturn(TaskStatus.AVAILABLE).when(hook).queryDatasyncTaskStatus();
        Assertions.assertEquals(true, hook.doubleCheckTaskStatus(TaskStatus.AVAILABLE, DatafactoryHook.taskFinishFlags));
        datasyncHookMockedStatic.close();
    }

    @Test
    public void testDescribeTaskExec() {
        DatafactoryHook hook = spy(new DatafactoryHook());
        doReturn(null).when(hook).queryDatasyncTaskExecStatus();
        Assertions.assertEquals(false,
                hook.doubleCheckExecStatus(TaskExecutionStatus.SUCCESS, DatafactoryHook.doneStatus));

        doReturn(TaskExecutionStatus.SUCCESS).when(hook).queryDatasyncTaskExecStatus();
        Assertions.assertEquals(true, hook.doubleCheckExecStatus(TaskExecutionStatus.SUCCESS, DatafactoryHook.doneStatus));
        datasyncHookMockedStatic.close();
    }

    private DatafactoryTask initTask(DatafactoryParameters DatasyncParameters) {
        TaskExecutionContext taskExecutionContext = createContext(DatasyncParameters);
        DatafactoryTask datasyncTask = new DatafactoryTask(taskExecutionContext);
        datasyncTask.init();
        return datasyncTask;
    }

    public TaskExecutionContext createContext(DatafactoryParameters DatasyncParameters) {
        String parameters = JSONUtils.toJsonString(DatasyncParameters);
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        return taskExecutionContext;
    }*/
}
