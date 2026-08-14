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
import org.apache.dolphinscheduler.plugin.task.api.SQLTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.enums.SqlSourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.api.utils.VarPoolUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;

import com.google.common.collect.Lists;

/**
 * Sql/Hql parameter
 */
@Data
public class SqlParameters extends AbstractParameters {

    /**
     * data source type，eg  MYSQL, POSTGRES, HIVE ...
     */
    private String type;

    /**
     * datasource id
     */
    private int datasource;

    private String sql;

    private SqlSourceType sqlSource;

    /**
     * sql resource file path in resource center
     */
    private String sqlResource;

    /**
     * sql type
     * 0 query
     * 1 NON_QUERY
     */
    private int sqlType;

    private Boolean sendEmail;

    private int displayRows;

    /**
     * show type
     * 0 TABLE
     * 1 TEXT
     * 2 attachment
     * 3 TABLE+attachment
     */
    private String showType;
    /**
     * SQL connection parameters
     */
    private String connParams;
    private List<String> preStatements;
    private List<String> postStatements;

    private int groupId;
    private String title;

    private int limit;

    @Override
    public boolean checkParameters() {
        if (datasource == 0 || StringUtils.isEmpty(type)) {
            return false;
        }
        if (StringUtils.isNotEmpty(sql)) {
            return true;
        }
        return StringUtils.isNotEmpty(sqlResource);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        List<ResourceInfo> resourceFiles = new ArrayList<>();
        if (StringUtils.isNotEmpty(sqlResource)) {
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setResourceName(sqlResource);
            resourceFiles.add(resourceInfo);
        }
        return resourceFiles;
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
            varPool = VarPoolUtils.mergeVarPool(Lists.newArrayList(varPool, outProperty));
            return;
        }
        List<Map<String, String>> sqlResult = getListMapByString(result);
        if (CollectionUtils.isEmpty(sqlResult)) {
            return;
        }
        // if sql return more than one line
        if (sqlResult.size() > 1) {
            Map<String, List<String>> sqlResultFormat = new HashMap<>();
            // init sqlResultFormat
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
                }
            }
        } else {
            // result only one line
            Map<String, String> firstRow = sqlResult.get(0);
            for (Property info : outProperty) {
                info.setValue(String.valueOf(firstRow.get(info.getProp())));
            }
        }
        varPool = VarPoolUtils.mergeVarPool(Lists.newArrayList(varPool, outProperty));

    }

    @Override
    public String toString() {
        return "SqlParameters{"
                + "type='" + type + '\''
                + ", datasource=" + datasource
                + ", sql='" + sql + '\''
                + ", sqlSource='" + sqlSource + '\''
                + ", sqlResource='" + sqlResource + '\''
                + ", sqlType=" + sqlType
                + ", sendEmail=" + sendEmail
                + ", displayRows=" + displayRows
                + ", limit=" + limit
                + ", showType='" + showType + '\''
                + ", connParams='" + connParams + '\''
                + ", groupId='" + groupId + '\''
                + ", title='" + title + '\''
                + ", preStatements=" + preStatements
                + ", postStatements=" + postStatements
                + '}';
    }

    @Override
    public ResourceParametersHelper getResources() {
        ResourceParametersHelper resources = super.getResources();
        resources.put(ResourceType.DATASOURCE, datasource);

        return resources;
    }

    /**
     * TODO SQLTaskExecutionContext needs to be optimized
     *
     * @param parametersHelper
     * @return
     */
    public SQLTaskExecutionContext generateExtendedContext(ResourceParametersHelper parametersHelper) {
        SQLTaskExecutionContext sqlTaskExecutionContext = new SQLTaskExecutionContext();

        DataSourceParameters dbSource =
                (DataSourceParameters) parametersHelper.getResourceParameters(ResourceType.DATASOURCE, datasource);
        sqlTaskExecutionContext.setConnectionParams(dbSource.getConnectionParams());

        return sqlTaskExecutionContext;
    }
}
