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
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_KILL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatavinesTask extends AbstractTask {

    private DatavinesParameters datavinesParameters;

    private DatavinesApiClient apiClient;

    private volatile boolean canceled;

    private volatile Thread executeThread;

    public DatavinesTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
    }

    @Override
    public void init() {
        datavinesParameters = JSONUtils.parseObject(taskRequest.getTaskParams(), DatavinesParameters.class);
        if (datavinesParameters == null) {
            throw new IllegalArgumentException("Datavines task params is not valid");
        }
        resolvePlaceholders();
        log.info("Initialize Datavines task params: {}", datavinesParameters);
        if (!datavinesParameters.checkParameters()) {
            throw new IllegalArgumentException("Datavines task params is not valid");
        }
        apiClient = new DatavinesApiClient(datavinesParameters);
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        executeThread = Thread.currentThread();
        try {
            long executionId = callWithRetry(apiClient::execute, "execute job");
            log.info("Datavines job submitted successfully, executionId: {}", executionId);

            String status = waitForCompletion(executionId);
            if (!DatavinesConstants.STATUS_SUCCESS.equalsIgnoreCase(status)) {
                setOutputParams(executionId, status);
                log.error("Datavines job execution {} finished with status: {}", executionId, status);
                setExitStatusCode(EXIT_CODE_FAILURE);
                return;
            }

            String result = callWithRetry(() -> apiClient.result(executionId), "fetch job result");
            setOutputParams(executionId, result);
            if (DatavinesConstants.STATUS_SUCCESS.equalsIgnoreCase(result)) {
                log.info("Datavines job execution {} succeeded", executionId);
                setExitStatusCode(EXIT_CODE_SUCCESS);
            } else {
                log.error("Datavines job execution {} failed, result: {}", executionId, result);
                setExitStatusCode(EXIT_CODE_FAILURE);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Datavines task has been interrupted or canceled");
            setExitStatusCode(EXIT_CODE_KILL);
        } catch (Exception e) {
            if (canceled) {
                log.warn("Datavines task has been canceled", e);
                setExitStatusCode(EXIT_CODE_KILL);
                return;
            }
            log.error("Datavines task execution failed", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("Execute Datavines task failed", e);
        } finally {
            executeThread = null;
        }
    }

    @Override
    public void cancel() throws TaskException {
        canceled = true;
        Thread thread = executeThread;
        if (thread != null) {
            thread.interrupt();
        }
        log.info("Datavines task cancel requested");
    }

    private String waitForCompletion(long executionId) throws InterruptedException {
        long deadline = System.currentTimeMillis() + datavinesParameters.getTimeout() * 1000L;
        while (true) {
            checkCanceled();
            if (System.currentTimeMillis() >= deadline) {
                throw new DatavinesApiException("Datavines job execution " + executionId
                        + " timed out after " + datavinesParameters.getTimeout() + " seconds", false);
            }
            String status = callWithRetry(() -> apiClient.status(executionId), "query job status");
            log.info("Datavines job execution {} status: {}", executionId, status);
            if (DatavinesConstants.STATUS_SUCCESS.equalsIgnoreCase(status)
                    || DatavinesConstants.STATUS_FAILURE.equalsIgnoreCase(status)
                    || DatavinesConstants.STATUS_CANCELED.equalsIgnoreCase(status)
                    || DatavinesConstants.STATUS_KILLED.equalsIgnoreCase(status)) {
                return status;
            }
            sleepSeconds(datavinesParameters.getInterval());
        }
    }

    private <T> T callWithRetry(Supplier<T> action, String actionName) throws InterruptedException {
        int retryTimes = datavinesParameters.getRetryTimes();
        for (int attempt = 0;; attempt++) {
            checkCanceled();
            try {
                return action.get();
            } catch (DatavinesApiException e) {
                if (!e.isRetryable() || attempt >= retryTimes) {
                    throw e;
                }
                log.warn("Datavines {} failed (attempt {}/{}): {}. Retrying in {} seconds...",
                        actionName, attempt + 1, retryTimes + 1, e.getMessage(), datavinesParameters.getInterval());
                sleepSeconds(datavinesParameters.getInterval());
            }
        }
    }

    private void sleepSeconds(int seconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + seconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            checkCanceled();
            long remaining = deadline - System.currentTimeMillis();
            Thread.sleep(Math.min(remaining, 500L));
        }
    }

    private void checkCanceled() throws InterruptedException {
        if (canceled || Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Datavines task canceled");
        }
    }

    private void setOutputParams(long executionId, String result) {
        Map<String, String> outputParams = new HashMap<>();
        outputParams.put(DatavinesConstants.OUT_EXECUTION_ID, String.valueOf(executionId));
        outputParams.put(DatavinesConstants.OUT_RESULT, result);

        datavinesParameters.addPropertyToValPool(buildOutputProperty(
                DatavinesConstants.OUT_EXECUTION_ID, String.valueOf(executionId)));
        datavinesParameters.addPropertyToValPool(buildOutputProperty(DatavinesConstants.OUT_RESULT, result));
        datavinesParameters.dealOutParam(outputParams);
        taskRequest.setVarPool(datavinesParameters.getVarPool());
    }

    private Property buildOutputProperty(String prop, String value) {
        Property property = new Property();
        property.setProp(prop);
        property.setDirect(Direct.OUT);
        property.setType(DataType.VARCHAR);
        property.setValue(value);
        return property;
    }

    private void resolvePlaceholders() {
        Map<String, String> prepareParamsMap = taskRequest.getPrepareParamsMap() == null
                ? Collections.<String, String>emptyMap()
                : ParameterUtils.convert(taskRequest.getPrepareParamsMap());
        datavinesParameters.setAddress(ParameterUtils.convertParameterPlaceholders(
                datavinesParameters.getAddress(), prepareParamsMap));
        datavinesParameters.setToken(ParameterUtils.convertParameterPlaceholders(
                datavinesParameters.getToken(), prepareParamsMap));
    }

    public AbstractParameters getParameters() {
        return datavinesParameters;
    }
}
