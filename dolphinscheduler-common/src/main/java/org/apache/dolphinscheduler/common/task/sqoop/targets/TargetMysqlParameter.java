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
package org.apache.dolphinscheduler.common.task.sqoop.targets;

/**
 * target mysql parameter
 */
public class TargetMysqlParameter {

    /**
     * target datasource
     */
    private int targetDatasource;
    /**
     * target table
     */
    private String targetTable;
    /**
     * target columns
     */
    private String targetColumns;
    /**
     * fields terminated
     */
    private String fieldsTerminated;
    /**
     * lines terminated
     */
    private String linesTerminated;
    /**
     * pre query
     */
    private String preQuery;
    /**
     * is update
     */
    private boolean isUpdate;
    /**
     * target update key
     */
    private String targetUpdateKey;
    /**
     * target update mode
     */
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

    public boolean getIsUpdate() {
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
