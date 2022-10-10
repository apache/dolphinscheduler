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

package org.apache.dolphinscheduler.plugin.task.api;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * abstract yarn task
 */
public abstract class AbstractYarnTask extends AbstractRemoteTask {

    /**
     * process task
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * rules for extracting application ID
     */
    protected static final Pattern YARN_APPLICATION_REGEX = Pattern.compile(TaskConstants.YARN_APPLICATION_REGEX);

    /**
     * Abstract Yarn Task
     *
     * @param taskRequest taskRequest
     */
    public AbstractYarnTask(TaskExecutionContext taskRequest) {
        super(taskRequest);
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskRequest,
                logger);
    }

    // todo split handle to submit and track
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            // SHELL task exit code
            TaskResponse response = shellCommandExecutor.run(buildCommand());
            setExitStatusCode(response.getExitStatusCode());
            // set appIds
            setAppIds(String.join(TaskConstants.COMMA, getApplicationIds()));
            setProcessId(response.getProcessId());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.info("The current yarn task has been interrupted", ex);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("The current yarn task has been interrupted", ex);
        } catch (Exception e) {
            logger.error("yarn process failure", e);
            exitStatusCode = -1;
            throw new TaskException("Execute task failed", e);
        }
    }

    // todo
    @Override
    public void submitApplication() throws TaskException {

    }

    // todo
    @Override
    public void trackApplicationStatus() throws TaskException {

    }

    /**
     * cancel application
     *
     * @throws TaskException exception
     */
    @Override
    public void cancelApplication() throws TaskException {
        // cancel process
        try {
            shellCommandExecutor.cancelApplication();
        } catch (Exception e) {
            throw new TaskException("cancel application error", e);
        }
    }

    /**
     * get application ids
     * @return
     * @throws TaskException
     */
    @Override
    public List<String> getApplicationIds() throws TaskException {
        return LogUtils.getAppIdsFromLogFile(taskRequest.getLogPath(), logger);
    }

    /**
     * create command
     *
     * @return String
     */
    protected abstract String buildCommand();

    /**
     * set main jar name
     */
    protected abstract void setMainJarName();

    /**
     * Get name of jar resource.
     *
     * @param mainJar
     * @return
     */
    protected String getResourceNameOfMainJar(ResourceInfo mainJar) {
        if (null == mainJar) {
            throw new RuntimeException("The jar for the task is required.");
        }

        return mainJar.getId() == null
                ? mainJar.getRes()
                // when update resource maybe has error
                : mainJar.getResourceName().replaceFirst("/", "");
    }
}
