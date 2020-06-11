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
package org.apache.dolphinscheduler.remote.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.dolphinscheduler.remote.utils.JsonSerializer;

import java.io.Serializable;
import java.util.Date;

/**
 *  execute task response command
 */
public class TaskExecuteResponseCommand implements Serializable {


    public TaskExecuteResponseCommand() {
    }

    public TaskExecuteResponseCommand(int taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    /**
     *  task instance id
     */
    private int taskInstanceId;

    /**
     *  status
     */
    private int status;


    /**
     *  end time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date endTime;


    /**
     * processId
     */
    private int processId;

    /**
     * appIds
     */
    private String appIds;


    public int getTaskInstanceId() {
        return taskInstanceId;
    }

    public void setTaskInstanceId(int taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
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

    /**
     * package response command
     * @return command
     */
    public Command convert2Command(){
        Command command = new Command();
        command.setType(CommandType.TASK_EXECUTE_RESPONSE);
        byte[] body = JsonSerializer.serialize(this);
        command.setBody(body);
        return command;
    }

    @Override
    public String toString() {
        return "TaskExecuteResponseCommand{" +
                "taskInstanceId=" + taskInstanceId +
                ", status=" + status +
                ", endTime=" + endTime +
                ", processId=" + processId +
                ", appIds='" + appIds + '\'' +
                '}';
    }
}
