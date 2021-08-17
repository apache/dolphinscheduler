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

package org.apache.dolphinscheduler.server.entity;

import org.apache.dolphinscheduler.common.enums.dq.RuleType;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    private RuleType ruleType;
    /**
     * input entry list
     */
    private List<DqRuleInputEntry> ruleInputEntryList;
    /**
     *  execute sql list
     */
    private List<DqRuleExecuteSql> executeSqlList;
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

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public List<DqRuleInputEntry> getRuleInputEntryList() {
        return ruleInputEntryList;
    }

    public void setRuleInputEntryList(List<DqRuleInputEntry> ruleInputEntryList) {
        this.ruleInputEntryList = ruleInputEntryList;
    }

    public void addRuleInputEntry(DqRuleInputEntry ruleInputEntry) {
        if (this.ruleInputEntryList == null) {
            this.ruleInputEntryList = new ArrayList<>();
        }
        this.ruleInputEntryList.add(ruleInputEntry);
    }

    public List<DqRuleExecuteSql> getExecuteSqlList() {
        return executeSqlList;
    }

    public void setExecuteSqlList(List<DqRuleExecuteSql> executeSqlList) {
        this.executeSqlList = executeSqlList;
    }

    public void addExecuteSql(DqRuleExecuteSql executeSqlDefinition) {
        if (this.executeSqlList == null) {
            this.executeSqlList = new ArrayList<>();
        }
        this.executeSqlList.add(executeSqlDefinition);
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
