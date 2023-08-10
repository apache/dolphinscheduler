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
import org.apache.dolphinscheduler.common.exception.StorageOperateNoConfiguredException;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.metrics.WorkerServerMetrics;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskExecutionCheckerUtils {

    public static void checkTenantExist(WorkerConfig workerConfig, TaskExecutionContext taskExecutionContext) {
        try {
            String tenantCode = taskExecutionContext.getTenantCode();
            if (TenantConstants.DEFAULT_TENANT_CODE.equals(tenantCode)) {
                log.warn("Current tenant is default tenant, will use {} to execute the task",
                        TenantConstants.BOOTSTRAPT_SYSTEM_USER);
                return;
            }
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
            taskExecutionContext.setAppInfoPath(FileUtils.getAppInfoPath(execLocalPath));
            Path executePath = Paths.get(taskExecutionContext.getExecutePath());
            FileUtils.createDirectoryIfNotPresent(executePath);
            if (OSUtils.isSudoEnable()) {
                FileUtils.setFileOwner(executePath, taskExecutionContext.getTenantCode());
            }
        } catch (Throwable ex) {
            throw new TaskException("Cannot create process execute dir", ex);
        }
    }

    public static void downloadResourcesIfNeeded(StorageOperate storageOperate,
                                                 TaskExecutionContext taskExecutionContext) {
        String execLocalPath = taskExecutionContext.getExecutePath();
        String tenant = taskExecutionContext.getTenantCode();
        String actualTenant =
                TenantConstants.DEFAULT_TENANT_CODE.equals(tenant) ? TenantConstants.BOOTSTRAPT_SYSTEM_USER : tenant;

        Map<String, String> projectRes = taskExecutionContext.getResources();
        if (MapUtils.isEmpty(projectRes)) {
            return;
        }
        List<Pair<String, String>> downloadFiles = new ArrayList<>();
        projectRes.keySet().forEach(fullName -> {
            String fileName = storageOperate.getResourceFileName(actualTenant, fullName);
            projectRes.put(fullName, fileName);
            File resFile = new File(execLocalPath, fileName);
            boolean notExist = !resFile.exists();
            if (notExist) {
                downloadFiles.add(Pair.of(fullName, fileName));
            } else {
                log.warn("Resource file : {} already exists will not download again ", resFile.getName());
            }
        });
        if (!downloadFiles.isEmpty() && !PropertyUtils.isResourceStorageStartup()) {
            throw new StorageOperateNoConfiguredException("Storage service config does not exist!");
        }

        if (CollectionUtils.isNotEmpty(downloadFiles)) {
            for (Pair<String, String> fileDownload : downloadFiles) {
                try {
                    String fullName = fileDownload.getLeft();
                    String fileName = fileDownload.getRight();
                    log.info("get resource file from path:{}", fullName);

                    long resourceDownloadStartTime = System.currentTimeMillis();
                    storageOperate.download(actualTenant, fullName, execLocalPath + File.separator + fileName, true);
                    if (OSUtils.isSudoEnable()) {
                        FileUtils.setFileOwner(Paths.get(execLocalPath, fileName),
                                taskExecutionContext.getTenantCode());
                    }
                    WorkerServerMetrics
                            .recordWorkerResourceDownloadTime(System.currentTimeMillis() - resourceDownloadStartTime);
                    WorkerServerMetrics.recordWorkerResourceDownloadSize(
                            Files.size(Paths.get(execLocalPath, fileName)));
                    WorkerServerMetrics.incWorkerResourceDownloadSuccessCount();
                } catch (Exception e) {
                    WorkerServerMetrics.incWorkerResourceDownloadFailureCount();
                    throw new TaskException(String.format("Download resource file: %s error", fileDownload), e);
                }
            }
        }
    }

}
