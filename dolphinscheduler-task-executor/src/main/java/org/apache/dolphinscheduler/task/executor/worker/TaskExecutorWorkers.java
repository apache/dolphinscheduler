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

package org.apache.dolphinscheduler.task.executor.worker;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TaskExecutorWorkers {

    private final List<TaskExecutorWorker> taskExecutorWorkers;

    private final LongAdder longAdder = new LongAdder();

    public TaskExecutorWorkers(final int workerSize) {
        this.taskExecutorWorkers = IntStream
                .range(0, workerSize)
                .mapToObj(TaskExecutorWorker::new)
                .collect(Collectors.toList());
    }

    public List<TaskExecutorWorker> getWorkers() {
        return taskExecutorWorkers;
    }

    public TaskExecutorWorker getWorkerById(final int workerId) {
        return taskExecutorWorkers.get(workerId);
    }

    public TaskExecutorWorker roundRobinSelectWorker() {
        int workerIndex = (int) (longAdder.longValue() % taskExecutorWorkers.size());
        longAdder.increment();
        return taskExecutorWorkers.get(workerIndex);
    }

    public Optional<TaskExecutorWorker> selectIdleWorker() {
        for (TaskExecutorWorker taskExecutorWorker : taskExecutorWorkers) {
            if (taskExecutorWorker.getRegisteredTaskExecutorSize() == 0) {
                return Optional.of(taskExecutorWorker);
            }
        }
        return Optional.empty();
    }
}
