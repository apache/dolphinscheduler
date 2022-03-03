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

package org.apache.dolphinscheduler.plugin.task.k8s;

import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * k8s task parameters
 */
public class K8sTaskParameters extends AbstractParameters {
    private String image;
    private String namespace;
    private String clusterName;
    private double minCpuCores;
    private double minMemorySpace;
    private String others;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public double getMinCpuCores() {
        return minCpuCores;
    }

    public void setMinCpuCores(double minCpuCores) {
        this.minCpuCores = minCpuCores;
    }

    public double getMinMemorySpace() {
        return minMemorySpace;
    }

    public void setMinMemorySpace(double minMemorySpace) {
        this.minMemorySpace = minMemorySpace;
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    @Override
    public boolean checkParameters() {
        return StringUtils.isNotEmpty(image) && StringUtils.isNotEmpty(namespace) && StringUtils.isNotEmpty(clusterName);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    @Override
    public void dealOutParam(String result) {
        if (CollectionUtils.isEmpty(localParams)) {
            return;
        }
        List<Property> outProperty = getOutProperty(localParams);
        if (CollectionUtils.isEmpty(outProperty)) {
            return;
        }
        if (StringUtils.isEmpty(result)) {
            varPool.addAll(outProperty);
            return;
        }
        List<Map<String, String>> sqlResult = getListMapByString(result);
        if (CollectionUtils.isEmpty(sqlResult)) {
            return;
        }
        //if sql return more than one line
        if (sqlResult.size() > 1) {
            Map<String, List<String>> sqlResultFormat = new HashMap<>();
            //init sqlResultFormat
            Set<String> keySet = sqlResult.get(0).keySet();
            for (String key : keySet) {
                sqlResultFormat.put(key, new ArrayList<>());
            }
            for (Map<String, String> info : sqlResult) {
                for (String key : info.keySet()) {
                    sqlResultFormat.get(key).add(String.valueOf(info.get(key)));
                }
            }
            for (Property info : outProperty) {
                if (info.getType() == DataType.LIST) {
                    info.setValue(JSONUtils.toJsonString(sqlResultFormat.get(info.getProp())));
                    varPool.add(info);
                }
            }
        } else {
            //result only one line
            Map<String, String> firstRow = sqlResult.get(0);
            for (Property info : outProperty) {
                info.setValue(String.valueOf(firstRow.get(info.getProp())));
                varPool.add(info);
            }
        }

    }

    @Override
    public String toString() {
        return "K8sTaskParameters{"
                + "image='" + image + '\''
                + ", namespace='" + namespace + '\''
                + ", clusterName='" + clusterName + '\''
                + ", minCpuCores=" + minCpuCores
                + ", minMemorySpace=" + minMemorySpace
                + ", others='" + others + '\''
                + '}';
    }
}
