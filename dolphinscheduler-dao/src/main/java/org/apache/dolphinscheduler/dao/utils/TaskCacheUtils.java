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

package org.apache.dolphinscheduler.dao.utils;

import static org.apache.dolphinscheduler.common.constants.Constants.CRC_SUFFIX;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;

@Slf4j
public class TaskCacheUtils {

    private TaskCacheUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static final String MERGE_TAG = "-";

    /**
     * generate cache key for task instance
     * the follow message will be used to generate cache key
     * 2. task version
     * 3. task is cache
     * 4. input VarPool, from upstream task and workflow global parameters
     * @param taskInstance task instance
     * @param taskExecutionContext taskExecutionContext
     * @param storageOperate storageOperate
     * @return cache key
     */
    public static String generateCacheKey(TaskInstance taskInstance, TaskExecutionContext taskExecutionContext,
                                          StorageOperate storageOperate) {
        List<String> keyElements = new ArrayList<>();
        keyElements.add(String.valueOf(taskInstance.getTaskCode()));
        keyElements.add(String.valueOf(taskInstance.getTaskDefinitionVersion()));
        keyElements.add(String.valueOf(taskInstance.getIsCache().getCode()));
        keyElements.add(String.valueOf(taskInstance.getEnvironmentConfig()));
        keyElements.add(getTaskInputVarPoolData(taskInstance, taskExecutionContext, storageOperate));
        String data = StringUtils.join(keyElements, "_");
        return DigestUtils.sha256Hex(data);
    }

    /**
     * generate cache key for task instance which is cache execute
     * this key will record which cache task instance will be copied, and cache key will be used
     * tagCacheKey = sourceTaskId + "-" + cacheKey
     * @param sourceTaskId source task id
     * @param cacheKey cache key
     * @return tagCacheKey
     */
    public static String generateTagCacheKey(Integer sourceTaskId, String cacheKey) {
        return sourceTaskId + MERGE_TAG + cacheKey;
    }

    /**
     * revert cache key tag to source task id and cache key
     * @param tagCacheKey cache key
     * @return Pair<Integer, String>, first is source task id, second is cache key
     */
    public static Pair<Integer, String> revertCacheKey(String tagCacheKey) {
        Pair<Integer, String> taskIdAndCacheKey;
        if (tagCacheKey == null) {
            taskIdAndCacheKey = Pair.of(-1, "");
            return taskIdAndCacheKey;
        }
        if (tagCacheKey.contains(MERGE_TAG)) {
            String[] split = tagCacheKey.split(MERGE_TAG);
            if (split.length == 2) {
                taskIdAndCacheKey = Pair.of(Integer.parseInt(split[0]), split[1]);
            } else {
                taskIdAndCacheKey = Pair.of(-1, "");
            }
            return taskIdAndCacheKey;
        } else {
            return Pair.of(-1, tagCacheKey);
        }
    }

    /**
     * get hash data of task input var pool
     * there are two parts of task input var pool: from upstream task and workflow global parameters
     * @param taskInstance task instance
     * taskExecutionContext taskExecutionContext
     */
    public static String getTaskInputVarPoolData(TaskInstance taskInstance, TaskExecutionContext context,
                                                 StorageOperate storageOperate) {
        JsonNode taskParams = JSONUtils.parseObject(taskInstance.getTaskParams());

        // The set of input values considered from localParams in the taskParams
        Set<String> propertyInSet = JSONUtils.toList(taskParams.get("localParams").toString(), Property.class).stream()
                .filter(property -> property.getDirect().equals(Direct.IN))
                .map(Property::getProp).collect(Collectors.toSet());

        // The set of input values considered from `${var}` form task definition
        propertyInSet.addAll(getScriptVarInSet(taskInstance));

        // var pool value from upstream task
        List<Property> varPool = JSONUtils.toList(taskInstance.getVarPool(), Property.class);

        Map<String, String> fileCheckSumMap = new HashMap<>();
        List<Property> fileInput = varPool.stream().filter(property -> property.getType().equals(DataType.FILE))
                .collect(Collectors.toList());
        fileInput.forEach(
                property -> fileCheckSumMap.put(property.getProp(), getValCheckSum(property, context, storageOperate)));

        // var pool value from workflow global parameters
        if (context.getPrepareParamsMap() != null) {
            Set<String> taskVarPoolSet = varPool.stream().map(Property::getProp).collect(Collectors.toSet());
            List<Property> globalContextVarPool = context.getPrepareParamsMap().entrySet().stream()
                    .filter(entry -> !taskVarPoolSet.contains(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
            varPool.addAll(globalContextVarPool);
        }

        // only consider var pool value which is in propertyInSet
        varPool = varPool.stream()
                .filter(property -> property.getDirect().equals(Direct.IN))
                .filter(property -> propertyInSet.contains(property.getProp()))
                .sorted(Comparator.comparing(Property::getProp))
                .collect(Collectors.toList());

        varPool.forEach(property -> {
            if (property.getType() == DataType.FILE) {
                property.setValue(fileCheckSumMap.get(property.getValue()));
            }
        });
        return JSONUtils.toJsonString(varPool);
    }

    /**
     * get checksum from crc32 file of file property in varPool
     * cache can be used if content of upstream output files are the same
     * @param fileProperty
     * @param context
     * @param storageOperate
     */
    public static String getValCheckSum(Property fileProperty, TaskExecutionContext context,
                                        StorageOperate storageOperate) {
        String resourceCRCPath = fileProperty.getValue() + CRC_SUFFIX;
        String resourceCRCWholePath = storageOperate.getResourceFullName(context.getTenantCode(), resourceCRCPath);
        String targetPath = String.format("%s/%s", context.getExecutePath(), resourceCRCPath);
        log.info("{} --- Remote:{} to Local:{}", "CRC file", resourceCRCWholePath, targetPath);
        String crcString = "";
        try {
            storageOperate.download(context.getTenantCode(), resourceCRCWholePath, targetPath, true);
            crcString = FileUtils.readFile2Str(new FileInputStream(targetPath));
            fileProperty.setValue(crcString);
        } catch (IOException e) {
            log.error("Replace checksum failed for file property {}.", fileProperty.getProp());
        }
        return crcString;
    }

    /**
     * get var in set from task definition
     * @param taskInstance task instance
     * @return var in set
     */
    public static List<String> getScriptVarInSet(TaskInstance taskInstance) {
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(taskInstance.getTaskParams());

        List<String> varInSet = new ArrayList<>();
        while (matcher.find()) {
            varInSet.add(matcher.group(1));
        }
        return varInSet;
    }

}
