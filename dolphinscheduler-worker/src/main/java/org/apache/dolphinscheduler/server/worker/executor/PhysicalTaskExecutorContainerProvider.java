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

package org.apache.dolphinscheduler.server.worker.executor;

import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.task.executor.container.ExclusiveThreadTaskExecutorContainer;
import org.apache.dolphinscheduler.task.executor.container.ITaskExecutorContainer;
import org.apache.dolphinscheduler.task.executor.container.ITaskExecutorContainerProvider;
import org.apache.dolphinscheduler.task.executor.container.TaskExecutorContainerConfig;

import org.springframework.stereotype.Component;

@Component
public class PhysicalTaskExecutorContainerProvider implements ITaskExecutorContainerProvider {

    private final ITaskExecutorContainer taskExecutorContainer;

    public PhysicalTaskExecutorContainerProvider(final WorkerConfig workerConfig) {
        final TaskExecutorContainerConfig containerConfig = TaskExecutorContainerConfig.builder()
                .containerName("exclusive-task-executor-container")
                .taskExecutorThreadPoolSize(workerConfig.getPhysicalTaskConfig().getTaskExecutorThreadSize())
                .build();
        this.taskExecutorContainer = new ExclusiveThreadTaskExecutorContainer(containerConfig);
    }

    @Override
    public ITaskExecutorContainer getExecutorContainer() {
        return taskExecutorContainer;
    }
}
