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

package org.apache.dolphinscheduler.task.executor.container;

import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.worker.ITaskExecutorWorker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskExecutorAssignmentTable {

    private final Map<Integer, Integer> assignedTaskExecutorIdToWorkerIdMapping = new ConcurrentHashMap<>();

    public void registerTaskExecutor(final ITaskExecutor taskExecutor, final ITaskExecutorWorker taskExecutorWorker) {
        assignedTaskExecutorIdToWorkerIdMapping.put(taskExecutor.getId(), taskExecutorWorker.getId());
    }

    public boolean isTaskExecutorRegistered(final ITaskExecutor taskExecutor) {
        return assignedTaskExecutorIdToWorkerIdMapping.containsKey(taskExecutor.getId());
    }

    public void unregisterTaskExecutor(final ITaskExecutor taskExecutor) {
        assignedTaskExecutorIdToWorkerIdMapping.remove(taskExecutor.getId());
    }

    public Integer getTaskExecutorWorkerId(final ITaskExecutor taskExecutor) {
        return assignedTaskExecutorIdToWorkerIdMapping.get(taskExecutor.getId());
    }

    public boolean isEmpty() {
        return assignedTaskExecutorIdToWorkerIdMapping.isEmpty();
    }
}
