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

package org.apache.dolphinscheduler.plugin.task.datafactory;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;
import static com.fasterxml.jackson.databind.MapperFeature.REQUIRE_SETTERS_FOR_GETTERS;

import lombok.Getter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

import lombok.Setter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Setter
@Getter
public class DatafactoryTask extends AbstractRemoteTask {

    private static final ObjectMapper objectMapper =
            JsonMapper.builder().configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                    .configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                    .configure(REQUIRE_SETTERS_FOR_GETTERS, true)
                    .propertyNamingStrategy(new PropertyNamingStrategies.UpperCamelCaseStrategy()).build();

    private final TaskExecutionContext taskExecutionContext;
    private DatafactoryParameters parameters;
    private DatafactoryHook hook;


    public DatafactoryTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public void init() {
        parameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), DatafactoryParameters.class);
        logger.info("Initialize Datafactory task params {}", JSONUtils.toPrettyJsonString(parameters));
        hook = new DatafactoryHook();
    }

    @Override
    public void submitApplication() throws TaskException {
        try {
            // start task
            exitStatusCode = startDatasyncTask();
            setExitStatusCode(exitStatusCode);
        } catch (Exception e) {
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("data factory start task error", e);
        }
        // set taskExecArn to the appIds if start success
        setAppIds(parameters.getRunId());
    }

    @Override
    public void cancelApplication() throws TaskException {
        checkApplicationId();
        hook.cancelDatasyncTask(parameters);
    }

    @Override
    public void trackApplicationStatus() throws TaskException {
        checkApplicationId();
        Boolean isFinishedSuccessfully;
        isFinishedSuccessfully = hook.queryStatus(parameters);
        if (!isFinishedSuccessfully) {
            exitStatusCode = TaskConstants.EXIT_CODE_FAILURE;
        } else {
            exitStatusCode = TaskConstants.EXIT_CODE_SUCCESS;
        }
    }

    /**
     * check datasync applicationId or get it from appId
     */
    private void checkApplicationId() {
        String taskExecArn = hook.getRunId();
        if (StringUtils.isEmpty(taskExecArn)) {
            if (StringUtils.isEmpty(getAppIds())) {
                throw new TaskException("datafactory runId is null, not created yet");
            }
            hook.setRunId(getAppIds());
        }
    }

    public int startDatasyncTask() {
        Boolean isStartSuccessfully = hook.startDatasyncTask(parameters);
        if (!isStartSuccessfully) {
            return TaskConstants.EXIT_CODE_FAILURE;
        }
        return TaskConstants.EXIT_CODE_SUCCESS;
    }

    @Override
    public DatafactoryParameters getParameters() {
        return parameters;
    }

}
