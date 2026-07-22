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

package org.apache.dolphinscheduler.server.master.engine.task.statemachine;

import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class TaskStateActionFactory {

    private final Map<TaskExecutionStatus, ITaskStateAction> taskStateActionMap = new HashMap<>();

    public TaskStateActionFactory(List<ITaskStateAction> taskStateActions) {
        taskStateActions.forEach(
                taskStateAction -> taskStateActionMap.put(taskStateAction.matchState(), taskStateAction));
        Arrays.stream(TaskExecutionStatus.values()).forEach(this::getTaskStateAction);
    }

    public ITaskStateAction getTaskStateAction(final TaskExecutionStatus taskExecutionStatus) {
        final ITaskStateAction taskStateAction = taskStateActionMap.get(taskExecutionStatus);
        if (taskStateAction == null) {
            throw new IllegalArgumentException("Cannot find TaskStateAction for state: " + taskExecutionStatus);
        }
        return taskStateAction;
    }

}
