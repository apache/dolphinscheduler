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
package org.apache.dolphinscheduler.common.task.sqoop.sources;

import org.apache.dolphinscheduler.common.process.Property;

import java.util.List;

/**
 * source mysql parameter
 */
public class SourceMysqlParameter {

    /**
     * src datasource
     */
    private int srcDatasource;
    /**
     * src table
     */
    private String srcTable;
    /**
     * src query type
     */
    private int srcQueryType;
    /**
     * src query sql
     */
    private String srcQuerySql;
    /**
     * src column type
     */
    private int srcColumnType;
    /**
     * src columns
     */
    private String srcColumns;
    /**
     * src condition list
     */
    private List<Property> srcConditionList;
    /**
     * map column hive
     */
    private List<Property> mapColumnHive;
    /**
     * map column java
     */
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
