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
import org.apache.dolphinscheduler.plugin.task.api.enums.UdfType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.UdfFuncParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.VarPoolUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Sql/Hql parameter
 */
public class SqlParameters extends AbstractParameters {

    /**
     * data source type，eg  MYSQL, POSTGRES, HIVE ...
     */
    private String type;

    /**
     * datasource id
     */
    private int datasource;

    /**
     * sql
     */
    private String sql;

    /**
     * sql type
     * 0 query
     * 1 NON_QUERY
     */
    private int sqlType;

    /**
     * send email
     */
    private Boolean sendEmail;

    /**
     * display rows
     */
    private int displayRows;

    /**
     * udf list
     */
    private String udfs;
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
    /**
     * Pre Statements
     */
    private List<String> preStatements;
    /**
     * Post Statements
     */
    private List<String> postStatements;

    /**
     * groupId
     */
    private int groupId;
    /**
     * title
     */
    private String title;

    private int limit;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDatasource() {
        return datasource;
    }

    public void setDatasource(int datasource) {
        this.datasource = datasource;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getUdfs() {
        return udfs;
    }

    public void setUdfs(String udfs) {
        this.udfs = udfs;
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    public Boolean getSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public int getDisplayRows() {
        return displayRows;
    }

    public void setDisplayRows(int displayRows) {
        this.displayRows = displayRows;
    }

    public String getShowType() {
        return showType;
    }

    public void setShowType(String showType) {
        this.showType = showType;
    }

    public String getConnParams() {
        return connParams;
    }

    public void setConnParams(String connParams) {
        this.connParams = connParams;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getPreStatements() {
        return preStatements;
    }

    public void setPreStatements(List<String> preStatements) {
        this.preStatements = preStatements;
    }

    public List<String> getPostStatements() {
        return postStatements;
    }

    public void setPostStatements(List<String> postStatements) {
        this.postStatements = postStatements;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean checkParameters() {
        return datasource != 0 && StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(sql);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
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
                + ", sqlType=" + sqlType
                + ", sendEmail=" + sendEmail
                + ", displayRows=" + displayRows
                + ", limit=" + limit
                + ", udfs='" + udfs + '\''
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

        // whether udf type
        boolean udfTypeFlag = Enums.getIfPresent(UdfType.class, Strings.nullToEmpty(this.getType())).isPresent()
                && !StringUtils.isEmpty(this.getUdfs());

        if (udfTypeFlag) {
            String[] udfFunIds = this.getUdfs().split(",");
            for (int i = 0; i < udfFunIds.length; i++) {
                resources.put(ResourceType.UDF, Integer.parseInt(udfFunIds[i]));
            }
        }
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

        // whether udf type
        boolean udfTypeFlag = Enums.getIfPresent(UdfType.class, Strings.nullToEmpty(this.getType())).isPresent()
                && !StringUtils.isEmpty(this.getUdfs());

        if (udfTypeFlag) {
            List<UdfFuncParameters> collect = parametersHelper.getResourceMap(ResourceType.UDF).entrySet().stream()
                    .map(entry -> (UdfFuncParameters) entry.getValue()).collect(Collectors.toList());
            sqlTaskExecutionContext.setUdfFuncParametersList(collect);
        }

        return sqlTaskExecutionContext;
    }
}
