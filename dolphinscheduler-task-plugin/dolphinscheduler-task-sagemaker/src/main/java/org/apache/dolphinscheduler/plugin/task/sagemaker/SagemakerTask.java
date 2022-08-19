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

package org.apache.dolphinscheduler.plugin.task.sagemaker;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;
import static com.fasterxml.jackson.databind.MapperFeature.REQUIRE_SETTERS_FOR_GETTERS;

import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import java.util.Map;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sagemaker.AmazonSageMaker;
import com.amazonaws.services.sagemaker.AmazonSageMakerClientBuilder;
import com.amazonaws.services.sagemaker.model.StartPipelineExecutionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

/**
 * SagemakerTask task, Used to start Sagemaker pipeline
 */
public class SagemakerTask extends AbstractTaskExecutor {

    private static final ObjectMapper objectMapper =
        new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false).configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true).configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            .configure(REQUIRE_SETTERS_FOR_GETTERS, true).setPropertyNamingStrategy(new PropertyNamingStrategy.UpperCamelCaseStrategy());
    /**
     * taskExecutionContext
     */
    private final TaskExecutionContext taskExecutionContext;
    /**
     * SageMaker parameters
     */
    private SagemakerParameters parameters;
    private PipelineUtils utils;

    public SagemakerTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;

    }

    @Override
    public void init() {
        logger.info("Sagemaker task params {}", taskExecutionContext.getTaskParams());

        parameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SagemakerParameters.class);

        if (!parameters.checkParameters()) {
            throw new SagemakerTaskException("Sagemaker task params is not valid");
        }

    }

    @Override
    public void handle() throws SagemakerTaskException {
        try {
            int exitStatusCode = handleStartPipeline();
            setExitStatusCode(exitStatusCode);
        } catch (Exception e) {
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new SagemakerTaskException("SageMaker task error", e);
        }
    }

    @Override
    public void cancelApplication(boolean cancelApplication) {
        // stop pipeline
        utils.stopPipelineExecution();
    }

    public int handleStartPipeline() {
        int exitStatusCode;
        StartPipelineExecutionRequest request = createStartPipelineRequest();

        try {
            AmazonSageMaker client = createClient();
            utils = new PipelineUtils(client);
            setAppIds(utils.getPipelineExecutionArn());
        } catch (Exception e) {
            throw new SagemakerTaskException("can not connect aws ", e);
        }

        // Start pipeline
        exitStatusCode = utils.startPipelineExecution(request);
        if (exitStatusCode == TaskConstants.EXIT_CODE_SUCCESS) {
            // Keep checking the health status
            exitStatusCode = utils.checkPipelineExecutionStatus();
        }
        return exitStatusCode;
    }

    public StartPipelineExecutionRequest createStartPipelineRequest() throws SagemakerTaskException {

        String requestJson = parameters.getSagemakerRequestJson();
        requestJson = parseRequstJson(requestJson);

        StartPipelineExecutionRequest startPipelineRequest;
        try {
            startPipelineRequest = objectMapper.readValue(requestJson, StartPipelineExecutionRequest.class);
        } catch (Exception e) {
            logger.error("can not parse SagemakerRequestJson from json: {}", requestJson);
            throw new SagemakerTaskException("can not parse SagemakerRequestJson ", e);
        }

        logger.info("Sagemaker task create StartPipelineRequest: {}", startPipelineRequest);
        return startPipelineRequest;
    }

    @Override
    public SagemakerParameters getParameters() {
        return parameters;
    }

    private String parseRequstJson(String requestJson) {
        // combining local and global parameters
        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
        return ParameterUtils.convertParameterPlaceholders(requestJson, ParamUtils.convert(paramsMap));
    }

    private AmazonSageMaker createClient() {
        final String awsAccessKeyId = PropertyUtils.getString(TaskConstants.AWS_ACCESS_KEY_ID);
        final String awsSecretAccessKey = PropertyUtils.getString(TaskConstants.AWS_SECRET_ACCESS_KEY);
        final String awsRegion = PropertyUtils.getString(TaskConstants.AWS_REGION);
        final BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
        final AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(basicAWSCredentials);
        // create a SageMaker client
        return AmazonSageMakerClientBuilder.standard()
            .withCredentials(awsCredentialsProvider)
            .withRegion(awsRegion)
            .build();
    }

}
