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

package org.apache.dolphinscheduler.plugin.task.seatunnel;

import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.seatunnel.parameter.BaseDataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.seatunnel.parameter.SeatunnelConfigParameters;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SeatunnelParameters extends AbstractParameters {

    /**
     * source config parameters
     */
    SeatunnelConfigParameters sourceConfig;

    /**
     * sink config parameters
     */
    SeatunnelConfigParameters sinkConfig;

    /**
     * task parallelism
     */
    private int parallelism;

    /**
     * Enable custom data filtering
     */
    private boolean customDataFilter;

    /**
     * Custom seatunnel transform config when customDataFilter set true
     */
    private String customTransform;

    private String startupScript;

    /**
     * Whether to use user-defined configuration
     */
    private Boolean useCustom;

    private String rawScript;

    /**
     * resource list
     */
    private List<ResourceInfo> resourceList;

    @Override
    public boolean checkParameters() {
        return Objects.nonNull(startupScript)
                && ((BooleanUtils.isTrue(useCustom) && StringUtils.isNotBlank(rawScript))
                        || (BooleanUtils.isFalse(useCustom) && sourceConfig != null && sinkConfig != null));
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return resourceList;
    }

    @Override
    public ResourceParametersHelper getResources() {
        ResourceParametersHelper resources = super.getResources();

        if (Objects.nonNull(sourceConfig) && sourceConfig.getDbType().isDatasourceType()) {
            BaseDataSourceParameters dataSourceParameters = (BaseDataSourceParameters) sourceConfig;
            int sourceDatabaseId = dataSourceParameters.getDatabaseId();
            if (sourceDatabaseId != 0) {
                resources.put(ResourceType.DATASOURCE, sourceDatabaseId);
            }
        }

        if (Objects.nonNull(sinkConfig) && sinkConfig.getDbType().isDatasourceType()) {
            BaseDataSourceParameters dataSourceParameters = (BaseDataSourceParameters) sinkConfig;
            int sinkDatabaseId = dataSourceParameters.getDatabaseId();
            if (sinkDatabaseId != 0) {
                resources.put(ResourceType.DATASOURCE, sinkDatabaseId);
            }

        }

        return resources;
    }

    public SeatunnelTaskExecutionContext generateExtendedContext(ResourceParametersHelper resourceParametersHelper) {

        SeatunnelTaskExecutionContext seatunnelTaskExecutionContext = new SeatunnelTaskExecutionContext();

        if (!useCustom && sourceConfig.getDbType().isDatasourceType()) {
            int dataSourceId = ((BaseDataSourceParameters) sourceConfig).getDatabaseId();
            DataSourceParameters sourceParameters = (DataSourceParameters) resourceParametersHelper
                    .getResourceParameters(ResourceType.DATASOURCE, dataSourceId);
            seatunnelTaskExecutionContext.setDataSourceId(dataSourceId);
            seatunnelTaskExecutionContext.setDataSourceType(sourceConfig.getDbType());
            seatunnelTaskExecutionContext.setSourceConnectionParams(sourceParameters.getConnectionParams());
        }

        if (!useCustom && sinkConfig.getDbType().isDatasourceType()) {
            int dataTargetId = ((BaseDataSourceParameters) sinkConfig).getDatabaseId();
            DataSourceParameters targetParameters = (DataSourceParameters) resourceParametersHelper
                    .getResourceParameters(ResourceType.DATASOURCE, dataTargetId);
            seatunnelTaskExecutionContext.setDataTargetId(dataTargetId);
            seatunnelTaskExecutionContext.setDataTargetType(sinkConfig.getDbType());
            seatunnelTaskExecutionContext.setTargetConnectionParams(targetParameters.getConnectionParams());
        }

        return seatunnelTaskExecutionContext;
    }
}
