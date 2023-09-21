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

import static org.apache.dolphinscheduler.common.constants.Constants.APPID_COLLECT;
import static org.apache.dolphinscheduler.common.constants.Constants.DEFAULT_COLLECT_WAY;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.shell.IShellInterceptorBuilder;
import org.apache.dolphinscheduler.plugin.task.api.shell.ShellInterceptorBuilderFactory;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;

import java.util.List;
import java.util.Map;

public abstract class AbstractYarnTask extends AbstractRemoteTask {

    private ShellCommandExecutor shellCommandExecutor;

    public AbstractYarnTask(TaskExecutionContext taskRequest) {
        super(taskRequest);
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskRequest,
                log);
    }

    // todo split handle to submit and track
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            IShellInterceptorBuilder shellActuatorBuilder = ShellInterceptorBuilderFactory.newBuilder()
                    .properties(getProperties())
                    // todo: do we need to move the replace to subclass?
                    .appendScript(getScript().replaceAll("\\r\\n", System.lineSeparator()));
            // SHELL task exit code
            TaskResponse response = shellCommandExecutor.run(shellActuatorBuilder, taskCallBack);
            setExitStatusCode(response.getExitStatusCode());
            // set appIds
            setAppIds(String.join(TaskConstants.COMMA, getApplicationIds()));
            setProcessId(response.getProcessId());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.info("The current yarn task has been interrupted", ex);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("The current yarn task has been interrupted", ex);
        } catch (Exception e) {
            log.error("yarn process failure", e);
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
        return LogUtils.getAppIds(taskRequest.getLogPath(), taskRequest.getAppInfoPath(),
                PropertyUtils.getString(APPID_COLLECT, DEFAULT_COLLECT_WAY));
    }

    /**
     * Get the script used to bootstrap the task
     */
    protected abstract String getScript();

    /**
     * Get the properties of the task used to replace the placeholders in the script.
     */
    protected abstract Map<String, String> getProperties();
}
