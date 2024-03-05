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

package org.apache.dolphinscheduler.server.master.dag;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Use to store the TaskExecutionRunnable of a DAG.
 */
public class TaskExecutionRunnableRepository implements ITaskExecutionRunnableRepository {

    private final Map<Integer, TaskExecutionRunnable> taskExecuteRunnableMap = new ConcurrentHashMap<>();

    private final Map<String, TaskExecutionRunnable> taskExecuteRunnableNameMap = new ConcurrentHashMap<>();

    @Override
    public void storeTaskExecutionRunnable(TaskExecutionRunnable taskExecutionRunnable) {
        taskExecuteRunnableMap.put(taskExecutionRunnable.getTaskExecutionContext().getTaskInstance().getId(),
                taskExecutionRunnable);
        taskExecuteRunnableNameMap.put(taskExecutionRunnable.getTaskExecutionContext().getTaskInstance().getName(),
                taskExecutionRunnable);
    }

    public TaskExecutionRunnable getTaskExecutionRunnableById(Integer taskInstanceId) {
        return taskExecuteRunnableMap.get(taskInstanceId);
    }

    @Override
    public TaskExecutionRunnable getTaskExecutionRunnableByName(String taskInstanceName) {
        return taskExecuteRunnableNameMap.get(taskInstanceName);
    }

    public Collection<TaskExecutionRunnable> getActiveTaskExecutionRunnable() {
        return taskExecuteRunnableMap.values()
                .stream()
                .filter(taskExecutionRunnable -> {
                    return taskExecutionRunnable.getTaskExecutionContext().getTaskInstance().getState().isRunning();
                })
                .collect(Collectors.toList());
    }

    public void removeTaskExecutionRunnable(Integer taskInstanceId) {
        TaskExecutionRunnable taskExecutionRunnable = taskExecuteRunnableMap.remove(taskInstanceId);
        if (taskExecutionRunnable != null) {
            taskExecuteRunnableNameMap
                    .remove(taskExecutionRunnable.getTaskExecutionContext().getTaskInstance().getName());
        }
    }

}
