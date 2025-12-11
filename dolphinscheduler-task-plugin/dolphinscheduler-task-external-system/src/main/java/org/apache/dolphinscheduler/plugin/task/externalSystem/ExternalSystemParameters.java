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

package org.apache.dolphinscheduler.plugin.task.externalSystem;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

import org.jetbrains.annotations.NotNull;

public class ExternalSystemParameters extends AbstractParameters {

    private int datasource;

    private int externalSystemId;

    private String authenticationToken;

    private String externalTaskId;
    private String externalTaskName;

    public int getDatasource() {
        return datasource;
    }

    public void setDatasource(int datasource) {
        this.datasource = datasource;
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public void setAuthenticationToken(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    public String getExternalTaskId() {
        return externalTaskId;
    }

    public void setExternalTaskId(String externalTaskId) {
        this.externalTaskId = externalTaskId;
    }

    public String getExternalTaskName() {
        return externalTaskName;
    }

    public void setExternalTaskName(String externalTaskName) {
        this.externalTaskName = externalTaskName;
    }

    public int getExternalSystemId() {
        return externalSystemId;
    }

    public void setExternalSystemId(int externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    @Override
    public ResourceParametersHelper getResources() {
        ResourceParametersHelper resources = super.getResources();
        resources.put(ResourceType.DATASOURCE, datasource);
        return resources;
    }

    public boolean checkParameters() {
        // Add validation logic here
        return true;
    }

    public BaseExternalSystemParams generateExtendedContext(@NotNull ResourceParametersHelper parametersHelper) {
        DataSourceParameters externalSystemResourceParameters =
                (DataSourceParameters) parametersHelper.getResourceParameters(ResourceType.DATASOURCE,
                        externalSystemId);
        BaseExternalSystemParams baseExternalSystemParams =
                JSONUtils.parseObject(externalSystemResourceParameters.getConnectionParams(),
                        BaseExternalSystemParams.class);
        return baseExternalSystemParams;
    }

}
