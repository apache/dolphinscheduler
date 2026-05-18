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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

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
     * @param context          task execution context
     * @param storageOperator  resource storage operator
     * @return a list of uploaded output file properties
     */
    public static List<Property> tryUploadOutputFiles(TaskExecutionContext context,
                                                      StorageOperator storageOperator) {
        List<Property> outFileParams = getOutFileLocalParams(context);
        if (outFileParams.isEmpty()) {
            log.debug("No output files to upload");
            return outFileParams;
        }

        List<Property> varPool = context.getVarPool();
        // get map of varPools for quick search
        Map<String, Property> varPoolsMap = varPool.stream()
                .filter(property -> Direct.OUT.equals(property.getDirect()))
                .collect(Collectors.toMap(Property::getProp, x -> x));

        for (Property outProp : outFileParams) {
            String localPath = context.getExecutePath() + File.separator + outProp.getValue();
            String srcPath = packIfDirectory(localPath);
            String srcCrcPath = srcPath + Constants.CRC_SUFFIX;

            // Generate CRC file
            try {
                FileUtils.writeContent2File(FileUtils.getFileChecksum(localPath), srcCrcPath);
            } catch (IOException e) {
                throw new TaskException("Generate CRC file failed: " + srcCrcPath, e);
            }

            // Build resource path
            String fileName = new File(srcPath).getName();
            String resourcePath = buildResourcePath(context, fileName);
            String resourceCrcPath = resourcePath + Constants.CRC_SUFFIX;

            // Upload file and CRC file
            try {
                String resourceFullPath = storageOperator.getStorageFileAbsolutePath(
                        context.getTenantCode(),
                        resourcePath);
                String resourceCrcFullPath = storageOperator.getStorageFileAbsolutePath(
                        context.getTenantCode(),
                        resourceCrcPath);
                log.info("Upload {} -> {}", srcPath, resourceFullPath);
                storageOperator.upload(srcPath, resourceFullPath, false, true);
                log.info("Upload CRC {} -> {}", srcCrcPath, resourceCrcFullPath);
                storageOperator.upload(srcCrcPath, resourceCrcFullPath, false, true);
            } catch (Exception e) {
                throw new TaskException("Upload file to storage failed: " + srcPath, e);
            }

            // update varPool
            Property oriProperty;
            // if the property is not in varPool, add it
            if (varPoolsMap.containsKey(outProp.getProp())) {
                oriProperty = varPoolsMap.get(outProp.getProp());
            } else {
                oriProperty = new Property(outProp.getProp(), Direct.OUT, DataType.FILE, outProp.getValue());
                varPool.add(oriProperty);
            }
            oriProperty.setProp(String.format("%s.%s", context.getTaskName(), oriProperty.getProp()));
            oriProperty.setValue(resourcePath);
        }

        return outFileParams;
    }

    /**
     * Download upstream task's FILE parameter files from resource center.
     *
     * @param taskChannel       task channel used to parse task parameters
     * @param context           task execution context, used to get local working directory and other information
     * @param storageOperator   resource storage operator
     * @return a list of download input file properties
     */
    public static List<Property> tryDownloadUpstreamFiles(TaskChannel taskChannel,
                                                          TaskExecutionContext context,
                                                          StorageOperator storageOperator) {
        AbstractParameters abstractParameters = taskChannel.parseParameters(context.getTaskParams());

        List<Property> inFileParams = abstractParameters.getLocalParams()
                .stream()
                .filter(prop -> prop.getDirect().equals(Direct.IN) && prop.getType().equals(DataType.FILE))
                .collect(Collectors.toList());

        if (inFileParams.isEmpty()) {
            log.debug("No input files to download");
            return inFileParams;
        }

        Map<String, Property> prepareParamsMap = context.getPrepareParamsMap();

        String executePath = context.getExecutePath();
        String downloadTmpPath = executePath + File.separator + Constants.DOWNLOAD_TMP;

        for (Property fileProp : inFileParams) {
            // Build local path
            String paramName = fileProp.getProp();
            String fileParamVarPoolKey = fileProp.getValue();
            Property prepareParam = prepareParamsMap.get(fileParamVarPoolKey);
            if (prepareParam == null) {
                log.error("{} not in  {}", paramName, prepareParamsMap.keySet());
                throw new TaskException(String.format("Can not find upstream file using %s, please check the key",
                        fileParamVarPoolKey));
            }

            String resourcePath = prepareParam.getValue();
            String localTargetPath = executePath + File.separator + paramName;

            // Build resource path
            boolean isPack = resourcePath.endsWith(Constants.PACK_SUFFIX);
            String downloadPath = isPack
                    ? downloadTmpPath + File.separator + new File(resourcePath).getName()
                    : localTargetPath;

            // Download resource file
            String resourceFullPath = storageOperator.getStorageFileAbsolutePath(context.getTenantCode(), resourcePath);
            log.info("Download {} -> {}", resourceFullPath, downloadPath);
            storageOperator.download(resourceFullPath, downloadPath, true);

            // If it is a packaged file, unpack to target directory
            if (isPack) {
                File packFile = new File(downloadPath);
                log.info("Unpack {} to {}", downloadPath, localTargetPath);
                ZipUtil.unpack(packFile, new File(localTargetPath));
            }
        }

        // Clean up temporary directory
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File(downloadTmpPath));
        } catch (IOException e) {
            log.warn("Delete temp directory {} failed, ignored.", downloadTmpPath, e);
        }

        return inFileParams;
    }

    public static List<Property> getOutFileLocalParams(TaskExecutionContext taskExecutionContext) {
        List<Property> localParamsProperty = new ArrayList<>();
        JsonNode taskParams = JSONUtils.parseObject(taskExecutionContext.getTaskParams());
        for (JsonNode localParam : taskParams.get("localParams")) {
            Property property = JSONUtils.parseObject(localParam.toString(), Property.class);

            if (property != null && property.getDirect().equals(Direct.OUT)
                    && property.getType().equals(DataType.FILE)) {
                localParamsProperty.add(property);
            }
        }
        return localParamsProperty;
    }

    public static String packIfDirectory(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new TaskException("File not found: " + path);
        }
        if (file.isDirectory()) {
            String zipPath = file.getPath() + Constants.PACK_SUFFIX;
            ZipUtil.pack(file, new File(zipPath));
            return zipPath;
        }
        return path;
    }

    public static String buildResourcePath(TaskExecutionContext context, String fileName) {
        String date = DateUtils.formatTimeStamp(context.getStartTime(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        String folder = String.format("%s/%s/%d/%d_%d",
                Constants.RESOURCE_TAG, date,
                context.getWorkflowDefinitionCode(),
                context.getWorkflowDefinitionVersion(),
                context.getWorkflowInstanceId());
        String safeTaskName = context.getTaskName().replace(" ", "_");
        return String.format("%s/%s_%s_%s", folder, safeTaskName, context.getTaskInstanceId(), fileName);
    }
}
