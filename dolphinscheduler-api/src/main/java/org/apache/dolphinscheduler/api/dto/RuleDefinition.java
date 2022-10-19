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

package org.apache.dolphinscheduler.api.dto;

import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;

import java.util.List;

/**
 * RuleDefinition
 */
public class RuleDefinition {

    /**
     * rule input entry list
     */
    private List<DqRuleInputEntry> ruleInputEntryList;

    /**
     * rule execute sql list
     */
    private List<DqRuleExecuteSql> executeSqlList;

    public RuleDefinition() {
    }

    public RuleDefinition(List<DqRuleInputEntry> ruleInputEntryList, List<DqRuleExecuteSql> executeSqlList) {
        this.ruleInputEntryList = ruleInputEntryList;
        this.executeSqlList = executeSqlList;
    }

    public List<DqRuleInputEntry> getRuleInputEntryList() {
        return ruleInputEntryList;
    }

    public void setRuleInputEntryList(List<DqRuleInputEntry> ruleInputEntryList) {
        this.ruleInputEntryList = ruleInputEntryList;
    }

    public List<DqRuleExecuteSql> getExecuteSqlList() {
        return executeSqlList;
    }

    public void setExecuteSqlList(List<DqRuleExecuteSql> executeSqlList) {
        this.executeSqlList = executeSqlList;
    }
}
