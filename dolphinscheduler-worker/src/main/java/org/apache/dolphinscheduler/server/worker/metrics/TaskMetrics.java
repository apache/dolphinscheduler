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

package org.apache.dolphinscheduler.server.worker.metrics;

import org.apache.dolphinscheduler.plugin.task.api.TaskChannelFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import lombok.experimental.UtilityClass;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

@UtilityClass
public class TaskMetrics {

    private final Map<String, Counter> taskTypeExecutionCounter = new HashMap<>();
    private final Counter taskUnknownTypeExecutionCounter =
            Counter.builder("ds.task.execution.count.by.type")
                    .tag("task_type", "unknown")
                    .description("task execution counter by type")
                    .register(Metrics.globalRegistry);

    static {
        for (TaskChannelFactory taskChannelFactory : ServiceLoader.load(TaskChannelFactory.class)) {
            taskTypeExecutionCounter.put(
                    taskChannelFactory.getName(),
                    Counter.builder("ds.task.execution.count.by.type")
                            .tag("task_type", taskChannelFactory.getName())
                            .description("task execution counter by type")
                            .register(Metrics.globalRegistry));
        }
    }

    public void incrTaskTypeExecuteCount(String taskType) {
        taskTypeExecutionCounter.getOrDefault(taskType, taskUnknownTypeExecutionCounter).increment();
    }

}
