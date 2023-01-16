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

package org.apache.dolphinscheduler.plugin.task.datafactory;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import lombok.Data;
import lombok.SneakyThrows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.datafactory.DataFactoryManager;
import com.azure.resourcemanager.datafactory.models.CreateRunResponse;
import com.azure.resourcemanager.datafactory.models.PipelineResource;
import com.azure.resourcemanager.datafactory.models.PipelineRun;
import com.azure.resourcemanager.datafactory.models.PipelineRuns;

@Data
public class DatafactoryHook {

    // public static DatafactoryStatus[] doneStatus =
    // {DatafactoryStatus.Canceling,DatafactoryStatus.InProgress,DatafactoryStatus.Queued};
    public static DatafactoryStatus[] taskFinishFlags =
            {DatafactoryStatus.Failed, DatafactoryStatus.Succeeded, DatafactoryStatus.Cancelled};
    protected final Logger logger =
            LoggerFactory.getLogger(String.format(TaskConstants.TASK_LOG_LOGGER_NAME_FORMAT, getClass()));
    private DataFactoryManager client;
    private AzureProfile profile;
    private TokenCredential credential;
    private String runId;

    public DatafactoryHook() {
        logger.info("initDatafactoryClient ......");
        client = createClient();
    }

    protected DataFactoryManager createClient() {
        final String AZURE_ACCESS_SUB_ID = PropertyUtils.getString(TaskConstants.AZURE_ACCESS_SUB_ID);
        final String AZURE_SECRET_TENANT_ID = PropertyUtils.getString(TaskConstants.AZURE_SECRET_TENANT_ID);
        profile = new AzureProfile(AZURE_SECRET_TENANT_ID, AZURE_ACCESS_SUB_ID, AzureEnvironment.AZURE);
        credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
        return DataFactoryManager.authenticate(credential, profile);
    }

    public Boolean startDatasyncTask(DatafactoryParameters parameters) {
        logger.info("initDatafactoryTask ......");
        PipelineResource pipelineResource = getPipelineResource(parameters);
        if (pipelineResource == null) {
            return false;
        }
        logger.info("startDatafactoryTask ......");
        CreateRunResponse run = pipelineResource.createRun();
        if (StringUtils.isEmpty(run.runId())) {
            return false;
        }
        runId = run.runId();
        parameters.setRunId(runId);
        return true;
    }

    public Boolean cancelDatasyncTask(DatafactoryParameters parameters) {
        logger.info("cancelTask ......");
        PipelineRuns pipelineRuns = client.pipelineRuns();
        try {
            pipelineRuns.cancel(parameters.getResourceGroupName(), parameters.getFactoryName(), parameters.getRunId());
        } catch (RuntimeException e) {
            logger.error("failed to cancel datafactory task: " + e.getMessage());
            return false;
        }
        return true;
    }

    public DatafactoryStatus queryDatasyncTaskStatus(DatafactoryParameters parameters) {
        logger.info("queryDatasyncTaskStatus ......");

        PipelineRuns pipelineRuns = client.pipelineRuns();
        PipelineRun pipelineRun =
                pipelineRuns.get(parameters.getResourceGroupName(), parameters.getFactoryName(), parameters.getRunId());

        if (pipelineRun != null) {
            logger.info("queryDatasyncTaskStatus ......{}", pipelineRun.status());
            return DatafactoryStatus.valueOf(pipelineRun.status());
        }
        return null;
    }

    private PipelineResource getPipelineResource(DatafactoryParameters parameters) {
        return client.pipelines().get(parameters.getResourceGroupName(), parameters.getFactoryName(),
                parameters.getPipelineName());
    }

    @SneakyThrows
    public Boolean queryStatus(DatafactoryParameters parameters) {
        List<DatafactoryStatus> stopStatusSet = Arrays.asList(taskFinishFlags);
        int maxRetry = 5;
        while (maxRetry > 0) {
            DatafactoryStatus status = queryDatasyncTaskStatus(parameters);

            if (status == null) {
                maxRetry--;
                continue;
            }

            if (stopStatusSet.contains(status)) {
                if (status.equals(DatafactoryStatus.Succeeded)) {
                    return true;
                }
                return false;
            }
            logger.debug("wait 10s to recheck finish status....");
            Thread.sleep(10000);
        }
        return false;
    }
}
