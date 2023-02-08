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

package org.apache.dolphinscheduler.plugin.task.api.parameters.dataquality;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.dataquality.spark.SparkParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * DataQualityParameters
 */
@Slf4j
public class DataQualityParameters extends AbstractParameters {

    /**
     * rule id
     */
    private int ruleId;
    /**
     * rule input entry value map
     */
    private Map<String, String> ruleInputParameter;
    /**
     * spark parameters
     */
    private SparkParameters sparkParameters;

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
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
            log.error("rule id is null");
            return false;
        }

        if (MapUtils.isEmpty(ruleInputParameter)) {
            log.error("rule input parameter is empty");
            return false;
        }

        return sparkParameters != null;
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    public SparkParameters getSparkParameters() {
        return sparkParameters;
    }

    public void setSparkParameters(SparkParameters sparkParameters) {
        this.sparkParameters = sparkParameters;
    }

}
