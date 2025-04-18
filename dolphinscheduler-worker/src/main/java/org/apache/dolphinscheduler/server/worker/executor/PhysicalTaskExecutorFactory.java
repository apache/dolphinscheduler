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

import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.ITaskExecutorFactory;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PhysicalTaskExecutorFactory implements ITaskExecutorFactory {

    private final WorkerConfig workerConfig;

    private final PhysicalTaskPluginFactory physicalTaskPluginFactory;

    private final StorageOperator storageOperator;

    public PhysicalTaskExecutorFactory(final WorkerConfig workerConfig,
                                       final PhysicalTaskPluginFactory physicalTaskPluginFactory,
                                       final StorageOperator storageOperator) {
        this.workerConfig = workerConfig;
        this.physicalTaskPluginFactory = physicalTaskPluginFactory;
        this.storageOperator = storageOperator;
    }

    @Override
    public ITaskExecutor createTaskExecutor(final TaskExecutionContext taskExecutionContext) {
        assemblyTaskLogPath(taskExecutionContext);

        final PhysicalTaskExecutorBuilder physicalTaskExecutorBuilder = PhysicalTaskExecutorBuilder.builder()
                .taskExecutionContext(taskExecutionContext)
                .workerConfig(workerConfig)
                .storageOperator(storageOperator)
                .physicalTaskPluginFactory(physicalTaskPluginFactory)
                .build();
        return new PhysicalTaskExecutor(physicalTaskExecutorBuilder);
    }

    private void assemblyTaskLogPath(final TaskExecutionContext taskExecutionContext) {
        taskExecutionContext.setLogPath(LogUtils.getTaskInstanceLogFullPath(taskExecutionContext));
    }

}
