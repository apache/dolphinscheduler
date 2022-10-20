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

package org.apache.dolphinscheduler.plugin.task.datasync;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;
import static com.fasterxml.jackson.databind.MapperFeature.REQUIRE_SETTERS_FOR_GETTERS;

import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import software.amazon.awssdk.services.datasync.model.TaskExecutionStatus;

import java.util.Collections;
import java.util.List;

import lombok.Setter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Setter
public class DatasyncTask extends AbstractRemoteTask {

    private static final ObjectMapper objectMapper =
            JsonMapper.builder().configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                    .configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                    .configure(REQUIRE_SETTERS_FOR_GETTERS, true)
                    .propertyNamingStrategy(new PropertyNamingStrategies.UpperCamelCaseStrategy()).build();

    private final TaskExecutionContext taskExecutionContext;
    private DatasyncParameters parameters;
    private DatasyncHook hook;

    public DatasyncTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public void init() {
        logger.info("Datasync task params {}", taskExecutionContext.getTaskParams());

        parameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), DatasyncParameters.class);
        initParams();

        hook = new DatasyncHook();
    }

    /**
     * init datasync hook
     */
    public void initParams() throws TaskException {
        if (parameters.isJsonFormat() && StringUtils.isNotEmpty(parameters.getJson())) {
            try {
                parameters = objectMapper.readValue(parameters.getJson(), DatasyncParameters.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            // parameters = JSONUtils.parseObject(parameters.getJson(), DatasyncParameters.class);
            logger.info("Datasync convert task params {}", parameters);
        }
    }

    @Override
    public void submitApplication() throws TaskException {
        try {
            int exitStatusCode = checkCreateTask();
            if (exitStatusCode == TaskConstants.EXIT_CODE_FAILURE) {
                // if create task failure go end
                setExitStatusCode(exitStatusCode);
                return;
            }
            // start task
            exitStatusCode = startDatasyncTask();
            setExitStatusCode(exitStatusCode);
        } catch (Exception e) {
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("datasync task error", e);
        }
        // set taskExecArn to the appIds if start success
        setAppIds(hook.getTaskExecArn());
    }

    @Override
    public void cancelApplication() throws TaskException {
        checkApplicationId();
        hook.cancelDatasyncTask();
    }

    @Override
    public void trackApplicationStatus() throws TaskException {
        checkApplicationId();
        Boolean isFinishedSuccessfully = null;
        isFinishedSuccessfully = hook.doubleCheckFinishStatus(TaskExecutionStatus.SUCCESS, DatasyncHook.doneStatus);
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
        String taskExecArn = hook.getTaskExecArn();
        if (StringUtils.isEmpty(taskExecArn)) {
            if (StringUtils.isEmpty(getAppIds())) {
                throw new TaskException("datasync taskExecArn is null, not created yet");
            }
            hook.setTaskExecArn(getAppIds());
        }
    }

    public int checkCreateTask() {

        Boolean isCreateSuccessfully = hook.createDatasyncTask(parameters);
        if (!isCreateSuccessfully) {
            return TaskConstants.EXIT_CODE_FAILURE;
        } else {
            return TaskConstants.EXIT_CODE_SUCCESS;
        }
    }

    public int startDatasyncTask() {
        Boolean isStartSuccessfully = hook.startDatasyncTask();
        if (!isStartSuccessfully) {
            return TaskConstants.EXIT_CODE_FAILURE;
        }
        return TaskConstants.EXIT_CODE_SUCCESS;
    }

    @Override
    public DatasyncParameters getParameters() {
        return parameters;
    }

}
