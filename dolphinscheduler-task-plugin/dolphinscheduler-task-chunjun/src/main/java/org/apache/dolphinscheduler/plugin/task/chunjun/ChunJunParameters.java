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

package org.apache.dolphinscheduler.plugin.task.chunjun;

import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.spi.enums.Flag;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * chunjun parameters
 */
public class ChunJunParameters extends AbstractParameters {

    /**
     * custom json config，default 1, support custom json
     */
    private int customConfig;

    /**
     * custom config json
     */
    private String json;

    /**
     * other arguments -confProp "{\"flink.checkpoint.interval\":60000}"
     */
    private String others;

    /**
     * deploy mode local standlone yarn-session yarn-per-job
     */
    private String deployMode;

    /**
     * customConfig value is 0, datasource type，eg mysql
     */
    private String dsType;

    /**
     * customConfig value is 0, datasource id int
     */
    private int dataSource;

    /**
     * customConfig value is 0, datasource targetType，eg  MYSQL, POSTGRES
     */
    private String dtType;

    /**
     * customConfig value is 0, data target id
     */
    private int dataTarget;

    /**
     * customConfig value is 0, sql
     */
    private String sql;

    /**
     * customConfig value is 0, target table
     */
    private String targetTable;

    /**
     * pre statements
     */
    private List<String> preStatements;

    /**
     * post statements
     */
    private List<String> postStatements;

    /**
     * customConfig value is 0, job speed byte
     */
    private int jobSpeedByte;

    /**
     * customConfig value is 0, job speed record count
     */
    private int jobSpeedRecord;


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

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    public String getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
    }

    @Override
    public String toString() {
        return "ChunJunParameters{"
            + "customConfig=" + customConfig
            + ", json='" + json + '\''
            + ", dsType='" + dsType + '\''
            + ", dataSource=" + dataSource
            + ", dtType='" + dtType + '\''
            + ", dataTarget=" + dataTarget
            + ", sql='" + sql + '\''
            + ", targetTable='" + targetTable + '\''
            + ", preStatements=" + preStatements
            + ", postStatements=" + postStatements
            + ", jobSpeedByte=" + jobSpeedByte
            + ", jobSpeedRecord=" + jobSpeedRecord
            + ", others=" + others
            + ", deployMode=" + deployMode
            + '}';
    }

    @Override
    public boolean checkParameters() {
        if (customConfig == Flag.NO.ordinal()) {
            return dataSource != 0 && dataTarget != 0
                && StringUtils.isNotEmpty(sql)
                && StringUtils.isNotEmpty(targetTable);
        } else {
            return StringUtils.isNotEmpty(json);
        }
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    @Override
    public ResourceParametersHelper getResources() {
        ResourceParametersHelper resources = super.getResources();

        if (customConfig == Flag.YES.ordinal()) {
            return resources;
        }
        resources.put(ResourceType.DATASOURCE, dataSource);
        resources.put(ResourceType.DATASOURCE, dataTarget);
        return resources;
    }
}
