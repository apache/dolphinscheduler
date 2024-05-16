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

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.lang3.StringUtils;

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
public class TaskFilesTransferUtils {

    // tmp path in local path for transfer
    final static String DOWNLOAD_TMP = ".DT_TMP";

    // suffix of the package file
    final static String PACK_SUFFIX = "_ds_pack.zip";

    // root path in resource storage
    final static String RESOURCE_TAG = "DATA_TRANSFER";

    private TaskFilesTransferUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * upload output files to resource storage
     *
     * @param taskExecutionContext is the context of task
     * @param storageOperate       is the storage operate
     * @throws TaskException TaskException
     */
    public static void uploadOutputFiles(TaskExecutionContext taskExecutionContext,
                                         StorageOperate storageOperate) throws TaskException {
        // get OUTPUT FILE parameters
        List<Property> localParamsProperty = getFileLocalParams(taskExecutionContext, Direct.OUT);
        if (localParamsProperty.isEmpty()) {
            return;
        }

        List<Property> varPools = getVarPools(taskExecutionContext);
        // get map of varPools for quick search
        Map<String, Property> varPoolsMap = varPools.stream()
                .filter(property -> Direct.OUT.equals(property.getDirect()))
                .collect(Collectors.toMap(Property::getProp, x -> x));

        log.info("Upload output files ...");
        for (Property property : localParamsProperty) {
            // get local file path
            String path = String.format("%s/%s", taskExecutionContext.getExecutePath(), property.getValue());
            String srcPath = packIfDir(path);

            // get crc file path
            String srcCRCPath = srcPath + CRC_SUFFIX;
            try {
                FileUtils.writeContent2File(FileUtils.getFileChecksum(path), srcCRCPath);
            } catch (IOException ex) {
                throw new TaskException(ex.getMessage(), ex);
            }

            // get remote file path
            String resourcePath = getResourcePath(taskExecutionContext, new File(srcPath).getName());
            String resourceCRCPath = resourcePath + CRC_SUFFIX;
            try {
                // upload file to storage
                String resourceWholePath =
                        storageOperate.getResourceFullName(taskExecutionContext.getTenantCode(), resourcePath);
                String resourceCRCWholePath =
                        storageOperate.getResourceFullName(taskExecutionContext.getTenantCode(), resourceCRCPath);
                log.info("{} --- Local:{} to Remote:{}", property, srcPath, resourceWholePath);
                storageOperate.upload(taskExecutionContext.getTenantCode(), srcPath, resourceWholePath, false, true);
                log.info("{} --- Local:{} to Remote:{}", "CRC file", srcCRCPath, resourceCRCWholePath);
                storageOperate.upload(taskExecutionContext.getTenantCode(), srcCRCPath, resourceCRCWholePath, false,
                        true);
            } catch (IOException ex) {
                throw new TaskException("Upload file to storage error", ex);
            }

            // update varPool
            Property oriProperty;
            // if the property is not in varPool, add it
            if (varPoolsMap.containsKey(property.getProp())) {
                oriProperty = varPoolsMap.get(property.getProp());
            } else {
                oriProperty = new Property(property.getProp(), Direct.OUT, DataType.FILE, property.getValue());
                varPools.add(oriProperty);
            }
            oriProperty.setProp(String.format("%s.%s", taskExecutionContext.getTaskName(), oriProperty.getProp()));
            oriProperty.setValue(resourcePath);
        }
        taskExecutionContext.setVarPool(JSONUtils.toJsonString(varPools));
    }

    /**
     * download upstream files from storage
     * only download files which are defined in the task parameters
     *
     * @param taskExecutionContext is the context of task
     * @param storageOperate       is the storage operate
     * @throws TaskException task exception
     */
    public static void downloadUpstreamFiles(TaskExecutionContext taskExecutionContext, StorageOperate storageOperate) {
        // get "IN FILE" parameters
        List<Property> localParamsProperty = getFileLocalParams(taskExecutionContext, Direct.IN);

        if (localParamsProperty.isEmpty()) {
            return;
        }

        List<Property> varPools = getVarPools(taskExecutionContext);
        // get map of varPools for quick search
        Map<String, Property> varPoolsMap = varPools
                .stream()
                .filter(property -> Direct.IN.equals(property.getDirect()))
                .collect(Collectors.toMap(Property::getProp, x -> x));

        String executePath = taskExecutionContext.getExecutePath();
        // data path to download packaged data
        String downloadTmpPath = String.format("%s/%s", executePath, DOWNLOAD_TMP);

        log.info("Download upstream files...");
        for (Property property : localParamsProperty) {
            Property inVarPool = varPoolsMap.get(property.getValue());
            if (inVarPool == null) {
                log.error("{} not in  {}", property.getValue(), varPoolsMap.keySet());
                throw new TaskException(String.format("Can not find upstream file using %s, please check the key",
                        property.getValue()));
            }

            String resourcePath = inVarPool.getValue();
            String targetPath = String.format("%s/%s", executePath, property.getProp());

            String downloadPath;
            // If the data is packaged, download it to a special directory (DOWNLOAD_TMP) and unpack it to the
            // targetPath
            boolean isPack = resourcePath.endsWith(PACK_SUFFIX);
            if (isPack) {
                downloadPath = String.format("%s/%s", downloadTmpPath, new File(resourcePath).getName());
            } else {
                downloadPath = targetPath;
            }

            try {
                String resourceWholePath =
                        storageOperate.getResourceFullName(taskExecutionContext.getTenantCode(), resourcePath);
                log.info("{} --- Remote:{} to Local:{}", property, resourceWholePath, downloadPath);
                storageOperate.download(resourceWholePath, downloadPath, true);
            } catch (IOException ex) {
                throw new TaskException("Download file from storage error", ex);
            }

            // unpack if the data is packaged
            if (isPack) {
                File downloadFile = new File(downloadPath);
                log.info("Unpack {} to {}", downloadPath, targetPath);
                ZipUtil.unpack(downloadFile, new File(targetPath));
            }
        }

        // delete DownloadTmp Folder if DownloadTmpPath exists
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File(downloadTmpPath));
        } catch (IOException e) {
            log.error("Delete DownloadTmpPath {} failed, this will not affect the task status", downloadTmpPath, e);
        }
    }

    /**
     * get local parameters property which type is FILE and direction is equal to direct
     *
     * @param taskExecutionContext is the context of task
     * @param direct               may be Direct.IN or Direct.OUT.
     * @return List<Property>
     */
    public static List<Property> getFileLocalParams(TaskExecutionContext taskExecutionContext, Direct direct) {
        List<Property> localParamsProperty = new ArrayList<>();
        JsonNode taskParams = JSONUtils.parseObject(taskExecutionContext.getTaskParams());
        for (JsonNode localParam : taskParams.get("localParams")) {
            Property property = JSONUtils.parseObject(localParam.toString(), Property.class);

            if (property.getDirect().equals(direct) && property.getType().equals(DataType.FILE)) {
                localParamsProperty.add(property);
            }
        }
        return localParamsProperty;
    }

    /**
     * get Resource path for manage files in storage
     *
     * @param taskExecutionContext is the context of task
     * @param fileName             is the file name
     * @return resource path, RESOURCE_TAG/DATE/ProcessDefineCode/ProcessDefineVersion_ProcessInstanceID/TaskName_TaskInstanceID_FileName
     */
    public static String getResourcePath(TaskExecutionContext taskExecutionContext, String fileName) {
        String date =
                DateUtils.formatTimeStamp(taskExecutionContext.getEndTime(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        // get resource Folder: RESOURCE_TAG/DATE/ProcessDefineCode/ProcessDefineVersion_ProcessInstanceID
        String resourceFolder = String.format("%s/%s/%d/%d_%d", RESOURCE_TAG, date,
                taskExecutionContext.getProcessDefineCode(), taskExecutionContext.getProcessDefineVersion(),
                taskExecutionContext.getProcessInstanceId());
        // get resource fileL: resourceFolder/TaskName_TaskInstanceID_FileName
        return String.format("%s/%s_%s_%s", resourceFolder, taskExecutionContext.getTaskName().replace(" ", "_"),
                taskExecutionContext.getTaskInstanceId(), fileName);
    }

    /**
     * get varPool from taskExecutionContext
     *
     * @param taskExecutionContext is the context of task
     * @return List<Property>
     */
    public static List<Property> getVarPools(TaskExecutionContext taskExecutionContext) {
        List<Property> varPools = new ArrayList<>();

        // get varPool
        String varPoolString = taskExecutionContext.getVarPool();
        if (StringUtils.isEmpty(varPoolString)) {
            return varPools;
        }
        // parse varPool
        for (JsonNode varPoolData : JSONUtils.parseArray(varPoolString)) {
            Property property = JSONUtils.parseObject(varPoolData.toString(), Property.class);
            varPools.add(property);
        }
        return varPools;
    }

    /**
     * If the path is a directory, pack it and return the path of the package
     *
     * @param path is the input path, may be a file or a directory
     * @return new path
     */
    public static String packIfDir(String path) throws TaskException {
        File file = new File(path);
        if (!file.exists()) {
            throw new TaskException(String.format("%s dose not exists", path));
        }
        String newPath;
        if (file.isDirectory()) {
            newPath = file.getPath() + PACK_SUFFIX;
            log.info("Pack {} to {}", path, newPath);
            ZipUtil.pack(file, new File(newPath));
        } else {
            newPath = path;
        }
        return newPath;
    }
}
