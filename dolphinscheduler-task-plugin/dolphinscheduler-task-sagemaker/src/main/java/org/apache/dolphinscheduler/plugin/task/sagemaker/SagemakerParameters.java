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

import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@ToString
@Slf4j
public class SagemakerParameters extends AbstractParameters {

    /**
     * request script
     */
    private String sagemakerRequestJson;
    private String username;
    private String password;
    private String awsRegion;
    private int datasource;
    private String type;

    @Override
    public boolean checkParameters() {
        return StringUtils.isNotEmpty(sagemakerRequestJson);
    }

    public SagemakerTaskExecutionContext generateExtendedContext(ResourceParametersHelper parametersHelper) {
        DataSourceParameters dataSourceParameters =
                (DataSourceParameters) parametersHelper.getResourceParameters(ResourceType.DATASOURCE, datasource);
        SagemakerTaskExecutionContext sagemakerTaskExecutionContext = new SagemakerTaskExecutionContext();
        sagemakerTaskExecutionContext.setConnectionParams(
                Objects.nonNull(dataSourceParameters) ? dataSourceParameters.getConnectionParams() : null);
        return sagemakerTaskExecutionContext;
    }

    @Override
    public ResourceParametersHelper getResources() {
        ResourceParametersHelper resources = super.getResources();
        resources.put(ResourceType.DATASOURCE, datasource);
        return resources;
    }

}
