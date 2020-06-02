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
package org.apache.dolphinscheduler.common.utils;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CycleEnum;
import org.apache.dolphinscheduler.common.enums.DependentRelation;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.DependentItem;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskNodeUtils {

    public static String buildTaskId() {
        return String.format("%s-%d", "tasks", 10000 + RandomUtils.nextInt(9999));
    }

    public static TaskNode buildDependTaskNode(String dependProcessName, String dependNodeName, int retryTimes, int retryInterval) {
        TaskNode node = new TaskNode();
        node.setId(buildTaskId());
        node.setName(String.format("%s | %s", dependProcessName, dependNodeName));
        node.setType(TaskType.DEPENDENT.toString());
        node.setRunFlag(Constants.FLOWNODE_RUN_FLAG_NORMAL);
        node.setMaxRetryTimes(retryTimes);
        node.setRetryInterval(retryInterval);
        node.setTimeout("{\"strategy\": \"\", \"interval\": null, \"enable\": false}");
        node.setTaskInstancePriority(Priority.HIGH);
        return node;
    }

    public static DependentItem buildDependentItem(int processDefinitionId, String nodeName) {
        DependentItem dependentItem = new DependentItem();
        dependentItem.setDefinitionId(processDefinitionId);
        dependentItem.setDepTasks(nodeName);
        dependentItem.setCycle(CycleEnum.DAY.toString().toLowerCase());
        dependentItem.setDateValue("today");
        return dependentItem;
    }

    public static void addNodeDependentItem(TaskNode taskNode, int dependProcessId, String dependNodeName) {
        addNodeDependentItem(taskNode, buildDependentItem(dependProcessId, dependNodeName));
    }

    public static void addNodeDependentItem(TaskNode taskNode, DependentItem dependentItem) {
        DependentParameters dependentParameters;
        if (StringUtils.isEmpty(taskNode.getDependence())) {
            dependentParameters = new DependentParameters();
        } else {
            dependentParameters = JSONUtils.parseObject(taskNode.getDependence(), DependentParameters.class);
        }

        List<DependentTaskModel> dependTaskList;
        if (CollectionUtils.isEmpty(dependentParameters.getDependTaskList())) {
            dependTaskList = new ArrayList<>();
        } else {
            dependTaskList = dependentParameters.getDependTaskList();
        }

        DependentTaskModel dependentTaskModel = new DependentTaskModel();
        List<DependentItem> dependItemList = new ArrayList<>();
        dependItemList.add(dependentItem);
        dependentTaskModel.setDependItemList(dependItemList);
        dependentTaskModel.setRelation(DependentRelation.AND);
        dependTaskList.add(dependentTaskModel);
        dependentParameters.setDependTaskList(dependTaskList);
        dependentParameters.setRelation(DependentRelation.AND);
        taskNode.setDependence(JSONUtils.toJsonString(dependentParameters));
    }

    public static void addNodeDepList(TaskNode taskNode, TaskNode dependNode) {
        if (CollectionUtils.isEmpty(taskNode.getDepList())) {
            taskNode.setDepList(Arrays.asList(dependNode.getName()));
        } else {
            if (!taskNode.getDepList().contains(dependNode.getName())) {
                taskNode.getDepList().add(dependNode.getName());
            }
        }
    }

}
