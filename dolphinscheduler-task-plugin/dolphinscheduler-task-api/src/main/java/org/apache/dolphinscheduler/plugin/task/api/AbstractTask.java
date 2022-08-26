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

import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskAlertInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * executive task
 */
public abstract class AbstractTask {

    /**
     * rules for extracting application ID
     */
    protected static final Pattern YARN_APPLICATION_REGEX = Pattern.compile(TaskConstants.YARN_APPLICATION_REGEX);

    /**
     * varPool string
     */
    protected String varPool;

    /**
     * taskExecutionContext
     **/
    protected TaskExecutionContext taskRequest;

    /**
     * SHELL process pid
     */
    protected int processId;

    /**
     * SHELL result string
     */
    protected String resultString;

    /**
     * other resource manager appId , for example : YARN etc
     */
    protected String appIds;

    /**
     * cancel flag
     */
    protected volatile boolean cancel = false;

    /**
     * exit code
     */
    protected volatile int exitStatusCode = -1;

    protected boolean needAlert = false;

    protected TaskAlertInfo taskAlertInfo;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    protected AbstractTask(TaskExecutionContext taskExecutionContext) {
        this.taskRequest = taskExecutionContext;
    }

    /**
     * init task
     */
    public void init() {
    }

    public String getPreScript() {
        return null;
    }

    public abstract void handle() throws TaskException;


    /**
     * cancel application
     *
     * @param status status
     * @throws Exception exception
     */
    public void cancelApplication(boolean status) throws Exception {
        this.cancel = status;
    }

    /**
     * get application ids
     * @return
     * @throws IOException
     */
    public Set<String> getApplicationIds() throws IOException {
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

    public void setVarPool(String varPool) {
        this.varPool = varPool;
    }

    public String getVarPool() {
        return varPool;
    }

    /**
     * get exit status code
     *
     * @return exit status code
     */
    public int getExitStatusCode() {
        return exitStatusCode;
    }

    public void setExitStatusCode(int exitStatusCode) {
        this.exitStatusCode = exitStatusCode;
    }

    public String getAppIds() {
        return appIds;
    }

    public void setAppIds(String appIds) {
        this.appIds = appIds;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public String getResultString() {
        return resultString;
    }

    public void setResultString(String resultString) {
        this.resultString = resultString;
    }

    public boolean getNeedAlert() {
        return needAlert;
    }

    public void setNeedAlert(boolean needAlert) {
        this.needAlert = needAlert;
    }

    public TaskAlertInfo getTaskAlertInfo() {
        return taskAlertInfo;
    }

    public void setTaskAlertInfo(TaskAlertInfo taskAlertInfo) {
        this.taskAlertInfo = taskAlertInfo;
    }

    /**
     * get task parameters
     *
     * @return AbstractParameters
     */
    public abstract AbstractParameters getParameters();

    /**
     * get exit status according to exitCode
     *
     * @return exit status
     */
    public TaskExecutionStatus getExitStatus() {
        TaskExecutionStatus status;
        switch (getExitStatusCode()) {
            case TaskConstants.EXIT_CODE_SUCCESS:
                status = TaskExecutionStatus.SUCCESS;
                break;
            case TaskConstants.EXIT_CODE_KILL:
                status = TaskExecutionStatus.KILL;
                break;
            default:
                status = TaskExecutionStatus.FAILURE;
                break;
        }
        return status;
    }

}