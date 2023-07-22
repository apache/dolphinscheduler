package org.apache.dolphinscheduler.plugin.task.sqoop.parameter;

/**
 * target common parameter
 */
public class TargetCommonParameter {

    /**
     * target datasource
     */
    protected int targetDatasource;

    public int getTargetDatasource() {
        return targetDatasource;
    }

    public void setTargetDatasource(int targetDatasource) {
        this.targetDatasource = targetDatasource;
    }

}
