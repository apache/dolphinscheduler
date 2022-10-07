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

package org.apache.dolphinscheduler.plugin.task.chunjun;

import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.spi.enums.Flag;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * chunjun parameters
 */
@Setter
@Getter
@ToString
public class ChunJunParameters extends AbstractParameters {

    /**
     * custom json config，default 1, support custom json
     */
    private int customConfig;

    /**
     * custom config json
     */
    private String json;

    /**
     * other arguments -confProp "{\"flink.checkpoint.interval\":60000}"
     */
    private String others;

    /**
     * deploy mode: [local, standalone, yarn-session, yarn-per-job]
     */
    private String deployMode;

    /**
     * customConfig value is 0, datasource type，eg mysql
     */
    private String dsType;

    /**
     * customConfig value is 0, datasource id int
     */
    private int dataSource;

    /**
     * customConfig value is 0, datasource targetType，eg  MYSQL, POSTGRES
     */
    private String dtType;

    /**
     * customConfig value is 0, data target id
     */
    private int dataTarget;

    /**
     * customConfig value is 0, sql
     */
    private String sql;

    /**
     * customConfig value is 0, target table
     */
    private String targetTable;

    /**
     * pre statements
     */
    private List<String> preStatements;

    /**
     * post statements
     */
    private List<String> postStatements;

    /**
     * customConfig value is 0, job speed byte
     */
    private int jobSpeedByte;

    /**
     * customConfig value is 0, job speed record count
     */
    private int jobSpeedRecord;

    @Override
    public boolean checkParameters() {
        if (customConfig == Flag.NO.ordinal()) {
            return dataSource != 0 && dataTarget != 0
                && StringUtils.isNotEmpty(sql)
                && StringUtils.isNotEmpty(targetTable);
        } else {
            return StringUtils.isNotEmpty(json);
        }
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    @Override
    public ResourceParametersHelper getResources() {
        ResourceParametersHelper resources = super.getResources();

        if (customConfig == Flag.YES.ordinal()) {
            return resources;
        }
        resources.put(ResourceType.DATASOURCE, dataSource);
        resources.put(ResourceType.DATASOURCE, dataTarget);
        return resources;
    }
}
