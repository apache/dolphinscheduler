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

package org.apache.dolphinscheduler.plugin.task.api.parameters;

import org.apache.dolphinscheduler.plugin.task.api.enums.DependentRelation;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DependentParameters extends AbstractParameters {

    private List<DependentTaskModel> dependTaskList;
    private DependentRelation relation;
    /** Time unit is second */
    private Integer checkInterval;
    private DependentFailurePolicyEnum failurePolicy;
    /** Time unit is minutes */
    private Integer failureWaitingTime;

    @Override
    public boolean checkParameters() {
        return true;
    }

    /**
     * the dependent task failure policy.
     */
    public enum DependentFailurePolicyEnum {
        DEPENDENT_FAILURE_FAILURE,
        DEPENDENT_FAILURE_WAITING
    }

}
