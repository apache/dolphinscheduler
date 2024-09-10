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

package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.common.enums.CycleEnum;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;

import java.util.List;

import lombok.Data;

@Data
public class DependentWorkflowDefinition {

    private long workflowDefinitionCode;

    private int workflowDefinitionVersion;

    private long taskDefinitionCode;

    private String taskParams;

    private String workerGroup;

    public CycleEnum getDependentCycle(long upstreamProcessDefinitionCode) {
        DependentParameters dependentParameters = this.getDependentParameters();
        List<DependentTaskModel> dependentTaskModelList = dependentParameters.getDependence().getDependTaskList();

        for (DependentTaskModel dependentTaskModel : dependentTaskModelList) {
            List<DependentItem> dependentItemList = dependentTaskModel.getDependItemList();
            for (DependentItem dependentItem : dependentItemList) {
                if (upstreamProcessDefinitionCode == dependentItem.getDefinitionCode()) {
                    return cycle2CycleEnum(dependentItem.getCycle());
                }
            }
        }

        return CycleEnum.DAY;
    }

    public CycleEnum cycle2CycleEnum(String cycle) {
        CycleEnum cycleEnum = null;

        switch (cycle) {
            case "day":
                cycleEnum = CycleEnum.DAY;
                break;
            case "hour":
                cycleEnum = CycleEnum.HOUR;
                break;
            case "week":
                cycleEnum = CycleEnum.WEEK;
                break;
            case "month":
                cycleEnum = CycleEnum.MONTH;
                break;
            default:
                break;
        }
        return cycleEnum;
    }

    public DependentParameters getDependentParameters() {
        return JSONUtils.parseObject(taskParams, DependentParameters.class);
    }

}
