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

/**
 * abstract yarn task
 */
public abstract class AbstractYarnTask extends AbstractTaskExecutor {
    /**
     * process task
     */
    private ShellCommandExecutor shellCommandExecutor;

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

    @Override
    public void handle() throws Exception {
        try {
            // SHELL task exit code
            TaskResponse response = shellCommandExecutor.run(buildCommand());
            setExitStatusCode(response.getExitStatusCode());
            setAppIds(response.getAppIds());
            setProcessId(response.getProcessId());
        } catch (Exception e) {
            logger.error("yarn process failure", e);
            exitStatusCode = -1;
            throw e;
        }
    }

    /**
     * cancel application
     *
     * @param status status
     * @throws Exception exception
     */
    @Override
    public void cancelApplication(boolean status) throws Exception {
        cancel = true;
        // cancel process
        shellCommandExecutor.cancelApplication();
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

        return mainJar.getId() == 0
            ? mainJar.getRes()
            // when update resource maybe has error
            : mainJar.getResourceName().replaceFirst("/", "");
    }
}
