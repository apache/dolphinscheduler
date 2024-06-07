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

import org.apache.dolphinscheduler.authentication.aws.AmazonSageMakerClientFactory;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.sagemaker.param.SagemakerConnectionParam;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.amazonaws.services.sagemaker.AmazonSageMaker;
import com.amazonaws.services.sagemaker.model.StartPipelineExecutionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * SagemakerTask task, Used to start Sagemaker pipeline
 */
@Slf4j
public class SagemakerTask extends AbstractRemoteTask {

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
            .configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            .configure(REQUIRE_SETTERS_FOR_GETTERS, true)
            .propertyNamingStrategy(new PropertyNamingStrategy.UpperCamelCaseStrategy())
            .build();
    /**
     * SageMaker parameters
     */
    private SagemakerParameters parameters;

    private AmazonSageMaker client;
    private PipelineUtils utils;
    private PipelineUtils.PipelineId pipelineId;
    private SagemakerConnectionParam sagemakerConnectionParam;
    private SagemakerTaskExecutionContext sagemakerTaskExecutionContext;
    private TaskExecutionContext taskExecutionContext;

    public SagemakerTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public void init() {

        parameters = JSONUtils.parseObject(taskRequest.getTaskParams(), SagemakerParameters.class);
        if (parameters == null) {
            throw new SagemakerTaskException("Sagemaker task params is empty");
        }
        if (!parameters.checkParameters()) {
            throw new SagemakerTaskException("Sagemaker task params is not valid");
        }
        sagemakerTaskExecutionContext =
                parameters.generateExtendedContext(taskExecutionContext.getResourceParametersHelper());
        sagemakerConnectionParam =
                (SagemakerConnectionParam) DataSourceUtils.buildConnectionParams(DbType.valueOf(parameters.getType()),
                        sagemakerTaskExecutionContext.getConnectionParams());
        parameters.setUsername(sagemakerConnectionParam.getUserName());
        parameters.setPassword(sagemakerConnectionParam.getPassword());
        parameters.setAwsRegion(sagemakerConnectionParam.getAwsRegion());
        log.info("Initialize Sagemaker task params {}", JSONUtils.toPrettyJsonString(parameters));

        client = createClient();
        utils = new PipelineUtils();
    }

    @Override
    public void submitApplication() throws TaskException {
        try {
            StartPipelineExecutionRequest request = createStartPipelineRequest();

            // Start pipeline
            pipelineId = utils.startPipelineExecution(client, request);

            // set AppId
            setAppIds(JSONUtils.toJsonString(pipelineId));
        } catch (Exception e) {
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("SageMaker task submit error", e);
        }
    }

    @Override
    public void cancelApplication() {
        initPipelineId();
        try {
            // stop pipeline
            utils.stopPipelineExecution(client, pipelineId);
        } catch (Exception e) {
            throw new TaskException("cancel application error", e);
        }
    }

    @Override
    public void trackApplicationStatus() throws TaskException {
        initPipelineId();
        // Keep checking the health status
        exitStatusCode = utils.checkPipelineExecutionStatus(client, pipelineId);
    }

    /**
     * init sagemaker applicationId if null
     */
    private void initPipelineId() {
        if (pipelineId == null) {
            if (StringUtils.isNotEmpty(getAppIds())) {
                pipelineId = JSONUtils.parseObject(getAppIds(), PipelineUtils.PipelineId.class);
            }
        }
        if (pipelineId == null) {
            throw new TaskException("sagemaker applicationID is null");
        }
    }

    public StartPipelineExecutionRequest createStartPipelineRequest() throws SagemakerTaskException {

        String requestJson = parameters.getSagemakerRequestJson();
        requestJson = parseRequstJson(requestJson);

        StartPipelineExecutionRequest startPipelineRequest;
        try {
            startPipelineRequest = objectMapper.readValue(requestJson, StartPipelineExecutionRequest.class);
        } catch (Exception e) {
            log.error("can not parse SagemakerRequestJson from json: {}", requestJson);
            throw new SagemakerTaskException("can not parse SagemakerRequestJson ", e);
        }

        log.info("Sagemaker task create StartPipelineRequest: {}", startPipelineRequest);
        return startPipelineRequest;
    }

    @Override
    public SagemakerParameters getParameters() {
        return parameters;
    }

    private String parseRequstJson(String requestJson) {
        Map<String, Property> paramsMap = taskRequest.getPrepareParamsMap();
        return ParameterUtils.convertParameterPlaceholders(requestJson, ParameterUtils.convert(paramsMap));
    }

    protected AmazonSageMaker createClient() {
        Map<String, String> awsProperties = PropertyUtils.getByPrefix("aws.sagemaker.", "");
        return AmazonSageMakerClientFactory.createAmazonSageMakerClient(awsProperties);
    }

}
