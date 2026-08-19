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

import static org.apache.dolphinscheduler.common.constants.Constants.CRC_SUFFIX;
import static org.apache.dolphinscheduler.common.constants.Constants.DOWNLOAD_TMP;
import static org.apache.dolphinscheduler.common.constants.Constants.PACK_SUFFIX;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_TAG;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.zeroturnaround.zip.ZipUtil;

import com.fasterxml.jackson.databind.JsonNode;

@Slf4j
public final class TaskFilesTransferUtils {

    private TaskFilesTransferUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Upload the task's output FILE parameter files to the resource center.
     *
     * @param taskExecutionContext task execution context
     * @param storageOperator      resource storage operator
     * @return a list of uploaded output file properties
     */
    public static List<Property> uploadOutputFiles(TaskExecutionContext taskExecutionContext,
                                                   StorageOperator storageOperator) {
        List<Property> outFileParams = getFileLocalParams(taskExecutionContext, Direct.OUT);
        if (outFileParams.isEmpty()) {
            log.debug("No output files to upload");
            return outFileParams;
        }

        List<Property> varPool = taskExecutionContext.getVarPool();
        if (varPool == null) {
            varPool = new ArrayList<>();
            taskExecutionContext.setVarPool(varPool);
        }
        // get map of varPools for quick search
        Map<String, Property> varPoolsMap = varPool.stream()
                .filter(property -> Direct.OUT.equals(property.getDirect()))
                .collect(Collectors.toMap(Property::getProp, x -> x));

        log.info("Upload output files ...");
        for (Property outProp : outFileParams) {
            // get local file path
            String localPath = taskExecutionContext.getExecutePath() + File.separator + outProp.getValue();
            String srcPath = packIfDirectory(localPath);

            // get CRC file path
            String srcCrcPath = srcPath + CRC_SUFFIX;
            try {
                FileUtils.writeContent2File(FileUtils.getFileChecksum(localPath), srcCrcPath);
            } catch (IOException e) {
                throw new TaskException("Generate CRC file failed: " + srcCrcPath, e);
            }

            // get remote file path
            String fileName = new File(srcPath).getName();
            String resourcePath = buildResourcePath(taskExecutionContext, fileName);
            String resourceCrcPath = resourcePath + CRC_SUFFIX;

            try {
                // upload file to storage
                String resourceFullPath = storageOperator.getStorageFileAbsolutePath(
                        taskExecutionContext.getTenantCode(), resourcePath);
                String resourceCrcFullPath = storageOperator.getStorageFileAbsolutePath(
                        taskExecutionContext.getTenantCode(), resourceCrcPath);
                log.info("{} --- Local:{} to Remote:{}", outProp, srcPath, resourceFullPath);
                storageOperator.upload(srcPath, resourceFullPath, false, true);
                log.info("CRC file --- Local:{} to Remote:{}", srcCrcPath, resourceCrcFullPath);
                storageOperator.upload(srcCrcPath, resourceCrcFullPath, false, true);
            } catch (Exception e) {
                throw new TaskException("Upload file to storage failed: " + srcPath, e);
            }

            // update varPool
            Property oriProperty;
            if (varPoolsMap.containsKey(outProp.getProp())) {
                oriProperty = varPoolsMap.get(outProp.getProp());
            } else {
                oriProperty = new Property(outProp.getProp(), Direct.OUT, DataType.FILE, outProp.getValue());
                varPool.add(oriProperty);
            }
            oriProperty.setProp(String.format("%s.%s", taskExecutionContext.getTaskName(), oriProperty.getProp()));
            oriProperty.setValue(resourcePath);
        }

        return outFileParams;
    }

    /**
     * Download upstream task's FILE parameter files from resource center.
     *
     * @param taskExecutionContext task execution context
     * @param storageOperator      resource storage operator
     * @return a list of downloaded input file properties
     */
    public static List<Property> downloadUpstreamFiles(TaskExecutionContext taskExecutionContext,
                                                       StorageOperator storageOperator) {
        // get "IN FILE" parameters
        List<Property> inFileParams = getFileLocalParams(taskExecutionContext, Direct.IN);

        if (inFileParams.isEmpty()) {
            log.debug("No input files to download");
            return inFileParams;
        }

        List<Property> varPool = taskExecutionContext.getVarPool();
        if (varPool == null || varPool.isEmpty()) {
            log.debug("VarPool is empty, no upstream files to download");
            return inFileParams;
        }

        // get map of varPools for quick search (look up by prop name)
        Map<String, Property> varPoolsMap = varPool.stream()
                .collect(Collectors.toMap(Property::getProp, x -> x, (existing, replacement) -> existing));

        String executePath = taskExecutionContext.getExecutePath();
        // data path to download packaged data
        String downloadTmpPath = executePath + File.separator + DOWNLOAD_TMP;

        log.info("Download upstream files...");
        for (Property fileProp : inFileParams) {
            // fileProp.getValue() is the upstream parameter reference, e.g., "ot.dir-data"
            String fileParamVarPoolKey = fileProp.getValue();
            Property varPoolEntry = varPoolsMap.get(fileParamVarPoolKey);
            if (varPoolEntry == null) {
                log.error("{} not found in varPool, available keys: {}", fileParamVarPoolKey, varPoolsMap.keySet());
                throw new TaskException(String.format(
                        "Cannot find upstream file using key '%s', please check the parameter reference",
                        fileParamVarPoolKey));
            }

            String resourcePath = varPoolEntry.getValue();
            String localTargetPath = executePath + File.separator + fileProp.getProp();

            // If the data is packaged, download it to a special directory (DOWNLOAD_TMP) and unpack it to the targetPath
            boolean isPack = resourcePath.endsWith(PACK_SUFFIX);
            String downloadPath = isPack
                    ? downloadTmpPath + File.separator + new File(resourcePath).getName()
                    : localTargetPath;

            String resourceFullPath = storageOperator.getStorageFileAbsolutePath(
                    taskExecutionContext.getTenantCode(), resourcePath);
            log.info("{} --- Remote:{} to Local:{}", fileProp, resourceFullPath, downloadPath);
            storageOperator.download(resourceFullPath, downloadPath, true);

            // unpack if the data is packaged
            if (isPack) {
                File downloadFile = new File(downloadPath);
                log.info("Unpack {} to {}", downloadPath, localTargetPath);
                ZipUtil.unpack(downloadFile, new File(localTargetPath));
            }
        }

        // delete DownloadTmp Folder if DownloadTmpPath exists
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File(downloadTmpPath));
        } catch (IOException e) {
            log.warn("Delete temp directory {} failed, this will not affect the task status", downloadTmpPath, e);
        }

        return inFileParams;
    }

    /**
     * Get local parameters property which type is FILE and direction is equal to direct.
     *
     * @param taskExecutionContext task execution context
     * @param direct               may be Direct.IN or Direct.OUT
     * @return List of Property
     */
    public static List<Property> getFileLocalParams(TaskExecutionContext taskExecutionContext, Direct direct) {
        List<Property> localParamsProperty = new ArrayList<>();
        JsonNode taskParams = JSONUtils.parseObject(taskExecutionContext.getTaskParams());
        JsonNode localParamsNode = taskParams.get("localParams");
        if (localParamsNode == null || !localParamsNode.isArray()) {
            return localParamsProperty;
        }
        for (JsonNode localParam : localParamsNode) {
            Property property = JSONUtils.parseObject(localParam.toString(), Property.class);
            if (property != null && property.getDirect().equals(direct) && property.getType().equals(DataType.FILE)) {
                localParamsProperty.add(property);
            }
        }
        return localParamsProperty;
    }

    /**
     * Build resource path for managing files in storage.
     *
     * @param taskExecutionContext task execution context
     * @param fileName             file name
     * @return resource path: RESOURCE_TAG/DATE/ProcessDefineCode/ProcessDefineVersion_ProcessInstanceID/TaskName_TaskInstanceID_FileName
     */
    public static String buildResourcePath(TaskExecutionContext taskExecutionContext, String fileName) {
        String date = DateUtils.formatTimeStamp(taskExecutionContext.getStartTime(),
                DateTimeFormatter.ofPattern("yyyyMMdd"));
        // get resource Folder: RESOURCE_TAG/DATE/ProcessDefineCode/ProcessDefineVersion_ProcessInstanceID
        String resourceFolder = String.format("%s/%s/%d/%d_%d", RESOURCE_TAG, date,
                taskExecutionContext.getWorkflowDefinitionCode(),
                taskExecutionContext.getWorkflowDefinitionVersion(),
                taskExecutionContext.getWorkflowInstanceId());
        // get resource file: resourceFolder/TaskName_TaskInstanceID_FileName
        String safeTaskName = taskExecutionContext.getTaskName().replace(" ", "_");
        return String.format("%s/%s_%s_%s", resourceFolder, safeTaskName,
                taskExecutionContext.getTaskInstanceId(), fileName);
    }

    /**
     * If the path is a directory, pack it and return the path of the package.
     *
     * @param path input path, may be a file or a directory
     * @return new path
     */
    public static String packIfDirectory(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new TaskException(String.format("%s does not exist", path));
        }
        if (file.isDirectory()) {
            String zipPath = file.getPath() + PACK_SUFFIX;
            log.info("Pack {} to {}", path, zipPath);
            ZipUtil.pack(file, new File(zipPath));
            return zipPath;
        }
        return path;
    }
}