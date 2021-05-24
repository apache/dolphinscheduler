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

import org.apache.dolphinscheduler.common.enums.DependentRelation;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.DependentItem;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TaskInstanceTest {

    /**
     * task instance sub process
     */
    @Test
    public void testTaskInstanceIsSubProcess() {
        TaskInstance taskInstance = new TaskInstance();

        //sub process
        taskInstance.setTaskType(TaskType.SUB_PROCESS.getDesc());
        Assert.assertTrue(taskInstance.isSubProcess());

        //not sub process
        taskInstance.setTaskType(TaskType.HTTP.getDesc());
        Assert.assertFalse(taskInstance.isSubProcess());

        //sub process
        taskInstance.setTaskType(TaskType.CONDITIONS.getDesc());
        Assert.assertTrue(taskInstance.isConditionsTask());

        //sub process
        taskInstance.setTaskType(TaskType.DEPENDENT.getDesc());
        Assert.assertTrue(taskInstance.isDependTask());
    }

    /**
     * test for TaskInstance.getDependence
     */
    @Test
    public void testTaskInstanceGetDependence() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskParams(JSONUtils.toJsonString(getDependentParameters()));
        taskInstance.getDependency();
    }

    /**
     *
     * @return
     */
    private DependentParameters getDependentParameters() {
        DependentParameters dependentParameters = new DependentParameters();
        List<DependentTaskModel> dependTaskList = new ArrayList<>();
        List<DependentItem> dependentItems = new ArrayList<>();
        DependentItem dependentItem = new DependentItem();
        dependentItem.setDepTasks("A");
        dependentItem.setDefinitionCode(222L);
        dependentItem.setCycle("today");
        dependentItems.add(dependentItem);
        dependentParameters.setDependTaskList(dependTaskList);
        dependentParameters.setRelation(DependentRelation.AND);
        return dependentParameters;
    }
}
