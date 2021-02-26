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

package org.apache.dolphinscheduler.common.task.dq;

import org.apache.dolphinscheduler.common.enums.dq.FormType;
import org.apache.dolphinscheduler.common.enums.dq.ValueType;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.dq.rule.ComparisonParameter;
import org.apache.dolphinscheduler.common.task.dq.rule.RuleDefinition;
import org.apache.dolphinscheduler.common.task.dq.rule.RuleInputEntry;
import org.apache.dolphinscheduler.common.task.spark.SparkParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.MathUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;

import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataQualityParameters
 */
public class DataQualityParameters extends AbstractParameters {

    private static final Logger logger = LoggerFactory.getLogger(org.apache.dolphinscheduler.common.task.dq.DataQualityParameters.class);

    /**
     * ruleId
     */
    private int ruleId;

    /**
     * ruleJson
     */
    private String ruleJson;

    /**
     * ruleInputParameter
     */
    private Map<String,String> ruleInputParameter;

    /**
     * sparkParameters
     */
    private SparkParameters sparkParameters;

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleJson() {
        return ruleJson;
    }

    public void setRuleJson(String ruleJson) {
        this.ruleJson = ruleJson;
    }

    public Map<String, String> getRuleInputParameter() {
        return ruleInputParameter;
    }

    public void setRuleInputParameter(Map<String, String> ruleInputParameter) {
        this.ruleInputParameter = ruleInputParameter;
    }

    /**
     * In this function ,we need more detailed check every parameter,
     * if the parameter is non-conformant will return false
     * @return boolean result
     */
    @Override
    public boolean checkParameters() {

        if (ruleId == 0) {
            return false;
        }

        if (StringUtils.isEmpty(ruleJson)) {
            return false;
        }

        RuleDefinition ruleDefinition = JSONUtils.parseObject(ruleJson,RuleDefinition.class);
        if (ruleDefinition == null) {
            return false;
        }

        if (MapUtils.isEmpty(ruleInputParameter)) {
            return false;
        }

        List<RuleInputEntry> defaultInputEntryList  = ruleDefinition.getRuleInputEntryList();
        ComparisonParameter comparisonParameter = ruleDefinition.getComparisonParameter();
        defaultInputEntryList.addAll(comparisonParameter.getInputEntryList());

        for (RuleInputEntry ruleInputEntry: defaultInputEntryList) {
            if (ruleInputEntry.getCanEdit() && FormType.INPUT == ruleInputEntry.getType()) {
                String value = ruleInputParameter.get(ruleInputEntry.getField());
                if (StringUtils.isNotEmpty(value)) {
                    ValueType valueType = ruleInputEntry.getValueType();
                    switch (valueType) {
                        case STRING:
                            if (value.contains(",")) {
                                logger.error("{} [{}] can not contains ',' ", ruleInputEntry.getField(),value);
                                return false;
                            }
                            break;
                        case NUMBER:
                            if (!MathUtils.isNum(value)) {
                                logger.error("{} [{}] should be a num", ruleInputEntry.getField(),value);
                                return false;
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        return sparkParameters != null;
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return null;
    }

    public SparkParameters getSparkParameters() {
        return sparkParameters;
    }

    public void setSparkParameters(SparkParameters sparkParameters) {
        this.sparkParameters = sparkParameters;
    }
}
