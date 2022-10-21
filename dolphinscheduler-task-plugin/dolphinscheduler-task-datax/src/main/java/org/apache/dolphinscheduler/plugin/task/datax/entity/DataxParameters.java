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

package org.apache.dolphinscheduler.plugin.task.datax.entity;

import lombok.Data;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.datax.DataxTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.datax.entity.ColumnInfo;
import org.apache.dolphinscheduler.spi.enums.Flag;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * DataX parameter
 */
public class DataxParameters extends AbstractParameters {

    /**
     * if custom json config，eg  0, 1
     */
    private int customConfig;

    /**
     * if custom SQL，eg  0, 1
     * TODO: not support yet
     */
    private int customSQL;

    /**
     * if customConfig eq 1 ,then json is usable
     */
    private String json;

    // Note: for reader part

    /**
     * data source type，eg  MYSQL, POSTGRES ...
     */
    private String dsType;

    /**
     * datasource id
     */
    private int dataSource;

    /**
     * source table
     */
    private String sourceTable;

    /**
     * where condition for select from source table
     */
    private String where;

    /**
     * split key. eg id
     */
    private String splitPk;

    /**
     * column infos of source table
     */
    private List<ColumnInfo> dsColumns;

    /**
     * partition values of source table
     */
    private List<String> dsPartitions;

    // Note: for writer part

    /**
     * data target type，eg  MYSQL, POSTGRES ...
     */
    private String dtType;

    /**
     * datatarget id
     */
    private int dataTarget;

    /**
     * column infos of target table
     */
    private List<ColumnInfo> dtColumns;

    /**
     * partition values of target table
     */
    private List<String> dtPartitions;

    /**
     * sql
     */
    private String sql;

    /**
     * target table
     */
    private String targetTable;

    /**
     * Pre Statements
     */
    private List<String> preStatements;

    /**
     * Post Statements
     */
    private List<String> postStatements;

    /**
     * Write Mode. eg INSERT INTO, UPDATE ON DUPLICATE ...
     */
    private int writeMode;

    /**
     * batch size for one commit
     */
    private int batchSize;

    /**
     * elasticsearchwriter-specific parameters. eg index, type ...
     */
    private ElasticSearchDataxParams elasticSearchDataxParams;

    // Note: for setting part

    /**
     * channel
     */
    private int channel;

    /**
     * speed byte num
     */
    private int jobSpeedByte;

    /**
     * speed record count
     */
    private int jobSpeedRecord;

    /**
     * Xms memory
     */
    private int xms;

    /**
     * Xmx memory
     */
    private int xmx;
    @Override
    public boolean checkParameters() {
        //TODO: 待前端接入后更新
        if(customConfig == Flag.YES.ordinal()){
            return StringUtils.isNotEmpty(json);
        }
        else {
            return dataSource != 0
                    && dataTarget != 0
                    && StringUtils.isNotEmpty(sql)
                    && StringUtils.isNotEmpty(targetTable);
        }
//        if(customConfig == Flag.YES.ordinal()){
//            return StringUtils.isNotEmpty(json);
//        }
//        else if(customSQL == Flag.YES.ordinal()) {
//            return dataSource != 0
//                    && dataTarget != 0
//                    && StringUtils.isNotEmpty(sql)
//                    && StringUtils.isNotEmpty(targetTable);
//        }else {
//            return dataSource != 0
//                    && dataTarget != 0
//                    && StringUtils.isNotEmpty(sourceTable)
//                    && StringUtils.isNotEmpty(targetTable)
//                    && CollectionUtils.isNotEmpty(dsColumns)
//                    && CollectionUtils.isNotEmpty(dtColumns);
//        }
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

    public DataxTaskExecutionContext generateExtendedContext(ResourceParametersHelper parametersHelper) {

        DataxTaskExecutionContext dataxTaskExecutionContext = new DataxTaskExecutionContext();
        if (customConfig == Flag.YES.ordinal()) {
            return dataxTaskExecutionContext;
        }

        DataSourceParameters dbSource = (DataSourceParameters) parametersHelper.getResourceParameters(ResourceType.DATASOURCE, dataSource);
        DataSourceParameters dbTarget = (DataSourceParameters) parametersHelper.getResourceParameters(ResourceType.DATASOURCE, dataTarget);

        if (Objects.nonNull(dbSource)) {
            dataxTaskExecutionContext.setDataSourceId(dataSource);
            dataxTaskExecutionContext.setSourcetype(dbSource.getType());
            dataxTaskExecutionContext.setSourceConnectionParams(dbSource.getConnectionParams());
        }

        if (Objects.nonNull(dbTarget)) {
            dataxTaskExecutionContext.setDataTargetId(dataTarget);
            dataxTaskExecutionContext.setTargetType(dbTarget.getType());
            dataxTaskExecutionContext.setTargetConnectionParams(dbTarget.getConnectionParams());
        }
        return dataxTaskExecutionContext;
    }

    public int getCustomConfig() {
        return customConfig;
    }

    public void setCustomConfig(int customConfig) {
        this.customConfig = customConfig;
    }

    public int getCustomSQL() {
        return customSQL;
    }

    public void setCustomSQL(int customSQL) {
        this.customSQL = customSQL;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getDsType() {
        return dsType;
    }

    public void setDsType(String dsType) {
        this.dsType = dsType;
    }

    public int getDataSource() {
        return dataSource;
    }

    public void setDataSource(int dataSource) {
        this.dataSource = dataSource;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getSplitPk() {
        return splitPk;
    }

    public void setSplitPk(String splitPk) {
        this.splitPk = splitPk;
    }

    public List<ColumnInfo> getDsColumns() {
        return dsColumns;
    }

    public void setDsColumns(List<ColumnInfo> dsColumns) {
        this.dsColumns = dsColumns;
    }

    public List<String> getDsPartitions() {
        return dsPartitions;
    }

    public void setDsPartitions(List<String> dsPartitions) {
        this.dsPartitions = dsPartitions;
    }

    public String getDtType() {
        return dtType;
    }

    public void setDtType(String dtType) {
        this.dtType = dtType;
    }

    public int getDataTarget() {
        return dataTarget;
    }

    public void setDataTarget(int dataTarget) {
        this.dataTarget = dataTarget;
    }

    public List<ColumnInfo> getDtColumns() {
        return dtColumns;
    }

    public void setDtColumns(List<ColumnInfo> dtColumns) {
        this.dtColumns = dtColumns;
    }

    public List<String> getDtPartitions() {
        return dtPartitions;
    }

    public void setDtPartitions(List<String> dtPartitions) {
        this.dtPartitions = dtPartitions;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
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

    public int getWriteMode() {
        return writeMode;
    }

    public void setWriteMode(int writeMode) {
        this.writeMode = writeMode;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public ElasticSearchDataxParams getElasticSearchDataxParams() {
        return elasticSearchDataxParams;
    }

    public void setElasticSearchDataxParams(ElasticSearchDataxParams elasticSearchDataxParams) {
        this.elasticSearchDataxParams = elasticSearchDataxParams;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getJobSpeedByte() {
        return jobSpeedByte;
    }

    public void setJobSpeedByte(int jobSpeedByte) {
        this.jobSpeedByte = jobSpeedByte;
    }

    public int getJobSpeedRecord() {
        return jobSpeedRecord;
    }

    public void setJobSpeedRecord(int jobSpeedRecord) {
        this.jobSpeedRecord = jobSpeedRecord;
    }

    public int getXms() {
        return xms;
    }

    public void setXms(int xms) {
        this.xms = xms;
    }

    public int getXmx() {
        return xmx;
    }

    public void setXmx(int xmx) {
        this.xmx = xmx;
    }

    @Override
    public String toString() {
        return "DataxParameters{" +
                "customConfig=" + customConfig +
                ", customSQL=" + customSQL +
                ", json='" + json + '\'' +
                ", dsType='" + dsType + '\'' +
                ", dataSource=" + dataSource +
                ", sourceTable='" + sourceTable + '\'' +
                ", where='" + where + '\'' +
                ", splitPk='" + splitPk + '\'' +
                ", dsColumns=" + dsColumns +
                ", dsPartitions=" + dsPartitions +
                ", dtType='" + dtType + '\'' +
                ", dataTarget=" + dataTarget +
                ", dtColumns=" + dtColumns +
                ", dtPartitions=" + dtPartitions +
                ", sql='" + sql + '\'' +
                ", targetTable='" + targetTable + '\'' +
                ", preStatements=" + preStatements +
                ", postStatements=" + postStatements +
                ", writeMode=" + writeMode +
                ", batchSize=" + batchSize +
                ", elasticSearchDataxParams=" + elasticSearchDataxParams +
                ", channel=" + channel +
                ", jobSpeedByte=" + jobSpeedByte +
                ", jobSpeedRecord=" + jobSpeedRecord +
                ", xms=" + xms +
                ", xmx=" + xmx +
                '}';
    }
}
