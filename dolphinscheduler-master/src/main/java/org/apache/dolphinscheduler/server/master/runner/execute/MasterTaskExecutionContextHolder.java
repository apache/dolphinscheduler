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

package org.apache.dolphinscheduler.server.master.runner.execute;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MasterTaskExecutionContextHolder {

    private static final Map<Integer, TaskExecutionContext> TASK_EXECUTION_CONTEXT_MAP = new ConcurrentHashMap<>();

    public static void putTaskExecutionContext(TaskExecutionContext taskExecutionContext) {
        if (TASK_EXECUTION_CONTEXT_MAP.containsKey(taskExecutionContext.getTaskInstanceId())) {
            log.error("The TaskExecutionContext {} already exists in the MasterTaskExecutionContextHolder",
                    taskExecutionContext);
        }
        TASK_EXECUTION_CONTEXT_MAP.put(taskExecutionContext.getTaskInstanceId(), taskExecutionContext);
    }

    public static TaskExecutionContext getTaskExecutionContext(int taskInstanceId) {
        return TASK_EXECUTION_CONTEXT_MAP.get(taskInstanceId);
    }

    public static void removeTaskExecutionContext(int taskInstanceId) {
        TASK_EXECUTION_CONTEXT_MAP.remove(taskInstanceId);
    }
}
