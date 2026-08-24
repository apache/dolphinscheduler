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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
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

/**
 * API-layer helpers for sensitive {@link Property} masking and keep-original merge.
 * <p>
 * PR1 of DSIP-105: no encryption. Crypto (PasswordUtils) belongs to a follow-up PR.
 */
@UtilityClass
public class SensitivePropertyUtils {

    public String mergeGlobalParams(String submittedGlobalParams, String existingGlobalParams) {
        List<Property> submittedProperties = GlobalParameterUtils.deserializeGlobalParameter(submittedGlobalParams);
        if (CollectionUtils.isEmpty(submittedProperties)) {
            return submittedGlobalParams;
        }
        List<Property> existingProperties = GlobalParameterUtils.deserializeGlobalParameter(existingGlobalParams);
        validateSensitivePlaceholders(submittedProperties, existingProperties);
        return GlobalParameterUtils.serializeGlobalParameter(
                PropertySensitiveUtils.mergeSensitiveValuePlaceholders(submittedProperties, existingProperties));
    }

    public String maskGlobalParams(String globalParams) {
        List<Property> properties = GlobalParameterUtils.deserializeGlobalParameter(globalParams);
        if (CollectionUtils.isEmpty(properties)) {
            return globalParams;
        }
        return GlobalParameterUtils.serializeGlobalParameter(PropertySensitiveUtils.maskSensitiveValues(properties));
    }

    public List<Property> maskSensitiveValues(List<Property> properties) {
        return PropertySensitiveUtils.maskSensitiveValues(properties);
    }

    public DagData maskDagData(DagData dagData) {
        if (dagData == null) {
            return null;
        }
        maskWorkflowDefinition(dagData.getWorkflowDefinition());
        if (CollectionUtils.isNotEmpty(dagData.getTaskDefinitionList())) {
            dagData.getTaskDefinitionList().forEach(SensitivePropertyUtils::maskTaskDefinition);
        }
        return dagData;
    }

    public WorkflowDefinition maskWorkflowDefinition(WorkflowDefinition workflowDefinition) {
        if (workflowDefinition == null) {
            return null;
        }
        List<Property> globalParams =
                PropertySensitiveUtils.maskSensitiveValues(
                        GlobalParameterUtils.deserializeGlobalParameter(workflowDefinition.getGlobalParams()));
        workflowDefinition.setGlobalParams(GlobalParameterUtils.serializeGlobalParameter(globalParams));
        workflowDefinition.setGlobalParamList(globalParams);
        return workflowDefinition;
    }

    public TaskDefinition maskTaskDefinition(TaskDefinition taskDefinition) {
        if (taskDefinition == null) {
            return null;
        }
        taskDefinition.setTaskParams(maskLocalParamsInTaskParams(taskDefinition.getTaskParams()));
        return taskDefinition;
    }

    public String mergeLocalParamsInTaskParams(String submittedTaskParams, String existingTaskParams) {
        return transformLocalParamsInTaskParams(submittedTaskParams, submittedProperties -> {
            List<Property> existingProperties = getLocalParams(existingTaskParams);
            validateSensitivePlaceholders(submittedProperties, existingProperties);
            return PropertySensitiveUtils.mergeSensitiveValuePlaceholders(submittedProperties, existingProperties);
        });
    }

    public String maskLocalParamsInTaskParams(String taskParams) {
        return transformLocalParamsInTaskParams(taskParams, PropertySensitiveUtils::maskSensitiveValues);
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

    public void validateSensitivePlaceholders(List<Property> submittedProperties, List<Property> existingProperties) {
        String invalidProp =
                PropertySensitiveUtils.findInvalidSensitivePlaceholderProp(submittedProperties, existingProperties);
        if (invalidProp != null) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                    "sensitive parameter '" + invalidProp
                            + "' cannot use ****** when creating or enabling sensitive; please re-enter the value");
        }
    }

    private String transformLocalParamsInTaskParams(String taskParams,
                                                    Function<List<Property>, List<Property>> transformFunction) {
        if (StringUtils.isEmpty(taskParams)) {
            return taskParams;
        }
        ObjectNode taskParamsNode = JSONUtils.parseObject(taskParams);
        if (taskParamsNode == null) {
            return taskParams;
        }
        JsonNode localParamsNode = taskParamsNode.findValue(LOCAL_PARAMS);
        if (localParamsNode == null || localParamsNode.isNull()) {
            return taskParams;
        }
        List<Property> localParams = JSONUtils.toList(localParamsNode.toString(), Property.class);
        taskParamsNode.set(LOCAL_PARAMS, JSONUtils.toJsonNode(transformFunction.apply(localParams)));
        return JSONUtils.toJsonString(taskParamsNode);
    }
}
