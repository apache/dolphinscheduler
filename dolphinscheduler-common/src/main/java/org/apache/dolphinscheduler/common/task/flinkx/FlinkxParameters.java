package org.apache.dolphinscheduler.common.task.flinkx;

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WriteMode;
import org.apache.dolphinscheduler.common.process.Column;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;

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
    private boolean polling;

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
    private String mode;

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
    private boolean isSplit;

    /**
     * split pk
     */
    private String splitPk;

    /**
     * if restore type，eg  true, false
     */
    private boolean isRestore;

    /**
     * if restore type，eg  true, false
     */
    private boolean isStream;

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
    private boolean isSqlStatement;

    /**
     * when using SqlStatement,this value needs to be used
     */
    private String customSql;


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

    public boolean isPolling() {
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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
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

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public List<Column> getSourceColumns() {
        return sourceColumns;
    }

    public void setSourceColumns(List<Column> sourceColumns) {
        this.sourceColumns = sourceColumns;
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

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public boolean isRestore() {
        return isRestore;
    }

    public void setRestore(boolean restore) {
        isRestore = restore;
    }

    public boolean isStream() {
        return isStream;
    }

    public void setStream(boolean stream) {
        isStream = stream;
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

    public int getJobSpeedChannel() {
        return jobSpeedChannel;
    }

    public void setJobSpeedChannel(int jobSpeedChannel) {
        this.jobSpeedChannel = jobSpeedChannel;
    }

    public boolean isSplit() {
        return isSplit;
    }

    public void setSplit(boolean isSplit) {
        this.isSplit = isSplit;
    }

    public String getSplitPk() {
        return splitPk;
    }

    public void setSplitPk(String splitPk) {
        this.splitPk = splitPk;
    }

    public boolean isSqlStatement() {
        return isSqlStatement;
    }

    public void setSqlStatement(boolean sqlStatement) {
        isSqlStatement = sqlStatement;
    }

    public String getCustomSql() {
        return customSql;
    }

    public void setCustomSql(String customSql) {
        this.customSql = customSql;
    }

    @Override
    public boolean checkParameters() {
        if (customConfig == Flag.NO.ordinal()) {
            if(isSqlStatement){
                return dataSource != 0
                        && dataTarget != 0
                        && StringUtils.isNotEmpty(customSql)
                        && StringUtils.isNotEmpty(targetColumns.toString())
                        && StringUtils.isNotEmpty(targetTable);
            }else {
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
                ", polling='" + polling + '\'' +
                ", pollingInterval='" + pollingInterval + '\'' +
                ", increColumn='" + increColumn + '\'' +
                ", startLocation='" + startLocation + '\'' +
                ", writeMode='" + writeMode + '\'' +
                ", uniqueKey='" + uniqueKey + '\'' +
                ", mode='" + mode + '\'' +
                ", dsType='" + dsType + '\'' +
                ", dataSource=" + dataSource +
                ", dtType='" + dtType + '\'' +
                ", dataTarget=" + dataTarget +
                ", sql='" + sql + '\'' +
                ", sourceTable='" + sourceTable + '\'' +
                ", targetTable='" + targetTable + '\'' +
                ", sourceColumns='" + sourceColumns + '\'' +
                ", targetColumns='" + targetColumns + '\'' +
                ", preStatements=" + preStatements + '\'' +
                ", postStatements=" + postStatements + '\'' +
                ", jobSpeedByte=" + jobSpeedByte + '\'' +
                ", isStream=" + isStream + '\'' +
                ", isRestore=" + isRestore + '\'' +
                ", restoreColumnIndex=" + restoreColumnIndex + '\'' +
                ", restoreColumnName=" + restoreColumnName + '\'' +
                ", maxRowNumForCheckpoint=" + maxRowNumForCheckpoint + '\'' +
                ", isSplit=" + isSplit + '\'' +
                ", splitPk=" + splitPk + '\'' +
                ", jobSpeedChannel=" + jobSpeedChannel + '\'' +
                ", isSqlStatement=" + isSqlStatement + '\'' +
                ", customSql=" + customSql + '\'' +
                '}';
    }
}
