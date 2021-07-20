package org.apache.dolphinscheduler.server.entity;

import java.io.Serializable;

public class FlinkxTaskExecutionContext implements Serializable {

    /**
     * dataSourceId
     */
    private int dataSourceId;

    /**
     * sourcetype
     */
    private int sourcetype;

    /**
     * sourceConnectionParams
     */
    private String sourceConnectionParams;

    /**
     * dataTargetId
     */
    private int dataTargetId;

    /**
     * targetType
     */
    private int targetType;

    /**
     * targetConnectionParams
     */
    private String targetConnectionParams;

    public int getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(int dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public int getSourcetype() {
        return sourcetype;
    }

    public void setSourcetype(int sourcetype) {
        this.sourcetype = sourcetype;
    }

    public String getSourceConnectionParams() {
        return sourceConnectionParams;
    }

    public void setSourceConnectionParams(String sourceConnectionParams) {
        this.sourceConnectionParams = sourceConnectionParams;
    }

    public int getDataTargetId() {
        return dataTargetId;
    }

    public void setDataTargetId(int dataTargetId) {
        this.dataTargetId = dataTargetId;
    }

    public int getTargetType() {
        return targetType;
    }

    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }

    public String getTargetConnectionParams() {
        return targetConnectionParams;
    }

    public void setTargetConnectionParams(String targetConnectionParams) {
        this.targetConnectionParams = targetConnectionParams;
    }

    @Override
    public String toString() {
        return "FlinkxTaskExecutionContext{" +
                "dataSourceId=" + dataSourceId +
                ", sourcetype=" + sourcetype +
                ", sourceConnectionParams='" + sourceConnectionParams + '\'' +
                ", dataTargetId=" + dataTargetId +
                ", targetType=" + targetType +
                ", targetConnectionParams='" + targetConnectionParams + '\'' +
                '}';
    }

}
