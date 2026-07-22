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
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorEventBus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PhysicalTaskExecutorBuilder {

    private TaskExecutionContext taskExecutionContext;

    private WorkerConfig workerConfig;

    private StorageOperator storageOperator;

    @Builder.Default
    private TaskExecutorEventBus taskExecutorEventBus = new TaskExecutorEventBus();

    private PhysicalTaskPluginFactory physicalTaskPluginFactory;

}
