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

import org.apache.dolphinscheduler.common.exception.StorageOperateNoConfiguredException;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.metrics.WorkerServerMetrics;
import org.apache.dolphinscheduler.service.storage.StorageOperate;
import org.apache.dolphinscheduler.service.utils.CommonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

public class TaskExecutionCheckerUtils {

    public static void checkTenantExist(WorkerConfig workerConfig, TaskExecutionContext taskExecutionContext) {
        try {
            String tenantCode = taskExecutionContext.getTenantCode();
            boolean osUserExistFlag;
            // if Using distributed is true and Currently supported systems are linux,Should not let it
            // automatically
            // create tenants,so TenantAutoCreate has no effect
            if (workerConfig.isTenantDistributedUser() && SystemUtils.IS_OS_LINUX) {
                // use the id command to judge in linux
                osUserExistFlag = OSUtils.existTenantCodeInLinux(tenantCode);
            } else if (OSUtils.isSudoEnable() && workerConfig.isTenantAutoCreate()) {
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
        } catch (TaskException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new TaskException(
                    String.format("TenantCode: %s doesn't exist", taskExecutionContext.getTenantCode()));
        }
    }

    public static void createProcessLocalPathIfAbsent(TaskExecutionContext taskExecutionContext) throws TaskException {
        try {
            // local execute path
            String execLocalPath = FileUtils.getProcessExecDir(
                    taskExecutionContext.getTenantCode(),
                    taskExecutionContext.getProjectCode(),
                    taskExecutionContext.getProcessDefineCode(),
                    taskExecutionContext.getProcessDefineVersion(),
                    taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId());
            taskExecutionContext.setExecutePath(execLocalPath);
            createDirectoryWithOwner(Paths.get(execLocalPath), taskExecutionContext.getTenantCode());
        } catch (Throwable ex) {
            throw new TaskException("Cannot create process execute dir", ex);
        }
    }

    public static void downloadResourcesIfNeeded(StorageOperate storageOperate,
                                                 TaskExecutionContext taskExecutionContext, Logger logger) {
        String execLocalPath = taskExecutionContext.getExecutePath();
        Map<String, String> projectRes = taskExecutionContext.getResources();
        if (MapUtils.isEmpty(projectRes)) {
            return;
        }
        List<Pair<String, String>> downloadFiles = new ArrayList<>();
        projectRes.forEach((key, value) -> {
            File resFile = new File(execLocalPath, key);
            boolean notExist = !resFile.exists();
            if (notExist) {
                downloadFiles.add(Pair.of(key, value));
            } else {
                logger.info("file : {} exists ", resFile.getName());
            }
        });
        if (!downloadFiles.isEmpty() && !PropertyUtils.getResUploadStartupState()) {
            throw new StorageOperateNoConfiguredException("Storage service config does not exist!");
        }

        if (CollectionUtils.isNotEmpty(downloadFiles)) {
            for (Pair<String, String> fileDownload : downloadFiles) {
                try {
                    // query the tenant code of the resource according to the name of the resource
                    String fullName = fileDownload.getLeft();
                    String tenantCode = fileDownload.getRight();
                    String resPath = storageOperate.getResourceFileName(tenantCode, fullName);
                    logger.info("get resource file from path:{}", resPath);
                    long resourceDownloadStartTime = System.currentTimeMillis();
                    storageOperate.download(tenantCode, resPath, execLocalPath + File.separator + fullName, false,
                            true);
                    WorkerServerMetrics
                            .recordWorkerResourceDownloadTime(System.currentTimeMillis() - resourceDownloadStartTime);
                    WorkerServerMetrics.recordWorkerResourceDownloadSize(
                            Files.size(Paths.get(execLocalPath, fullName)));
                    WorkerServerMetrics.incWorkerResourceDownloadSuccessCount();
                } catch (Exception e) {
                    WorkerServerMetrics.incWorkerResourceDownloadFailureCount();
                    throw new TaskException(String.format("Download resource file: %s error", fileDownload), e);
                }
            }
        }
    }

    private static void createDirectoryWithOwner(Path filePath, String tenant) {
        if (Files.exists(filePath)) {
            return;
        }
        try {
            Files.createDirectories(filePath);
            if (!CommonUtils.isSetTaskDirToTenantEnable()) {
                // we need to open sudo, then we can change the owner.
                return;
            }
            UserPrincipalLookupService userPrincipalLookupService =
                    FileSystems.getDefault().getUserPrincipalLookupService();
            UserPrincipal tenantPrincipal = userPrincipalLookupService.lookupPrincipalByName(tenant);
            Files.setOwner(filePath, tenantPrincipal);
        } catch (IOException e) {
            throw new TaskException("Set tenant directory permission failed, tenant: " + tenant, e);
        }
    }
}
