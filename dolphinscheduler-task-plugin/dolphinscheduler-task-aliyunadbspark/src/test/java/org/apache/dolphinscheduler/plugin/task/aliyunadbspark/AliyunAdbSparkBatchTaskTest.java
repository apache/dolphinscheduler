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

package org.apache.dolphinscheduler.plugin.task.aliyunadbspark;

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
public class AliyunAdbSparkBatchTaskTest {

    @Mock
    private TaskExecutionContext mockTaskExecutionContext;

    @Mock
    private Client mockAliyunAdbSparkClient;

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
    private AliyunAdbSparkBatchTask aliyunAdbSparkTask;

    private static final String mockAppId = "s123bj456";

    private static final int mockDatasourceId = 1;

    private final String mockConnectionJson =
            "{\n" +
                    "    \"accessKeyId\":\"mockAccessKeyId\",\n" +
                    "    \"accessKeySecret\":\"mockAccessKeySecret\",\n" +
                    "    \"regionId\":\"cn-beijing\"\n" +
                    "}";

    private final String taskParamJson =
            "{\n" +
                    "    \"dbClusterId\":\"amv-xxx\",\n" +
                    "    \"resourceGroupName\":\"spark\",\n" +
                    "    \"appName\":\"sparkpi\",\n" +
                    "    \"appType\":\"Batch\",\n" +
                    "    \"data\":{\n" +
                    "        \"file\":\"local:///tmp/spark-examples.jar\",\n" +
                    "        \"className\":\"org.apache.spark.examples.SparkPi\",\n" +
                    "        \"conf\":{\n" +
                    "            \"spark.driver.resourceSpec\":\"medium\",\n" +
                    "            \"spark.executor.instances\":2,\n" +
                    "            \"spark.executor.resourceSpec\":\"medium\"\n" +
                    "        }\n" +
                    "    },\n" +
                    "    \"datasource\":1,\n" +
                    "    \"type\":\"ALIYUN_ADB_SPARK\"\n" +
                    "}";

    @BeforeEach
    public void setUp() {
        when(mockTaskExecutionContext.getTaskParams()).thenReturn(taskParamJson);
        DataSourceParameters dataSourceParameters = new DataSourceParameters();
        dataSourceParameters.setConnectionParams(mockConnectionJson);
        dataSourceParameters.setType(DbType.ALIYUN_ADB_SPARK);
        when(mockResourceParametersHelper.getResourceParameters(any(ResourceType.class), anyInt()))
                .thenReturn(dataSourceParameters);
        when(mockTaskExecutionContext.getResourceParametersHelper()).thenReturn(mockResourceParametersHelper);
        when(aliyunAdbSparkTask.createClient(anyString(), anyString(), anyString()))
                .thenReturn(mockAliyunAdbSparkClient);
    }

    @Test
    public void testInit() {
        aliyunAdbSparkTask.init();
        verify(mockTaskExecutionContext).getTaskParams();
        verify(mockResourceParametersHelper).getResourceParameters(ResourceType.DATASOURCE, mockDatasourceId);
    }

    @Test
    public void testSubmitApplication() throws Exception {
        // init
        aliyunAdbSparkTask.init();

        // mock
        when(mockAliyunAdbSparkClient.submitSparkApp(any())).thenReturn(mockSubmitSparkAppResponse);
        when(mockSubmitSparkAppResponse.getBody()).thenReturn(mockSubmitSparkAppResponseBody);
        when(mockSubmitSparkAppResponseBody.getData()).thenReturn(mockSubmitSparkAppResponseBodyData);
        when(mockSubmitSparkAppResponseBodyData.getAppId()).thenReturn(mockAppId);

        when(mockAliyunAdbSparkClient.getSparkAppState(any(GetSparkAppStateRequest.class)))
                .thenReturn(mockGetSparkAppStateResponse);
        when(mockGetSparkAppStateResponse.getBody()).thenReturn(mockGetSparkAppStateResponseBody);
        when(mockGetSparkAppStateResponseBody.getData()).thenReturn(mockGetSparkAppStateResponseBodyData);
        when(mockGetSparkAppStateResponseBodyData.getState()).thenReturn("SUBMITTED");

        // action
        aliyunAdbSparkTask.submitApplication();

        // verify
        Assertions.assertEquals(mockAppId, aliyunAdbSparkTask.getAppIds());
        Assertions.assertEquals(TaskExecutionStatus.SUCCESS, aliyunAdbSparkTask.getExitStatus());
    }

    @Test
    public void testTraceApplication() throws Exception {
        // init
        aliyunAdbSparkTask.init();
        aliyunAdbSparkTask.setAppIds(mockAppId);

        // mock
        when(mockAliyunAdbSparkClient.getSparkAppState(any(GetSparkAppStateRequest.class)))
                .thenReturn(mockGetSparkAppStateResponse);
        when(mockGetSparkAppStateResponse.getBody()).thenReturn(mockGetSparkAppStateResponseBody);
        when(mockGetSparkAppStateResponseBody.getData()).thenReturn(mockGetSparkAppStateResponseBodyData);
        when(mockGetSparkAppStateResponseBodyData.getState()).thenReturn("COMPLETED");

        // action
        aliyunAdbSparkTask.trackApplicationStatus();

        // verify
        Assertions.assertEquals(TaskExecutionStatus.SUCCESS, aliyunAdbSparkTask.getExitStatus());
    }

    @Test
    public void testCancelApplication() throws Exception {
        // init
        aliyunAdbSparkTask.init();
        aliyunAdbSparkTask.setAppIds(mockAppId);

        // action
        aliyunAdbSparkTask.cancelApplication();

        // verify
        KillSparkAppRequest killSparkAppRequest = new KillSparkAppRequest();
        killSparkAppRequest.setAppId(mockAppId);
        verify(mockAliyunAdbSparkClient).killSparkApp(any());
    }

}
