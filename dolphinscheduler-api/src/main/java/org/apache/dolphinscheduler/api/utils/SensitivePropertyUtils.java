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
 * <p>
 * Crypto boundary (definition params only):
 * <ul>
 * <li>{@link #encodeForPersist} / {@link #encodeLocalParamsInTaskParams} — right before DB write</li>
 * <li>{@link #decodeAfterLoad} / {@link #decodeLocalParamsInTaskParams} — right after DB read</li>
 * </ul>
 * Business logic (merge / curing / permission) stays on plaintext. Instance {@code global_params}
 * stay plaintext (no encode). Query responses use {@link #mask}.
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
     * Update: restore DB value for keep-original {@code ******} without encoding.
     * {@code existingProperties} must already be plaintext ({@link #decodeAfterLoad}).
     * Empty / null is a real empty value. {@code false→true} + {@code ******} is allowed;
     * {@code true→false} + {@code ******} is rejected.
     * Instance {@code global_params} use this and stay plaintext.
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
     * Persist boundary: encode every non-empty {@code sensitive=true} value.
     * Call immediately before writing definition rows. Idempotent when encryption is off.
     */
    public List<Property> encodeForPersist(List<Property> properties) {
        return PropertySensitiveUtils.transformSensitiveValues(properties, PasswordUtils::encodePassword);
    }

    /**
     * Load boundary: decode every non-empty {@code sensitive=true} value.
     * Call immediately after reading definition rows into business logic.
     */
    public List<Property> decodeAfterLoad(List<Property> properties) {
        return PropertySensitiveUtils.transformSensitiveValues(properties, PasswordUtils::decodePassword);
    }

    /**
     * {@code true→false}: if a non-UI client re-sent the raw DB ciphertext, restore plaintext.
     * {@code existingRawFromDb} is still ciphertext (before {@link #decodeAfterLoad}).
     */
    public List<Property> normalizeResubmittedCiphertext(List<Property> mergedPlaintext,
                                                         List<Property> existingRawFromDb) {
        if (CollectionUtils.isEmpty(mergedPlaintext) || CollectionUtils.isEmpty(existingRawFromDb)) {
            return mergedPlaintext;
        }
        Map<String, Property> existingRawMap = existingRawFromDb.stream()
                .filter(Objects::nonNull)
                .filter(property -> property.getProp() != null)
                .collect(Collectors.toMap(Property::getProp, Function.identity(), (left, right) -> right));
        List<Property> normalized = new ArrayList<>(mergedPlaintext.size());
        for (Property property : mergedPlaintext) {
            Property copy = PropertySensitiveUtils.copy(property);
            if (!PropertySensitiveUtils.isSensitive(copy)) {
                Property existingRaw = existingRawMap.get(copy.getProp());
                if (existingRaw != null && existingRaw.isSensitive()
                        && StringUtils.isNotEmpty(copy.getValue())
                        && Objects.equals(copy.getValue(), existingRaw.getValue())) {
                    copy.setValue(PasswordUtils.decodePassword(copy.getValue()));
                }
            }
            normalized.add(copy);
        }
        return normalized;
    }

    /**
     * Build start/command params. {@code globalParams} must already be plaintext
     * ({@link #decodeAfterLoad} at the load boundary).
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
        Map<String, Property> globals = CollectionUtils.emptyIfNull(globalParams).stream()
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

    /** @deprecated use {@link #decodeAfterLoad} */
    public List<Property> decodeSensitiveValues(List<Property> properties) {
        return decodeAfterLoad(properties);
    }

    /**
     * Merge task {@code localParams} to plaintext for business logic.
     * {@code existingTaskParams} may still be DB ciphertext; existing side is decoded here.
     * Call {@link #encodeLocalParamsInTaskParams} immediately before definition persist.
     */
    public String mergeLocalParams(String submittedTaskParams, String existingTaskParams) {
        return rewriteLocalParams(submittedTaskParams, submitted -> {
            if (StringUtils.isEmpty(existingTaskParams)) {
                requireNoPlaceholder(submitted);
                return PropertySensitiveUtils.copy(submitted);
            }
            List<Property> existingRaw = getLocalParams(existingTaskParams);
            List<Property> existingPlain = decodeAfterLoad(existingRaw);
            List<Property> merged = merge(submitted, existingPlain);
            return normalizeResubmittedCiphertext(merged, existingRaw);
        });
    }

    /** Persist boundary for task definition {@code taskParams.localParams}. */
    public String encodeLocalParamsInTaskParams(String taskParams) {
        return rewriteLocalParams(taskParams, SensitivePropertyUtils::encodeForPersist);
    }

    /** Load boundary for task definition / task instance {@code taskParams.localParams}. */
    public String decodeLocalParamsInTaskParams(String taskParams) {
        return rewriteLocalParams(taskParams, SensitivePropertyUtils::decodeAfterLoad);
    }

    /** Persist boundary helper for workflow definition {@code globalParams} JSON. */
    public String encodeGlobalParams(String globalParams) {
        List<Property> properties = GlobalParameterUtils.deserializeGlobalParameter(globalParams);
        if (CollectionUtils.isEmpty(properties)) {
            return globalParams;
        }
        return GlobalParameterUtils.serializeGlobalParameter(encodeForPersist(properties));
    }

    /** Load boundary helper for workflow definition {@code globalParams} JSON. */
    public String decodeGlobalParams(String globalParams) {
        List<Property> properties = GlobalParameterUtils.deserializeGlobalParameter(globalParams);
        if (CollectionUtils.isEmpty(properties)) {
            return globalParams;
        }
        return GlobalParameterUtils.serializeGlobalParameter(decodeAfterLoad(properties));
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
