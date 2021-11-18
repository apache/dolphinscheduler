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

package org.apache.dolphinscheduler.common.task.flinkx;

import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.enums.Flag;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class FlinkxParameters extends AbstractParameters {

    /**
     * if custom json config，eg  0, 1
     */
    private int customConfig;

    /**
     * if customConfig eq 1 ,then json is usable
     */
    private String json;

    /**
     * if polling
     */
    private Boolean polling;

    /**
     * if polling is true, need to set pollingInterval
     */
    private int pollingInterval;

    /**
     * if polling is true, need to set increColumn
     */
    private String increColumn;

    /**
     * if polling is true, need to set startLocation
     */
    private String startLocation;

    /**
     * write mode
     * INSERT,UPDATE,REPLACE
     */
    private WriteMode writeMode;

    /**
     * unique key
     */
    private List<String> uniqueKey;

    /**
     * flinkx deploy mode
     */
    private String deployMode;

    /**
     * data source type，eg  MYSQL, POSTGRES ...
     */
    private String dsType;

    /**
     * datasource id
     */
    private int dataSource;

    /**
     * data target type，eg  MYSQL, POSTGRES ...
     */
    private String dtType;

    /**
     * datatarget id
     */
    private int dataTarget;

    /**
     * sql
     */
    private String sql;

    /**
     * source table
     */
    private String sourceTable;

    /**
     * source table columns
     */
    private List<Column> sourceColumns;

    /**
     * target table
     */
    private String targetTable;

    /**
     * target table columns
     */
    private List<Column> targetColumns;

    /**
     * Pre Statements
     */
    private List<String> preStatements;

    /**
     * Post Statements
     */
    private List<String> postStatements;

    /**
     * speed byte num
     */
    private int jobSpeedByte;

    /**
     * speed channel num
     */
    private int jobSpeedChannel;

    /**
     * is split pk
     */
    private boolean split;

    /**
     * split pk
     */
    private String splitPk;

    /**
     * if restore type，eg  true, false
     */
    private Boolean restore;

    /**
     * if restore type，eg  true, false
     */
    private Boolean stream;

    /**
     * restore column index, eg -1
     */
    private int restoreColumnIndex;

    /**
     * restore column name, eg ''
     */
    private String restoreColumnName;

    /**
     * max row num for checkpoint, eg 10000
     */
    private int maxRowNumForCheckpoint;

    /**
     * when using SqlStatement,it's true
     */
    private Boolean sqlStatement;

    /**
     * when using SqlStatement,this value needs to be used
     */
    private String customSql;

    private int batchSize;

    public int getCustomConfig() {
        return customConfig;
    }

    public void setCustomConfig(int customConfig) {
        this.customConfig = customConfig;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public boolean getPolling() {
        return polling;
    }

    public void setPolling(boolean polling) {
        this.polling = polling;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public String getIncreColumn() {
        return increColumn;
    }

    public void setIncreColumn(String increColumn) {
        this.increColumn = increColumn;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public WriteMode getWriteMode() {
        return writeMode;
    }

    public void setWriteMode(WriteMode writeMode) {
        this.writeMode = writeMode;
    }

    public List<String> getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(List<String> uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
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

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public List<Column> getSourceColumns() {
        return sourceColumns;
    }

    public void setSourceColumns(List<Column> sourceColumns) {
        this.sourceColumns = sourceColumns;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public List<Column> getTargetColumns() {
        return targetColumns;
    }

    public void setTargetColumns(List<Column> targetColumns) {
        this.targetColumns = targetColumns;
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

    public int getJobSpeedByte() {
        return jobSpeedByte;
    }

    public void setJobSpeedByte(int jobSpeedByte) {
        this.jobSpeedByte = jobSpeedByte;
    }

    public int getJobSpeedChannel() {
        return jobSpeedChannel;
    }

    public void setJobSpeedChannel(int jobSpeedChannel) {
        this.jobSpeedChannel = jobSpeedChannel;
    }

    public boolean getSplit() {
        return split;
    }

    public void setSplit(boolean split) {
        this.split = split;
    }

    public String getSplitPk() {
        return splitPk;
    }

    public void setSplitPk(String splitPk) {
        this.splitPk = splitPk;
    }

    public boolean getRestore() {
        return restore;
    }

    public void setRestore(boolean restore) {
        this.restore = restore;
    }

    public boolean getStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public int getRestoreColumnIndex() {
        return restoreColumnIndex;
    }

    public void setRestoreColumnIndex(int restoreColumnIndex) {
        this.restoreColumnIndex = restoreColumnIndex;
    }

    public String getRestoreColumnName() {
        return restoreColumnName;
    }

    public void setRestoreColumnName(String restoreColumnName) {
        this.restoreColumnName = restoreColumnName;
    }

    public int getMaxRowNumForCheckpoint() {
        return maxRowNumForCheckpoint;
    }

    public void setMaxRowNumForCheckpoint(int maxRowNumForCheckpoint) {
        this.maxRowNumForCheckpoint = maxRowNumForCheckpoint;
    }

    public boolean getSqlStatement() {
        return sqlStatement;
    }

    public void setSqlStatement(boolean sqlStatement) {
        this.sqlStatement = sqlStatement;
    }

    public String getCustomSql() {
        return customSql;
    }

    public void setCustomSql(String customSql) {
        this.customSql = customSql;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public boolean checkParameters() {
        if (customConfig == Flag.NO.ordinal()) {
            if (sqlStatement) {
                return dataSource != 0
                        && dataTarget != 0
                        && StringUtils.isNotEmpty(customSql)
                        && StringUtils.isNotEmpty(targetColumns.toString())
                        && StringUtils.isNotEmpty(targetTable);
            } else {
                return dataSource != 0
                        && dataTarget != 0
                        && StringUtils.isNotEmpty(sourceColumns.toString())
                        && StringUtils.isNotEmpty(sourceTable)
                        && StringUtils.isNotEmpty(targetColumns.toString())
                        && StringUtils.isNotEmpty(targetTable);
            }
        } else {
            return StringUtils.isNotEmpty(json);
        }
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "FlinkxParameters{" +
                "customConfig=" + customConfig +
                ", json='" + json + '\'' +
                ", polling=" + polling +
                ", pollingInterval=" + pollingInterval +
                ", increColumn='" + increColumn + '\'' +
                ", startLocation='" + startLocation + '\'' +
                ", writeMode=" + writeMode +
                ", uniqueKey=" + uniqueKey +
                ", deployMode='" + deployMode + '\'' +
                ", dsType='" + dsType + '\'' +
                ", dataSource=" + dataSource +
                ", dtType='" + dtType + '\'' +
                ", dataTarget=" + dataTarget +
                ", sql='" + sql + '\'' +
                ", sourceTable='" + sourceTable + '\'' +
                ", sourceColumns=" + sourceColumns +
                ", targetTable='" + targetTable + '\'' +
                ", targetColumns=" + targetColumns +
                ", preStatements=" + preStatements +
                ", postStatements=" + postStatements +
                ", jobSpeedByte=" + jobSpeedByte +
                ", jobSpeedChannel=" + jobSpeedChannel +
                ", split=" + split +
                ", splitPk=" + splitPk +
                ", restore=" + restore +
                ", stream=" + stream +
                ", restoreColumnIndex=" + restoreColumnIndex +
                ", restoreColumnName=" + restoreColumnName +
                ", maxRowNumForCheckpoint=" + maxRowNumForCheckpoint +
                ", sqlStatement=" + sqlStatement +
                ", customSql='" + customSql + '\'' +
                ", batchSize=" + batchSize +
                '}';
    }
}
