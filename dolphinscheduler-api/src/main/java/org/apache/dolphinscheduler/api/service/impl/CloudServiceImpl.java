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

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.datafactory.DataFactoryManager;
import com.azure.resourcemanager.datafactory.models.Factories;
import com.azure.resourcemanager.datafactory.models.PipelineResource;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import org.apache.dolphinscheduler.api.configuration.TaskTypeConfiguration;
import org.apache.dolphinscheduler.api.dto.FavTaskDto;
import org.apache.dolphinscheduler.api.service.CloudService;
import org.apache.dolphinscheduler.api.service.FavTaskService;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.FavTask;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.FavTaskMapper;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CloudServiceImpl extends BaseServiceImpl implements CloudService {

    private static final String ACCESS_KEY_ID = PropertyUtils.getString(TaskConstants.AZURE_ACCESS_KEY_ID);
    private static final String SECRET_ACCESS_KEY = PropertyUtils.getString(TaskConstants.AZURE_SECRET_ACCESS_KEY);
    private static final String AZURE_ACCESS_SUB_ID = PropertyUtils.getString(TaskConstants.AZURE_ACCESS_SUB_ID);
    private static final String AZURE_SECRET_TENANT_ID = PropertyUtils.getString(TaskConstants.AZURE_SECRET_TENANT_ID);
    private static final AzureProfile profile = new AzureProfile(AZURE_SECRET_TENANT_ID,AZURE_ACCESS_SUB_ID,AzureEnvironment.AZURE);
    private static final TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
    private static final DataFactoryManager manager;
    private static final AzureResourceManager azure;

    static {
        manager = DataFactoryManager.authenticate(credential, profile);
        azure = AzureResourceManager.authenticate(credential, profile).withDefaultSubscription();
    }
    @Override
    public List<String> listDataFactory(User loginUser) {
        /*AzureProfile profile = new AzureProfile(AZURE_SECRET_TENANT_ID,AZURE_ACCESS_SUB_ID,AzureEnvironment.AZURE);

        TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
        DataFactoryManager manager = DataFactoryManager.authenticate(credential, profile);*/

        System.out.println("~~~~~~~~~~~~~~print all factories:");
        Factories factories = manager.factories();
        List<String> names =new ArrayList<>();
        factories.list().stream().forEach(e->names.add(e.name()));
        return names;
    }

    @Override
    public List<String> listResourceGroup(User loginUser) {
        /*AzureProfile profile = new AzureProfile(AZURE_SECRET_TENANT_ID,AZURE_ACCESS_SUB_ID,AzureEnvironment.AZURE);

        TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
        AzureResourceManager azure = AzureResourceManager.authenticate(credential, profile).withDefaultSubscription();*/
        ResourceGroups resourceGroups = azure.resourceGroups();
        List<String> names =new ArrayList<>();
        resourceGroups.list().stream().forEach(e->names.add(e.name()));
        return names;
    }

    @Override
    public List<String> listPipeline(User loginUser, String factoryName, String resourceGroupName) {
        PagedIterable<PipelineResource> pipelineResources = manager.pipelines().listByFactory(resourceGroupName, factoryName);
        List<String> names =new ArrayList<>();
        pipelineResources.stream().forEach(e->names.add(e.name()));
        return names;
    }
}
