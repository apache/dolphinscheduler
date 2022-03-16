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

package org.apache.dolphinscheduler.plugin.task.emr;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;
import static com.fasterxml.jackson.databind.MapperFeature.REQUIRE_SETTERS_FOR_GETTERS;

import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import java.util.HashSet;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.amazonaws.SdkBaseException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.ClusterState;
import com.amazonaws.services.elasticmapreduce.model.ClusterStateChangeReason;
import com.amazonaws.services.elasticmapreduce.model.ClusterStateChangeReasonCode;
import com.amazonaws.services.elasticmapreduce.model.ClusterStatus;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterResult;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowRequest;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;
import com.amazonaws.services.elasticmapreduce.model.TerminateJobFlowsRequest;
import com.amazonaws.services.elasticmapreduce.model.TerminateJobFlowsResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.collect.Sets;

public class EmrTask extends AbstractTaskExecutor {

    /**
     * taskExecutionContext
     */
    private final TaskExecutionContext taskExecutionContext;
    /**
     * emr parameters
     */
    private EmrParameters emrParameters;
    private AmazonElasticMapReduce emrClient;

    private String clusterId;

    private final HashSet<String> waitingStateSet = Sets.newHashSet(
        ClusterState.STARTING.toString(),
        ClusterState.BOOTSTRAPPING.toString(),
        ClusterState.RUNNING.toString()
    );

    /**
     * config ObjectMapper features and propertyNamingStrategy
     * use UpperCamelCaseStrategy support capital letters parse
     * @see PropertyNamingStrategy.UpperCamelCaseStrategy
     */
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
        .configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
        .configure(REQUIRE_SETTERS_FOR_GETTERS, true)
        .setTimeZone(TimeZone.getDefault())
        .setPropertyNamingStrategy(new PropertyNamingStrategy.UpperCamelCaseStrategy());

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    protected EmrTask(TaskExecutionContext taskExecutionContext) {

        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {

        final String taskParams = taskExecutionContext.getTaskParams();
        logger.info("emr task params:{}", taskParams);
        emrParameters = JSONUtils.parseObject(taskParams, EmrParameters.class);
        if (emrParameters == null || !emrParameters.checkParameters()) {
            throw new EmrTaskException("emr task params is not valid");
        }
        emrClient = createEmrClient();
    }

    @Override
    public void handle() throws InterruptedException {
        ClusterStatus clusterStatus = null;
        try {
            RunJobFlowRequest runJobFlowRequest = createRunJobFlowRequest();

            // submit runJobFlowRequest to aws
            RunJobFlowResult result = emrClient.runJobFlow(runJobFlowRequest);

            clusterId = result.getJobFlowId();
            // TODO: Failover on EMR Task type has not been implemented. In this time, DS only supports failover on yarn task type . Other task type, such as EMR task, k8s task not ready yet.
            setAppIds(clusterId);

            clusterStatus = getClusterStatus();

            while (waitingStateSet.contains(clusterStatus.getState())) {
                TimeUnit.SECONDS.sleep(10);
                clusterStatus = getClusterStatus();
            }

        } catch (EmrTaskException | SdkBaseException e) {
            logger.error("emr task submit failed with error", e);
        } finally {
            final int exitStatusCode = calculateExitStatusCode(clusterStatus);
            setExitStatusCode(exitStatusCode);
            logger.info("emr task finished with cluster status : {}", clusterStatus);
        }
    }

    /**
     * parse json string to RunJobFlowRequest
     *
     * @return RunJobFlowRequest
     */
    private RunJobFlowRequest createRunJobFlowRequest() {

        final RunJobFlowRequest runJobFlowRequest;
        try {
            runJobFlowRequest = objectMapper.readValue(emrParameters.getJobFlowDefineJson(), RunJobFlowRequest.class);
        } catch (JsonProcessingException e) {
            throw new EmrTaskException("can not parse RunJobFlowRequest from json", e);
        }

        return runJobFlowRequest;
    }

    /**
     * calculate task exitStatusCode
     *
     * @param clusterStatus aws emr cluster status
     * @return exitStatusCode
     */
    private int calculateExitStatusCode(ClusterStatus clusterStatus) {
        if (clusterStatus == null) {
            return TaskConstants.EXIT_CODE_FAILURE;
        } else {
            String state = clusterStatus.getState();
            ClusterStateChangeReason stateChangeReason = clusterStatus.getStateChangeReason();
            ClusterState clusterState = ClusterState.valueOf(state);
            switch (clusterState) {
                case WAITING:
                    return TaskConstants.EXIT_CODE_SUCCESS;
                case TERMINATED:
                case TERMINATING:
                    String code = stateChangeReason.getCode();
                    if (code != null && code.equalsIgnoreCase(ClusterStateChangeReasonCode.ALL_STEPS_COMPLETED.toString())) {
                        return TaskConstants.EXIT_CODE_SUCCESS;
                    } else {
                        return TaskConstants.EXIT_CODE_KILL;
                    }
                default:
                    return TaskConstants.EXIT_CODE_FAILURE;
            }
        }

    }

    private ClusterStatus getClusterStatus() {
        DescribeClusterRequest describeClusterRequest = new DescribeClusterRequest().withClusterId(clusterId);
        DescribeClusterResult result = emrClient.describeCluster(describeClusterRequest);
        if (result == null) {
            throw new EmrTaskException("fetch cluster status failed");
        }
        ClusterStatus clusterStatus = result.getCluster().getStatus();
        logger.info("emr cluster [clusterId:{}] running with status:{}", clusterId, clusterStatus);
        return clusterStatus;

    }

    @Override
    public AbstractParameters getParameters() {
        return emrParameters;
    }

    /**
     * create emr client from BasicAWSCredentials
     *
     * @return AmazonElasticMapReduce
     */
    private AmazonElasticMapReduce createEmrClient() {

        final String awsAccessKeyId = PropertyUtils.getString(TaskConstants.AWS_ACCESS_KEY_ID);
        final String awsSecretAccessKey = PropertyUtils.getString(TaskConstants.AWS_SECRET_ACCESS_KEY);
        final String awsRegion = PropertyUtils.getString(TaskConstants.AWS_REGION);
        final BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
        final AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(basicAWSCredentials);
        // create an EMR client
        return AmazonElasticMapReduceClientBuilder.standard()
            .withCredentials(awsCredentialsProvider)
            .withRegion(awsRegion)
            .build();
    }

    @Override
    public void cancelApplication(boolean status) throws Exception {
        super.cancelApplication(status);
        logger.info("trying terminate job flow, taskId:{}, clusterId:{}", this.taskExecutionContext.getTaskInstanceId(), clusterId);
        TerminateJobFlowsRequest terminateJobFlowsRequest = new TerminateJobFlowsRequest().withJobFlowIds(clusterId);
        TerminateJobFlowsResult terminateJobFlowsResult = emrClient.terminateJobFlows(terminateJobFlowsRequest);
        logger.info("the result of terminate job flow is:{}", terminateJobFlowsResult);
    }

}
