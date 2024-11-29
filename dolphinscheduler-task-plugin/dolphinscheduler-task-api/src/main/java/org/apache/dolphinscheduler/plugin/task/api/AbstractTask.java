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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTask {

    @Getter
    @Setter
    protected Map<String, String> taskOutputParams;

    /**
     * taskExecutionContext
     **/
    protected TaskExecutionContext taskRequest;

    /**
     * SHELL process pid
     */
    protected int processId;

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

    // todo: return TaskResult rather than store the result in Task
    public abstract void handle(TaskCallBack taskCallBack) throws TaskException;

    public abstract void cancel() throws TaskException;

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
        if (exitStatusCode == TaskConstants.EXIT_CODE_SUCCESS) {
            return TaskExecutionStatus.SUCCESS;
        }
        if (exitStatusCode == TaskConstants.EXIT_CODE_KILL || exitStatusCode == TaskConstants.EXIT_CODE_HARD_KILL) {
            return TaskExecutionStatus.KILL;
        }
        return TaskExecutionStatus.FAILURE;
    }

    /**
     * log handle
     *
     * @param logs log list
     */
    public void logHandle(LinkedBlockingQueue<String> logs) {

        StringJoiner joiner = new StringJoiner("\n\t");
        while (!logs.isEmpty()) {
            joiner.add(logs.poll());
        }
        log.info(" -> {}", joiner);
    }

    /**
     * regular expressions match the contents between two specified strings
     *
     * @param content        content
     * @param sqlParamsMap   sql params map
     * @param paramsPropsMap params props map
     */
    public void setSqlParamsMap(String content, Map<Integer, Property> sqlParamsMap,
                                Map<String, Property> paramsPropsMap, int taskInstanceId) {
        if (paramsPropsMap == null) {
            return;
        }

        Matcher m = TaskConstants.SQL_PARAMS_PATTERN.matcher(content);
        int index = 1;
        while (m.find()) {

            String paramName = m.group(TaskConstants.GROUP_NAME1);
            if (paramName == null) {
                paramName = m.group(TaskConstants.GROUP_NAME2);
            }

            Property prop = paramsPropsMap.get(paramName);

            if (prop == null) {
                log.error(
                        "setSqlParamsMap: No Property with paramName: {} is found in paramsPropsMap of task instance"
                                + " with id: {}. So couldn't put Property in sqlParamsMap.",
                        paramName, taskInstanceId);
            } else {
                sqlParamsMap.put(index, prop);
                index++;
                log.info(
                        "setSqlParamsMap: Property with paramName: {} put in sqlParamsMap of content {} successfully.",
                        paramName, content);
            }

        }
    }
}
