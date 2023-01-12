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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CycleEnum;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;

import java.util.List;

/**
 * dependent process definition
 */
public class DependentProcessDefinition {

    /**
     * process definition code
     */
    private long processDefinitionCode;

    /**
     * process definition name
     */
    private String processDefinitionName;

    /**
     * process definition version
     **/
    private int processDefinitionVersion;

    /**
     * task definition name
     */
    private long taskDefinitionCode;

    /**
     * task definition params
     */
    private String taskParams;

    /**
     * schedule worker group
     */
    private String workerGroup;

    /**
     * get dependent cycle
     * @return CycleEnum
     */
    public CycleEnum getDependentCycle(long upstreamProcessDefinitionCode) {
        DependentParameters dependentParameters = this.getDependentParameters();
        List<DependentTaskModel> dependentTaskModelList = dependentParameters.getDependTaskList();

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
        return JSONUtils.parseObject(getDependence(), DependentParameters.class);
    }

    public String getDependence() {
        return JSONUtils.getNodeString(this.taskParams, Constants.DEPENDENCE);
    }

    public String getProcessDefinitionName() {
        return this.processDefinitionName;
    }

    public void setProcessDefinitionName(String name) {
        this.processDefinitionName = name;
    }

    public long getProcessDefinitionCode() {
        return this.processDefinitionCode;
    }

    public void setProcessDefinitionCode(long code) {
        this.processDefinitionCode = code;
    }

    public int getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public void setProcessDefinitionVersion(int processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
    }

    public long getTaskDefinitionCode() {
        return this.taskDefinitionCode;
    }

    public void setTaskDefinitionCode(long code) {
        this.taskDefinitionCode = code;
    }

    public String getTaskParams() {
        return this.taskParams;
    }

    public void setTaskParams(String taskParams) {
        this.taskParams = taskParams;
    }

    public String getWorkerGroup() {
        return this.workerGroup;
    }

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }

}
