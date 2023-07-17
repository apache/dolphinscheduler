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

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.datafactory.DataFactoryManager;
import com.azure.resourcemanager.datafactory.models.CreateRunResponse;
import com.azure.resourcemanager.datafactory.models.PipelineResource;
import com.azure.resourcemanager.datafactory.models.PipelineRun;
import com.azure.resourcemanager.datafactory.models.PipelineRuns;

@Data
public class DatafactoryHook {

    public static DatafactoryStatus[] taskFinishFlags =
            {DatafactoryStatus.Failed, DatafactoryStatus.Succeeded, DatafactoryStatus.Cancelled};
    protected final Logger log =
            LoggerFactory.getLogger(DatafactoryHook.class);
    private final int QUERY_INTERVAL = PropertyUtils.getInt(TaskConstants.QUERY_INTERVAL, 10000);
    private DataFactoryManager client;
    private static AzureProfile profile;
    private static ClientSecretCredential credential;
    private String runId;

    public DatafactoryHook() {
        log.info("initDatafactoryClient ......");
        client = createClient();
    }

    protected static DataFactoryManager createClient() {
        final String AZURE_ACCESS_SUB_ID = PropertyUtils.getString(TaskConstants.AZURE_ACCESS_SUB_ID);
        final String AZURE_SECRET_TENANT_ID = PropertyUtils.getString(TaskConstants.AZURE_SECRET_TENANT_ID);
        final String AZURE_CLIENT_ID = PropertyUtils.getString(TaskConstants.AZURE_CLIENT_ID);
        final String AZURE_CLIENT_SECRET = PropertyUtils.getString(TaskConstants.AZURE_CLIENT_SECRET);
        profile =
                new AzureProfile(AZURE_SECRET_TENANT_ID, AZURE_ACCESS_SUB_ID, AzureEnvironment.AZURE);
        credential = new ClientSecretCredentialBuilder()
                .clientId(AZURE_CLIENT_ID)
                .clientSecret(AZURE_CLIENT_SECRET)
                .tenantId(AZURE_SECRET_TENANT_ID)
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
        return DataFactoryManager.authenticate(credential, profile);
    }

    public Boolean startDatafactoryTask(DatafactoryParameters parameters) {
        log.info("initDatafactoryTask ......");
        PipelineResource pipelineResource = getPipelineResource(parameters);
        if (pipelineResource == null) {
            return false;
        }
        log.info("startDatafactoryTask ......");
        CreateRunResponse run = pipelineResource.createRun();
        if (StringUtils.isEmpty(run.runId())) {
            return false;
        }
        runId = run.runId();
        parameters.setRunId(runId);
        return true;
    }

    public Boolean cancelDatafactoryTask(DatafactoryParameters parameters) {
        log.info("cancelTask ......");
        PipelineRuns pipelineRuns = client.pipelineRuns();
        try {
            pipelineRuns.cancel(parameters.getResourceGroupName(), parameters.getFactoryName(), runId);
        } catch (RuntimeException e) {
            log.error("failed to cancel datafactory task: " + e.getMessage());
            return false;
        }
        return true;
    }

    public DatafactoryStatus queryDatafactoryTaskStatus(DatafactoryParameters parameters) {
        log.info("queryDatafactoryTaskStatus ......");

        PipelineRuns pipelineRuns = client.pipelineRuns();
        PipelineRun pipelineRun =
                pipelineRuns.get(parameters.getResourceGroupName(), parameters.getFactoryName(), runId);

        if (pipelineRun != null) {
            log.info("queryDatafactoryTaskStatus ......{}", pipelineRun.status());
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
            DatafactoryStatus status = queryDatafactoryTaskStatus(parameters);

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
            log.debug("wait {}ms to recheck finish status....", QUERY_INTERVAL);
            Thread.sleep(QUERY_INTERVAL);
        }
        return false;
    }
}
