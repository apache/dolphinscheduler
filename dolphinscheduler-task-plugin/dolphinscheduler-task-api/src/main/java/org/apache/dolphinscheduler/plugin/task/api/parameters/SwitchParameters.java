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

import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;

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
public class SwitchParameters extends AbstractParameters {

    // due to history reasons, the field name is switchResult
    private SwitchResult switchResult;

    // The next branch which should be executed after the switch logic task executed.
    private Long nextBranch;

    @Override
    public boolean checkParameters() {
        if (switchResult == null) {
            return false;
        }
        if (CollectionUtils.isEmpty(switchResult.getDependTaskList()) && switchResult.getNextNode() == null) {
            return false;
        }
        for (SwitchResultVo switchResultVo : switchResult.getDependTaskList()) {
            if (switchResultVo == null || switchResultVo.getNextNode() == null) {
                return false;
            }
        }
        return true;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SwitchResult {

        // switch condition
        private List<SwitchResultVo> dependTaskList;

        // default branch node code in switch task
        private Long nextNode;
    }

}
