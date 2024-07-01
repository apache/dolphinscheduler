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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.service.CloudService;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.datafactory.DataFactoryManager;
import com.azure.resourcemanager.datafactory.models.Factories;
import com.azure.resourcemanager.datafactory.models.PipelineResource;
import com.azure.resourcemanager.resources.models.ResourceGroups;

@Service
public class CloudServiceImpl extends BaseServiceImpl implements CloudService {

    private static final String AZURE_CLIENT_ID = PropertyUtils.getString(TaskConstants.AZURE_CLIENT_ID);
    private static final String AZURE_CLIENT_SECRET = PropertyUtils.getString(TaskConstants.AZURE_CLIENT_SECRET);
    private static final String AZURE_ACCESS_SUB_ID = PropertyUtils.getString(TaskConstants.AZURE_ACCESS_SUB_ID);
    private static final String AZURE_SECRET_TENANT_ID = PropertyUtils.getString(TaskConstants.AZURE_SECRET_TENANT_ID);
    private static final AzureProfile profile =
            new AzureProfile(AZURE_SECRET_TENANT_ID, AZURE_ACCESS_SUB_ID, AzureEnvironment.AZURE);
    private static final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
            .clientId(AZURE_CLIENT_ID)
            .clientSecret(AZURE_CLIENT_SECRET)
            .tenantId(AZURE_SECRET_TENANT_ID)
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
    private static final DataFactoryManager manager;
    private static final AzureResourceManager azure;

    static {
        manager = DataFactoryManager.authenticate(clientSecretCredential, profile);
        azure = AzureResourceManager.authenticate(clientSecretCredential, profile).withDefaultSubscription();
    }

    @Override
    public List<String> listDataFactory(User loginUser) {
        Factories factories = manager.factories();
        List<String> names = new ArrayList<>();
        factories.list().stream().forEach(e -> names.add(e.name()));
        return names;
    }

    @Override
    public List<String> listResourceGroup(User loginUser) {
        ResourceGroups resourceGroups = azure.resourceGroups();
        List<String> names = new ArrayList<>();
        resourceGroups.list().stream().forEach(e -> names.add(e.name()));
        return names;
    }

    @Override
    public List<String> listPipeline(User loginUser, String factoryName, String resourceGroupName) {
        PagedIterable<PipelineResource> pipelineResources =
                manager.pipelines().listByFactory(resourceGroupName, factoryName);
        List<String> names = new ArrayList<>();
        pipelineResources.stream().forEach(e -> names.add(e.name()));
        return names;
    }
}
