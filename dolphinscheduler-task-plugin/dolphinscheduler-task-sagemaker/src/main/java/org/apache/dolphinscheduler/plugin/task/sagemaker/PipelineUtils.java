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

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sagemaker.AmazonSageMaker;
import com.amazonaws.services.sagemaker.model.DescribePipelineExecutionRequest;
import com.amazonaws.services.sagemaker.model.DescribePipelineExecutionResult;
import com.amazonaws.services.sagemaker.model.ListPipelineExecutionStepsRequest;
import com.amazonaws.services.sagemaker.model.ListPipelineExecutionStepsResult;
import com.amazonaws.services.sagemaker.model.PipelineExecutionStep;
import com.amazonaws.services.sagemaker.model.StartPipelineExecutionRequest;
import com.amazonaws.services.sagemaker.model.StartPipelineExecutionResult;
import com.amazonaws.services.sagemaker.model.StopPipelineExecutionRequest;
import com.amazonaws.services.sagemaker.model.StopPipelineExecutionResult;

public class PipelineUtils {


    protected final Logger logger = LoggerFactory.getLogger(String.format(TaskConstants.TASK_LOG_LOGGER_NAME_FORMAT, getClass()));
    private final AmazonSageMaker client;
    private String pipelineExecutionArn;
    private String clientRequestToken;
    private String pipelineStatus;

    public PipelineUtils(AmazonSageMaker client) {
        this.client = client;
    }

    public int startPipelineExecution(StartPipelineExecutionRequest request) {
        int exitStatusCode = TaskConstants.EXIT_CODE_FAILURE;
        try {
            StartPipelineExecutionResult result = client.startPipelineExecution(request);
            pipelineExecutionArn = result.getPipelineExecutionArn();
            clientRequestToken = request.getClientRequestToken();
            exitStatusCode = TaskConstants.EXIT_CODE_SUCCESS;
            logger.info("Start pipeline: {} success", pipelineExecutionArn);
        } catch (Exception e) {
            logger.error("Start pipeline error: {}", e.getMessage());
        }

        return exitStatusCode;
    }

    public void stopPipelineExecution() {
        StopPipelineExecutionRequest request = new StopPipelineExecutionRequest();
        request.setPipelineExecutionArn(pipelineExecutionArn);
        request.setClientRequestToken(clientRequestToken);

        try {
            StopPipelineExecutionResult result = client.stopPipelineExecution(request);
            logger.info("Stop pipeline: {} success", result.getPipelineExecutionArn());
        } catch (Exception e) {
            logger.error("Stop pipeline error: {}", e.getMessage());
        }

    }

    public int checkPipelineExecutionStatus() {
        describePipelineExecution();
        while (pipelineStatus.equals("Executing")) {
            logger.info("check Pipeline Steps running");
            listPipelineExecutionSteps();
            ThreadUtils.sleep(SagemakerConstants.CHECK_PIPELINE_EXECUTION_STATUS_INTERVAL);
            describePipelineExecution();
        }

        int exitStatusCode = TaskConstants.EXIT_CODE_FAILURE;
        if (pipelineStatus.equals("Succeeded")) {
            exitStatusCode = TaskConstants.EXIT_CODE_SUCCESS;
        }
        logger.info("exit : {}", exitStatusCode);
        logger.info("PipelineExecutionStatus : {}", pipelineStatus);
        return exitStatusCode;
    }

    private void describePipelineExecution() {
        DescribePipelineExecutionRequest request = new DescribePipelineExecutionRequest();
        request.setPipelineExecutionArn(pipelineExecutionArn);
        DescribePipelineExecutionResult result = client.describePipelineExecution(request);
        pipelineStatus = result.getPipelineExecutionStatus();
        logger.info("PipelineExecutionStatus: {}", pipelineStatus);
    }

    private void listPipelineExecutionSteps() {
        ListPipelineExecutionStepsRequest request = new ListPipelineExecutionStepsRequest();
        request.setPipelineExecutionArn(pipelineExecutionArn);
        request.setMaxResults(SagemakerConstants.PIPELINE_MAX_RESULTS);
        ListPipelineExecutionStepsResult result = client.listPipelineExecutionSteps(request);
        List<PipelineExecutionStep> steps = result.getPipelineExecutionSteps();
        Collections.reverse(steps);
        logger.info("pipelineStepsStatus: ");
        for (PipelineExecutionStep step : steps) {
            String stepMessage = step.toString();
            logger.info(stepMessage);
        }
    }

    public String getPipelineExecutionArn() {
        return pipelineExecutionArn;
    }
}