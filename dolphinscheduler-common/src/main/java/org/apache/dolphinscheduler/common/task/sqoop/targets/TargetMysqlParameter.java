package org.apache.dolphinscheduler.common.task.sqoop.targets;

public class TargetMysqlParameter {

    private int targetDatasource;
    private String targetTable;
    private String targetColumns;
    private String fieldsTerminated;
    private String linesTerminated;
    private String preQuery;
    private boolean isUpdate;
    private String targetUpdateKey;
    private String targetUpdateMode;

    public int getTargetDatasource() {
        return targetDatasource;
    }

    public void setTargetDatasource(int targetDatasource) {
        this.targetDatasource = targetDatasource;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getTargetColumns() {
        return targetColumns;
    }

    public void setTargetColumns(String targetColumns) {
        this.targetColumns = targetColumns;
    }

    public String getFieldsTerminated() {
        return fieldsTerminated;
    }

    public void setFieldsTerminated(String fieldsTerminated) {
        this.fieldsTerminated = fieldsTerminated;
    }

    public String getLinesTerminated() {
        return linesTerminated;
    }

    public void setLinesTerminated(String linesTerminated) {
        this.linesTerminated = linesTerminated;
    }

    public String getPreQuery() {
        return preQuery;
    }

    public void setPreQuery(String preQuery) {
        this.preQuery = preQuery;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public String getTargetUpdateKey() {
        return targetUpdateKey;
    }

    public void setTargetUpdateKey(String targetUpdateKey) {
        this.targetUpdateKey = targetUpdateKey;
    }

    public String getTargetUpdateMode() {
        return targetUpdateMode;
    }

    public void setTargetUpdateMode(String targetUpdateMode) {
        this.targetUpdateMode = targetUpdateMode;
    }
}
