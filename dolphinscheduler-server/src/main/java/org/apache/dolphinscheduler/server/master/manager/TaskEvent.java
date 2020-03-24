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

package org.apache.dolphinscheduler.server.master.manager;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;

import java.util.Date;

/**
 * task event
 */
public class TaskEvent {

    public static final String ACK = "ack";
    public static final String RESPONSE = "response";

    /**
     * taskInstanceId
     */
    private int taskInstanceId;

    /**
     * worker address
     */
    private String workerAddress;

    /**
     * state
     */
    private ExecutionStatus state;

    /**
     * start time
     */
    private Date startTime;

    /**
     * end time
     */
    private Date endTime;

    /**
     * execute path
     */
    private String executePath;

    /**
     * log path
     */
    private String logPath;

    /**
     * processId
     */
    private int processId;

    /**
     * appIds
     */
    private String appIds;

    /**
     * ack / response
     */
    private String type;


    /**
     * receive ack info
     * @param state state
     * @param startTime startTime
     * @param workerAddress workerAddress
     * @param executePath executePath
     * @param logPath logPath
     * @param taskInstanceId taskInstanceId
     * @param type type
     */
    public void receiveAck(ExecutionStatus state,
                           Date startTime,
                           String workerAddress,
                           String executePath,
                           String logPath,
                           int taskInstanceId,
                           String type){
        this.state = state;
        this.startTime = startTime;
        this.workerAddress = workerAddress;
        this.executePath = executePath;
        this.logPath = logPath;
        this.taskInstanceId = taskInstanceId;
        this.type = type;
    }

    /**
     * receive response info
     * @param state state
     * @param endTime endTime
     * @param processId processId
     * @param appIds appIds
     * @param taskInstanceId taskInstanceId
     * @param type type
     */
    public void receiveResponse(ExecutionStatus state,
                                Date endTime,
                                int processId,
                                String appIds,
                                int taskInstanceId,
                                String type){
        this.state = state;
        this.endTime = endTime;
        this.processId = processId;
        this.appIds = appIds;
        this.taskInstanceId = taskInstanceId;
        this.type = type;
    }

    public int getTaskInstanceId() {
        return taskInstanceId;
    }

    public void setTaskInstanceId(int taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    public String getWorkerAddress() {
        return workerAddress;
    }

    public void setWorkerAddress(String workerAddress) {
        this.workerAddress = workerAddress;
    }

    public ExecutionStatus getState() {
        return state;
    }

    public void setState(ExecutionStatus state) {
        this.state = state;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getExecutePath() {
        return executePath;
    }

    public void setExecutePath(String executePath) {
        this.executePath = executePath;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "TaskEvent{" +
                "taskInstanceId=" + taskInstanceId +
                ", workerAddress='" + workerAddress + '\'' +
                ", state=" + state +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", executePath='" + executePath + '\'' +
                ", logPath='" + logPath + '\'' +
                ", processId=" + processId +
                ", appIds='" + appIds + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
