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

package org.apache.dolphinscheduler.plugin.task.dms;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;
import static com.fasterxml.jackson.databind.MapperFeature.REQUIRE_SETTERS_FOR_GETTERS;

import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.beanutils.BeanUtils;

import java.util.Collections;
import java.util.List;

import com.amazonaws.services.databasemigrationservice.model.InvalidResourceStateException;
import com.amazonaws.services.databasemigrationservice.model.ReplicationTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

public class DmsTask extends AbstractRemoteTask {

    private static final ObjectMapper objectMapper =
        new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
            .configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            .configure(REQUIRE_SETTERS_FOR_GETTERS, true)
            .setPropertyNamingStrategy(new PropertyNamingStrategy.UpperCamelCaseStrategy());
    /**
     * taskExecutionContext
     */
    private final TaskExecutionContext taskExecutionContext;
    public DmsHook dmsHook;
    /**
     * Dms parameters
     */
    private DmsParameters parameters;
    private DmsHook.ApplicationIds appId;

    public DmsTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;

    }

    @Override
    public void init() throws TaskException {
        logger.info("Dms task params {}", taskExecutionContext.getTaskParams());
        parameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), DmsParameters.class);
        initDmsHook();
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public void submitApplication() throws TaskException {
        exitStatusCode = checkCreateReplicationTask();
        if (exitStatusCode == TaskConstants.EXIT_CODE_SUCCESS) {
            exitStatusCode = startReplicationTask();
        } else {
            throw new TaskException("Failed to create a ReplicationTask");
        }

        // if the task is not running, the task will be deleted
        if (exitStatusCode == TaskConstants.EXIT_CODE_FAILURE && !parameters.getIsRestartTask()) {
            dmsHook.deleteReplicationTask();
        } else {
            appId = dmsHook.getApplicationIds();
            setAppIds(JSONUtils.toJsonString(appId));
        }
    }

    @Override
    public void trackApplicationStatus() {
        initAppId();
        dmsHook.setReplicationTaskArn(appId.getReplicationTaskArn());
        // if CdcStopPosition is not set, the task will not continue to check the running status
        if (isStopTaskWhenCdc()) {
            logger.info("This is a cdc task and cdcStopPosition is not set, the task will not continue to check the running status");
            exitStatusCode = TaskConstants.EXIT_CODE_SUCCESS;
            return;
        }

        Boolean isFinishedSuccessfully = dmsHook.checkFinishedReplicationTask();
        if (isFinishedSuccessfully) {
            exitStatusCode = TaskConstants.EXIT_CODE_SUCCESS;
        } else {
            throw new TaskException("DMS task failed to track");
        }
    }

    /**
     * init DMS remote AppId if null
     */
    private void initAppId() {
        if (appId == null) {
            if (StringUtils.isNotEmpty(getAppIds())) {
                appId = JSONUtils.parseObject(getAppIds(), DmsHook.ApplicationIds.class);
            }
        }
        if (appId == null) {
            throw new TaskException("DMS applicationID is null");
        }
    }

    public int checkCreateReplicationTask() throws TaskException {

        // if IsRestartTask, return success, do not create replication task
        if (parameters.getIsRestartTask()) {
            return TaskConstants.EXIT_CODE_SUCCESS;
        }

        // if not IsRestartTask, create replication task
        Boolean isCreateSuccessfully;
        try {
            isCreateSuccessfully = dmsHook.createReplicationTask();
        } catch (Exception e) {
            throw new TaskException("DMS task create replication task error", e);
        }

        // if create replication task successfully, return EXIT_CODE_SUCCESS, else return EXIT_CODE_FAILURE
        if (isCreateSuccessfully) {
            return TaskConstants.EXIT_CODE_SUCCESS;
        } else {
            return TaskConstants.EXIT_CODE_FAILURE;
        }
    }

    /**
     * start replication task
     *
     * @return
     * @throws TaskException
     */
    public int startReplicationTask() {

        Boolean isStartSuccessfully = false;
        try {
            isStartSuccessfully = dmsHook.startReplicationTask();
        } catch (InvalidResourceStateException e) {
            logger.error("Failed to start a task, error message: {}", e.getErrorMessage());

            // Only restart task when the error contains "Test connection", means instance can not connect to source or target
            if (!e.getErrorMessage().contains("Test connection")) {
                return TaskConstants.EXIT_CODE_FAILURE;
            }

            logger.info("restart replication task");
            // if only restart task, run dmsHook.describeReplicationTasks to get replication task arn
            if (parameters.getIsRestartTask()) {
                dmsHook.describeReplicationTasks();
            }

            // test connection endpoint again and restart task if connection is ok
            if (dmsHook.testConnectionEndpoint()) {
                isStartSuccessfully = dmsHook.startReplicationTask();
            }
        }

        // if start replication task failed, return EXIT_CODE_FAILURE
        if (!isStartSuccessfully) {
            return TaskConstants.EXIT_CODE_FAILURE;
        }

        return TaskConstants.EXIT_CODE_SUCCESS;
    }

    /**
     * check if stop task when cdc
     *
     * @return true if stop task when cdc type and cdcStopPosition is not set, else return false
     */
    public Boolean isStopTaskWhenCdc() {
        ReplicationTask replicationTask = dmsHook.describeReplicationTasks();
        String migrationType = replicationTask.getMigrationType();
        return migrationType.contains("cdc") && parameters.getCdcStopPosition() == null;
    }

    /**
     * init dms hook
     */
    public void initDmsHook() throws TaskException {
        convertJsonParameters();

        dmsHook = new DmsHook();
        try {
            BeanUtils.copyProperties(dmsHook, parameters);
        } catch (Exception e) {
            throw new TaskException("DMS task init error", e);
        }

        if (!StringUtils.isNotEmpty(parameters.getStartReplicationTaskType())) {
            if (parameters.getIsRestartTask()) {
                dmsHook.setStartReplicationTaskType(DmsHook.START_TYPE.RELOAD_TARGET);
            } else {
                dmsHook.setStartReplicationTaskType(DmsHook.START_TYPE.START_REPLICATION);
            }
        }
    }

    /**
     * convert json parameters to dms parameters
     */
    public void convertJsonParameters() throws TaskException {
        // create a new parameter object using the json data if the json data is not empty
        if (parameters.getIsJsonFormat() && parameters.getJsonData() != null) {
            // combining local and global parameters
            String jsonData = ParameterUtils.convertParameterPlaceholders(parameters.getJsonData(), ParamUtils.convert(taskExecutionContext.getPrepareParamsMap()));

            boolean isRestartTask = parameters.getIsRestartTask();
            try {
                parameters = objectMapper.readValue(jsonData, DmsParameters.class);
                parameters.setIsRestartTask(isRestartTask);
            } catch (Exception e) {
                logger.error("Failed to convert json data to DmsParameters object.", e);
                throw new TaskException(e.getMessage());
            }
        }
    }

    @Override
    public DmsParameters getParameters() {
        return parameters;
    }

    @Override
    public void cancelApplication() {
        dmsHook.stopReplicationTask();
    }

}
