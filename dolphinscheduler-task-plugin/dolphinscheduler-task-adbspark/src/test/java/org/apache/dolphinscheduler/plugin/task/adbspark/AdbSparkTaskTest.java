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

package org.apache.dolphinscheduler.plugin.task.adbspark;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.spi.enums.DbType;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aliyun.adb20211201.Client;
import com.aliyun.adb20211201.models.GetSparkAppStateRequest;
import com.aliyun.adb20211201.models.GetSparkAppStateResponse;
import com.aliyun.adb20211201.models.GetSparkAppStateResponseBody;
import com.aliyun.adb20211201.models.KillSparkAppRequest;
import com.aliyun.adb20211201.models.SubmitSparkAppResponse;
import com.aliyun.adb20211201.models.SubmitSparkAppResponseBody;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class AdbSparkTaskTest {

    @Mock
    private TaskExecutionContext mockTaskExecutionContext;

    @Mock
    private Client mockAdbSparkClient;

    @Mock
    private ResourceParametersHelper mockResourceParametersHelper;

    @Mock
    private SubmitSparkAppResponseBody.SubmitSparkAppResponseBodyData mockSubmitSparkAppResponseBodyData;

    @Mock
    private SubmitSparkAppResponseBody mockSubmitSparkAppResponseBody;

    @Mock
    private SubmitSparkAppResponse mockSubmitSparkAppResponse;

    @Mock
    private GetSparkAppStateResponseBody.GetSparkAppStateResponseBodyData mockGetSparkAppStateResponseBodyData;

    @Mock
    private GetSparkAppStateResponseBody mockGetSparkAppStateResponseBody;

    @Mock
    private GetSparkAppStateResponse mockGetSparkAppStateResponse;

    @InjectMocks
    @Spy
    private AdbSparkTask adbSparkTask;

    private static final String mockAppId = "s123bj456";

    private static final int mockDatasourceId = 1;

    private final String mockConnectionJson =
            "{\n" +
                    "    \"aliyunAccessKeyId\":\"mockAccessKeyId\",\n" +
                    "    \"aliyunAccessKeySecret\":\"mockAccessKeySecret\",\n" +
                    "    \"aliyunRegionId\":\"cn-beijing\"\n" +
                    "}";

    private final String taskParamJson =
            "{\n" +
                    "    \"dbClusterId\":\"amv-xxx\",\n" +
                    "    \"resourceGroupName\":\"spark\",\n" +
                    "    \"appName\":\"sparkpi\",\n" +
                    "    \"appType\":\"Batch\",\n" +
                    "    \"data\": \"{\\n\\\"args\\\": [\\n\\\"1000\\\"\\n]}\",\n" +
                    "    \"datasource\":1,\n" +
                    "    \"type\":\"ADBSPARK\"\n" +
                    "}";

    @BeforeEach
    public void setUp() {
        when(mockTaskExecutionContext.getTaskParams()).thenReturn(taskParamJson);
        DataSourceParameters dataSourceParameters = new DataSourceParameters();
        dataSourceParameters.setConnectionParams(mockConnectionJson);
        dataSourceParameters.setType(DbType.ADBSPARK);
        when(mockResourceParametersHelper.getResourceParameters(any(ResourceType.class), anyInt()))
                .thenReturn(dataSourceParameters);
        when(mockTaskExecutionContext.getResourceParametersHelper()).thenReturn(mockResourceParametersHelper);
        when(adbSparkTask.createClient(anyString(), anyString(), anyString()))
                .thenReturn(mockAdbSparkClient);
    }

    @Test
    public void testInit() {
        adbSparkTask.init();
        verify(mockTaskExecutionContext).getTaskParams();
        verify(mockResourceParametersHelper).getResourceParameters(ResourceType.DATASOURCE, mockDatasourceId);
    }

    @Test
    public void testSubmitApplication() throws Exception {
        // init
        adbSparkTask.init();

        // mock
        when(mockAdbSparkClient.submitSparkApp(any())).thenReturn(mockSubmitSparkAppResponse);
        when(mockSubmitSparkAppResponse.getBody()).thenReturn(mockSubmitSparkAppResponseBody);
        when(mockSubmitSparkAppResponseBody.getData()).thenReturn(mockSubmitSparkAppResponseBodyData);
        when(mockSubmitSparkAppResponseBodyData.getAppId()).thenReturn(mockAppId);

        when(mockAdbSparkClient.getSparkAppState(any(GetSparkAppStateRequest.class)))
                .thenReturn(mockGetSparkAppStateResponse);
        when(mockGetSparkAppStateResponse.getBody()).thenReturn(mockGetSparkAppStateResponseBody);
        when(mockGetSparkAppStateResponseBody.getData()).thenReturn(mockGetSparkAppStateResponseBodyData);
        when(mockGetSparkAppStateResponseBodyData.getState()).thenReturn("SUBMITTED");

        // action
        adbSparkTask.submitApplication();

        // verify
        Assertions.assertEquals(mockAppId, adbSparkTask.getAppIds());
        Assertions.assertEquals(TaskExecutionStatus.SUCCESS, adbSparkTask.getExitStatus());
    }

    @Test
    public void testTraceApplication() throws Exception {
        // init
        adbSparkTask.init();
        adbSparkTask.setAppIds(mockAppId);

        // mock
        when(mockAdbSparkClient.getSparkAppState(any(GetSparkAppStateRequest.class)))
                .thenReturn(mockGetSparkAppStateResponse);
        when(mockGetSparkAppStateResponse.getBody()).thenReturn(mockGetSparkAppStateResponseBody);
        when(mockGetSparkAppStateResponseBody.getData()).thenReturn(mockGetSparkAppStateResponseBodyData);
        when(mockGetSparkAppStateResponseBodyData.getState()).thenReturn("COMPLETED");

        // action
        adbSparkTask.trackApplicationStatus();

        // verify
        Assertions.assertEquals(TaskExecutionStatus.SUCCESS, adbSparkTask.getExitStatus());
    }

    @Test
    public void testCancelApplication() throws Exception {
        // init
        adbSparkTask.init();
        adbSparkTask.setAppIds(mockAppId);

        // action
        adbSparkTask.cancelApplication();

        // verify
        KillSparkAppRequest killSparkAppRequest = new KillSparkAppRequest();
        killSparkAppRequest.setAppId(mockAppId);
        verify(mockAdbSparkClient).killSparkApp(any());
    }

}
