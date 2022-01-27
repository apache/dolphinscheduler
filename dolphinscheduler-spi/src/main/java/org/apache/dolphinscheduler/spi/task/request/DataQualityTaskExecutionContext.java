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

package org.apache.dolphinscheduler.spi.task.request;

import java.io.Serializable;

/**
 * DataQualityTaskExecutionContext
 */
public class DataQualityTaskExecutionContext implements Serializable {

    /**
     * rule id
     */
    private int ruleId;
    /**
     * rule name
     */
    private String ruleName;
    /**
     * rule type
     */
    private int ruleType;
    /**
     * input entry list
     */
    private String ruleInputEntryList;
    /**
     *  execute sql list
     */
    private String executeSqlList;
    /**
     * if comparison value calculate from statistics value table
     */
    private boolean comparisonNeedStatisticsValueTable = false;
    /**
     * compare with fixed value
     */
    private boolean compareWithFixedValue = false;
    /**
     * error output path
     */
    private String hdfsPath;
    /**
     * sourceConnector type
     */
    private String sourceConnectorType;
    /**
     * source type
     */
    private int sourceType;
    /**
     * source connection params
     */
    private String sourceConnectionParams;
    /**
     * target connector type
     */
    private String targetConnectorType;
    /**
     * target type
     */
    private int targetType;
    /**
     * target connection params
     */
    private String targetConnectionParams;
    /**
     * source connector type
     */
    private String writerConnectorType;
    /**
     * writer type
     */
    private int writerType;
    /**
     * writer table
     */
    private String writerTable;
    /**
     * writer connection params
     */
    private String writerConnectionParams;
    /**
     * statistics value connector type
     */
    private String statisticsValueConnectorType;
    /**
     * statistics value type
     */
    private int statisticsValueType;
    /**
     * statistics value table
     */
    private String statisticsValueTable;
    /**
     * statistics value writer connection params
     */
    private String statisticsValueWriterConnectionParams;

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public String getSourceConnectorType() {
        return sourceConnectorType;
    }

    public void setSourceConnectorType(String sourceConnectorType) {
        this.sourceConnectorType = sourceConnectorType;
    }

    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceConnectionParams() {
        return sourceConnectionParams;
    }

    public void setSourceConnectionParams(String sourceConnectionParams) {
        this.sourceConnectionParams = sourceConnectionParams;
    }

    public String getTargetConnectorType() {
        return targetConnectorType;
    }

    public void setTargetConnectorType(String targetConnectorType) {
        this.targetConnectorType = targetConnectorType;
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

    public int getWriterType() {
        return writerType;
    }

    public void setWriterType(int writerType) {
        this.writerType = writerType;
    }

    public String getWriterConnectionParams() {
        return writerConnectionParams;
    }

    public void setWriterConnectionParams(String writerConnectionParams) {
        this.writerConnectionParams = writerConnectionParams;
    }

    public String getWriterTable() {
        return writerTable;
    }

    public void setWriterTable(String writerTable) {
        this.writerTable = writerTable;
    }

    public String getWriterConnectorType() {
        return writerConnectorType;
    }

    public void setWriterConnectorType(String writerConnectorType) {
        this.writerConnectorType = writerConnectorType;
    }

    public String getStatisticsValueConnectorType() {
        return statisticsValueConnectorType;
    }

    public void setStatisticsValueConnectorType(String statisticsValueConnectorType) {
        this.statisticsValueConnectorType = statisticsValueConnectorType;
    }

    public int getStatisticsValueType() {
        return statisticsValueType;
    }

    public void setStatisticsValueType(int statisticsValueType) {
        this.statisticsValueType = statisticsValueType;
    }

    public String getStatisticsValueTable() {
        return statisticsValueTable;
    }

    public void setStatisticsValueTable(String statisticsValueTable) {
        this.statisticsValueTable = statisticsValueTable;
    }

    public String getStatisticsValueWriterConnectionParams() {
        return statisticsValueWriterConnectionParams;
    }

    public void setStatisticsValueWriterConnectionParams(String statisticsValueWriterConnectionParams) {
        this.statisticsValueWriterConnectionParams = statisticsValueWriterConnectionParams;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public int getRuleType() {
        return ruleType;
    }

    public void setRuleType(int ruleType) {
        this.ruleType = ruleType;
    }

    public String getRuleInputEntryList() {
        return ruleInputEntryList;
    }

    public void setRuleInputEntryList(String ruleInputEntryList) {
        this.ruleInputEntryList = ruleInputEntryList;
    }

    public String getExecuteSqlList() {
        return executeSqlList;
    }

    public void setExecuteSqlList(String executeSqlList) {
        this.executeSqlList = executeSqlList;
    }

    public boolean isComparisonNeedStatisticsValueTable() {
        return comparisonNeedStatisticsValueTable;
    }

    public void setComparisonNeedStatisticsValueTable(boolean comparisonNeedStatisticsValueTable) {
        this.comparisonNeedStatisticsValueTable = comparisonNeedStatisticsValueTable;
    }

    public boolean isCompareWithFixedValue() {
        return compareWithFixedValue;
    }

    public void setCompareWithFixedValue(boolean compareWithFixedValue) {
        this.compareWithFixedValue = compareWithFixedValue;
    }

    public String getHdfsPath() {
        return hdfsPath;
    }

    public void setHdfsPath(String hdfsPath) {
        this.hdfsPath = hdfsPath;
    }
}
