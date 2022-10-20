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

package org.apache.dolphinscheduler.plugin.task.datax.entity;

import lombok.Data;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.datax.DataxTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.datax.entity.ColumnInfo;
import org.apache.dolphinscheduler.spi.enums.Flag;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * DataX parameter
 */
@Data
public class DataxParameters extends AbstractParameters {

    /**
     * if custom json config，eg  0, 1
     */
    private int customConfig;

    /**
     * if custom SQL，eg  0, 1
     */
    private int customSQL;

    /**
     * if customConfig eq 1 ,then json is usable
     */
    private String json;

    // Note: for reader part

    /**
     * data source type，eg  MYSQL, POSTGRES ...
     */
    private String dsType;

    /**
     * datasource id
     */
    private int dataSource;

    /**
     * source table
     */
    private String sourceTable;

    /**
     * where condition for select from source table
     */
    private String where;

    /**
     * split key. eg id
     */
    private String splitPk;

    /**
     * column infos of source table
     */
    private List<ColumnInfo> dsColumns;

    /**
     * partition values of source table
     */
    private List<String> dsPartitions;

    // Note: for writer part

    /**
     * data target type，eg  MYSQL, POSTGRES ...
     */
    private String dtType;

    /**
     * datatarget id
     */
    private int dataTarget;

    /**
     * column infos of target table
     */
    private List<ColumnInfo> dtColumns;

    /**
     * partition values of target table
     */
    private List<String> dtPartitions;

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
     * Write Mode. eg INSERT INTO, UPDATE ON DUPLICATE ...
     */
    private int writeMode;

    /**
     * batch size for one commit
     */
    private int batchSize;

    // Note: for setting part

    /**
     * channel
     */
    private int channel;

    /**
     * speed byte num
     */
    private int jobSpeedByte;

    /**
     * speed record count
     */
    private int jobSpeedRecord;

    /**
     * Xms memory
     */
    private int xms;

    /**
     * Xmx memory
     */
    private int xmx;

    @Override
    public boolean checkParameters() {
        //TODO: 待前端接入后更新
        if(customConfig == Flag.YES.ordinal()){
            return StringUtils.isNotEmpty(json);
        }
        else {
            return dataSource != 0
                    && dataTarget != 0
                    && StringUtils.isNotEmpty(sql)
                    && StringUtils.isNotEmpty(targetTable);
        }
//        if(customConfig == Flag.YES.ordinal()){
//            return StringUtils.isNotEmpty(json);
//        }
//        else if(customSQL == Flag.YES.ordinal()) {
//            return dataSource != 0
//                    && dataTarget != 0
//                    && StringUtils.isNotEmpty(sql)
//                    && StringUtils.isNotEmpty(targetTable);
//        }else {
//            return dataSource != 0
//                    && dataTarget != 0
//                    && StringUtils.isNotEmpty(sourceTable)
//                    && StringUtils.isNotEmpty(targetTable)
//                    && CollectionUtils.isNotEmpty(dsColumns)
//                    && CollectionUtils.isNotEmpty(dtColumns);
//        }
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "DataxParameters{" +
                "customConfig=" + customConfig +
                ", customSQL=" + customSQL +
                ", json='" + json + '\'' +
                ", dsType='" + dsType + '\'' +
                ", dataSource=" + dataSource +
                ", sourceTable='" + sourceTable + '\'' +
                ", where='" + where + '\'' +
                ", splitKey='" + splitPk + '\'' +
                ", dsColumns=" + dsColumns +
                ", dtType='" + dtType + '\'' +
                ", dataTarget=" + dataTarget +
                ", dtColumns=" + dtColumns +
                ", sql='" + sql + '\'' +
                ", targetTable='" + targetTable + '\'' +
                ", preStatements=" + preStatements +
                ", postStatements=" + postStatements +
                ", writeMode=" + writeMode +
                ", batchSize=" + batchSize +
                ", channel=" + channel +
                ", jobSpeedByte=" + jobSpeedByte +
                ", jobSpeedRecord=" + jobSpeedRecord +
                ", xms=" + xms +
                ", xmx=" + xmx +
                '}';
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

    public DataxTaskExecutionContext generateExtendedContext(ResourceParametersHelper parametersHelper) {

        DataxTaskExecutionContext dataxTaskExecutionContext = new DataxTaskExecutionContext();
        if (customConfig == Flag.YES.ordinal()) {
            return dataxTaskExecutionContext;
        }

        DataSourceParameters dbSource = (DataSourceParameters) parametersHelper.getResourceParameters(ResourceType.DATASOURCE, dataSource);
        DataSourceParameters dbTarget = (DataSourceParameters) parametersHelper.getResourceParameters(ResourceType.DATASOURCE, dataTarget);

        if (Objects.nonNull(dbSource)) {
            dataxTaskExecutionContext.setDataSourceId(dataSource);
            dataxTaskExecutionContext.setSourcetype(dbSource.getType());
            dataxTaskExecutionContext.setSourceConnectionParams(dbSource.getConnectionParams());
        }

        if (Objects.nonNull(dbTarget)) {
            dataxTaskExecutionContext.setDataTargetId(dataTarget);
            dataxTaskExecutionContext.setTargetType(dbTarget.getType());
            dataxTaskExecutionContext.setTargetConnectionParams(dbTarget.getConnectionParams());
        }
        return dataxTaskExecutionContext;
    }
}
