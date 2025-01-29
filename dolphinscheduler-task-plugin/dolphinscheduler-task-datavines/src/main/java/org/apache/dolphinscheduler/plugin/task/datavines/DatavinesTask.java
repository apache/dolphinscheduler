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

package org.apache.dolphinscheduler.plugin.task.datavines;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.datavines.utils.RequestUtils;

import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;

@Slf4j
public class DatavinesTask extends AbstractRemoteTask {

    private final TaskExecutionContext taskExecutionContext;

    private DatavinesParameters datavinesParameters;
    private String jobExecutionId;
    private boolean executionStatus;

    protected DatavinesTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public void init() {
        final String taskParams = taskExecutionContext.getTaskParams();
        this.datavinesParameters = JSONUtils.parseObject(taskParams, DatavinesParameters.class);
        log.info("initialize datavines task params : {}", JSONUtils.toPrettyJsonString(datavinesParameters));
        if (this.datavinesParameters == null || !this.datavinesParameters.checkParameters()) {
            throw new DatavinesTaskException("datavines task params is not valid");
        }
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        super.handle(taskCallBack);
    }

    @Override
    public void submitApplication() throws TaskException {
        executeJob();
    }

    @Override
    public void trackApplicationStatus() throws TaskException {
        trackApplicationStatusInner();
    }

    private void executeJob() {
        try {
            String address = this.datavinesParameters.getAddress();
            String jobId = this.datavinesParameters.getJobId();
            String token = this.datavinesParameters.getToken();
            String apiResultDataKey = DatavinesTaskConstants.API_RESULT_DATA;
            JsonNode result = RequestUtils.executeJob(address, jobId, token);
            if (checkResult(result)) {
                jobExecutionId = result.get(apiResultDataKey).asText();
                executionStatus = true;
            }
        } catch (Exception ex) {
            Thread.currentThread().interrupt();
            log.error(DatavinesTaskConstants.SUBMIT_FAILED_MSG, ex);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException(DatavinesTaskConstants.SUBMIT_FAILED_MSG, ex);
        }
    }

    public void trackApplicationStatusInner() throws TaskException {
        try {
            String address = this.datavinesParameters.getAddress();
            if (executionStatus && jobExecutionId == null) {
                // Use address-taskId as app id
                setAppIds(String.format(DatavinesTaskConstants.APPIDS_FORMAT, address, this.jobExecutionId));
                setExitStatusCode(mapStatusToExitCode(false));
                log.info("datavines task failed.");
                return;
            }
            String apiResultDataKey = DatavinesTaskConstants.API_RESULT_DATA;
            boolean finishFlag = false;
            while (!finishFlag) {
                JsonNode jobExecutionStatus =
                        RequestUtils.getJobExecutionStatus(address, jobExecutionId,
                                this.datavinesParameters.getToken());
                if (!checkResult(jobExecutionStatus)) {
                    break;
                }
                String jobExecutionStatusStr = jobExecutionStatus.get(apiResultDataKey).asText();
                switch (jobExecutionStatusStr) {
                    case DatavinesTaskConstants.STATUS_SUCCESS:
                        setAppIds(String.format(DatavinesTaskConstants.APPIDS_FORMAT, address, this.jobExecutionId));
                        JsonNode jobExecutionResult =
                                RequestUtils.getJobExecutionResult(address, jobExecutionId,
                                        this.datavinesParameters.getToken());
                        if (!checkResult(jobExecutionResult)) {
                            break;
                        }

                        String jobExecutionResultStr = jobExecutionResult.get(apiResultDataKey).asText();
                        boolean checkResult = true;
                        if (this.datavinesParameters.isFailureBlock()) {
                            checkResult = DatavinesTaskConstants.STATUS_SUCCESS.equals(jobExecutionResultStr);
                        }

                        setExitStatusCode(mapStatusToExitCode(checkResult));
                        log.info("datavines task finished, execution status is {} and check result is {}",
                                jobExecutionStatusStr, jobExecutionResultStr);
                        finishFlag = true;
                        break;
                    case DatavinesTaskConstants.STATUS_FAILURE:
                    case DatavinesTaskConstants.STATUS_KILL:
                        errorHandle("task execution status: " + jobExecutionStatusStr);
                        finishFlag = true;
                        break;
                    default:
                        Thread.sleep(DatavinesTaskConstants.SLEEP_MILLIS);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error(DatavinesTaskConstants.TRACK_FAILED_MSG, ex);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException(DatavinesTaskConstants.TRACK_FAILED_MSG, ex);
        }
    }

    /**
     * map datavines task status to exitStatusCode
     *
     * @param status datavines job status
     * @return exitStatusCode
     */
    private int mapStatusToExitCode(boolean status) {
        if (status) {
            return EXIT_CODE_SUCCESS;
        } else {
            return EXIT_CODE_FAILURE;
        }
    }

    private boolean checkResult(JsonNode result) {
        boolean isCorrect = true;
        if (result instanceof MissingNode || result instanceof NullNode) {
            errorHandle(DatavinesTaskConstants.API_ERROR_MSG);
            isCorrect = false;
        } else if (result.get(DatavinesTaskConstants.API_RESULT_CODE)
                .asInt() != DatavinesTaskConstants.API_RESULT_CODE_SUCCESS) {
            errorHandle(result.get(DatavinesTaskConstants.API_RESULT_MSG));
            isCorrect = false;
        }
        return isCorrect;
    }

    private void errorHandle(Object msg) {
        setExitStatusCode(EXIT_CODE_FAILURE);
        log.error("datavines task execute failed with error: {}", msg);
    }

    @Override
    public AbstractParameters getParameters() {
        return datavinesParameters;
    }

    @Override
    public void cancelApplication() throws TaskException {
        String address = this.datavinesParameters.getAddress();
        log.info("trying terminate datavines task, taskId: {}, address: {}, taskId: {}",
                this.taskExecutionContext.getTaskInstanceId(),
                address,
                jobExecutionId);
        RequestUtils.killJobExecution(address, jobExecutionId, this.datavinesParameters.getToken());
        log.warn("datavines task terminated, taskId: {}, address: {}, jobExecutionId: {}",
                this.taskExecutionContext.getTaskInstanceId(),
                address,
                jobExecutionId);
    }
}
