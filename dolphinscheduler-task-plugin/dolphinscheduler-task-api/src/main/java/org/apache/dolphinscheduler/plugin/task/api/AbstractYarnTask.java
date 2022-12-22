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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
        // not retry submit task if appId exists
        if (StringUtils.isNotEmpty(taskRequest.getAppIds())) {
            logger.info("task {} has already been submitted before", taskRequest);
            setExitStatusCode(TaskConstants.EXIT_CODE_SUCCESS);
            setAppIds(taskRequest.getAppIds());
            setProcessId(taskRequest.getProcessId());
            taskRequest.getCompletedCollectAppId().complete(true);
            taskCallBack.sendRunningInfo(taskRequest);
            return;
        }

        try {

            // SHELL task exit code
            TaskResponse response = shellCommandExecutor.run(buildCommand(), exitAfterSubmitTask(), oneAppIdPerTask());
            setExitStatusCode(response.getExitStatusCode());
            // set appIds
            setAppIds(String.join(TaskConstants.COMMA, getApplicationIds()));
            setProcessId(response.getProcessId());
            setProcess(response.getProcess());
            // send process id to master to kill process when worker crashes before get appId
            // if process exit after submit, process id is 0, not usable
            if (getProcessId() > 0) {
                taskRequest.setProcessId(getProcessId());
                taskCallBack.sendRunningInfo(taskRequest);
            }
            // wait appId, report status with appId in time
            Set<String> appIds = getApplicationIds();
            if (appIds.size() > 0) {
                setAppIds(String.join(TaskConstants.COMMA, appIds));
                taskCallBack.sendRunningInfo(taskRequest);
            }

            if (Objects.nonNull(response.getProcess()) && exitAfterSubmitTask()) {
                // monitor by app id
                long startTime = System.currentTimeMillis();
                long remainTime = taskRequest.getRemainTime();

                boolean status = false;
                try {
                    status = response.getProcess().waitFor(remainTime, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    logger.info("process {} interrupted", getProcessId());
                }
                if (status) {

                    // SHELL task state
                    setExitStatusCode(response.getProcess().exitValue());

                } else {
                    logger.error("process has failure, the task timeout configuration value is:{}, ready to kill ...",
                            taskRequest.getTaskTimeout());
                    ProcessUtils.kill(taskRequest);
                    setExitStatusCode(EXIT_CODE_FAILURE);
                }
                logger.info(
                        "waiting process exit, execute path:{}, processId:{} ,exitStatusCode:{}, processWaitForStatus:{}, take {}",
                        taskRequest.getExecutePath(), taskRequest.getProcessId(), getExitStatusCode(), status,
                        System.currentTimeMillis() - startTime);

            }

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
    public Set<String> getApplicationIds() throws TaskException {
        Set<String> appIds = new HashSet<>();
        long startTime = System.currentTimeMillis();
        logger.info("task {} waiting collect appId ", taskRequest.getTaskInstanceId());
        try {
            taskRequest.getCompletedCollectAppId().get(5, TimeUnit.MINUTES);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("collect app id error.", e);
        }
        logger.info("task {} complete collect appId, take {} ", taskRequest.getTaskInstanceId(),
                System.currentTimeMillis() - startTime);

        if (StringUtils.isNotEmpty(taskRequest.getAppIds())) {
            appIds.addAll(Arrays.asList(taskRequest.getAppIds().split(Constants.COMMA)));
        }

        return appIds;
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
