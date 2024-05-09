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

import org.apache.dolphinscheduler.common.constants.TenantConstants;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.server.worker.config.TenantConfig;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.metrics.WorkerServerMetrics;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskExecutionContextUtils {

    public static String getOrCreateTenant(WorkerConfig workerConfig, TaskExecutionContext taskExecutionContext) {
        try {
            TenantConfig tenantConfig = workerConfig.getTenantConfig();

            String tenantCode = taskExecutionContext.getTenantCode();
            if (TenantConstants.DEFAULT_TENANT_CODE.equals(tenantCode) && tenantConfig.isDefaultTenantEnabled()) {
                log.info("Current tenant is default tenant, will use bootstrap user: {} to execute the task",
                        TenantConstants.BOOTSTRAPT_SYSTEM_USER);
                return TenantConstants.BOOTSTRAPT_SYSTEM_USER;
            }
            boolean osUserExistFlag;
            // if Using distributed is true and Currently supported systems are linux,Should not let it
            // automatically
            // create tenants,so TenantAutoCreate has no effect
            if (tenantConfig.isDistributedTenantEnabled() && SystemUtils.IS_OS_LINUX) {
                // use the id command to judge in linux
                osUserExistFlag = OSUtils.existTenantCodeInLinux(tenantCode);
            } else if (OSUtils.isSudoEnable() && tenantConfig.isAutoCreateTenantEnabled()) {
                // if not exists this user, then create
                OSUtils.createUserIfAbsent(tenantCode);
                osUserExistFlag = OSUtils.getUserList().contains(tenantCode);
            } else {
                osUserExistFlag = OSUtils.getUserList().contains(tenantCode);
            }
            if (!osUserExistFlag) {
                throw new TaskException(
                        String.format("TenantCode: %s doesn't exist", tenantCode));
            }
            return tenantCode;
        } catch (TaskException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new TaskException(
                    String.format("TenantCode: %s doesn't exist", taskExecutionContext.getTenantCode()), ex);
        }
    }

    public static void createTaskInstanceWorkingDirectory(TaskExecutionContext taskExecutionContext) throws TaskException {
        // local execute path
        String taskInstanceWorkingDirectory = FileUtils.getTaskInstanceWorkingDirectory(
                taskExecutionContext.getTenantCode(),
                taskExecutionContext.getProjectCode(),
                taskExecutionContext.getProcessDefineCode(),
                taskExecutionContext.getProcessDefineVersion(),
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId());
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

    public static ResourceContext downloadResourcesIfNeeded(String tenant,
                                                            TaskChannel taskChannel,
                                                            StorageOperate storageOperate,
                                                            TaskExecutionContext taskExecutionContext) {
        AbstractParameters abstractParameters = taskChannel.parseParameters(
                ParametersNode.builder()
                        .taskType(taskExecutionContext.getTaskType())
                        .taskParams(taskExecutionContext.getTaskParams())
                        .build());

        List<ResourceInfo> resourceFilesList = abstractParameters.getResourceFilesList();
        if (CollectionUtils.isEmpty(resourceFilesList)) {
            log.debug("There is no resource file need to download");
            return new ResourceContext();
        }

        ResourceContext resourceContext = new ResourceContext();
        String taskWorkingDirectory = taskExecutionContext.getExecutePath();

        for (ResourceInfo resourceInfo : resourceFilesList) {
            String resourceAbsolutePathInStorage = resourceInfo.getResourceName();
            String resourceRelativePath = storageOperate.getResourceFileName(tenant, resourceAbsolutePathInStorage);
            String resourceAbsolutePathInLocal = Paths.get(taskWorkingDirectory, resourceRelativePath).toString();
            File file = new File(resourceAbsolutePathInLocal);
            if (!file.exists()) {
                try {
                    long resourceDownloadStartTime = System.currentTimeMillis();
                    storageOperate.download(resourceAbsolutePathInStorage, resourceAbsolutePathInLocal, true);
                    log.debug("Download resource file {} under: {} successfully", resourceAbsolutePathInStorage,
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
