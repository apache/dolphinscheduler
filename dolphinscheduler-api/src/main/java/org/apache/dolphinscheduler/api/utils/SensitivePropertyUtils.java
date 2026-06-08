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

package org.apache.dolphinscheduler.api.utils;

import static org.apache.dolphinscheduler.common.constants.Constants.LOCAL_PARAMS;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertySensitiveUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import lombok.experimental.UtilityClass;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@UtilityClass
public class SensitivePropertyUtils {

    public String encryptGlobalParams(String globalParams) {
        List<Property> properties = GlobalParameterUtils.deserializeGlobalParameter(globalParams);
        if (CollectionUtils.isEmpty(properties)) {
            return globalParams;
        }
        return GlobalParameterUtils.serializeGlobalParameter(encryptSensitiveValues(properties));
    }

    public String mergeAndEncryptGlobalParams(String submittedGlobalParams, String existingGlobalParams) {
        List<Property> submittedProperties = GlobalParameterUtils.deserializeGlobalParameter(submittedGlobalParams);
        if (CollectionUtils.isEmpty(submittedProperties)) {
            return submittedGlobalParams;
        }
        List<Property> existingProperties =
                decryptSensitiveValues(GlobalParameterUtils.deserializeGlobalParameter(existingGlobalParams));
        return GlobalParameterUtils.serializeGlobalParameter(encryptSensitiveValues(
                PropertySensitiveUtils.mergeSensitiveValuePlaceholders(submittedProperties, existingProperties)));
    }

    public String decryptAndMaskGlobalParams(String globalParams) {
        List<Property> properties = GlobalParameterUtils.deserializeGlobalParameter(globalParams);
        if (CollectionUtils.isEmpty(properties)) {
            return globalParams;
        }
        return GlobalParameterUtils.serializeGlobalParameter(decryptAndMaskSensitiveValues(properties));
    }

    public List<Property> decryptAndMaskSensitiveValues(List<Property> properties) {
        return PropertySensitiveUtils.maskSensitiveValues(decryptSensitiveValues(properties));
    }

    public DagData decryptAndMaskDagData(DagData dagData) {
        if (dagData == null) {
            return null;
        }
        decryptAndMaskWorkflowDefinition(dagData.getWorkflowDefinition());
        if (CollectionUtils.isNotEmpty(dagData.getTaskDefinitionList())) {
            dagData.getTaskDefinitionList().forEach(SensitivePropertyUtils::decryptAndMaskTaskDefinition);
        }
        return dagData;
    }

    public WorkflowDefinition decryptAndMaskWorkflowDefinition(WorkflowDefinition workflowDefinition) {
        if (workflowDefinition == null) {
            return null;
        }
        List<Property> globalParams =
                decryptAndMaskSensitiveValues(GlobalParameterUtils.deserializeGlobalParameter(
                        workflowDefinition.getGlobalParams()));
        workflowDefinition.setGlobalParams(GlobalParameterUtils.serializeGlobalParameter(globalParams));
        workflowDefinition.setGlobalParamList(globalParams);
        return workflowDefinition;
    }

    public TaskDefinition decryptAndMaskTaskDefinition(TaskDefinition taskDefinition) {
        if (taskDefinition == null) {
            return null;
        }
        taskDefinition.setTaskParams(decryptAndMaskLocalParamsInTaskParams(taskDefinition.getTaskParams()));
        return taskDefinition;
    }

    public String encryptLocalParamsInTaskParams(String taskParams) {
        return transformLocalParamsInTaskParams(taskParams, SensitivePropertyUtils::encryptSensitiveValues);
    }

    public String mergeAndEncryptLocalParamsInTaskParams(String submittedTaskParams, String existingTaskParams) {
        return transformLocalParamsInTaskParams(submittedTaskParams, submittedProperties -> {
            List<Property> existingProperties = decryptSensitiveValues(getLocalParams(existingTaskParams));
            return encryptSensitiveValues(
                    PropertySensitiveUtils.mergeSensitiveValuePlaceholders(submittedProperties, existingProperties));
        });
    }

    public String decryptAndMaskLocalParamsInTaskParams(String taskParams) {
        return transformLocalParamsInTaskParams(taskParams, SensitivePropertyUtils::decryptAndMaskSensitiveValues);
    }

    public String decryptLocalParamsInTaskParams(String taskParams) {
        return transformLocalParamsInTaskParams(taskParams, SensitivePropertyUtils::decryptSensitiveValues);
    }

    public List<Property> encryptSensitiveValues(List<Property> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return Collections.emptyList();
        }
        List<Property> encryptedProperties = PropertySensitiveUtils.copy(properties);
        encryptedProperties.stream()
                .filter(PropertySensitiveUtils::isSensitive)
                .filter(property -> StringUtils.isNotEmpty(property.getValue()))
                .filter(property -> !PropertySensitiveUtils.isSensitiveValuePlaceholder(property.getValue()))
                .forEach(property -> property.setValue(PasswordUtils.encodePassword(property.getValue())));
        return encryptedProperties;
    }

    public List<Property> decryptSensitiveValues(List<Property> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return Collections.emptyList();
        }
        List<Property> decryptedProperties = PropertySensitiveUtils.copy(properties);
        decryptedProperties.stream()
                .filter(PropertySensitiveUtils::isSensitive)
                .filter(property -> StringUtils.isNotEmpty(property.getValue()))
                .filter(property -> !PropertySensitiveUtils.isSensitiveValuePlaceholder(property.getValue()))
                .forEach(property -> property.setValue(PasswordUtils.decodePassword(property.getValue())));
        return decryptedProperties;
    }

    public List<Property> getLocalParams(String taskParams) {
        if (StringUtils.isEmpty(taskParams)) {
            return Collections.emptyList();
        }
        String localParams = JSONUtils.getNodeString(taskParams, LOCAL_PARAMS);
        if (StringUtils.isEmpty(localParams)) {
            return Collections.emptyList();
        }
        return JSONUtils.toList(localParams, Property.class);
    }

    private String transformLocalParamsInTaskParams(String taskParams,
                                                    Function<List<Property>, List<Property>> transformFunction) {
        if (StringUtils.isEmpty(taskParams)) {
            return taskParams;
        }
        ObjectNode taskParamsNode = JSONUtils.parseObject(taskParams);
        JsonNode localParamsNode = taskParamsNode.findValue(LOCAL_PARAMS);
        if (localParamsNode == null || localParamsNode.isNull()) {
            return taskParams;
        }
        List<Property> localParams = JSONUtils.toList(localParamsNode.toString(), Property.class);
        taskParamsNode.set(LOCAL_PARAMS, JSONUtils.toJsonNode(transformFunction.apply(localParams)));
        return JSONUtils.toJsonString(taskParamsNode);
    }
}
