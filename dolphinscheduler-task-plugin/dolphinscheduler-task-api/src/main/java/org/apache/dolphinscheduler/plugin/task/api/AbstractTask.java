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
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskAlertInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * executive task
 */
public abstract class AbstractTask {

    public static final Marker FINALIZE_SESSION_MARKER = MarkerFactory.getMarker("FINALIZE_SESSION");

    protected final Logger logger =
            LoggerFactory.getLogger(String.format(TaskConstants.TASK_LOG_LOGGER_NAME_FORMAT, getClass()));

    public String rgex = "['\"]*\\$\\{(.*?)\\}['\"]*";

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

    public abstract void handle(TaskCallBack taskCallBack) throws TaskException;

    public abstract void cancel() throws TaskException;

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

    public String getAppIds() {
        return appIds;
    }

    public void setAppIds(String appIds) {
        this.appIds = appIds;
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

    /**
     * log handle
     *
     * @param logs log list
     */
    public void logHandle(LinkedBlockingQueue<String> logs) {
        // note that the "new line" is added here to facilitate log parsing
        if (logs.contains(FINALIZE_SESSION_MARKER.toString())) {
            logger.info(FINALIZE_SESSION_MARKER, FINALIZE_SESSION_MARKER.toString());
        } else {
            StringJoiner joiner = new StringJoiner("\n\t");
            while (!logs.isEmpty()) {
                joiner.add(logs.poll());
            }
            logger.info(" -> {}", joiner);
        }
    }

    /**
     * regular expressions match the contents between two specified strings
     *
     * @param content content
     * @param rgex rgex
     * @param sqlParamsMap sql params map
     * @param paramsPropsMap params props map
     */
    public void setSqlParamsMap(String content, String rgex, Map<Integer, Property> sqlParamsMap,
                                Map<String, Property> paramsPropsMap, int taskInstanceId) {
        if (paramsPropsMap == null) {
            return;
        }

        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(content);
        int index = 1;
        while (m.find()) {

            String paramName = m.group(1);
            Property prop = paramsPropsMap.get(paramName);

            if (prop == null) {
                logger.error(
                        "setSqlParamsMap: No Property with paramName: {} is found in paramsPropsMap of task instance"
                                + " with id: {}. So couldn't put Property in sqlParamsMap.",
                        paramName, taskInstanceId);
            } else {
                sqlParamsMap.put(index, prop);
                index++;
                logger.info(
                        "setSqlParamsMap: Property with paramName: {} put in sqlParamsMap of content {} successfully.",
                        paramName, content);
            }

        }
    }
}
