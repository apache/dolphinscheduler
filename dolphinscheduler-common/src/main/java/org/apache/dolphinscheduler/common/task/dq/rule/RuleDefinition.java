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

package org.apache.dolphinscheduler.common.task.dq.rule;

import org.apache.dolphinscheduler.common.enums.dq.RuleType;

import java.util.List;

/**
 * RuleDefinition
 */
public class RuleDefinition {

    /**
     * ruleName
     */
    private String ruleName;
    /**
     * ruleType
     */
    private RuleType ruleType;
    /**
     * input entry list
     */
    private List<RuleInputEntry> ruleInputEntryList;
    /**
     * mid execute sql list
     */
    private List<ExecuteSqlDefinition> midExecuteSqlList;
    /**
     * statistics execute sql list
     */
    private List<ExecuteSqlDefinition> statisticsExecuteSqlList;

    private ComparisonParameter comparisonParameter;

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

    public List<RuleInputEntry> getRuleInputEntryList() {
        return ruleInputEntryList;
    }

    public void setRuleInputEntryList(List<RuleInputEntry> ruleInputEntryList) {
        this.ruleInputEntryList = ruleInputEntryList;
    }

    public List<ExecuteSqlDefinition> getMidExecuteSqlList() {
        return midExecuteSqlList;
    }

    public void setMidExecuteSqlList(List<ExecuteSqlDefinition> midExecuteSqlList) {
        this.midExecuteSqlList = midExecuteSqlList;
    }

    public List<ExecuteSqlDefinition> getStatisticsExecuteSqlList() {
        return statisticsExecuteSqlList;
    }

    public void setStatisticsExecuteSqlList(List<ExecuteSqlDefinition> statisticsExecuteSqlList) {
        this.statisticsExecuteSqlList = statisticsExecuteSqlList;
    }

    public ComparisonParameter getComparisonParameter() {
        return comparisonParameter;
    }

    public void setComparisonParameter(ComparisonParameter comparisonParameter) {
        this.comparisonParameter = comparisonParameter;
    }

}