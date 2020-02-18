package org.apache.dolphinscheduler.common.task.sqoop.sources;

import org.apache.dolphinscheduler.common.process.Property;

import java.util.List;

public class SourceMysqlParameter {

    private int srcDatasource;
    private String srcTable;
    private int srcQueryType;
    private String srcQuerySql;
    private int srcColumnType;
    private String srcColumns;
    private List<Property> srcConditionList;
    private List<Property> mapColumnHive;
    private List<Property> mapColumnJava;

    public int getSrcDatasource() {
        return srcDatasource;
    }

    public void setSrcDatasource(int srcDatasource) {
        this.srcDatasource = srcDatasource;
    }

    public String getSrcTable() {
        return srcTable;
    }

    public void setSrcTable(String srcTable) {
        this.srcTable = srcTable;
    }

    public int getSrcQueryType() {
        return srcQueryType;
    }

    public void setSrcQueryType(int srcQueryType) {
        this.srcQueryType = srcQueryType;
    }

    public String getSrcQuerySql() {
        return srcQuerySql;
    }

    public void setSrcQuerySql(String srcQuerySql) {
        this.srcQuerySql = srcQuerySql;
    }

    public int getSrcColumnType() {
        return srcColumnType;
    }

    public void setSrcColumnType(int srcColumnType) {
        this.srcColumnType = srcColumnType;
    }

    public String getSrcColumns() {
        return srcColumns;
    }

    public void setSrcColumns(String srcColumns) {
        this.srcColumns = srcColumns;
    }

    public List<Property> getSrcConditionList() {
        return srcConditionList;
    }

    public void setSrcConditionList(List<Property> srcConditionList) {
        this.srcConditionList = srcConditionList;
    }

    public List<Property> getMapColumnHive() {
        return mapColumnHive;
    }

    public void setMapColumnHive(List<Property> mapColumnHive) {
        this.mapColumnHive = mapColumnHive;
    }

    public List<Property> getMapColumnJava() {
        return mapColumnJava;
    }

    public void setMapColumnJava(List<Property> mapColumnJava) {
        this.mapColumnJava = mapColumnJava;
    }
}
