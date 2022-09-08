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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    protected final Logger logger =
            LoggerFactory.getLogger(String.format(TaskConstants.TASK_LOG_LOGGER_NAME_FORMAT, getClass()));
    private static final String EXECUTING = "Executing";
    private static final String SUCCEEDED = "Succeeded";

    public PipelineId startPipelineExecution(AmazonSageMaker client, StartPipelineExecutionRequest request) {
        StartPipelineExecutionResult result = client.startPipelineExecution(request);
        String pipelineExecutionArn = result.getPipelineExecutionArn();
        String clientRequestToken = request.getClientRequestToken();
        logger.info("Start success, pipeline: {}, token: {}", pipelineExecutionArn, clientRequestToken);

        return new PipelineId(pipelineExecutionArn, clientRequestToken);
    }

    public void stopPipelineExecution(AmazonSageMaker client, PipelineId pipelineId) {
        StopPipelineExecutionRequest request = new StopPipelineExecutionRequest();
        request.setPipelineExecutionArn(pipelineId.getPipelineExecutionArn());
        request.setClientRequestToken(pipelineId.getClientRequestToken());

        StopPipelineExecutionResult result = client.stopPipelineExecution(request);
        logger.info("Stop pipeline: {} success", result.getPipelineExecutionArn());
    }

    public int checkPipelineExecutionStatus(AmazonSageMaker client, PipelineId pipelineId) {
        String pipelineStatus = describePipelineExecution(client, pipelineId);
        while (EXECUTING.equals(pipelineStatus)) {
            logger.info("check Pipeline Steps running");
            listPipelineExecutionSteps(client, pipelineId);
            ThreadUtils.sleep(SagemakerConstants.CHECK_PIPELINE_EXECUTION_STATUS_INTERVAL);
            describePipelineExecution(client, pipelineId);
        }

        int exitStatusCode = TaskConstants.EXIT_CODE_FAILURE;
        if (SUCCEEDED.equals(pipelineStatus)) {
            exitStatusCode = TaskConstants.EXIT_CODE_SUCCESS;
        }
        logger.info("PipelineExecutionStatus : {}, exitStatusCode: {}", pipelineStatus, exitStatusCode);
        return exitStatusCode;
    }

    private String describePipelineExecution(AmazonSageMaker client, PipelineId pipelineId) {
        DescribePipelineExecutionRequest request = new DescribePipelineExecutionRequest();
        request.setPipelineExecutionArn(pipelineId.getPipelineExecutionArn());
        DescribePipelineExecutionResult result = client.describePipelineExecution(request);
        logger.info("PipelineExecutionStatus: {}", result.getPipelineExecutionStatus());
        return result.getPipelineExecutionStatus();
    }

    private void listPipelineExecutionSteps(AmazonSageMaker client, PipelineId pipelineId) {
        ListPipelineExecutionStepsRequest request = new ListPipelineExecutionStepsRequest();
        request.setPipelineExecutionArn(pipelineId.getPipelineExecutionArn());
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PipelineId {

        private String pipelineExecutionArn;
        private String clientRequestToken;
    }
}
