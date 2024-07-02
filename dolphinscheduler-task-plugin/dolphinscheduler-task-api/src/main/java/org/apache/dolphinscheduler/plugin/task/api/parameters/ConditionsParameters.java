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

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConditionsParameters extends AbstractParameters {

    private ConditionDependency dependence;

    private ConditionResult conditionResult;

    @Override
    public boolean checkParameters() {
        if (dependence == null || CollectionUtils.isEmpty(dependence.getDependTaskList())) {
            return false;
        }
        if (conditionResult == null || CollectionUtils.isEmpty(conditionResult.getSuccessNode())
                || CollectionUtils.isEmpty(conditionResult.getFailedNode())) {
            return false;
        }
        return true;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConditionDependency {

        private List<DependentTaskModel> dependTaskList;

        private DependentRelation relation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConditionResult {

        private boolean conditionSuccess;

        private List<Long> successNode;

        private List<Long> failedNode;
    }

}
