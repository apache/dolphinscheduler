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
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LOCAL_PARAMS_LIST;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.vo.TaskDefinitionVO;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.ErrorCommand;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstanceDependentDetails;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.master.command.AbstractCommandParam;
import org.apache.dolphinscheduler.extract.master.command.ICommandParam;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertySensitiveUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.VarPoolUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * HTTP-layer helpers for {@link Property#isSensitive()}.
 * Definition write path: merge keep-original then encode with {@link PasswordUtils} when enabled.
 * Query masks values as {@code ******}. Start / execution use decrypt copies.
 */
@UtilityClass
public class SensitivePropertyUtils {

    /** Create: {@code ******} is never a real value. */
    public void requireNoPlaceholder(List<Property> properties) {
        String invalidProp = PropertySensitiveUtils.findPlaceholderProp(properties);
        if (invalidProp != null) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                    "parameter '" + invalidProp
                            + "' cannot use ****** when creating; please re-enter the value");
        }
    }

    /**
     * Create definition params: reject {@code ******}, then encode sensitive plaintext.
     */
    public List<Property> encodeForCreate(List<Property> properties) {
        requireNoPlaceholder(properties);
        return encodeNewSensitivePlaintext(properties, Collections.emptyList());
    }

    /**
     * Update definition params: restore DB value for keep-original {@code ******}, then encode.
     * Empty / null is a real empty value. {@code false→true} + {@code ******} is allowed;
     * {@code true→false} + {@code ******} is rejected.
     * Runtime instance updates should call {@link #merge} only (no encode).
     */
    public List<Property> mergeAndEncode(List<Property> submittedProperties, List<Property> existingProperties) {
        List<Property> merged = merge(submittedProperties, existingProperties);
        return encodeNewSensitivePlaintext(merged, existingProperties);
    }

    /**
     * Update: restore DB value for keep-original {@code ******} without encoding.
     * Used for runtime instance global_params (plaintext materialization).
     */
    public List<Property> merge(List<Property> submittedProperties, List<Property> existingProperties) {
        if (CollectionUtils.isEmpty(submittedProperties)) {
            return submittedProperties;
        }
        String invalidProp = PropertySensitiveUtils.findInvalidSensitivePlaceholderProp(
                submittedProperties, existingProperties);
        if (invalidProp != null) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                    "parameter '" + invalidProp
                            + "' cannot use ****** when creating, enabling, or disabling sensitive; please re-enter the value");
        }
        return PropertySensitiveUtils.mergeSensitiveValuePlaceholders(submittedProperties, existingProperties);
    }

    /**
     * Build start/command params before persist.
     * Decrypts definition globals first, then:
     * <ul>
     * <li>Same name as a workflow global: inherit all global attributes, only override {@code value}
     * ({@code ******} keeps the global plaintext).</li>
     * <li>Not matching any global: treat as a non-sensitive start param; bare {@code ******} is dropped.</li>
     * </ul>
     */
    public List<Property> restoreStartParams(List<Property> startParams, List<Property> globalParams) {
        if (CollectionUtils.isEmpty(startParams)) {
            return startParams;
        }
        List<Property> decryptedGlobals = decodeSensitiveValues(globalParams);
        Map<String, Property> globals = CollectionUtils.emptyIfNull(decryptedGlobals).stream()
                .filter(Objects::nonNull)
                .filter(property -> property.getProp() != null)
                .collect(Collectors.toMap(Property::getProp, Function.identity(), (left, right) -> right));
        List<Property> restored = new ArrayList<>();
        for (Property startParam : startParams) {
            if (startParam == null) {
                continue;
            }
            Property global = globals.get(startParam.getProp());
            if (global != null) {
                Property inherited = PropertySensitiveUtils.copy(global);
                if (!PropertySensitiveUtils.isSensitiveValuePlaceholder(startParam.getValue())) {
                    inherited.setValue(startParam.getValue());
                }
                restored.add(inherited);
            } else if (PropertySensitiveUtils.isSensitiveValuePlaceholder(startParam.getValue())) {
                continue;
            } else {
                Property nonGlobal = PropertySensitiveUtils.copy(startParam);
                nonGlobal.setSensitive(false);
                restored.add(nonGlobal);
            }
        }
        return restored;
    }

    public List<Property> decodeSensitiveValues(List<Property> properties) {
        return PropertySensitiveUtils.transformSensitiveValues(properties, PasswordUtils::decodePassword);
    }

    public String mergeLocalParams(String submittedTaskParams, String existingTaskParams) {
        return mergeLocalParams(submittedTaskParams, existingTaskParams, true);
    }

    /**
     * @param encodeForDefinition when true, encode after merge (workflow/task definition persist);
     *                            when false, merge only (runtime instance edit).
     */
    public String mergeLocalParams(String submittedTaskParams, String existingTaskParams,
                                   boolean encodeForDefinition) {
        return rewriteLocalParams(submittedTaskParams, submitted -> {
            if (StringUtils.isEmpty(existingTaskParams)) {
                return encodeForDefinition ? encodeForCreate(submitted) : requireNoPlaceholderAndReturn(submitted);
            }
            List<Property> existing = getLocalParams(existingTaskParams);
            return encodeForDefinition ? mergeAndEncode(submitted, existing) : merge(submitted, existing);
        });
    }

    private List<Property> requireNoPlaceholderAndReturn(List<Property> submitted) {
        requireNoPlaceholder(submitted);
        return submitted;
    }

    /**
     * Encode sensitive values that are new plaintext for definition persist.
     * Keep-original from an already-sensitive existing value is written as-is (no re-encode).
     * {@code false→true} keep-original merges non-sensitive plaintext and then encodes.
     */
    private List<Property> encodeNewSensitivePlaintext(List<Property> mergedProperties,
                                                       List<Property> existingProperties) {
        if (CollectionUtils.isEmpty(mergedProperties)) {
            return mergedProperties;
        }
        Map<String, Property> existingMap = CollectionUtils.emptyIfNull(existingProperties).stream()
                .filter(Objects::nonNull)
                .filter(property -> property.getProp() != null)
                .collect(Collectors.toMap(Property::getProp, Function.identity(), (left, right) -> right));
        List<Property> encoded = new ArrayList<>(mergedProperties.size());
        for (Property property : mergedProperties) {
            Property copy = PropertySensitiveUtils.copy(property);
            if (!PropertySensitiveUtils.isSensitive(copy)) {
                Property existing = existingMap.get(copy.getProp());
                // true→false: if a non-UI client re-sent ciphertext, decode back to plaintext.
                if (existing != null && existing.isSensitive()
                        && StringUtils.isNotEmpty(copy.getValue())
                        && Objects.equals(copy.getValue(), existing.getValue())) {
                    copy.setValue(PasswordUtils.decodePassword(copy.getValue()));
                }
                encoded.add(copy);
                continue;
            }
            if (StringUtils.isEmpty(copy.getValue())) {
                encoded.add(copy);
                continue;
            }
            Property existing = existingMap.get(copy.getProp());
            if (existing != null && existing.isSensitive()
                    && Objects.equals(copy.getValue(), existing.getValue())) {
                // true→true keep-original (or identical resubmit): already persisted form.
                encoded.add(copy);
                continue;
            }
            copy.setValue(PasswordUtils.encodePassword(copy.getValue()));
            encoded.add(copy);
        }
        return encoded;
    }

    /**
     * Copy then mask for HTTP responses. Do not mask Service return values in place.
     * Nested {@link DagData} is remasked from the original (BeanUtils is shallow).
     */
    public Map<String, Map<String, Object>> mask(Map<String, Map<String, Object>> source) {
        if (source == null) {
            return null;
        }
        return maskLocalParamsMap(source);
    }

    public DagData mask(DagData source) {
        if (source == null) {
            return null;
        }
        DagData copy = new DagData();
        BeanUtils.copyProperties(source, copy);
        copy.setWorkflowDefinition(mask(source.getWorkflowDefinition()));
        if (source.getTaskDefinitionList() != null) {
            copy.setTaskDefinitionList(source.getTaskDefinitionList().stream()
                    .map(SensitivePropertyUtils::mask)
                    .collect(Collectors.toList()));
        }
        return copy;
    }

    public WorkflowInstance mask(WorkflowInstance source) {
        if (source == null) {
            return null;
        }
        WorkflowInstance copy = new WorkflowInstance();
        BeanUtils.copyProperties(source, copy);
        copy.setGlobalParams(maskGlobalParams(copy.getGlobalParams()));
        copy.setVarPool(maskVarPool(copy.getVarPool()));
        copy.setCommandParam(maskCommandParam(copy.getCommandParam()));
        copy.setDagData(mask(source.getDagData()));
        return copy;
    }

    public Command mask(Command source) {
        if (source == null) {
            return null;
        }
        Command copy = new Command();
        BeanUtils.copyProperties(source, copy);
        copy.setCommandParam(maskCommandParam(copy.getCommandParam()));
        return copy;
    }

    public ErrorCommand mask(ErrorCommand source) {
        if (source == null) {
            return null;
        }
        ErrorCommand copy = new ErrorCommand();
        BeanUtils.copyProperties(source, copy);
        copy.setCommandParam(maskCommandParam(copy.getCommandParam()));
        return copy;
    }

    public WorkflowDefinition mask(WorkflowDefinition source) {
        if (source == null) {
            return null;
        }
        WorkflowDefinition copy;
        if (source instanceof WorkflowDefinitionLog) {
            copy = new WorkflowDefinitionLog();
        } else {
            copy = new WorkflowDefinition();
        }
        BeanUtils.copyProperties(source, copy);
        applyWorkflowDefinitionMask(copy);
        return copy;
    }

    public TaskDefinition mask(TaskDefinition source) {
        if (source == null) {
            return null;
        }
        TaskDefinition copy;
        if (source instanceof TaskDefinitionVO) {
            copy = new TaskDefinitionVO();
        } else if (source instanceof TaskDefinitionLog) {
            copy = new TaskDefinitionLog();
        } else {
            copy = new TaskDefinition();
        }
        BeanUtils.copyProperties(source, copy);
        applyTaskDefinitionMask(copy);
        return copy;
    }

    public TaskInstance mask(TaskInstance source) {
        if (source == null) {
            return null;
        }
        TaskInstance copy;
        if (source instanceof TaskInstanceDependentDetails) {
            copy = new TaskInstanceDependentDetails<>();
        } else {
            copy = new TaskInstance();
        }
        BeanUtils.copyProperties(source, copy);
        applyTaskInstanceMask(copy);
        return copy;
    }

    private void applyWorkflowDefinitionMask(WorkflowDefinition copy) {
        copy.setGlobalParams(maskGlobalParams(copy.getGlobalParams()));
        copy.setGlobalParamMap(null);
    }

    private void applyTaskDefinitionMask(TaskDefinition copy) {
        copy.setTaskParams(rewriteLocalParams(copy.getTaskParams(),
                PropertySensitiveUtils::maskSensitiveValues));
        copy.setTaskParamMap(null);
    }

    private void applyTaskInstanceMask(TaskInstance copy) {
        copy.setTaskParams(rewriteLocalParams(copy.getTaskParams(),
                PropertySensitiveUtils::maskSensitiveValues));
        copy.setVarPool(maskVarPool(copy.getVarPool()));
    }

    private String maskGlobalParams(String globalParams) {
        List<Property> properties = GlobalParameterUtils.deserializeGlobalParameter(globalParams);
        if (CollectionUtils.isEmpty(properties)) {
            return globalParams;
        }
        return GlobalParameterUtils.serializeGlobalParameter(PropertySensitiveUtils.maskSensitiveValues(properties));
    }

    private String maskVarPool(String varPool) {
        if (StringUtils.isEmpty(varPool)) {
            return varPool;
        }
        List<Property> properties = VarPoolUtils.deserializeVarPool(varPool);
        if (CollectionUtils.isEmpty(properties)) {
            return varPool;
        }
        return VarPoolUtils.serializeVarPool(PropertySensitiveUtils.maskSensitiveValues(properties));
    }

    /**
     * Mask {@link ICommandParam#getCommandParams()} in the response copy.
     * Start restores {@code ******} to plaintext before Master persists {@code commandParam};
     * query must hide those values again.
     */
    private String maskCommandParam(String commandParam) {
        if (StringUtils.isEmpty(commandParam)) {
            return commandParam;
        }
        ICommandParam parsed = JSONUtils.parseObject(commandParam, ICommandParam.class);
        if (!(parsed instanceof AbstractCommandParam)) {
            return commandParam;
        }
        AbstractCommandParam abstractCommandParam = (AbstractCommandParam) parsed;
        if (CollectionUtils.isEmpty(abstractCommandParam.getCommandParams())) {
            return commandParam;
        }
        abstractCommandParam.setCommandParams(
                PropertySensitiveUtils.maskSensitiveValues(abstractCommandParam.getCommandParams()));
        return JSONUtils.toJsonString(abstractCommandParam);
    }

    private Map<String, Map<String, Object>> maskLocalParamsMap(Map<String, Map<String, Object>> localParams) {
        Map<String, Map<String, Object>> masked = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : localParams.entrySet()) {
            Map<String, Object> inner = entry.getValue();
            if (inner == null) {
                masked.put(entry.getKey(), null);
                continue;
            }
            Map<String, Object> copied = new LinkedHashMap<>(inner);
            Object localParamsList = copied.get(LOCAL_PARAMS_LIST);
            if (localParamsList instanceof List<?>) {
                List<Property> properties = new ArrayList<>();
                for (Object item : (List<?>) localParamsList) {
                    if (item instanceof Property) {
                        properties.add((Property) item);
                    }
                }
                copied.put(LOCAL_PARAMS_LIST, PropertySensitiveUtils.maskSensitiveValues(properties));
            }
            masked.put(entry.getKey(), copied);
        }
        return masked;
    }

    private List<Property> getLocalParams(String taskParams) {
        if (StringUtils.isEmpty(taskParams)) {
            return Collections.emptyList();
        }
        String localParams = JSONUtils.getNodeString(taskParams, LOCAL_PARAMS);
        if (StringUtils.isEmpty(localParams)) {
            return Collections.emptyList();
        }
        return JSONUtils.toList(localParams, Property.class);
    }

    private String rewriteLocalParams(String taskParams, Function<List<Property>, List<Property>> transform) {
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
        taskParamsNode.set(LOCAL_PARAMS, JSONUtils.toJsonNode(transform.apply(localParams)));
        return JSONUtils.toJsonString(taskParamsNode);
    }
}
