package org.apache.dolphinscheduler.plugin.task.sqoop.parameter;

/**
 * source common parameter
 */
public class SourceCommonParameter {

    /**
     * src datasource
     */
    protected int srcDatasource;

    public int getSrcDatasource() {
        return srcDatasource;
    }

    public void setSrcDatasource(int srcDatasource) {
        this.srcDatasource = srcDatasource;
    }

}
