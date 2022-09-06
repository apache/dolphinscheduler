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

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * k8s task parameters
 */
public class K8sTaskParameters extends AbstractParameters {
    private String image;
    private String namespace;
    private double minCpuCores;
    private double minMemorySpace;

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

    @Override
    public boolean checkParameters() {
        return StringUtils.isNotEmpty(image) && StringUtils.isNotEmpty(namespace)
            ;
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "K8sTaskParameters{"
            + "image='" + image + '\''
            + ", namespace='" + namespace + '\''
            + ", minCpuCores=" + minCpuCores
            + ", minMemorySpace=" + minMemorySpace
            + '}';
    }
}
