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

package org.apache.dolphinscheduler.server.worker.utils;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.plugin.storage.api.ResourceMetadata;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.server.worker.metrics.WorkerServerMetrics;

import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskExecutionContextUtils {

    public static void createTaskInstanceWorkingDirectory(TaskExecutionContext taskExecutionContext) throws TaskException {
        // local execute path
        String taskInstanceWorkingDirectory =
                FileUtils.getTaskInstanceWorkingDirectory(taskExecutionContext.getTaskInstanceId());
        try {
            if (new File(taskInstanceWorkingDirectory).exists()) {
                FileUtils.deleteFile(taskInstanceWorkingDirectory);
                log.warn("The TaskInstance WorkingDirectory: {} is exist, will recreate again",
                        taskInstanceWorkingDirectory);
            }
            FileUtils.createDirectoryWith755(Paths.get(taskInstanceWorkingDirectory));

            taskExecutionContext.setExecutePath(taskInstanceWorkingDirectory);
            taskExecutionContext.setAppInfoPath(FileUtils.getAppInfoPath(taskInstanceWorkingDirectory));
        } catch (Throwable ex) {
            throw new TaskException(
                    "Cannot create TaskInstance WorkingDirectory: " + taskInstanceWorkingDirectory + " failed", ex);
        }
    }

    public static ResourceContext downloadResourcesIfNeeded(TaskChannel taskChannel,
                                                            StorageOperator storageOperator,
                                                            TaskExecutionContext taskExecutionContext) {
        AbstractParameters abstractParameters = taskChannel.parseParameters(taskExecutionContext.getTaskParams());

        List<ResourceInfo> resourceFilesList = abstractParameters.getResourceFilesList();
        if (CollectionUtils.isEmpty(resourceFilesList)) {
            log.debug("There is no resource file need to download");
            return new ResourceContext();
        }

        ResourceContext resourceContext = new ResourceContext();
        String taskWorkingDirectory = taskExecutionContext.getExecutePath();

        for (ResourceInfo resourceInfo : resourceFilesList) {
            String resourceAbsolutePathInStorage = resourceInfo.getResourceName();
            ResourceMetadata resourceMetaData = storageOperator.getResourceMetaData(resourceAbsolutePathInStorage);
            String resourceAbsolutePathInLocal =
                    Paths.get(taskWorkingDirectory, resourceMetaData.getResourceRelativePath()).toString();
            File file = new File(resourceAbsolutePathInLocal);
            if (!file.exists()) {
                try {
                    long resourceDownloadStartTime = System.currentTimeMillis();
                    storageOperator.download(resourceAbsolutePathInStorage, resourceAbsolutePathInLocal, true);
                    log.info("Download resource file {} -> {} successfully", resourceAbsolutePathInStorage,
                            resourceAbsolutePathInLocal);
                    FileUtils.setFileTo755(file);
                    WorkerServerMetrics
                            .recordWorkerResourceDownloadTime(System.currentTimeMillis() - resourceDownloadStartTime);
                    WorkerServerMetrics
                            .recordWorkerResourceDownloadSize(Files.size(Paths.get(resourceAbsolutePathInLocal)));
                    WorkerServerMetrics.incWorkerResourceDownloadSuccessCount();
                } catch (Exception ex) {
                    WorkerServerMetrics.incWorkerResourceDownloadFailureCount();
                    throw new TaskException(
                            String.format("Download resource file: %s error", resourceAbsolutePathInStorage), ex);
                }
            }
            ResourceContext.ResourceItem resourceItem = ResourceContext.ResourceItem.builder()
                    .resourceAbsolutePathInStorage(resourceAbsolutePathInStorage)
                    .resourceAbsolutePathInLocal(resourceAbsolutePathInLocal)
                    .build();
            resourceContext.addResourceItem(resourceItem);
        }
        return resourceContext;
    }

}
