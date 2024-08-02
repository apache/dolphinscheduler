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
package org.apache.dolphinscheduler.plugin.task.aliyunserverlessspark;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.aliyun.emr_serverless_spark20230808.Client;
import com.aliyun.emr_serverless_spark20230808.models.CancelJobRunRequest;
import com.aliyun.emr_serverless_spark20230808.models.CancelJobRunResponse;
import com.aliyun.emr_serverless_spark20230808.models.GetJobRunRequest;
import com.aliyun.emr_serverless_spark20230808.models.GetJobRunResponse;
import com.aliyun.emr_serverless_spark20230808.models.GetJobRunResponseBody;
import com.aliyun.emr_serverless_spark20230808.models.StartJobRunRequest;
import com.aliyun.emr_serverless_spark20230808.models.StartJobRunResponse;
import com.aliyun.emr_serverless_spark20230808.models.StartJobRunResponseBody;

@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AliyunServerlessSparkTaskTest {

    @Mock
    private TaskExecutionContext mockTaskExecutionContext;

    @Mock
    private Client mockAliyunServerlessSparkClient;

    @Mock
    private ResourceParametersHelper mockResourceParametersHelper;

    @Mock
    private TaskCallBack mockTaskCallBack;

    @Mock
    private StartJobRunRequest mockStartJobRunRequest;

    @Mock
    private StartJobRunResponse mockStartJobRunResponse;

    @Mock
    private GetJobRunRequest mockGetJobRunRequest;

    @Mock
    private GetJobRunResponse mockGetJobRunResponse;

    @Mock
    private CancelJobRunRequest mockCancelJobRunRequest;

    @Mock
    private CancelJobRunResponse mockCancelJobRunResponse;

    @InjectMocks
    @Spy
    private AliyunServerlessSparkTask aliyunServerlessSparkTask;

    private static final String mockAccessKeyId = "mockAccessKeyId";

    private static final String mockAccessKeySecret = "mockAccessKeySecret";

    private static final String mockRegionId = "cn-hangzhou";

    private static final String mockEndpoint = "emr-serverless-spark-vpc.cn-hangzhou.aliyuncs.com";

    private static final int mockDatasourceId = 1;

    private static final String taskParamsString =
            "{\"localParams\":[],\"resourceList\":[],\"workspaceId\":\"w-ae42e9c929275cc5\",\"resourceQueueId\":\"root_queue\",\"codeType\":\"JAR\",\"jobName\":\"spark\",\"entryPoint\":\"oss://datadev-oss-hdfs-test/spark-resource/examples/jars/spark-examples_2.12-3.3.1.jar\",\"entryPointArguments\":\"10\",\"sparkSubmitParameters\":\"--class org.apache.spark.examples.SparkPi --conf spark.executor.cores=4 --conf spark.executor.memory=20g --conf spark.driver.cores=4 --conf spark.driver.memory=8g --conf spark.executor.instances=1\",\"isProduction\":true,\"type\":\"ALIYUN_SERVERLESS_SPARK\",\"datasource\":1}";

    private static final String connectionParamsString =
            "{\"accessKeyId\":\"mockAccessKeyId\",\"accessKeySecret\":\"mockAccessKeySecret\",\"regionId\":\"cn-hangzhou\",\"endpoint\":\"emr-serverless-spark-vpc.cn-hangzhou.aliyuncs.com\",\"password\":\"\"}";

    private static final String mockJobRunId = "jr-f6a1d0dd17d6b8a3";

    private static final String mockWorkspaceId = "w-ae42e9c929275cc5";

    private static final String mockResourceQueueId = "root_queue";

    private static final String mockSparkSubmitParameters =
            "--class org.apache.spark.examples.SparkPi --conf spark.executor.cores=4 --conf spark.executor.memory=20g --conf spark.driver.cores=4 --conf spark.driver.memory=8g --conf spark.executor.instances=1";

    private static final String mockEntryPoint =
            "oss://datadev-oss-hdfs-test/spark-resource/examples/jars/spark-examples_2.12-3.3.1.jar";

    private static final String mockEntryPointArguments = "10";

    @BeforeEach
    public void before() {
        when(mockTaskExecutionContext.getTaskParams()).thenReturn(taskParamsString);
        DataSourceParameters dataSourceParameters = new DataSourceParameters();
        dataSourceParameters.setConnectionParams(connectionParamsString);
        dataSourceParameters.setType(DbType.ALIYUN_SERVERLESS_SPARK);
        when(mockResourceParametersHelper.getResourceParameters(any(), any())).thenReturn(dataSourceParameters);
        when(mockTaskExecutionContext.getResourceParametersHelper()).thenReturn(mockResourceParametersHelper);
        Assertions.assertDoesNotThrow(
                () -> when(aliyunServerlessSparkTask.buildAliyunServerlessSparkClient(any(), any(), any(), any()))
                        .thenReturn(mockAliyunServerlessSparkClient));
    }

    @Test
    public void testInit() throws Exception {
        aliyunServerlessSparkTask.init();
        verify(mockTaskExecutionContext).getTaskParams();
        verify(mockResourceParametersHelper).getResourceParameters(ResourceType.DATASOURCE, mockDatasourceId);
        verify(aliyunServerlessSparkTask).buildAliyunServerlessSparkClient(mockAccessKeyId, mockAccessKeySecret,
                mockRegionId, mockEndpoint);
    }

    @Test
    public void testHandle() {
        doReturn(mockStartJobRunRequest).when(aliyunServerlessSparkTask).buildStartJobRunRequest(any());
        StartJobRunResponseBody startJobRunResponseBody = new StartJobRunResponseBody();
        startJobRunResponseBody.setJobRunId(mockJobRunId);
        doReturn(startJobRunResponseBody).when(mockStartJobRunResponse).getBody();
        Assertions.assertDoesNotThrow(
                () -> doReturn(mockStartJobRunResponse).when(mockAliyunServerlessSparkClient)
                        .startJobRunWithOptions(any(), any(), any(), any()));

        doReturn(mockGetJobRunRequest).when(aliyunServerlessSparkTask).buildGetJobRunRequest();
        GetJobRunResponseBody getJobRunResponseBody = new GetJobRunResponseBody();
        GetJobRunResponseBody.GetJobRunResponseBodyJobRun jobRun =
                new GetJobRunResponseBody.GetJobRunResponseBodyJobRun();
        jobRun.setState(RunState.Success.name());
        getJobRunResponseBody.setJobRun(jobRun);
        doReturn(getJobRunResponseBody).when(mockGetJobRunResponse).getBody();
        Assertions.assertDoesNotThrow(
                () -> doReturn(mockGetJobRunResponse).when(mockAliyunServerlessSparkClient).getJobRun(any(), any(),
                        any()));

        aliyunServerlessSparkTask.init();
        aliyunServerlessSparkTask.handle(mockTaskCallBack);
        verify(aliyunServerlessSparkTask).setAppIds(mockJobRunId);
        verify(aliyunServerlessSparkTask).setExitStatusCode(TaskConstants.EXIT_CODE_SUCCESS);
    }

    @Test
    public void testCancelApplication() throws Exception {
        doReturn(mockCancelJobRunRequest).when(aliyunServerlessSparkTask).buildCancelJobRunRequest();
        Assertions.assertDoesNotThrow(
                () -> doReturn(mockCancelJobRunResponse).when(mockAliyunServerlessSparkClient).cancelJobRun(any(),
                        any(), any()));

        aliyunServerlessSparkTask.init();
        aliyunServerlessSparkTask.cancelApplication();
        verify(aliyunServerlessSparkTask).buildCancelJobRunRequest();
        verify(mockAliyunServerlessSparkClient).cancelJobRun(eq(mockWorkspaceId), any(), eq(mockCancelJobRunRequest));
    }

    @Test
    public void testBuildStartJobRunRequest() {
        AliyunServerlessSparkParameters mockAliyunServerlessSparkParameters =
                mock(AliyunServerlessSparkParameters.class);
        doReturn(mockResourceQueueId).when(mockAliyunServerlessSparkParameters).getResourceQueueId();
        doReturn("JAR").when(mockAliyunServerlessSparkParameters).getCodeType();
        doReturn("ds-test").when(mockAliyunServerlessSparkParameters).getJobName();
        doReturn(mockSparkSubmitParameters).when(mockAliyunServerlessSparkParameters).getSparkSubmitParameters();
        doReturn(mockEntryPoint).when(mockAliyunServerlessSparkParameters).getEntryPoint();
        doReturn(mockEntryPointArguments).when(mockAliyunServerlessSparkParameters).getEntryPointArguments();

        aliyunServerlessSparkTask.buildStartJobRunRequest(mockAliyunServerlessSparkParameters);

        verify(mockAliyunServerlessSparkParameters).getResourceQueueId();
        verify(mockAliyunServerlessSparkParameters).getCodeType();
        verify(mockAliyunServerlessSparkParameters).getJobName();
        verify(mockAliyunServerlessSparkParameters).getEngineReleaseVersion();
        verify(mockAliyunServerlessSparkParameters).isProduction();
    }

}
