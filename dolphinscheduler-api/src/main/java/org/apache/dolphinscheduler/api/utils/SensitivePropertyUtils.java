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

import org.apache.dolphinscheduler.api.dto.workflow.WorkflowDefinitionVariablesDTO;
import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowInstanceVariablesDTO;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertySensitiveUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import lombok.experimental.UtilityClass;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * API-layer helpers for sensitive {@link Property} masking and keep-original merge.
 * <p>
 * PR1 of DSIP-105: no encryption. Crypto (PasswordUtils) belongs to a follow-up PR.
 * HTTP responses are masked at the outbound boundary via {@link #maskApiResponseData};
 * persistence and in-process service results stay plaintext.
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
        // setGlobalParams does not rebuild the cached map; drop it so getters re-read masked values
        workflowDefinition.setGlobalParamMap(null);
        return workflowDefinition;
    }

    public TaskDefinition maskTaskDefinition(TaskDefinition taskDefinition) {
        if (taskDefinition == null) {
            return null;
        }
        taskDefinition.setTaskParams(maskLocalParamsInTaskParams(taskDefinition.getTaskParams()));
        taskDefinition.setTaskParamMap(null);
        return taskDefinition;
    }

    /**
     * JSON deep-copy then mask, so MyBatis-mapped entities (e.g. version list records) stay unchanged.
     */
    @SuppressWarnings("unchecked")
    public <T extends WorkflowDefinition> T copyAndMaskWorkflowDefinition(T workflowDefinition) {
        if (workflowDefinition == null) {
            return null;
        }
        T copy = JSONUtils.parseObject(JSONUtils.toJsonString(workflowDefinition),
                (Class<T>) workflowDefinition.getClass());
        if (copy == null) {
            return (T) maskWorkflowDefinition(workflowDefinition);
        }
        return (T) maskWorkflowDefinition(copy);
    }

    /**
     * JSON deep-copy then mask, so MyBatis-mapped entities (e.g. version list records) stay unchanged.
     */
    @SuppressWarnings("unchecked")
    public <T extends TaskDefinition> T copyAndMaskTaskDefinition(T taskDefinition) {
        if (taskDefinition == null) {
            return null;
        }
        T copy = JSONUtils.parseObject(JSONUtils.toJsonString(taskDefinition),
                (Class<T>) taskDefinition.getClass());
        if (copy == null) {
            return (T) maskTaskDefinition(taskDefinition);
        }
        return (T) maskTaskDefinition(copy);
    }

    public DagData copyAndMaskDagData(DagData dagData) {
        if (dagData == null) {
            return null;
        }
        DagData copy = JSONUtils.parseObject(JSONUtils.toJsonString(dagData), DagData.class);
        if (copy == null) {
            copy = new DagData(copyAndMaskWorkflowDefinition(dagData.getWorkflowDefinition()),
                    dagData.getWorkflowTaskRelationList(),
                    copyTaskDefinitionList(dagData.getTaskDefinitionList()));
            return copy;
        }
        return maskDagData(copy);
    }

    public WorkflowInstance copyAndMaskWorkflowInstance(WorkflowInstance workflowInstance) {
        if (workflowInstance == null) {
            return null;
        }
        WorkflowInstance copy = JSONUtils.parseObject(JSONUtils.toJsonString(workflowInstance), WorkflowInstance.class);
        if (copy == null) {
            return workflowInstance;
        }
        copy.setGlobalParams(maskGlobalParams(copy.getGlobalParams()));
        if (copy.getDagData() != null) {
            maskDagData(copy.getDagData());
        }
        return copy;
    }

    /**
     * Whether {@code data} can carry sensitive {@link Property} values that HTTP must mask.
     * Unrelated {@link Result} payloads (login, cluster, user, …) should skip copy/mask.
     */
    public boolean containsSensitivePropertyPayload(Object data) {
        if (data == null) {
            return false;
        }
        if (data instanceof Result) {
            return containsSensitivePropertyPayload(((Result<?>) data).getData());
        }
        if (data instanceof PageInfo) {
            return containsSensitivePropertyPayload(((PageInfo<?>) data).getTotalList());
        }
        if (data instanceof WorkflowDefinitionVariablesDTO
                || data instanceof WorkflowInstanceVariablesDTO
                || data instanceof WorkflowInstance
                || data instanceof DagData
                || data instanceof WorkflowDefinition
                || data instanceof TaskDefinition
                || data instanceof Property) {
            return true;
        }
        if (data instanceof List) {
            for (Object item : (List<?>) data) {
                if (containsSensitivePropertyPayload(item)) {
                    return true;
                }
            }
            return false;
        }
        if (data instanceof Map) {
            for (Object value : ((Map<?, ?>) data).values()) {
                if (containsSensitivePropertyPayload(value)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * Deep-copy then mask types that can appear in {@code Result.data}.
     * Unrelated payloads are returned as-is without copying. Persistence objects are not mutated.
     */
    @SuppressWarnings("unchecked")
    public Object maskApiResponseData(Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof Result) {
            Result<Object> result = (Result<Object>) data;
            result.setData(maskApiResponseData(result.getData()));
            return result;
        }
        if (!containsSensitivePropertyPayload(data)) {
            return data;
        }
        if (data instanceof PageInfo) {
            PageInfo<Object> pageInfo = (PageInfo<Object>) data;
            List<Object> totalList = pageInfo.getTotalList();
            if (CollectionUtils.isNotEmpty(totalList)) {
                pageInfo.setTotalList((List<Object>) maskApiResponseData(totalList));
            }
            return pageInfo;
        }
        if (data instanceof WorkflowDefinitionVariablesDTO) {
            WorkflowDefinitionVariablesDTO dto = (WorkflowDefinitionVariablesDTO) data;
            dto.setGlobalParams(maskSensitiveValues(dto.getGlobalParams()));
            dto.setLocalParams((Map<String, Map<String, Object>>) maskApiResponseData(dto.getLocalParams()));
            return dto;
        }
        if (data instanceof WorkflowInstanceVariablesDTO) {
            WorkflowInstanceVariablesDTO dto = (WorkflowInstanceVariablesDTO) data;
            dto.setGlobalParams(maskSensitiveValues(dto.getGlobalParams()));
            dto.setLocalParams((Map<String, Map<String, Object>>) maskApiResponseData(dto.getLocalParams()));
            return dto;
        }
        if (data instanceof WorkflowInstance) {
            return copyAndMaskWorkflowInstance((WorkflowInstance) data);
        }
        if (data instanceof DagData) {
            return copyAndMaskDagData((DagData) data);
        }
        if (data instanceof WorkflowDefinition) {
            return copyAndMaskWorkflowDefinition((WorkflowDefinition) data);
        }
        if (data instanceof TaskDefinition) {
            return copyAndMaskTaskDefinition((TaskDefinition) data);
        }
        if (data instanceof List) {
            List<?> list = (List<?>) data;
            if (CollectionUtils.isEmpty(list)) {
                return data;
            }
            if (list.get(0) instanceof Property) {
                return maskSensitiveValues((List<Property>) data);
            }
            List<Object> masked = new ArrayList<>(list.size());
            for (Object item : list) {
                masked.add(maskApiResponseData(item));
            }
            return masked;
        }
        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            Map<Object, Object> masked = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                masked.put(entry.getKey(), maskApiResponseData(entry.getValue()));
            }
            return masked;
        }
        return data;
    }

    private List<TaskDefinition> copyTaskDefinitionList(List<TaskDefinition> taskDefinitions) {
        if (CollectionUtils.isEmpty(taskDefinitions)) {
            return taskDefinitions;
        }
        List<TaskDefinition> copied = new ArrayList<>(taskDefinitions.size());
        for (TaskDefinition taskDefinition : taskDefinitions) {
            copied.add(copyAndMaskTaskDefinition(taskDefinition));
        }
        return copied;
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
                    "parameter '" + invalidProp
                            + "' cannot use ****** when creating, enabling, or disabling sensitive; please re-enter the value");
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
