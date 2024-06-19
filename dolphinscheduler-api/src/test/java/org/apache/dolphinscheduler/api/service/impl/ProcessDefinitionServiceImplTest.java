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
package org.apache.dolphinscheduler.api.service.impl;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_CONDITIONS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_DEPENDENT;
import static org.mockito.ArgumentMatchers.any;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ConditionsParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ProcessDefinitionServiceImplTest {

    @Test
    void rebuildConditionTaskDefinitionTest_1() {

        ProcessDefinitionServiceImpl processDefinitionService = Mockito.spy(new ProcessDefinitionServiceImpl());

        String taskParams =
                "{\"localParams\":[],\"resourceList\":[],\"dependence\":{\"relation\":\"AND\",\"dependTaskList\":[{\"relation\":\"AND\","
                        +
                        "\"dependItemList\":[{\"depTaskCode\":13939480829216,\"status\":\"SUCCESS\"},{\"depTaskCode\":13939482198816,\"status\":\"FAILURE\"}]}]},"
                        +
                        "\"conditionResult\":{}}";

        ArrayList<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();

        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
        taskDefinitionLog.setTaskParams(taskParams);
        taskDefinitionLog.setTaskType(TASK_TYPE_DEPENDENT);
        taskDefinitionLogs.add(taskDefinitionLog);

        HashMap<Long, Long> taskCodeMap = new HashMap<>();
        taskCodeMap.put(13939480829216L, 1L);
        taskCodeMap.put(13939482198816L, 2L);

        processDefinitionService.rebuildConditionTaskDefinition(taskDefinitionLogs, taskCodeMap);

        Mockito.verify(processDefinitionService, Mockito.times(0)).createSingleProcessDefinition(any(), any());

    }

    @Test
    void rebuildConditionTaskDefinitionTest_2() {

        ProcessDefinitionServiceImpl processDefinitionService = new ProcessDefinitionServiceImpl();
        String taskParams =
                "{\"localParams\":[],\"resourceList\":[],\"dependence\":{\"relation\":\"AND\",\"dependTaskList\":[{\"relation\":\"AND\","
                        +
                        "\"dependItemList\":[{\"depTaskCode\":13939480829216,\"status\":\"SUCCESS\"},{\"depTaskCode\":13939482198816,\"status\":\"FAILURE\"}]}]},"
                        +
                        "\"conditionResult\":{}}";

        ArrayList<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();

        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
        taskDefinitionLog.setTaskParams(taskParams);
        taskDefinitionLog.setTaskType(TASK_TYPE_CONDITIONS);
        taskDefinitionLogs.add(taskDefinitionLog);

        HashMap<Long, Long> taskCodeMap = new HashMap<>();
        taskCodeMap.put(13939480829216L, 1L);
        taskCodeMap.put(13939482198816L, 2L);

        processDefinitionService.rebuildConditionTaskDefinition(taskDefinitionLogs, taskCodeMap);

        String dependentParametersStr =
                JSONUtils.getNodeString(taskDefinitionLog.getTaskParams(), Constants.DEPENDENCE);
        DependentParameters dependency = JSONUtils.parseObject(dependentParametersStr, DependentParameters.class);

        for (DependentTaskModel dependentTaskModel : dependency.getDependTaskList()) {

            for (DependentItem dependentItem : dependentTaskModel.getDependItemList()) {

                if (dependentItem.getStatus().equals(TaskExecutionStatus.SUCCESS)) {
                    assert dependentItem.getDepTaskCode() == 1L;
                }

                if (dependentItem.getStatus().equals(TaskExecutionStatus.FAILURE)) {
                    assert dependentItem.getDepTaskCode() == 2L;
                }
            }

        }

    }

    @Test
    void rebuildConditionTaskDefinitionTest_3() {

        ProcessDefinitionServiceImpl processDefinitionService = new ProcessDefinitionServiceImpl();
        String taskParams =
                "{\"localParams\":[],\"resourceList\":[],\"dependence\":{\"relation\":\"AND\",\"dependTaskList\":[{\"relation\":\"AND\","
                        +
                        "\"dependItemList\":[{\"depTaskCode\":13939480829216,\"status\":\"SUCCESS\"},{\"depTaskCode\":13939482198816,\"status\":\"FAILURE\"}]}]},"
                        +
                        "\"conditionResult\":{\"successNode\":[13939480829217], \"failedNode\":[13939480829218]}}";

        ArrayList<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();

        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
        taskDefinitionLog.setTaskParams(taskParams);
        taskDefinitionLog.setTaskType(TASK_TYPE_CONDITIONS);
        taskDefinitionLogs.add(taskDefinitionLog);

        HashMap<Long, Long> taskCodeMap = new HashMap<>();
        taskCodeMap.put(13939480829216L, 1L);
        taskCodeMap.put(13939482198816L, 2L);
        taskCodeMap.put(13939480829217L, 3L);
        taskCodeMap.put(13939480829218L, 4L);

        processDefinitionService.rebuildConditionTaskDefinition(taskDefinitionLogs, taskCodeMap);

        String dependentParametersStr =
                JSONUtils.getNodeString(taskDefinitionLog.getTaskParams(), Constants.DEPENDENCE);
        DependentParameters dependency = JSONUtils.parseObject(dependentParametersStr, DependentParameters.class);

        for (DependentTaskModel dependentTaskModel : dependency.getDependTaskList()) {

            for (DependentItem dependentItem : dependentTaskModel.getDependItemList()) {

                if (dependentItem.getStatus().equals(TaskExecutionStatus.SUCCESS)) {
                    assert dependentItem.getDepTaskCode() == 1L;
                }

                if (dependentItem.getStatus().equals(TaskExecutionStatus.FAILURE)) {
                    assert dependentItem.getDepTaskCode() == 2L;
                }
            }

        }

        String conditionsParametersStr =
                JSONUtils.getNodeString(taskDefinitionLog.getTaskParams(), Constants.CONDITION_RESULT);
        ConditionsParameters conditionResult =
                JSONUtils.parseObject(conditionsParametersStr, ConditionsParameters.class);
        Long successNode = conditionResult.getSuccessNode().get(0);
        assert successNode == 3L;

        Long failedNode = conditionResult.getFailedNode().get(0);
        assert failedNode == 4L;

    }
}
