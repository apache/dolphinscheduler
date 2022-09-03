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
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
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

    @Override
    public void handle() throws TaskException {
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

    /**
     * cancel application
     *
     * @throws Exception exception
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
    public Set<String> getApplicationIds() throws TaskException {
        Set<String> appIds = new HashSet<>();

        File file = new File(taskRequest.getLogPath());
        if (!file.exists()) {
            return appIds;
        }

        /*
         * analysis log? get submitted yarn application id
         */
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(taskRequest.getLogPath()), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String appId = findAppId(line);
                if (StringUtils.isNotEmpty(appId)) {
                    appIds.add(appId);
                }
            }
        } catch (FileNotFoundException e) {
            throw new TaskException("get application id error, file not found, path:" + taskRequest.getLogPath());
        } catch (IOException e) {
            throw new TaskException("get application id error, path:" + taskRequest.getLogPath(), e);
        }
        return appIds;
    }

    /**
     * find app id
     *
     * @param line line
     * @return appid
     */
    protected String findAppId(String line) {
        Matcher matcher = YARN_APPLICATION_REGEX.matcher(line);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
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
