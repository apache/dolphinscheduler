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

package org.apache.dolphinscheduler.plugin.task.api.log;

import static org.apache.dolphinscheduler.common.constants.Constants.LOCAL_PARAMS;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertySensitiveUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builds a logging-only copy of {@link TaskExecutionContext} with sensitive property
 * values replaced by {@code ******} before JSON serialization. The live execution
 * context is left intact.
 * <p>
 * Never returns the live execution context: if deep-copy fails, a minimal stub without
 * parameter payloads is returned instead so plaintext secrets cannot leak into logs.
 */
@Slf4j
@UtilityClass
public class TaskExecutionContextLogRedactor {

    public TaskExecutionContext redactForLog(final TaskExecutionContext source) {
        return redactForLog(source, TaskExecutionContextLogRedactor::copyViaJson);
    }

    /**
     * Visible for tests so the copy-failure path can be exercised without mocking JSONUtils.
     */
    static TaskExecutionContext redactForLog(final TaskExecutionContext source,
                                             final Function<TaskExecutionContext, TaskExecutionContext> copier) {
        if (source == null) {
            return null;
        }
        TaskExecutionContext copy;
        try {
            copy = copier.apply(source);
        } catch (RuntimeException ex) {
            log.warn("Failed to copy TaskExecutionContext for log redaction, taskInstanceId={}: {}",
                    source.getTaskInstanceId(), ex.toString());
            return safeStubForLog(source);
        }
        if (copy == null) {
            log.warn("Failed to copy TaskExecutionContext for log redaction, taskInstanceId={}",
                    source.getTaskInstanceId());
            return safeStubForLog(source);
        }
        maskPrepareParamsMap(copy);
        maskGlobalParams(copy);
        maskVarPool(copy);
        maskTaskParamsLocalParams(copy);
        return copy;
    }

    static TaskExecutionContext copyViaJson(final TaskExecutionContext source) {
        String json = JSONUtils.toJsonString(source);
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        return JSONUtils.parseObject(json, TaskExecutionContext.class);
    }

    /**
     * Identifiers only — no prepareParams / globalParams / taskParams / varPool payloads.
     */
    static TaskExecutionContext safeStubForLog(final TaskExecutionContext source) {
        TaskExecutionContext stub = new TaskExecutionContext();
        stub.setTaskInstanceId(source.getTaskInstanceId());
        stub.setTaskName(source.getTaskName());
        stub.setTaskType(source.getTaskType());
        stub.setWorkflowInstanceId(source.getWorkflowInstanceId());
        return stub;
    }

    private void maskPrepareParamsMap(final TaskExecutionContext copy) {
        Map<String, Property> prepareParamsMap = copy.getPrepareParamsMap();
        if (prepareParamsMap == null || prepareParamsMap.isEmpty()) {
            return;
        }
        Map<String, Property> masked = new HashMap<>(prepareParamsMap.size());
        for (Map.Entry<String, Property> entry : prepareParamsMap.entrySet()) {
            masked.put(entry.getKey(), PropertySensitiveUtils.maskSensitiveValue(entry.getValue()));
        }
        copy.setPrepareParamsMap(masked);
    }

    private void maskGlobalParams(final TaskExecutionContext copy) {
        List<Property> globalParams = GlobalParameterUtils.deserializeGlobalParameter(copy.getGlobalParams());
        if (CollectionUtils.isEmpty(globalParams)) {
            return;
        }
        copy.setGlobalParams(
                GlobalParameterUtils
                        .serializeGlobalParameter(PropertySensitiveUtils.maskSensitiveValues(globalParams)));
    }

    private void maskVarPool(final TaskExecutionContext copy) {
        List<Property> varPool = copy.getVarPool();
        if (CollectionUtils.isEmpty(varPool)) {
            return;
        }
        copy.setVarPool(PropertySensitiveUtils.maskSensitiveValues(varPool));
    }

    private void maskTaskParamsLocalParams(final TaskExecutionContext copy) {
        String taskParams = copy.getTaskParams();
        if (StringUtils.isEmpty(taskParams)) {
            return;
        }
        ObjectNode taskParamsNode = JSONUtils.parseObject(taskParams);
        if (taskParamsNode == null) {
            return;
        }
        JsonNode localParamsNode = taskParamsNode.get(LOCAL_PARAMS);
        if (localParamsNode == null || localParamsNode.isNull()) {
            return;
        }
        List<Property> localParams = JSONUtils.toList(localParamsNode.toString(), Property.class);
        taskParamsNode.set(LOCAL_PARAMS, JSONUtils.toJsonNode(PropertySensitiveUtils.maskSensitiveValues(localParams)));
        copy.setTaskParams(JSONUtils.toJsonString(taskParamsNode));
    }
}
