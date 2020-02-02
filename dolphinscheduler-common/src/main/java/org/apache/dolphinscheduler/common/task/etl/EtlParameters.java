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
package org.apache.dolphinscheduler.common.task.etl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.task.AbstractParameters;

/**
 * Sql/Hql parameter
 */
public class EtlParameters extends AbstractParameters {

    /**
     * data source type，eg  MYSQL, POSTGRES ...
     */
    private String dsType;

    /**
     * datasource id
     */
    private int datasource;

    /**
     * data target type，eg  MYSQL, POSTGRES ...
     */
    private String dtType;

    /**
     * datatarget id
     */
    private int datatarget;

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
     * speed byte num
     */
    private int jobSpeedByte;

    /**
     * speed record count
     */
    private int jobSpeedRecord;

    public String getDsType() {
        return dsType;
    }

    public void setDsType(String dsType) {
        this.dsType = dsType;
    }

    public int getDatasource() {
        return datasource;
    }

    public void setDatasource(int datasource) {
        this.datasource = datasource;
    }

    public String getDtType() {
        return dtType;
    }

    public void setDtType(String dtType) {
        this.dtType = dtType;
    }

    public int getDatatarget() {
        return datatarget;
    }

    public void setDatatarget(int datatarget) {
        this.datatarget = datatarget;
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

    @Override
    public boolean checkParameters() {
        if (!(datasource != 0 && StringUtils.isNotEmpty(dsType) && StringUtils.isNotEmpty(sql))) {
            return false;
        }

        if (!(datatarget != 0 && StringUtils.isNotEmpty(dtType) && StringUtils.isNotEmpty(targetTable))) {
            return false;
        }

        return true;
    }

    @Override
    public List<String> getResourceFilesList() {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "EtlParameters{" +
                "dsType='" + dsType + '\'' +
                ", datasource=" + datasource +
                ", dtType='" + dtType + '\'' +
                ", datatarget=" + datatarget +
                ", sql='" + sql + '\'' +
                ", targetTable='" + targetTable + '\'' +
                ", jobSpeedByte='" + jobSpeedByte + '\'' +
                ", jobSpeedRecord='" + jobSpeedRecord + '\'' +
                '}';
    }
}
