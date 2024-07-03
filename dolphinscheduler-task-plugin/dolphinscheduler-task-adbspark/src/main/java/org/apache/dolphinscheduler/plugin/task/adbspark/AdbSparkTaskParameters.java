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

package org.apache.dolphinscheduler.plugin.task.adbspark;

import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class AdbSparkTaskParameters extends AbstractParameters {

    /**
     * ADB Cluster ID
     */
    private String dbClusterId;

    /**
     * ADB Resource Group Name, which type is Job
     */
    private String resourceGroupName;

    /**
     * Spark Application Name
     */
    private String appName;

    /**
     * Spark Application Type, such as Batch, SQL
     */
    private String appType;

    /**
     * Spark Application Configuration Data
     */
    private String data;

    private int datasource;

    private String type;

    @Override
    public boolean checkParameters() {
        return StringUtils.isNotBlank(dbClusterId) &&
                StringUtils.isNotBlank(resourceGroupName) &&
                StringUtils.isNotBlank(data);
    }

    @Override
    public ResourceParametersHelper getResources() {
        ResourceParametersHelper resourceParametersHelper = super.getResources();
        resourceParametersHelper.put(ResourceType.DATASOURCE, datasource);
        return resourceParametersHelper;
    }
}
