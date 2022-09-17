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

package org.apache.dolphinscheduler.plugin.task.dvc;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;

import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * shell task
 */
public class DvcTask extends AbstractTask {

    /**
     * dvc parameters
     */
    private DvcParameters parameters;

    /**
     * shell command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public DvcTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle, taskExecutionContext, logger);
    }

    @Override
    public void init() {
        logger.info("dvc task params {}", taskExecutionContext.getTaskParams());

        parameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), DvcParameters.class);

        if (!parameters.checkParameters()) {
            throw new RuntimeException("dvc task params is not valid");
        }
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            // construct process
            String command = buildCommand();
            TaskResponse commandExecuteResult = shellCommandExecutor.run(command);
            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            setProcessId(commandExecuteResult.getProcessId());
            parameters.dealOutParam(shellCommandExecutor.getVarPool());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("The current DvcTask has been interrupted", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("The current DvcTask has been interrupted", e);
        } catch (Exception e) {
            logger.error("dvc task error", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("Execute dvc task failed", e);
        }
    }

    @Override
    public void cancel() throws TaskException {
        // cancel process
        try {
            shellCommandExecutor.cancelApplication();
        } catch (Exception e) {
            throw new TaskException("cancel application error", e);
        }
    }

    public String buildCommand() {
        String command = "";
        String taskType = parameters.getDvcTaskType();
        if (taskType.equals(DvcConstants.DVC_TASK_TYPE.UPLOAD)) {
            command = buildUploadCommond();
        } else if (taskType.equals(DvcConstants.DVC_TASK_TYPE.DOWNLOAD)) {
            command = buildDownCommond();
        } else if (taskType.equals(DvcConstants.DVC_TASK_TYPE.INIT)) {
            command = buildInitDvcCommond();
        }
        logger.info("Run DVC task with command: \n{}", command);
        return command;
    }

    private String buildUploadCommond() {
        List<String> args = new ArrayList<>();
        args.add(String.format(DvcConstants.CHECK_AND_SET_DVC_REPO, parameters.getDvcRepository()));
        args.add(String.format(DvcConstants.SET_DATA_PATH, parameters.getDvcLoadSaveDataPath()));
        args.add(String.format(DvcConstants.SET_DATA_LOCATION, parameters.getDvcDataLocation()));
        args.add(String.format(DvcConstants.SET_VERSION, parameters.getDvcVersion()));
        args.add(String.format(DvcConstants.SET_MESSAGE, parameters.getDvcMessage()));
        args.add(DvcConstants.GIT_CLONE_DVC_REPO);
        args.add(DvcConstants.DVC_AUTOSTAGE);
        args.add(DvcConstants.DVC_ADD_DATA);
        args.add(DvcConstants.GIT_UPDATE_FOR_UPDATE_DATA);

        String command = String.join("\n", args);
        return command;

    }

    private String buildDownCommond() {
        List<String> args = new ArrayList<>();
        args.add(String.format(DvcConstants.CHECK_AND_SET_DVC_REPO, parameters.getDvcRepository()));
        args.add(String.format(DvcConstants.SET_DATA_PATH, parameters.getDvcLoadSaveDataPath()));
        args.add(String.format(DvcConstants.SET_DATA_LOCATION, parameters.getDvcDataLocation()));
        args.add(String.format(DvcConstants.SET_VERSION, parameters.getDvcVersion()));
        args.add(DvcConstants.DVC_DOWNLOAD);

        String command = String.join("\n", args);
        return command;

    }

    private String buildInitDvcCommond() {
        List<String> args = new ArrayList<>();
        args.add(String.format(DvcConstants.CHECK_AND_SET_DVC_REPO, parameters.getDvcRepository()));
        args.add(DvcConstants.GIT_CLONE_DVC_REPO);
        args.add(DvcConstants.DVC_INIT);
        args.add(String.format(DvcConstants.DVC_ADD_REMOTE, parameters.getDvcStoreUrl()));
        args.add(DvcConstants.GIT_UPDATE_FOR_INIT_DVC);

        String command = String.join("\n", args);
        return command;

    }

    @Override
    public AbstractParameters getParameters() {
        return parameters;
    }

}
