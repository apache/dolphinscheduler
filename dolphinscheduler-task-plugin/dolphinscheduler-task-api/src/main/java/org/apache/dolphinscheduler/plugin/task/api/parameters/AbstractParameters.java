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
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.api.utils.VarPoolUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;

@Getter
@Slf4j
public abstract class AbstractParameters implements IParameters {

    @Setter
    public List<Property> localParams;

    public List<Property> varPool = new ArrayList<>();

    @Override
    public abstract boolean checkParameters();

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    public Map<String, Property> getLocalParametersMap() {
        Map<String, Property> localParametersMaps = new LinkedHashMap<>();
        if (localParams != null) {
            for (Property property : localParams) {
                localParametersMaps.put(property.getProp(), property);
            }
        }
        return localParametersMaps;
    }

    public K8sTaskExecutionContext generateK8sTaskExecutionContext(ResourceParametersHelper parametersHelper,
                                                                   int datasource) {
        DataSourceParameters dataSourceParameters =
                (DataSourceParameters) parametersHelper.getResourceParameters(ResourceType.DATASOURCE, datasource);
        K8sTaskExecutionContext k8sTaskExecutionContext = new K8sTaskExecutionContext();
        k8sTaskExecutionContext.setConnectionParams(
                Objects.nonNull(dataSourceParameters) ? dataSourceParameters.getConnectionParams() : null);
        return k8sTaskExecutionContext;
    }

    /**
     * get input local parameters map if the param direct is IN
     *
     * @return parameters map
     */
    public Map<String, Property> getInputLocalParametersMap() {
        Map<String, Property> localParametersMaps = new LinkedHashMap<>();
        if (localParams != null) {
            for (Property property : localParams) {
                // The direct of some tasks is empty, default IN
                if (property.getDirect() == null || Objects.equals(Direct.IN, property.getDirect())) {
                    localParametersMaps.put(property.getProp(), property);
                }
            }
        }
        return localParametersMaps;
    }

    /**
     * get varPool map
     *
     * @return parameters map
     */
    public Map<String, Property> getVarPoolMap() {
        Map<String, Property> varPoolMap = new LinkedHashMap<>();
        if (varPool != null) {
            for (Property property : varPool) {
                varPoolMap.put(property.getProp(), property);
            }
        }
        return varPoolMap;
    }

    public void setVarPool(String varPool) {
        if (StringUtils.isEmpty(varPool)) {
            this.varPool = new ArrayList<>();
        } else {
            this.varPool = JSONUtils.toList(varPool, Property.class);
        }
    }

    public void dealOutParam(Map<String, String> taskOutputParams) {
        List<Property> outProperty = getOutProperty(localParams);
        if (CollectionUtils.isEmpty(outProperty)) {
            return;
        }
        if (CollectionUtils.isNotEmpty(outProperty) && MapUtils.isNotEmpty(taskOutputParams)) {
            // Inject the value
            for (Property info : outProperty) {
                String value = taskOutputParams.get(info.getProp());
                if (value != null) {
                    info.setValue(value);
                }
            }
        }

        varPool = VarPoolUtils.mergeVarPool(Lists.newArrayList(varPool, outProperty));
    }

    protected List<Property> getOutProperty(List<Property> params) {
        if (CollectionUtils.isEmpty(params)) {
            return new ArrayList<>();
        }
        return params.stream()
                .filter(info -> info.getDirect() == Direct.OUT)
                .collect(Collectors.toList());
    }

    public List<Map<String, String>> getListMapByString(String json) {
        List<Map<String, String>> allParams = new ArrayList<>();
        ArrayNode paramsByJson = JSONUtils.parseArray(json);
        for (JsonNode jsonNode : paramsByJson) {
            Map<String, String> param = JSONUtils.toMap(jsonNode.toString());
            allParams.add(param);
        }
        return allParams;
    }

    public ResourceParametersHelper getResources() {
        return new ResourceParametersHelper();
    }

    public void addPropertyToValPool(Property property) {
        varPool.removeIf(p -> p.getProp().equals(property.getProp()));
        varPool.add(property);
    }
}
