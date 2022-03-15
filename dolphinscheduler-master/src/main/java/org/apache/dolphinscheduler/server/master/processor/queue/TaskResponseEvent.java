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

package org.apache.dolphinscheduler.server.master.processor.queue;

import org.apache.dolphinscheduler.common.enums.Event;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;

import java.util.Date;

import io.netty.channel.Channel;

/**
 * task event
 */
public class TaskResponseEvent {

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
    private Event event;

    /**
     * varPool
     */
    private String varPool;

    /**
     * channel
     */
    private Channel channel;

    private int processInstanceId;
    
    public static TaskResponseEvent newAck(ExecutionStatus state,
                                           Date startTime,
                                           String workerAddress,
                                           String executePath,
                                           String logPath,
                                           int taskInstanceId,
                                           Channel channel,
                                           int processInstanceId) {
        TaskResponseEvent event = new TaskResponseEvent();
        event.setState(state);
        event.setStartTime(startTime);
        event.setWorkerAddress(workerAddress);
        event.setExecutePath(executePath);
        event.setLogPath(logPath);
        event.setTaskInstanceId(taskInstanceId);
        event.setEvent(Event.ACK);
        event.setChannel(channel);
        event.setProcessInstanceId(processInstanceId);
        return event;
    }

    public static TaskResponseEvent newResult(ExecutionStatus state,
                                              Date endTime,
                                              int processId,
                                              String appIds,
                                              int taskInstanceId,
                                              String varPool,
                                              Channel channel,
                                              int processInstanceId) {
        TaskResponseEvent event = new TaskResponseEvent();
        event.setState(state);
        event.setEndTime(endTime);
        event.setProcessId(processId);
        event.setAppIds(appIds);
        event.setTaskInstanceId(taskInstanceId);
        event.setEvent(Event.RESULT);
        event.setVarPool(varPool);
        event.setChannel(channel);
        event.setProcessInstanceId(processInstanceId);
        return event;
    }

    public String getVarPool() {
        return varPool;
    }

    public void setVarPool(String varPool) {
        this.varPool = varPool;
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

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public int getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(int processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
}
