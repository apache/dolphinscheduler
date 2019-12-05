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
package org.apache.dolphinscheduler.common.task.measure;

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.process.MeasureProperty;
import org.apache.dolphinscheduler.common.task.AbstractParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * measure parameter
 */
public class MeasureParameters extends AbstractParameters {

    /**
     * data source type，eg  MYSQL, POSTGRES, HIVE ...
     */
    private String type;

    /**
     * datasource id
     */
    private int datasource;

    /**
     * sql
     */
    private String sql;

    /**
     * show type
     * 0 TABLE
     * 1 TEXT
     * 2 attachment
     * 3 TABLE+attachment
     */
    private String showType;

    /**
     * SQL connection parameters
     */
    private String connParams;

    /**
     *  measure params
     */
    private List<MeasureProperty> measureParams;

    /**
     * title
     */
    private String title;

    /**
     * receivers
     */
    private String receivers;

    /**
     * receivers cc
     */
    private String receiversCc;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDatasource() {
        return datasource;
    }

    public void setDatasource(int datasource) {
        this.datasource = datasource;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getShowType() {
        return showType;
    }

    public void setShowType(String showType) {
        this.showType = showType;
    }

    public String getConnParams() {
        return connParams;
    }

    public void setConnParams(String connParams) {
        this.connParams = connParams;
    }

    public List<MeasureProperty> getMeasureParams() {
        return measureParams;
    }

    public void setMeasureParams(List<MeasureProperty> measureParams) {
        this.measureParams = measureParams;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReceivers() {
        return receivers;
    }

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }

    public String getReceiversCc() {
        return receiversCc;
    }

    public void setReceiversCc(String receiversCc) {
        this.receiversCc = receiversCc;
    }




    @Override
    public boolean checkParameters() {
        return datasource != 0 && StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(sql);
    }

    @Override
    public List<String> getResourceFilesList() {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "MeasureParameters{" +
                "type='" + type + '\'' +
                ", datasource=" + datasource +
                ", sql='" + sql + '\'' +
                ", showType='" + showType + '\'' +
                ", connParams='" + connParams + '\'' +
                ", measureParams=" + measureParams +
                ", title='" + title + '\'' +
                ", receivers='" + receivers + '\'' +
                ", receiversCc='" + receiversCc + '\'' +
                '}';
    }
}
