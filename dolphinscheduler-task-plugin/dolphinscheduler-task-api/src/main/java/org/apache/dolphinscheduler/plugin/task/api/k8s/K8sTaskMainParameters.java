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

package org.apache.dolphinscheduler.plugin.task.api.k8s;

import java.util.Map;

/**
 * k8s task parameters
 */
public class K8sTaskMainParameters {

    private String image;
    private String namespaceName;
    private String clusterName;
    private double minCpuCores;
    private double minMemorySpace;
    private Map<String, String> paramsMap;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Map<String, String> getParamsMap() {
        return paramsMap;
    }

    public void setParamsMap(Map<String, String> paramsMap) {
        this.paramsMap = paramsMap;
    }

    @Override
    public String toString() {
        return "K8sTaskMainParameters{"
             + "image='" + image + '\''
             + ", namespaceName='" + namespaceName + '\''
             + ", clusterName='" + clusterName + '\''
             + ", minCpuCores=" + minCpuCores
             + ", minMemorySpace=" + minMemorySpace
             + ", paramsMap=" + paramsMap
             + '}';
    }
}
