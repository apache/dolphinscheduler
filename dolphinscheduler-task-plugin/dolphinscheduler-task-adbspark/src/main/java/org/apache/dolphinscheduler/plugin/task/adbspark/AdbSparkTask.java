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

package org.apache.dolphinscheduler.plugin.task.adbspark;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.adbspark.AdbSparkClientWrapper;
import org.apache.dolphinscheduler.plugin.datasource.adbspark.param.AdbSparkConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import com.aliyun.adb20211201.Client;
import com.aliyun.adb20211201.models.GetSparkAppStateRequest;
import com.aliyun.adb20211201.models.GetSparkAppStateResponse;
import com.aliyun.adb20211201.models.KillSparkAppRequest;
import com.aliyun.adb20211201.models.SubmitSparkAppRequest;
import com.aliyun.adb20211201.models.SubmitSparkAppResponse;
import com.google.common.collect.Sets;

@Slf4j
public class AdbSparkTask extends AbstractRemoteTask {

    public final TaskExecutionContext taskExecutionContext;

    private Client adbSparkClient;

    private AdbSparkConnectionParam adbSparkConnectionParam;

    private AdbSparkTaskParameters adbSparkTaskParameters;

    private String applicationId;

    private String accessKeyId;

    private String accessKeySecret;

    private String regionId;

    private final HashSet<String> waitingStateSet = Sets.newHashSet(
            AdbSparkState.SUBMITTED.toString(),
            AdbSparkState.STARTING.toString(),
            AdbSparkState.RUNNING.toString(),
            AdbSparkState.FAILING.toString(),
            AdbSparkState.SUCCEEDING.toString(),
            AdbSparkState.KILLING.toString());

    protected AdbSparkTask(TaskExecutionContext taskExecutionContext) {
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
        adbSparkTaskParameters = JSONUtils.parseObject(taskParams, AdbSparkTaskParameters.class);

        if (this.adbSparkTaskParameters == null || !this.adbSparkTaskParameters.checkParameters()) {
            throw new AdbSparkTaskException("Task parameters for aliyun adb spark application are invalid");
        }

        ResourceParametersHelper resourceParametersHelper = taskExecutionContext.getResourceParametersHelper();
        DataSourceParameters dataSourceParameters =
                (DataSourceParameters) resourceParametersHelper.getResourceParameters(ResourceType.DATASOURCE,
                        adbSparkTaskParameters.getDatasource());
        adbSparkConnectionParam = (AdbSparkConnectionParam) DataSourceUtils.buildConnectionParams(
                DbType.valueOf(adbSparkTaskParameters.getType()),
                dataSourceParameters.getConnectionParams());

        accessKeyId = adbSparkConnectionParam.getAliyunAccessKeyId();
        accessKeySecret = adbSparkConnectionParam.getAliyunAccessKeySecret();
        regionId = adbSparkConnectionParam.getAliyunRegionId();

        adbSparkClient = createClient(accessKeyId, accessKeySecret, regionId);
    }

    @Override
    public AbstractParameters getParameters() {
        return adbSparkTaskParameters;
    }

    @Override
    public void submitApplication() throws TaskException {
        String appState = null;

        SubmitSparkAppRequest submitSparkAppRequest = buildSubmitSparkAppRequest();

        SubmitSparkAppResponse submitSparkAppResponse;
        try {
            submitSparkAppResponse = adbSparkClient.submitSparkApp(submitSparkAppRequest);
            applicationId = submitSparkAppResponse.getBody().getData().getAppId();
            setAppIds(applicationId);
            log.info("Successfully submitted adb spark application, appId: {}", applicationId);

            appState = getSparkAppState();
        } catch (Exception e) {
            log.error("Failed to submit spark application", e);
            throw new AdbSparkTaskException(e.getMessage(), e.getCause());
        } finally {
            final int exitStatusCode = calculateExitStatusCode(appState);
            setExitStatusCode(exitStatusCode);
            log.info("adb spark task finished with state: {}", appState);
        }

    }

    @Override
    public void trackApplicationStatus() throws TaskException {
        if (StringUtils.isBlank(applicationId)) {
            log.warn("Try to track no-specified adb spark application");
            return;
        }

        String appState = null;
        try {
            appState = getSparkAppState();

            while (waitingStateSet.contains(appState)) {
                TimeUnit.SECONDS.sleep(10);
                appState = getSparkAppState();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AdbSparkTaskException("Failed to execute adb spark task", e);
        } catch (AdbSparkTaskException e) {
            // nothing to do
        } finally {
            final int exitStatusCode = calculateExitStatusCode(appState);
            setExitStatusCode(exitStatusCode);
            log.info("adb spark task finished with state: {}", appState);
        }
    }

    @Override
    public void cancelApplication() throws TaskException {
        if (StringUtils.isBlank(applicationId)) {
            log.warn("Try to kill no-specified adb spark application");
            return;
        }

        log.info("trying cancel adb spark, taskId:{}, appId:{}", this.taskExecutionContext.getTaskInstanceId(),
                applicationId);
        KillSparkAppRequest killSparkAppRequest = new KillSparkAppRequest();
        killSparkAppRequest.setAppId(applicationId);

        try {
            adbSparkClient.killSparkApp(killSparkAppRequest);
        } catch (Exception e) {
            log.error("failed to cancel adb spark application", e);
            throw new AdbSparkTaskException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Calculates the exit status code based on the application state.
     * @param appState adb spark application status
     * @return exitStatusCode
     */
    private int calculateExitStatusCode(String appState) {
        if (StringUtils.isBlank(appState)) {
            return TaskConstants.EXIT_CODE_FAILURE;
        } else {
            AdbSparkState adbSparkState = AdbSparkState.fromValue(appState);
            switch (adbSparkState) {
                case KILLED:
                    return TaskConstants.EXIT_CODE_KILL;
                case FAILED:
                case FATAL:
                case UNKNOWN:
                    return TaskConstants.EXIT_CODE_FAILURE;
                default:
                    return TaskConstants.EXIT_CODE_SUCCESS;
            }
        }
    }

    protected Client createClient(String accessKeyId, String accessKeySecret, String regionId) {
        try (
                AdbSparkClientWrapper wrapper =
                        new AdbSparkClientWrapper(accessKeyId, accessKeySecret, regionId)) {
            return wrapper.getAdbSparkClient();
        } catch (Exception e) {
            log.error("Failed to initialize Aliyun ADB Spark Client", e);
            throw new AdbSparkTaskException(e.getMessage(), e.getCause());
        }
    }

    protected SubmitSparkAppRequest buildSubmitSparkAppRequest() {
        SubmitSparkAppRequest submitSparkAppRequest = new SubmitSparkAppRequest();

        // Set necessary parameters
        submitSparkAppRequest.setDBClusterId(adbSparkTaskParameters.getDbClusterId());
        submitSparkAppRequest.setResourceGroupName(adbSparkTaskParameters.getResourceGroupName());
        submitSparkAppRequest.setData(adbSparkTaskParameters.getData());

        // Set optional parameters if they are not empty
        if (StringUtils.isNotBlank(adbSparkTaskParameters.getAppName())) {
            submitSparkAppRequest.setAppName(adbSparkTaskParameters.getAppName());
        }
        if (StringUtils.isNotBlank(adbSparkTaskParameters.getAppType())) {
            submitSparkAppRequest.setAppType(adbSparkTaskParameters.getAppType());
        }

        return submitSparkAppRequest;
    }

    /**
     * Get Spark Application State
     *
     * @return adb spark application state
     */
    private String getSparkAppState() {
        GetSparkAppStateRequest getSparkAppStateRequest = new GetSparkAppStateRequest();
        getSparkAppStateRequest.setAppId(applicationId);

        GetSparkAppStateResponse getSparkAppStateResponse;
        try {
            getSparkAppStateResponse = adbSparkClient.getSparkAppState(getSparkAppStateRequest);
        } catch (Exception e) {
            log.error("Failed to get spark application state", e);
            throw new AdbSparkTaskException(e.getMessage(), e.getCause());
        }
        String appState = getSparkAppStateResponse.getBody().getData().getState();
        log.info("ADB Spark Application [AppId:{}] with state: {}", applicationId, appState);
        return appState;
    }
}
