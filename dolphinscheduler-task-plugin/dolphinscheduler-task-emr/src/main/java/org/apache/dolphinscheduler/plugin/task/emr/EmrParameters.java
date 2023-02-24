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

package org.apache.dolphinscheduler.plugin.task.emr;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmrParameters extends AbstractParameters {

    /**
     * emr program type
     * 0 RUN_JOB_FLOW, 1 ADD_JOB_FLOW_STEPS
     */
    private ProgramType programType;

    /**
     * job flow define in json format
     *
     * @see <a href="https://docs.aws.amazon.com/emr/latest/APIReference/API_RunJobFlow.html#API_RunJobFlow_Examples">API_RunJobFlow_Examples</a>
     */
    private String jobFlowDefineJson;

    /**
     * steps define in json format
     *
     * @see <a href="https://docs.aws.amazon.com/emr/latest/APIReference/API_AddJobFlowSteps.html#API_AddJobFlowSteps_Examples">API_AddJobFlowSteps_Examples</a>
     */
    private String stepsDefineJson;

    @Override
    public boolean checkParameters() {
        /*
         * When saving a task, the programType cannot be empty and jobFlowDefineJson or stepsDefineJson cannot be empty:
         * (1) When ProgramType is RUN_JOB_FLOW, jobFlowDefineJson cannot be empty. (2) When ProgramType is
         * ADD_JOB_FLOW_STEPS, stepsDefineJson cannot be empty.
         */
        return programType != null
                && (StringUtils.isNotEmpty(jobFlowDefineJson) || StringUtils.isNotEmpty(stepsDefineJson));
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return Collections.emptyList();

    }

    @Override
    public String toString() {
        return "EmrParameters{"
                + "programType=" + programType
                + ", jobFlowDefineJson='" + jobFlowDefineJson + '\''
                + ", stepsDefineJson='" + stepsDefineJson + '\''
                + '}';
    }
}
