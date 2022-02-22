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

import static org.apache.dolphinscheduler.spi.task.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.EXIT_CODE_KILL;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.EXIT_CODE_SUCCESS;

import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.TaskConstants;
import org.apache.dolphinscheduler.spi.task.request.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import java.util.concurrent.TimeUnit;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.CancelStepsRequest;
import com.amazonaws.services.elasticmapreduce.model.ClusterStateChangeReason;
import com.amazonaws.services.elasticmapreduce.model.ClusterStatus;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterResult;
import com.amazonaws.services.elasticmapreduce.model.ListStepsRequest;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowRequest;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;

public class EmrTask extends AbstractTaskExecutor {

    /**
     * taskExecutionContext
     */
    private final TaskRequest taskExecutionContext;
    /**
     * emr parameters
     */
    private EmrParameters emrParameters;
    private AmazonElasticMapReduce emrClient;

    private String clusterId;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    protected EmrTask(TaskRequest taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;

    }

    @Override
    public void init() {
        logger.info("emr task params:{}", taskExecutionContext.getTaskParams());
        emrParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), EmrParameters.class);
        if (emrParameters == null || !emrParameters.checkParameters()) {
            throw new RuntimeException("emr task params is not valid");
        }
        emrClient = getEmrClient();

    }

    @Override
    public void handle() {
        try {
            RunJobFlowRequest runJobFlowRequest = getRunJobFlowRequest();

            // submit runJobFlowRequest to aws
            RunJobFlowResult result = emrClient.runJobFlow(runJobFlowRequest);

            clusterId = result.getJobFlowId();

            ClusterStatus clusterStatus = getClusterStatus();
            while (ClusterStateEnum.valueOf(clusterStatus.getState()).ordinal() <= 2) {
                logger.info("cluster running with status:{}", clusterStatus);
                TimeUnit.SECONDS.sleep(10);
                clusterStatus = getClusterStatus();
            }

            calculateExitStatusCode(clusterStatus);

            logger.info("emr task finished with status:{}", clusterStatus);
        } catch (Exception e) {
            exitStatusCode = EXIT_CODE_FAILURE;
            logger.error("emr task submit failed with error", e);
        }
    }

    private RunJobFlowRequest getRunJobFlowRequest() throws Exception {
        RunJobFlowRequest runJobFlowRequest = JSONUtils.parseObject(emrParameters.getJobFlowDefineJson(), RunJobFlowRequest.class);
        if (runJobFlowRequest == null) {
            throw new Exception("can not parse RunJobFlowRequest from json");
        }
        return runJobFlowRequest;
    }

    /**
     * calculate task exitStatusCode
     *
     * @param clusterStatus aws emr cluster status
     */
    private void calculateExitStatusCode(ClusterStatus clusterStatus) {
        if (clusterStatus != null) {
            String state = clusterStatus.getState();
            ClusterStateChangeReason stateChangeReason = clusterStatus.getStateChangeReason();
            ClusterStateEnum stateEnum = ClusterStateEnum.valueOf(state);
            switch (stateEnum) {
                case TERMINATED:
                case TERMINATING:
                    if (stateChangeReason.getCode().equalsIgnoreCase("ALL_STEPS_COMPLETED")) {
                        exitStatusCode = EXIT_CODE_SUCCESS;
                    } else {
                        exitStatusCode = EXIT_CODE_KILL;
                    }

                    break;
                case TERMINATED_WITH_ERRORS:
                    exitStatusCode = EXIT_CODE_FAILURE;
                    break;
                default:
                    exitStatusCode = EXIT_CODE_SUCCESS;
                    break;
            }
        } else {
            exitStatusCode = EXIT_CODE_FAILURE;
        }
    }

    private ClusterStatus getClusterStatus() throws Exception {
        DescribeClusterRequest describeClusterRequest = new DescribeClusterRequest().withClusterId(clusterId);
        DescribeClusterResult result = emrClient.describeCluster(describeClusterRequest);
        if (result != null) {
            return result.getCluster().getStatus();
        } else {
            throw new Exception("fetch cluster status failed");
        }

    }

    @Override
    public AbstractParameters getParameters() {
        return emrParameters;
    }

    private AmazonElasticMapReduce getEmrClient() {

        String awsNamedProfilesPath = PropertyUtils.getString(TaskConstants.AWS_NAMED_PROFILES_PATH);
        String profileName = emrParameters.getProfileName();
        // specifies  named profile in  aws named profiles file path as the credentials provider
        AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(awsNamedProfilesPath, profileName);

        // create an EMR client using the credentials and region specified in order to create the cluster
        return AmazonElasticMapReduceClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion(emrParameters.getRegion())
            .build();
    }

    @Override
    public void cancelApplication(boolean status) throws Exception {
        super.cancelApplication(status);
        ListStepsRequest listStepsRequest = new ListStepsRequest().withClusterId(clusterId);
        CancelStepsRequest cancelStepsRequest = new CancelStepsRequest().withClusterId(clusterId)
            .withStepIds(listStepsRequest.getStepIds());
        emrClient.cancelSteps(cancelStepsRequest);
    }

    private enum ClusterStateEnum {
        STARTING,
        BOOTSTRAPPING,
        RUNNING,
        WAITING,
        TERMINATING,
        TERMINATED,
        TERMINATED_WITH_ERRORS

    }
}
