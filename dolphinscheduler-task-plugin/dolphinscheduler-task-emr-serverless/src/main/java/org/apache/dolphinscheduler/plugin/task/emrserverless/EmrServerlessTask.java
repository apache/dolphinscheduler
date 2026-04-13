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

package org.apache.dolphinscheduler.plugin.task.emrserverless;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;
import static com.fasterxml.jackson.databind.MapperFeature.REQUIRE_SETTERS_FOR_GETTERS;

import org.apache.dolphinscheduler.authentication.aws.AWSCredentialsProviderFactor;
import org.apache.dolphinscheduler.authentication.aws.AwsConfigurationKeys;
import org.apache.dolphinscheduler.common.constants.SystemConstants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import com.amazonaws.SdkBaseException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.emrserverless.AWSEMRServerless;
import com.amazonaws.services.emrserverless.AWSEMRServerlessClientBuilder;
import com.amazonaws.services.emrserverless.model.CancelJobRunRequest;
import com.amazonaws.services.emrserverless.model.CancelJobRunResult;
import com.amazonaws.services.emrserverless.model.GetJobRunRequest;
import com.amazonaws.services.emrserverless.model.GetJobRunResult;
import com.amazonaws.services.emrserverless.model.JobRun;
import com.amazonaws.services.emrserverless.model.JobRunState;
import com.amazonaws.services.emrserverless.model.StartJobRunRequest;
import com.amazonaws.services.emrserverless.model.StartJobRunResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.Sets;

/**
 * Amazon EMR Serverless Task.
 * <p>
 * Submits a job run to an EMR Serverless application and tracks it until completion.
 * Supports Spark and Hive job types.
 * </p>
 */
@Slf4j
public class EmrServerlessTask extends AbstractRemoteTask {

    /**
     * EMR Serverless job run states that indicate the job is still in progress.
     */
    private static final HashSet<String> WAITING_STATES = Sets.newHashSet(
            JobRunState.SUBMITTED.toString(),
            JobRunState.PENDING.toString(),
            JobRunState.SCHEDULED.toString(),
            JobRunState.RUNNING.toString());

    /**
     * ObjectMapper configured for AWS SDK request/response deserialization.
     */
    static final ObjectMapper objectMapper = JsonMapper.builder()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
            .configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            .configure(REQUIRE_SETTERS_FOR_GETTERS, true)
            .defaultTimeZone(SystemConstants.DEFAULT_TIME_ZONE)
            .propertyNamingStrategy(new PropertyNamingStrategies.UpperCamelCaseStrategy())
            .build();

    private final TaskExecutionContext taskExecutionContext;

    private EmrServerlessParameters emrServerlessParameters;

    private AWSEMRServerless emrServerlessClient;

    /**
     * jobRunId returned by StartJobRun or recovered from appIds
     */
    private String jobRunId;

    protected EmrServerlessTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        final String taskParams = taskExecutionContext.getTaskParams();
        emrServerlessParameters = JSONUtils.parseObject(taskParams, EmrServerlessParameters.class);
        log.info("Initialize EMR Serverless task params: {}", JSONUtils.toPrettyJsonString(taskParams));

        if (emrServerlessParameters == null || !emrServerlessParameters.checkParameters()) {
            throw new EmrServerlessTaskException("EMR Serverless task params are not valid");
        }

        emrServerlessClient = createEmrServerlessClient();
    }

    @Override
    public void submitApplication() throws TaskException {
        try {
            StartJobRunRequest request = buildStartJobRunRequest();

            log.info("Submitting EMR Serverless job run to application: {}",
                    emrServerlessParameters.getApplicationId());
            StartJobRunResult result = emrServerlessClient.startJobRun(request);

            jobRunId = result.getJobRunId();
            // Store jobRunId for failover recovery; applicationId is always available from parameters
            setAppIds(jobRunId);
            log.info("Successfully submitted EMR Serverless job run, jobRunId: {}", jobRunId);

        } catch (EmrServerlessTaskException | SdkBaseException e) {
            log.error("EMR Serverless task submit failed", e);
            throw new TaskException("EMR Serverless task submit failed", e);
        }
    }

    @Override
    public void trackApplicationStatus() throws TaskException {
        try {
            // Recover jobRunId from appIds if needed (failover case)
            if (StringUtils.isEmpty(jobRunId) && StringUtils.isNotEmpty(getAppIds())) {
                jobRunId = getAppIds();
                log.info("Recovered EMR Serverless jobRunId from appIds: {}", jobRunId);
            }

            if (StringUtils.isEmpty(jobRunId)) {
                throw new EmrServerlessTaskException("jobRunId is empty, cannot track application status");
            }

            String currentState = getJobRunState();
            while (WAITING_STATES.contains(currentState)) {
                TimeUnit.SECONDS.sleep(10);
                currentState = getJobRunState();
            }

            final int exitCode = mapStateToExitCode(currentState);
            setExitStatusCode(exitCode);
            log.info("EMR Serverless job run [{}] finished with state: {}, exitCode: {}",
                    jobRunId, currentState, exitCode);

        } catch (EmrServerlessTaskException | SdkBaseException e) {
            log.error("EMR Serverless task tracking failed", e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TaskException("EMR Serverless task tracking interrupted", e);
        }
    }

    @Override
    public void cancelApplication() throws TaskException {
        if (StringUtils.isEmpty(jobRunId)) {
            log.warn("jobRunId is empty, skip cancel");
            return;
        }
        log.info("Cancelling EMR Serverless job run, applicationId: {}, jobRunId: {}",
                emrServerlessParameters.getApplicationId(), jobRunId);
        try {
            CancelJobRunRequest request = new CancelJobRunRequest()
                    .withApplicationId(emrServerlessParameters.getApplicationId())
                    .withJobRunId(jobRunId);
            CancelJobRunResult result = emrServerlessClient.cancelJobRun(request);
            log.info("Cancel job run result: {}", result);
        } catch (SdkBaseException e) {
            throw new TaskException("Failed to cancel EMR Serverless job run", e);
        }
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public AbstractParameters getParameters() {
        return emrServerlessParameters;
    }

    /**
     * Build StartJobRunRequest from parameters and user-provided JSON.
     */
    private StartJobRunRequest buildStartJobRunRequest() {
        String startJobRunRequestJson;
        try {
            startJobRunRequestJson = ParameterUtils.convertParameterPlaceholders(
                    emrServerlessParameters.getStartJobRunRequestJson(),
                    ParameterUtils.convert(taskExecutionContext.getPrepareParamsMap()));
        } catch (Exception e) {
            throw new EmrServerlessTaskException("Failed to resolve parameter placeholders", e);
        }

        StartJobRunRequest request;
        try {
            request = objectMapper.readValue(startJobRunRequestJson, StartJobRunRequest.class);
        } catch (JsonProcessingException e) {
            throw new EmrServerlessTaskException(
                    "Cannot parse StartJobRunRequest from JSON: " + startJobRunRequestJson, e);
        }

        // Override applicationId and executionRoleArn from top-level parameters
        request.setApplicationId(emrServerlessParameters.getApplicationId());
        request.setExecutionRoleArn(emrServerlessParameters.getExecutionRoleArn());

        // Set job name if provided
        if (StringUtils.isNotEmpty(emrServerlessParameters.getJobName())) {
            request.setName(emrServerlessParameters.getJobName());
        } else {
            request.setName(taskExecutionContext.getTaskName());
        }

        // Set client token for idempotency
        request.setClientToken(taskExecutionContext.getTaskInstanceId() + "-" + System.currentTimeMillis());

        return request;
    }

    /**
     * Get the current state of the job run.
     */
    private String getJobRunState() {
        GetJobRunRequest request = new GetJobRunRequest()
                .withApplicationId(emrServerlessParameters.getApplicationId())
                .withJobRunId(jobRunId);
        GetJobRunResult result = emrServerlessClient.getJobRun(request);

        if (result == null || result.getJobRun() == null) {
            throw new EmrServerlessTaskException("Failed to get job run status");
        }

        JobRun jobRun = result.getJobRun();
        String state = jobRun.getState();
        log.info("EMR Serverless job run [applicationId:{}, jobRunId:{}] state: {}",
                emrServerlessParameters.getApplicationId(), jobRunId, state);
        return state;
    }

    /**
     * Map EMR Serverless job run final state to DolphinScheduler exit code.
     */
    private int mapStateToExitCode(String state) {
        if (state == null) {
            return TaskConstants.EXIT_CODE_FAILURE;
        }
        if (JobRunState.SUCCESS.toString().equals(state)) {
            return TaskConstants.EXIT_CODE_SUCCESS;
        } else if (JobRunState.CANCELLED.toString().equals(state)) {
            return TaskConstants.EXIT_CODE_KILL;
        } else {
            return TaskConstants.EXIT_CODE_FAILURE;
        }
    }

    /**
     * Create EMR Serverless client.
     * Strategy: try aws.emr.* config first, fallback to DefaultAWSCredentialsProviderChain.
     */
    protected AWSEMRServerless createEmrServerlessClient() {
        Map<String, String> awsProperties = PropertyUtils.getByPrefix("aws.emr.", "");
        AWSEMRServerlessClientBuilder builder = AWSEMRServerlessClientBuilder.standard();

        AWSCredentialsProvider credentialsProvider;
        try {
            credentialsProvider = AWSCredentialsProviderFactor.credentialsProvider(awsProperties);
            log.info("Using AWS credentials from aws.emr.* configuration");
        } catch (Exception e) {
            log.info("No valid aws.emr.* credentials config found, falling back to DefaultAWSCredentialsProviderChain");
            credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
        }
        builder.withCredentials(credentialsProvider);

        String region = awsProperties.get(AwsConfigurationKeys.AWS_REGION);

        // Support custom endpoint for testing with LocalStack or other AWS mocks
        String endpoint = PropertyUtils.getString("emr.serverless.endpoint", System.getenv("EMR_SERVERLESS_ENDPOINT"));
        if (StringUtils.isNotBlank(endpoint)) {
            log.info("Using custom EMR Serverless endpoint: {}", endpoint);
            if (StringUtils.isEmpty(region)) {
                region = "us-east-1"; // Default region for LocalStack
            }
            builder.withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(endpoint, region));
        } else if (StringUtils.isNotEmpty(region)) {
            builder.withRegion(region);
        }

        return builder.build();
    }
}
