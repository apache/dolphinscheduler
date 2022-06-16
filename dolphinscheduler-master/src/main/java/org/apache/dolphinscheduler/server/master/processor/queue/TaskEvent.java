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
import org.apache.dolphinscheduler.remote.command.TaskExecuteResponseCommand;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRunningCommand;
import org.apache.dolphinscheduler.remote.command.TaskRecallCommand;
import org.apache.dolphinscheduler.remote.utils.ChannelUtils;

import java.util.Date;

import io.netty.channel.Channel;
import lombok.Data;

/**
 * task event
 */
@Data
public class TaskEvent {

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

    public static TaskEvent newDispatchEvent(int processInstanceId, int taskInstanceId, String workerAddress) {
        TaskEvent event = new TaskEvent();
        event.setProcessInstanceId(processInstanceId);
        event.setTaskInstanceId(taskInstanceId);
        event.setWorkerAddress(workerAddress);
        event.setEvent(Event.DISPATCH);
        return event;
    }

    public static TaskEvent newRunningEvent(TaskExecuteRunningCommand command, Channel channel) {
        TaskEvent event = new TaskEvent();
        event.setProcessInstanceId(command.getProcessInstanceId());
        event.setTaskInstanceId(command.getTaskInstanceId());
        event.setState(ExecutionStatus.of(command.getStatus()));
        event.setStartTime(command.getStartTime());
        event.setExecutePath(command.getExecutePath());
        event.setLogPath(command.getLogPath());
        event.setChannel(channel);
        event.setWorkerAddress(ChannelUtils.toAddress(channel).getAddress());
        event.setEvent(Event.RUNNING);
        return event;
    }

    public static TaskEvent newResultEvent(TaskExecuteResponseCommand command, Channel channel) {
        TaskEvent event = new TaskEvent();
        event.setProcessInstanceId(command.getProcessInstanceId());
        event.setTaskInstanceId(command.getTaskInstanceId());
        event.setState(ExecutionStatus.of(command.getStatus()));
        event.setStartTime(command.getStartTime());
        event.setExecutePath(command.getExecutePath());
        event.setLogPath(command.getLogPath());
        event.setEndTime(command.getEndTime());
        event.setProcessId(command.getProcessId());
        event.setAppIds(command.getAppIds());
        event.setVarPool(command.getVarPool());
        event.setChannel(channel);
        event.setWorkerAddress(ChannelUtils.toAddress(channel).getAddress());
        event.setEvent(Event.RESULT);
        return event;
    }

    public static TaskEvent newRecallEvent(TaskRecallCommand command, Channel channel) {
        TaskEvent event = new TaskEvent();
        event.setTaskInstanceId(command.getTaskInstanceId());
        event.setProcessInstanceId(command.getProcessInstanceId());
        event.setChannel(channel);
        event.setEvent(Event.WORKER_REJECT);
        return event;
    }
}
