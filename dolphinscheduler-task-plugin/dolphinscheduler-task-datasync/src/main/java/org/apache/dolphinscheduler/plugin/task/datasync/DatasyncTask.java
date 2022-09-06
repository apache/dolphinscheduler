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

import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

public class DatasyncTask extends AbstractTaskExecutor {

    private final TaskExecutionContext taskExecutionContext;
    private DatasyncParameters parameters;
    private DatasyncHook hook;

    public DatasyncTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;

    }

    @Override
    public void init() {
        logger.info("Datasync task params {}", taskExecutionContext.getTaskParams());

        parameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), DatasyncParameters.class);
        hook=new DatasyncHook();
    }

    @Override
    public void handle() throws TaskException {
        try {
            int exitStatusCode = runDmsReplicationTask();
            setExitStatusCode(exitStatusCode);
        } catch (Exception e) {
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("DMS task error", e);
        }
    }

    public int runDmsReplicationTask() {
        int exitStatusCode;
        exitStatusCode = checkCreateReplicationTask();
        if (exitStatusCode == TaskConstants.EXIT_CODE_SUCCESS) {
            exitStatusCode = startReplicationTask();
        }
        return exitStatusCode;
    }

    public int checkCreateReplicationTask() {

        Boolean isCreateSuccessfully = hook.createDatasyncTask(parameters);
        if (!isCreateSuccessfully) {
            return TaskConstants.EXIT_CODE_FAILURE;
        } else {
            return TaskConstants.EXIT_CODE_SUCCESS;
        }
    }

    public int startReplicationTask() {
        Boolean isStartSuccessfully = hook.startDatasyncTask();
        if (!isStartSuccessfully) {
            return TaskConstants.EXIT_CODE_FAILURE;
        }

        Boolean isFinishedSuccessfully = hook.awaitReplicationTaskStatus(DatasyncHook.STATUS.SUCCESSFUL,DatasyncHook.doneStatus);
        if (!isFinishedSuccessfully) {
            return TaskConstants.EXIT_CODE_FAILURE;
        } else {
            return TaskConstants.EXIT_CODE_SUCCESS;
        }
    }

    @Override
    public DatasyncParameters getParameters() {
        return parameters;
    }

    @Override
    public void cancelApplication(boolean cancelApplication) {
        hook.cancelDatasyncTask();
    }

}