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
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import com.aliyun.emr_serverless_spark20230808.Client;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

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

    private String jobRunId;

    private RunState previousState;

    private RunState currentState;

    protected AliyunServerlessSparkTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        final String taskParams = taskExecutionContext.getTaskParams();
        aliyunServerlessSparkParameters = JSONUtils.parseObject(taskParams, AliyunServerlessSparkParameters.class);
        if (this.aliyunServerlessSparkParameters == null || !this.aliyunServerlessSparkParameters.checkParameters()) {
            throw new AliyunServerlessSparkTaskException("Aliyun-Serverless-Spark task parameters are not valid!");
        }

        String accessKeyId = aliyunServerlessSparkParameters.getAccessKeyId();
        String accessKeySecret = aliyunServerlessSparkParameters.getAccessKeySecret();
        String regionId = aliyunServerlessSparkParameters.getRegionId();
        try {
            aliyunServerlessSparkClient = buildAliyunServerlessSparkClient(accessKeyId, accessKeySecret, regionId);
        } catch (Exception e) {
            log.error("Failed to build Aliyun-Serverless-Spark client!", e);
            throw new AliyunServerlessSparkTaskException("Failed to build Aliyun-Serverless-Spark client!");
        }

        previousState = RunState.Submitted;
        currentState = RunState.Submitted;
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            StartJobRunRequest startJobRunRequest = buildStartJobRunRequest(aliyunServerlessSparkParameters);
            RuntimeOptions runtime = new RuntimeOptions();
            Map<String, String> headers = new HashMap<>();
            StartJobRunResponse startJobRunResponse = aliyunServerlessSparkClient.startJobRunWithOptions(aliyunServerlessSparkParameters.getWorkspaceId(), startJobRunRequest, headers, runtime);
            jobRunId = startJobRunResponse.getBody().getJobRunId();
            setAppIds(jobRunId);
            log.info("Successfully submitted serverless spark job, jobRunId - {}", jobRunId);

            // todo: deal with null
            while(!RunState.isFinal(currentState)) {
                GetJobRunRequest getJobRunRequest = buildGetJobRunRequest(aliyunServerlessSparkParameters);
                GetJobRunResponse getJobRunResponse = aliyunServerlessSparkClient.getJobRun(aliyunServerlessSparkParameters.getWorkspaceId(), jobRunId, getJobRunRequest);
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

    private int mapFinalStateToExitCode(RunState state) {
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

    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    private Client buildAliyunServerlessSparkClient(String accessKeyId, String accessKeySecret, String regionId) throws Exception {
        String endpoint = String.format("emr-serverless-spark.%s.aliyuncs.com", regionId);
        Config config = new Config()
            .setEndpoint(endpoint)
            .setAccessKeyId(accessKeyId)
            .setAccessKeySecret(accessKeySecret);
        return new Client(config);
    }

    private StartJobRunRequest buildStartJobRunRequest(AliyunServerlessSparkParameters aliyunServerlessSparkParameters) {
        StartJobRunRequest startJobRunRequest = new StartJobRunRequest();
        startJobRunRequest.setRegionId(aliyunServerlessSparkParameters.getRegionId());
        startJobRunRequest.setResourceQueueId(aliyunServerlessSparkParameters.getResourceQueueId());
        startJobRunRequest.setCodeType(aliyunServerlessSparkParameters.getCodeType());
        startJobRunRequest.setName(aliyunServerlessSparkParameters.getJobName());
        String engineReleaseVersion = aliyunServerlessSparkParameters.getEngineReleaseVersion();
        engineReleaseVersion = StringUtils.isEmpty(engineReleaseVersion) ? "esr-2.1-native (Spark 3.3.1, Scala 2.12, Native Runtime)" : engineReleaseVersion;
        startJobRunRequest.setReleaseVersion(engineReleaseVersion);
        Tag tag = new Tag();
        tag.setKey("environment");
        String envType = aliyunServerlessSparkParameters.isProduction() ? "production" : "dev";
        tag.setValue(envType);
        startJobRunRequest.setTags(Collections.singletonList(tag));
        List<String> entryPointArguments = StringUtils.isEmpty(aliyunServerlessSparkParameters.getEntryPointArguments()) ?
            Collections.emptyList() : Arrays.asList(aliyunServerlessSparkParameters.getEntryPointArguments().split(";"));
        JobDriver.JobDriverSparkSubmit jobDriverSparkSubmit = new JobDriver.JobDriverSparkSubmit()
            .setEntryPoint(aliyunServerlessSparkParameters.getEntryPoint())
            .setEntryPointArguments(entryPointArguments)
            .setSparkSubmitParameters(aliyunServerlessSparkParameters.getSparkSubmitParameters());
        JobDriver jobDriver = new com.aliyun.emr_serverless_spark20230808.models.JobDriver()
            .setSparkSubmit(jobDriverSparkSubmit);
        startJobRunRequest.setJobDriver(jobDriver);
        return startJobRunRequest;
    }

    private GetJobRunRequest buildGetJobRunRequest(AliyunServerlessSparkParameters aliyunServerlessSparkParameters) {
        GetJobRunRequest getJobRunRequest = new GetJobRunRequest();
        getJobRunRequest.setRegionId(aliyunServerlessSparkParameters.getRegionId());
        return getJobRunRequest;
    }
}
