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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.ApplicationInfo;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.utils.TaskExecutionContextUtils;
import org.apache.dolphinscheduler.server.worker.utils.TaskFilesTransferUtils;
import org.apache.dolphinscheduler.server.worker.utils.TenantUtils;
import org.apache.dolphinscheduler.task.executor.AbstractTaskExecutor;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.TaskExecutorState;
import org.apache.dolphinscheduler.task.executor.TaskExecutorStateMappings;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorRuntimeContextChangedLifecycleEvent;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PhysicalTaskExecutor extends AbstractTaskExecutor {

    private final WorkerConfig workerConfig;

    private final StorageOperator storageOperator;

    @Getter
    private AbstractTask physicalTask;

    private final PhysicalTaskPluginFactory physicalTaskPluginFactory;

    public PhysicalTaskExecutor(final PhysicalTaskExecutorBuilder physicalTaskExecutorBuilder) {
        super(physicalTaskExecutorBuilder.getTaskExecutionContext(),
                physicalTaskExecutorBuilder.getTaskExecutorEventBus());
        this.workerConfig = physicalTaskExecutorBuilder.getWorkerConfig();
        this.storageOperator = physicalTaskExecutorBuilder.getStorageOperator();
        this.physicalTaskPluginFactory = physicalTaskExecutorBuilder.getPhysicalTaskPluginFactory();
    }

    @Override
    protected void initializeTaskPlugin() {
        this.physicalTask = physicalTaskPluginFactory.createPhysicalTask(this);
        log.info("Initialized physicalTask: {} successfully", taskExecutionContext.getTaskType());

        this.physicalTask.init();

        this.physicalTask.getParameters().setVarPool(taskExecutionContext.getVarPool());
        log.info("Set taskVarPool: {} successfully", taskExecutionContext.getVarPool());
    }

    @Override
    protected void doTriggerTaskPlugin() {
        final ITaskExecutor taskExecutor = this;
        physicalTask.handle(new TaskCallBack() {

            @Override
            public void updateRemoteApplicationInfo(final int taskInstanceId, final ApplicationInfo applicationInfo) {
                taskExecutionContext.setAppIds(applicationInfo.getAppIds());
                taskExecutorEventBus.publish(TaskExecutorRuntimeContextChangedLifecycleEvent.of(taskExecutor));
            }

            @Override
            public void updateTaskInstanceInfo(final int taskInstanceId) {
                taskExecutorEventBus.publish(TaskExecutorRuntimeContextChangedLifecycleEvent.of(taskExecutor));
            }
        });
    }

    @Override
    protected TaskExecutorState doTrackTaskPluginStatus() {
        return TaskExecutorStateMappings.mapState(physicalTask.getExitStatus());
    }

    @Override
    public void pause() {
        log.warn("The physical doesn't support pause");
    }

    @Override
    public void kill() {
        if (physicalTask != null) {
            physicalTask.cancel();
        }
    }

    @Override
    protected void initializeTaskContext() {
        super.initializeTaskContext();

        taskExecutionContext.setTaskAppId(String.valueOf(taskExecutionContext.getTaskInstanceId()));

        taskExecutionContext.setTenantCode(TenantUtils.getOrCreateActualTenant(workerConfig, taskExecutionContext));
        log.info("TenantCode: {} check successfully", taskExecutionContext.getTenantCode());

        TaskExecutionContextUtils.createTaskInstanceWorkingDirectory(taskExecutionContext);
        log.info("TaskInstance working directory: {} create successfully", taskExecutionContext.getExecutePath());

        final ResourceContext resourceContext = TaskExecutionContextUtils.downloadResourcesIfNeeded(
                physicalTaskPluginFactory.getTaskChannel(this),
                storageOperator,
                taskExecutionContext);
        taskExecutionContext.setResourceContext(resourceContext);
        log.info("Download resources successfully: \n{}", taskExecutionContext.getResourceContext());

        // todo: remove this. The cache should be deprecated
        TaskFilesTransferUtils.downloadUpstreamFiles(taskExecutionContext, storageOperator);
        log.info("Download upstream files: {} successfully",
                TaskFilesTransferUtils.getFileLocalParams(taskExecutionContext, Direct.IN));

        log.info("End initialize task {}", JSONUtils.toPrettyJsonString(taskExecutionContext));
    }

    @Override
    public String toString() {
        return "PhysicalTaskExecutor{" +
                "id=" + taskExecutionContext.getTaskInstanceId() +
                ", name=" + taskExecutionContext.getTaskName() +
                ", stat=" + taskExecutorState.get() +
                '}';
    }

}
