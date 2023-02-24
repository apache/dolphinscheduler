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
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * job params related class
 */
public abstract class AbstractParameters implements IParameters {

    @Override
    public abstract boolean checkParameters();

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    /**
     * local parameters
     */
    public List<Property> localParams;

    /**
     * var pool
     */
    public List<Property> varPool;

    /**
     * get local parameters list
     *
     * @return Property list
     */
    public List<Property> getLocalParams() {
        return localParams;
    }

    public void setLocalParams(List<Property> localParams) {
        this.localParams = localParams;
    }

    /**
     * get local parameters map
     * @return parameters map
     */
    public Map<String, Property> getLocalParametersMap() {
        Map<String, Property> localParametersMaps = new LinkedHashMap<>();
        if (localParams != null) {
            for (Property property : localParams) {
                localParametersMaps.put(property.getProp(), property);
            }
        }
        return localParametersMaps;
    }

    /**
     * get input local parameters map if the param direct is IN
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

    public List<Property> getVarPool() {
        return varPool;
    }

    public void setVarPool(String varPool) {
        if (StringUtils.isEmpty(varPool)) {
            this.varPool = new ArrayList<>();
        } else {
            this.varPool = JSONUtils.toList(varPool, Property.class);
        }
    }

    public void dealOutParam(String result) {
        if (CollectionUtils.isEmpty(localParams)) {
            return;
        }
        List<Property> outProperty = getOutProperty(localParams);
        if (CollectionUtils.isEmpty(outProperty)) {
            return;
        }
        if (StringUtils.isEmpty(result)) {
            outProperty.forEach(this::addPropertyToValPool);
            return;
        }
        Map<String, String> taskResult = getMapByString(result);
        if (taskResult.size() == 0) {
            return;
        }
        for (Property info : outProperty) {
            String propValue = taskResult.get(info.getProp());
            if (StringUtils.isNotEmpty(propValue)) {
                info.setValue(propValue);
                addPropertyToValPool(info);
            }
        }
    }

    public List<Property> getOutProperty(List<Property> params) {
        if (CollectionUtils.isEmpty(params)) {
            return new ArrayList<>();
        }
        List<Property> result = new ArrayList<>();
        for (Property info : params) {
            if (info.getDirect() == Direct.OUT) {
                result.add(info);
            }
        }
        return result;
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

    /**
     * shell's result format is key=value$VarPool$key=value$VarPool$
     * @param result
     * @return
     */
    public static Map<String, String> getMapByString(String result) {
        String[] formatResult = result.split("\\$VarPool\\$");
        Map<String, String> format = new HashMap<>();
        for (String info : formatResult) {
            if (StringUtils.isNotEmpty(info) && info.contains("=")) {
                String[] keyValue = info.split("=");
                format.put(keyValue[0], keyValue[1]);
            }
        }
        return format;
    }

    public ResourceParametersHelper getResources() {
        return new ResourceParametersHelper();
    }

    public void addPropertyToValPool(Property property) {
        varPool.removeIf(p -> p.getProp().equals(property.getProp()));
        varPool.add(property);
    }
}
