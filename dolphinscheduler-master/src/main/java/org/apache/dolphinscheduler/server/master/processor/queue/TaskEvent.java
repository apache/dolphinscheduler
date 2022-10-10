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

import org.apache.dolphinscheduler.common.enums.TaskEventType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.remote.command.TaskExecuteResultCommand;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRunningCommand;
import org.apache.dolphinscheduler.remote.command.TaskRejectCommand;

import java.util.Date;

import lombok.Data;
import io.netty.channel.Channel;

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
    private TaskExecutionStatus state;

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
    private TaskEventType event;

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
        event.setEvent(TaskEventType.DISPATCH);
        return event;
    }

    public static TaskEvent newRunningEvent(TaskExecuteRunningCommand command, Channel channel, String workerAddress) {
        TaskEvent event = new TaskEvent();
        event.setProcessInstanceId(command.getProcessInstanceId());
        event.setTaskInstanceId(command.getTaskInstanceId());
        event.setState(command.getStatus());
        event.setStartTime(DateUtils.timeStampToDate(command.getStartTime()));
        event.setExecutePath(command.getExecutePath());
        event.setLogPath(command.getLogPath());
        event.setAppIds(command.getAppIds());
        event.setChannel(channel);
        event.setWorkerAddress(workerAddress);
        event.setEvent(TaskEventType.RUNNING);
        return event;
    }

    public static TaskEvent newResultEvent(TaskExecuteResultCommand command, Channel channel, String workerAddress) {
        TaskEvent event = new TaskEvent();
        event.setProcessInstanceId(command.getProcessInstanceId());
        event.setTaskInstanceId(command.getTaskInstanceId());
        event.setState(TaskExecutionStatus.of(command.getStatus()));
        event.setStartTime(DateUtils.timeStampToDate(command.getStartTime()));
        event.setExecutePath(command.getExecutePath());
        event.setLogPath(command.getLogPath());
        event.setEndTime(DateUtils.timeStampToDate(command.getEndTime()));
        event.setProcessId(command.getProcessId());
        event.setAppIds(command.getAppIds());
        event.setVarPool(command.getVarPool());
        event.setChannel(channel);
        event.setWorkerAddress(workerAddress);
        event.setEvent(TaskEventType.RESULT);
        return event;
    }

    public static TaskEvent newRecallEvent(TaskRejectCommand command, Channel channel) {
        TaskEvent event = new TaskEvent();
        event.setTaskInstanceId(command.getTaskInstanceId());
        event.setProcessInstanceId(command.getProcessInstanceId());
        event.setChannel(channel);
        event.setEvent(TaskEventType.WORKER_REJECT);
        return event;
    }
}
