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

import java.util.Set;

import lombok.experimental.UtilityClass;

import com.google.common.collect.Sets;

@UtilityClass
public class TaskUtils {

    private final String blockingLogicTask = "BLOCKING";
    private final String conditionLogicTask = "CONDITIONS";

    private final String dependentLogicTask = "DEPENDENT";
    private final String subWorkflowLogicTask = "SUB_PROCESS";
    private final String switchLogicTask = "SWITCH";
    private final String dynamicLogicTask = "DYNAMIC";

    // todo: Add to SPI
    private final Set<String> MASTER_TASK_TYPES = Sets.newHashSet(
            blockingLogicTask,
            conditionLogicTask,
            dependentLogicTask,
            subWorkflowLogicTask,
            switchLogicTask,
            dynamicLogicTask);

    // todo: add to task plugin spi
    public boolean isLogicTask(String taskType) {
        return MASTER_TASK_TYPES.contains(taskType);
    }

}
