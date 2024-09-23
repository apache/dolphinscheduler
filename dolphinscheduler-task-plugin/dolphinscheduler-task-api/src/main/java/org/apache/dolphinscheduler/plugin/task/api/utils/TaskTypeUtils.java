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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.dolphinscheduler.plugin.task.api.ILogicTaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.plugin.task.api.task.ConditionsLogicTaskChannelFactory;
import org.apache.dolphinscheduler.plugin.task.api.task.DependentLogicTaskChannelFactory;
import org.apache.dolphinscheduler.plugin.task.api.task.DynamicLogicTaskChannelFactory;
import org.apache.dolphinscheduler.plugin.task.api.task.SubWorkflowLogicTaskChannelFactory;
import org.apache.dolphinscheduler.plugin.task.api.task.SwitchLogicTaskChannelFactory;

import org.apache.commons.lang3.StringUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TaskTypeUtils {

    public boolean isSwitchTask(String taskType) {
        return SwitchLogicTaskChannelFactory.NAME.equals(taskType);
    }

    public boolean isConditionTask(String taskType) {
        return ConditionsLogicTaskChannelFactory.NAME.equals(taskType);
    }

    public boolean isSubWorkflowTask(String taskType) {
        return SubWorkflowLogicTaskChannelFactory.NAME.equals(taskType);
    }

    public boolean isDependentTask(String taskType) {
        return DependentLogicTaskChannelFactory.NAME.equals(taskType);
    }

    public boolean isDynamicTask(String taskType) {
        return DynamicLogicTaskChannelFactory.NAME.equals(taskType);
    }

    public boolean isLogicTask(String taskType) {
        checkArgument(StringUtils.isNotEmpty(taskType), "taskType cannot be empty");
        return TaskPluginManager.getTaskChannel(taskType) instanceof ILogicTaskChannel;
    }

}
