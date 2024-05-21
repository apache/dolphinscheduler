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

package org.apache.dolphinscheduler.server.worker.runner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to store all running and waiting {@link WorkerTaskExecutor}. If the task has been finished, it will be removed from the map.
 */
public class WorkerTaskExecutorHolder {

    private static final Map<Integer, WorkerTaskExecutor> workerTaskExecutorMap = new HashMap<>();

    public static void put(WorkerTaskExecutor workerTaskExecutor) {
        int taskInstanceId = workerTaskExecutor.getTaskExecutionContext().getTaskInstanceId();
        if (workerTaskExecutorMap.containsKey(taskInstanceId)) {
            throw new IllegalArgumentException("TaskInstance: " + taskInstanceId + " already exists");
        }
        workerTaskExecutorMap.put(taskInstanceId, workerTaskExecutor);
    }

    public static WorkerTaskExecutor get(int taskInstanceId) {
        return workerTaskExecutorMap.get(taskInstanceId);
    }

    public static WorkerTaskExecutor remove(int taskInstanceId) {
        return workerTaskExecutorMap.remove(taskInstanceId);
    }

    public static void clear() {
        workerTaskExecutorMap.clear();
    }

    public static int size() {
        return workerTaskExecutorMap.size();
    }

    public static Collection<WorkerTaskExecutor> getAllTaskExecutor() {
        return workerTaskExecutorMap.values();
    }
}
