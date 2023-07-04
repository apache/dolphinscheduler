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

package org.apache.dolphinscheduler.server.master.utils;

import org.apache.dolphinscheduler.server.master.runner.task.blocking.BlockingLogicTask;
import org.apache.dolphinscheduler.server.master.runner.task.condition.ConditionLogicTask;
import org.apache.dolphinscheduler.server.master.runner.task.dependent.DependentLogicTask;
import org.apache.dolphinscheduler.server.master.runner.task.dynamic.DynamicLogicTask;
import org.apache.dolphinscheduler.server.master.runner.task.subworkflow.SubWorkflowLogicTask;
import org.apache.dolphinscheduler.server.master.runner.task.switchtask.SwitchLogicTask;

import java.util.Set;

import lombok.experimental.UtilityClass;

import com.google.common.collect.Sets;

@UtilityClass
public class TaskUtils {

    // todo: Add to SPI
    private final Set<String> MASTER_TASK_TYPES = Sets.newHashSet(
            BlockingLogicTask.TASK_TYPE,
            ConditionLogicTask.TASK_TYPE,
            DependentLogicTask.TASK_TYPE,
            SubWorkflowLogicTask.TASK_TYPE,
            SwitchLogicTask.TASK_TYPE,
            DynamicLogicTask.TASK_TYPE);

    public boolean isMasterTask(String taskType) {
        return MASTER_TASK_TYPES.contains(taskType);
    }
}
