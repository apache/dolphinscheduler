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
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;

@EqualsAndHashCode(callSuper = true)
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
        // Read numeric tokens directly as strings: a JsonNode round-trip can convert
        // scientific notation to double and lose precision before OUT type conversion.
        List<Map<String, String>> sqlResult = JSONUtils.parseObject(result,
                new TypeReference<List<Map<String, String>>>() {
                });
        if (CollectionUtils.isEmpty(sqlResult)) {
            return;
        }
        // collect the values of each column so that the OUT parameter format does not
        // depend on the result row count, see https://github.com/apache/dolphinscheduler/issues/18750
        Map<String, List<String>> sqlResultFormat = new HashMap<>();
        for (Map<String, String> row : sqlResult) {
            for (String key : row.keySet()) {
                sqlResultFormat.computeIfAbsent(key, k -> new ArrayList<>()).add(String.valueOf(row.get(key)));
            }
        }
        for (Property info : outProperty) {
            List<String> columnValues = sqlResultFormat.get(info.getProp());
            if (info.getType() == DataType.LIST) {
                // LIST OUT parameter always keeps the JSON array format, even if the result only has one row
                info.setValue(JSONUtils.toJsonString(columnValues));
            } else if (columnValues != null) {
                // non LIST OUT parameter keeps the last row value, which is compatible with the previous
                // single row behaviour
                info.setValue(convertOutParamValue(info, columnValues.get(columnValues.size() - 1)));
            }
        }
        varPool = VarPoolUtils.mergeVarPool(Lists.newArrayList(varPool, outProperty));

    }

    private String convertOutParamValue(Property info, String value) {
        if (info.getType() != DataType.LONG) {
            return value;
        }
        try {
            return Long.toString(new BigDecimal(value).longValueExact());
        } catch (NumberFormatException | ArithmeticException e) {
            throw new TaskException("Invalid SQL OUT parameter '" + info.getProp()
                    + "' of type LONG: '" + value + "' must be an integer within the LONG range", e);
        }
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
