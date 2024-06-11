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

package org.apache.dolphinscheduler.plugin.task.trigger;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@Slf4j
public class TriggerTask extends AbstractRemoteTask {

    private final TaskExecutionContext taskExecutionContext;
    private TriggerParameters parameters;

    public TriggerTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public void init() {
        parameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), TriggerParameters.class);
        log.info("Initialize Datafactory task params {}", JSONUtils.toPrettyJsonString(parameters));
    }

    @Override
    public void submitApplication() throws TaskException {
        try {
            // start task
            exitStatusCode = startDatafactoryTask();
            setExitStatusCode(exitStatusCode);
        } catch (Exception e) {
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("data factory start task error", e);
        }
        // set runId to the appIds if start success
        setAppIds(parameters.getRunId());
    }

    @Override
    public void cancelApplication() throws TaskException {
        checkApplicationId();
        exitStatusCode = TaskConstants.EXIT_CODE_KILL;
    }

    @Override
    public void trackApplicationStatus() throws TaskException {
        checkApplicationId();
        Boolean isFinishedSuccessfully = false;
        if (!isFinishedSuccessfully) {
            exitStatusCode = TaskConstants.EXIT_CODE_FAILURE;
        } else {
            exitStatusCode = TaskConstants.EXIT_CODE_SUCCESS;
        }
    }

    /**
     * check datafactory applicationId or get it from appId
     */
    private void checkApplicationId() {
    }

    public int startDatafactoryTask() {
        return 0;
    }

    @Override
    public TriggerParameters getParameters() {
        return parameters;
    }

}
