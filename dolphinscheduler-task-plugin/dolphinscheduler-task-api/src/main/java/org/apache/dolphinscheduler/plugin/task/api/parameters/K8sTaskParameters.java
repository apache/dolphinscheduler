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

package org.apache.dolphinscheduler.plugin.task.api.parameters;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.Label;
import org.apache.dolphinscheduler.plugin.task.api.model.NodeSelectorExpression;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class K8sTaskParameters extends AbstractParameters {

    private String image;
    private String namespace;
    private String command;
    private String args;
    private String pullSecret;
    private String imagePullPolicy;
    private double minCpuCores;
    private double minMemorySpace;
    private List<Label> customizedLabels;
    private List<NodeSelectorExpression> nodeSelectors;
    private String kubeConfig;
    private int datasource;
    private String type;
    @Override
    public boolean checkParameters() {
        return StringUtils.isNotEmpty(image);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    @Override
    public ResourceParametersHelper getResources() {
        ResourceParametersHelper resources = super.getResources();
        resources.put(ResourceType.DATASOURCE, datasource);
        return resources;
    }

    public K8sTaskExecutionContext generateK8sTaskExecutionContext(
                                                                   ResourceParametersHelper parametersHelper,
                                                                   int datasource) {
        DataSourceParameters dataSourceParameters =
                (DataSourceParameters) parametersHelper
                        .getResourceParameters(ResourceType.DATASOURCE, datasource);

        String connectionParams = null;
        if (dataSourceParameters != null) {
            connectionParams = dataSourceParameters.getConnectionParams();
        }

        String configYaml = null;
        String namespace = null;
        if (StringUtils.isNotEmpty(connectionParams) && JSONUtils.checkJsonValid(connectionParams, false)) {
            K8sTaskParameters connectionTaskParameters =
                    JSONUtils.parseObject(connectionParams, K8sTaskParameters.class);
            configYaml = connectionTaskParameters.getKubeConfig();
            namespace = connectionTaskParameters.getNamespace();
        }

        return K8sTaskExecutionContext.builder()
                .configYaml(configYaml)
                .namespace(namespace)
                .connectionParams(connectionParams)
                .build();
    }
}
