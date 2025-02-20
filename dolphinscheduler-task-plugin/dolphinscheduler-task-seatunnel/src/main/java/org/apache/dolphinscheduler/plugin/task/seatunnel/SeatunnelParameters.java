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

import static org.apache.dolphinscheduler.plugin.task.seatunnel.Constants.DORIS;
import static org.apache.dolphinscheduler.plugin.task.seatunnel.Constants.HDFS;
import static org.apache.dolphinscheduler.plugin.task.seatunnel.Constants.MYSQL;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.seatunnel.parameter.CommonConfigParameters;
import org.apache.dolphinscheduler.plugin.task.seatunnel.parameter.DorisParameters;
import org.apache.dolphinscheduler.plugin.task.seatunnel.parameter.HdfsFileParameters;
import org.apache.dolphinscheduler.plugin.task.seatunnel.parameter.MysqlParameters;

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
     * source type
     */
    private String sourceType;

    /**
     * target type
     */
    private String targetType;

    /**
     * source config parameters
     */
    private String sourceConfig;

    /**
     * sink config parameters
     */
    private String targetConfig;

    /**
     * task parallelism
     */
    private int parallelism;

    /**
     * job.mode type in seatunnel env
     */
    private JobModeEnum jobMode;

    /**
     * Enable custom data filtering
     */
    private boolean customDataFilter;

    /**
     * Custom seatunnel transform config when customDataFilter set true
     */
    private String customTransform;

    /**
     * startup script
     */
    private String startupScript;

    /**
     * Whether to use user-defined configuration
     */
    private boolean useCustom;

    /**
     * raw script
     */
    private String rawScript;

    /**
     * Xms memory
     */
    private int xms;

    /**
     * Xmx memory
     */
    private int xmx;

    /**
     * resource list
     */
    private List<ResourceInfo> resourceList;

    @Override
    public boolean checkParameters() {
        return Objects.nonNull(startupScript)
                && ((BooleanUtils.isTrue(useCustom) && StringUtils.isNotBlank(rawScript))
                        || (BooleanUtils.isFalse(useCustom) && sourceConfig != null && targetConfig != null));
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return resourceList;
    }

    @Override
    public ResourceParametersHelper getResources() {
        ResourceParametersHelper resources = super.getResources();

        if (this.isUseCustom()) {
            return resources;
        }

        if (StringUtils.isNotEmpty(sourceConfig)) {
            CommonConfigParameters sourceParameters =
                    (CommonConfigParameters) JSONUtils.parseObject(this.getSourceConfig(),
                            getSourceParameter(this.getSourceType()));
            int sourceDatabaseId = sourceParameters.getDatabaseId();
            if (sourceDatabaseId != 0) {
                resources.put(ResourceType.DATASOURCE, sourceDatabaseId);
            }
        }

        if (StringUtils.isNotEmpty(targetConfig)) {
            CommonConfigParameters sinkParameters =
                    (CommonConfigParameters) JSONUtils.parseObject(this.getTargetConfig(),
                            getTargetParameter(this.getTargetType()));
            int sinkDatabaseId = sinkParameters.getDatabaseId();
            if (sinkDatabaseId != 0) {
                resources.put(ResourceType.DATASOURCE, sinkDatabaseId);
            }
        }

        return resources;
    }

    public SeatunnelTaskExecutionContext generateExtendedContext(ResourceParametersHelper resourceParametersHelper) {

        SeatunnelTaskExecutionContext seatunnelTaskExecutionContext = new SeatunnelTaskExecutionContext();

        if (this.isUseCustom()
                || resourceParametersHelper.getResourceMap().isEmpty()) {
            return seatunnelTaskExecutionContext;
        }

        CommonConfigParameters sourceParameter = (CommonConfigParameters) JSONUtils.parseObject(this.getSourceConfig(),
                getSourceParameter(this.getSourceType()));

        CommonConfigParameters sinkParameter = (CommonConfigParameters) JSONUtils.parseObject(this.getTargetConfig(),
                getTargetParameter(this.getTargetType()));

        DataSourceParameters dataSource = (DataSourceParameters) resourceParametersHelper
                .getResourceParameters(ResourceType.DATASOURCE, sourceParameter.getDatabaseId());

        DataSourceParameters dataSink = (DataSourceParameters) resourceParametersHelper
                .getResourceParameters(ResourceType.DATASOURCE, sinkParameter.getDatabaseId());

        if (Objects.nonNull(dataSource)) {
            seatunnelTaskExecutionContext.setDataSourceId(sourceParameter.getDatabaseId());
            seatunnelTaskExecutionContext.setDataSourceType(dataSource.getType());
            seatunnelTaskExecutionContext.setSourceConnectionParams(dataSource.getConnectionParams());
        }

        if (Objects.nonNull(dataSink)) {
            seatunnelTaskExecutionContext.setDataTargetId(sinkParameter.getDatabaseId());
            seatunnelTaskExecutionContext.setDataTargetType(dataSink.getType());
            seatunnelTaskExecutionContext.setTargetConnectionParams(dataSink.getConnectionParams());
        }

        return seatunnelTaskExecutionContext;
    }

    private Class<?> getSourceParameter(String sourceType) {
        switch (sourceType) {
            case MYSQL:
                return MysqlParameters.class;
            case HDFS:
                return HdfsFileParameters.class;
            case DORIS:
                return DorisParameters.class;
            default:
                return null;
        }
    }

    private Class<?> getTargetParameter(String sinkType) {
        switch (sinkType) {
            case MYSQL:
                return MysqlParameters.class;
            case HDFS:
                return HdfsFileParameters.class;
            case DORIS:
                return DorisParameters.class;
            default:
                return null;
        }
    }
}
