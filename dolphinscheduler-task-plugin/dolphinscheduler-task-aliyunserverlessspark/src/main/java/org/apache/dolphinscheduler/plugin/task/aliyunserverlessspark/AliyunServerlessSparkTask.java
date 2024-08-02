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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.aliyunserverlessspark.AliyunServerlessSparkConstants;
import org.apache.dolphinscheduler.plugin.datasource.aliyunserverlessspark.param.AliyunServerlessSparkConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.aliyun.emr_serverless_spark20230808.Client;
import com.aliyun.emr_serverless_spark20230808.models.CancelJobRunRequest;
import com.aliyun.emr_serverless_spark20230808.models.GetJobRunRequest;
import com.aliyun.emr_serverless_spark20230808.models.GetJobRunResponse;
import com.aliyun.emr_serverless_spark20230808.models.JobDriver;
import com.aliyun.emr_serverless_spark20230808.models.StartJobRunRequest;
import com.aliyun.emr_serverless_spark20230808.models.StartJobRunResponse;
import com.aliyun.emr_serverless_spark20230808.models.Tag;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;

@Slf4j
public class AliyunServerlessSparkTask extends AbstractRemoteTask {

    private final TaskExecutionContext taskExecutionContext;

    private Client aliyunServerlessSparkClient;

    private AliyunServerlessSparkParameters aliyunServerlessSparkParameters;

    private AliyunServerlessSparkConnectionParam aliyunServerlessSparkConnectionParam;

    private String jobRunId;

    private RunState currentState;

    private String accessKeyId;

    private String accessKeySecret;

    private String regionId;

    private String endpoint;

    protected AliyunServerlessSparkTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        final String taskParams = taskExecutionContext.getTaskParams();
        aliyunServerlessSparkParameters = JSONUtils.parseObject(taskParams, AliyunServerlessSparkParameters.class);
        log.info("aliyunServerlessSparkParameters - {}", aliyunServerlessSparkParameters);
        if (this.aliyunServerlessSparkParameters == null || !this.aliyunServerlessSparkParameters.checkParameters()) {
            throw new AliyunServerlessSparkTaskException("Aliyun-Serverless-Spark task parameters are not valid!");
        }

        ResourceParametersHelper resourceParametersHelper = taskExecutionContext.getResourceParametersHelper();
        DataSourceParameters dataSourceParameters = (DataSourceParameters) resourceParametersHelper
                .getResourceParameters(ResourceType.DATASOURCE, aliyunServerlessSparkParameters.getDatasource());
        aliyunServerlessSparkConnectionParam = (AliyunServerlessSparkConnectionParam) DataSourceUtils
                .buildConnectionParams(
                        DbType.valueOf(aliyunServerlessSparkParameters.getType()),
                        dataSourceParameters.getConnectionParams());

        accessKeyId = aliyunServerlessSparkConnectionParam.getAccessKeyId();
        accessKeySecret = aliyunServerlessSparkConnectionParam.getAccessKeySecret();
        regionId = aliyunServerlessSparkConnectionParam.getRegionId();
        endpoint = aliyunServerlessSparkConnectionParam.getEndpoint();

        try {
            aliyunServerlessSparkClient =
                    buildAliyunServerlessSparkClient(accessKeyId, accessKeySecret, regionId, endpoint);
        } catch (Exception e) {
            log.error("Failed to build Aliyun-Serverless-Spark client!", e);
            throw new AliyunServerlessSparkTaskException("Failed to build Aliyun-Serverless-Spark client!");
        }

        currentState = RunState.Submitted;
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            StartJobRunRequest startJobRunRequest = buildStartJobRunRequest(aliyunServerlessSparkParameters);
            RuntimeOptions runtime = new RuntimeOptions();
            Map<String, String> headers = new HashMap<>();
            StartJobRunResponse startJobRunResponse = aliyunServerlessSparkClient.startJobRunWithOptions(
                    aliyunServerlessSparkParameters.getWorkspaceId(), startJobRunRequest, headers, runtime);
            jobRunId = startJobRunResponse.getBody().getJobRunId();
            setAppIds(jobRunId);
            log.info("Successfully submitted serverless spark job, jobRunId - {}", jobRunId);

            while (!RunState.isFinal(currentState)) {
                GetJobRunRequest getJobRunRequest = buildGetJobRunRequest();
                GetJobRunResponse getJobRunResponse = aliyunServerlessSparkClient
                        .getJobRun(aliyunServerlessSparkParameters.getWorkspaceId(), jobRunId, getJobRunRequest);
                currentState = RunState.valueOf(getJobRunResponse.getBody().getJobRun().getState());
                log.info("job - {} state - {}", jobRunId, currentState);
                Thread.sleep(10 * 1000L);
            }

            setExitStatusCode(mapFinalStateToExitCode(currentState));

        } catch (Exception e) {
            log.error("Serverless spark job failed!", e);
            throw new AliyunServerlessSparkTaskException("Serverless spark job failed!");
        }
    }

    @Override
    public void submitApplication() throws TaskException {

    }

    @Override
    public void trackApplicationStatus() throws TaskException {

    }

    protected int mapFinalStateToExitCode(RunState state) {
        switch (state) {
            case Success:
                return TaskConstants.EXIT_CODE_SUCCESS;
            case Failed:
                return TaskConstants.EXIT_CODE_KILL;
            default:
                return TaskConstants.EXIT_CODE_FAILURE;
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return aliyunServerlessSparkParameters;
    }

    @Override
    public void cancelApplication() throws TaskException {
        CancelJobRunRequest cancelJobRunRequest = buildCancelJobRunRequest();
        try {
            aliyunServerlessSparkClient.cancelJobRun(aliyunServerlessSparkParameters.getWorkspaceId(), jobRunId,
                    cancelJobRunRequest);
        } catch (Exception e) {
            log.error("Failed to cancel serverless spark job run", e);
        }
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    protected Client buildAliyunServerlessSparkClient(String accessKeyId, String accessKeySecret,
                                                      String regionId, String endpoint) throws Exception {
        if (StringUtils.isEmpty(endpoint)) {
            endpoint = String.format(AliyunServerlessSparkConstants.ENDPOINT_TEMPLATE, regionId);
        }

        Config config = new Config()
                .setEndpoint(endpoint)
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        return new Client(config);
    }

    protected StartJobRunRequest buildStartJobRunRequest(AliyunServerlessSparkParameters aliyunServerlessSparkParameters) {
        StartJobRunRequest startJobRunRequest = new StartJobRunRequest();
        startJobRunRequest.setRegionId(regionId);
        startJobRunRequest.setResourceQueueId(aliyunServerlessSparkParameters.getResourceQueueId());
        startJobRunRequest.setCodeType(aliyunServerlessSparkParameters.getCodeType());
        startJobRunRequest.setName(aliyunServerlessSparkParameters.getJobName());
        String engineReleaseVersion = aliyunServerlessSparkParameters.getEngineReleaseVersion();
        engineReleaseVersion =
                StringUtils.isEmpty(engineReleaseVersion) ? AliyunServerlessSparkConstants.DEFAULT_ENGINE
                        : engineReleaseVersion;
        startJobRunRequest.setReleaseVersion(engineReleaseVersion);
        Tag envTag = new Tag();
        envTag.setKey(AliyunServerlessSparkConstants.ENV_KEY);
        String envType = aliyunServerlessSparkParameters.isProduction() ? AliyunServerlessSparkConstants.ENV_PROD
                : AliyunServerlessSparkConstants.ENV_DEV;
        envTag.setValue(envType);
        Tag workflowTag = new Tag();
        workflowTag.setKey(AliyunServerlessSparkConstants.WORKFLOW_KEY);
        workflowTag.setValue(AliyunServerlessSparkConstants.WORKFLOW_VALUE);
        startJobRunRequest.setTags(Arrays.asList(envTag, workflowTag));
        List<String> entryPointArguments =
                StringUtils.isEmpty(aliyunServerlessSparkParameters.getEntryPointArguments()) ? Collections.emptyList()
                        : Arrays.asList(aliyunServerlessSparkParameters.getEntryPointArguments()
                                .split(AliyunServerlessSparkConstants.ENTRY_POINT_ARGUMENTS_DELIMITER));
        JobDriver.JobDriverSparkSubmit jobDriverSparkSubmit = new JobDriver.JobDriverSparkSubmit()
                .setEntryPoint(aliyunServerlessSparkParameters.getEntryPoint())
                .setEntryPointArguments(entryPointArguments)
                .setSparkSubmitParameters(aliyunServerlessSparkParameters.getSparkSubmitParameters());
        JobDriver jobDriver = new com.aliyun.emr_serverless_spark20230808.models.JobDriver()
                .setSparkSubmit(jobDriverSparkSubmit);
        startJobRunRequest.setJobDriver(jobDriver);
        return startJobRunRequest;
    }

    protected GetJobRunRequest buildGetJobRunRequest() {
        GetJobRunRequest getJobRunRequest = new GetJobRunRequest();
        getJobRunRequest.setRegionId(regionId);
        return getJobRunRequest;
    }

    protected CancelJobRunRequest buildCancelJobRunRequest() {
        CancelJobRunRequest cancelJobRunRequest = new CancelJobRunRequest();
        cancelJobRunRequest.setRegionId(regionId);
        return cancelJobRunRequest;
    }
}
