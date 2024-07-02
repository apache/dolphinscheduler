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

package org.apache.dolphinscheduler.plugin.task.aliyunadbspark;

import org.apache.dolphinscheduler.plugin.datasource.aliyunadbspark.AliyunAdbSparkClientWrapper;
import org.apache.dolphinscheduler.plugin.datasource.aliyunadbspark.param.AliyunAdbSparkConnectionParam;
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
public class AliyunAdbSparkBaseTask extends AbstractRemoteTask {

    public final TaskExecutionContext taskExecutionContext;

    private Client aliyunAdbSparkClient;

    private AliyunAdbSparkConnectionParam aliyunAdbSparkConnectionParam;

    private String applicationId;

    private String accessKeyId;

    private String accessKeySecret;

    private String regionId;

    private final HashSet<String> waitingStateSet = Sets.newHashSet(
            AliyunAdbSparkState.SUBMITTED.toString(),
            AliyunAdbSparkState.STARTING.toString(),
            AliyunAdbSparkState.RUNNING.toString(),
            AliyunAdbSparkState.FAILING.toString(),
            AliyunAdbSparkState.SUCCEEDING.toString(),
            AliyunAdbSparkState.KILLING.toString());

    protected AliyunAdbSparkBaseTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public AbstractParameters getParameters() {
        return null;
    }

    @Override
    public void submitApplication() throws TaskException {
        String appState = null;

        SubmitSparkAppRequest submitSparkAppRequest = buildSubmitSparkAppRequest();

        SubmitSparkAppResponse submitSparkAppResponse;
        try {
            submitSparkAppResponse = aliyunAdbSparkClient.submitSparkApp(submitSparkAppRequest);
            applicationId = submitSparkAppResponse.getBody().getData().getAppId();
            setAppIds(applicationId);
            log.info("Successfully submitted adb spark application, appId: {}", applicationId);

            appState = getSparkAppState();
        } catch (Exception e) {
            log.error("Failed to submit spark application", e);
            throw new AliyunAdbSparkTaskException(e.getMessage(), e.getCause());
        } finally {
            final int exitStatusCode = calculateExitStatusCode(appState);
            setExitStatusCode(exitStatusCode);
            log.info("adb spark task finished with state: {}", appState);
        }

    }

    @Override
    public void trackApplicationStatus() throws TaskException {
        String appState = null;
        try {
            appState = getSparkAppState();

            while (waitingStateSet.contains(appState)) {
                TimeUnit.SECONDS.sleep(10);
                appState = getSparkAppState();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AliyunAdbSparkTaskException("Failed to execute adb spark task", e);
        } catch (AliyunAdbSparkTaskException e) {
            // nothing to do
        } finally {
            final int exitStatusCode = calculateExitStatusCode(appState);
            setExitStatusCode(exitStatusCode);
            log.info("adb spark task finished with state: {}", appState);
        }
    }

    @Override
    public void cancelApplication() throws TaskException {
        log.info("trying cancel adb spark, taskId:{}, appId:{}", this.taskExecutionContext.getTaskInstanceId(),
                applicationId);
        KillSparkAppRequest killSparkAppRequest = new KillSparkAppRequest();
        killSparkAppRequest.setAppId(applicationId);

        try {
            aliyunAdbSparkClient.killSparkApp(killSparkAppRequest);
        } catch (Exception e) {
            log.error("failed to cancel adb spark application", e);
            throw new AliyunAdbSparkTaskException(e.getMessage(), e.getCause());
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
            AliyunAdbSparkState adbSparkState = AliyunAdbSparkState.fromValue(appState);
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

    protected <T extends AliyunAdbSparkBaseParameters> void init2(T params) {
        ResourceParametersHelper resourceParametersHelper = taskExecutionContext.getResourceParametersHelper();
        DataSourceParameters dataSourceParameters =
                (DataSourceParameters) resourceParametersHelper.getResourceParameters(ResourceType.DATASOURCE,
                        params.getDatasource());
        aliyunAdbSparkConnectionParam = (AliyunAdbSparkConnectionParam) DataSourceUtils.buildConnectionParams(
                DbType.valueOf(params.getType()),
                dataSourceParameters.getConnectionParams());

        accessKeyId = aliyunAdbSparkConnectionParam.getAliyunAccessKeyId();
        accessKeySecret = aliyunAdbSparkConnectionParam.getAliyunAccessKeySecret();
        regionId = aliyunAdbSparkConnectionParam.getAliyunRegionId();

        aliyunAdbSparkClient = createClient(accessKeyId, accessKeySecret, regionId);
    }

    protected Client createClient(String accessKeyId, String accessKeySecret, String regionId) {
        try (
                AliyunAdbSparkClientWrapper wrapper =
                        new AliyunAdbSparkClientWrapper(accessKeyId, accessKeySecret, regionId);) {
            return wrapper.getAliyunAdbSparkClient();
        } catch (Exception e) {
            log.error("Failed to initialize Aliyun ADB Spark Client", e);
            throw new AliyunAdbSparkTaskException(e.getMessage(), e.getCause());
        }
    }

    protected SubmitSparkAppRequest buildSubmitSparkAppRequest() {
        return null;
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
            getSparkAppStateResponse = aliyunAdbSparkClient.getSparkAppState(getSparkAppStateRequest);
        } catch (Exception e) {
            log.error("Failed to get spark application state", e);
            throw new AliyunAdbSparkTaskException(e.getMessage(), e.getCause());
        }
        String appState = getSparkAppStateResponse.getBody().getData().getState();
        log.info("ADB Spark Application [AppId:{}] with state: {}", applicationId, appState);
        return appState;
    }
}
