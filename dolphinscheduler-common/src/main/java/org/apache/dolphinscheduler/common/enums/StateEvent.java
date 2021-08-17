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

package org.apache.dolphinscheduler.common.enums;

import io.netty.channel.Channel;

/**
 * state event
 */
public class StateEvent {

    /**
     * origin_pid-origin_task_id-process_instance_id-task_instance_id
     */
    private String key;

    private StateEventType type;

    private ExecutionStatus executionStatus;

    private int taskInstanceId;

    private int processInstanceId;

    private String context;

    private Channel channel;

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    public int getTaskInstanceId() {
        return taskInstanceId;
    }

    public int getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(int processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setTaskInstanceId(int taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "State Event :"
                + "key: " + key
                + " type: " + type.toString()
                + " executeStatus: " + executionStatus
                + " task instance id: " + taskInstanceId
                + " process instance id: " + processInstanceId
                + " context: " + context
                ;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setType(StateEventType type) {
        this.type = type;
    }

    public StateEventType getType() {
        return this.type;
    }
}
