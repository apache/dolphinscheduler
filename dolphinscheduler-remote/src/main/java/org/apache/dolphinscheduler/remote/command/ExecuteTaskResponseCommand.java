/* * Licensed to the Apache Software Foundation (ASF) under one or more * contributor license agreements.  See the NOTICE file distributed with * this work for additional information regarding copyright ownership. * The ASF licenses this file to You under the Apache License, Version 2.0 * (the "License"); you may not use this file except in compliance with * the License.  You may obtain a copy of the License at * *    http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */package org.apache.dolphinscheduler.remote.command;import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;import java.io.Serializable;import java.util.Date;/** *  execute task response command */public class ExecuteTaskResponseCommand implements Serializable {    public ExecuteTaskResponseCommand() {    }    public ExecuteTaskResponseCommand(int taskInstanceId) {        this.taskInstanceId = taskInstanceId;    }    /**     *  task instance id     */    private int taskInstanceId;    /**     *  status     */    private int status;    /**     *  end time     */    private Date endTime;    public int getTaskInstanceId() {        return taskInstanceId;    }    public void setTaskInstanceId(int taskInstanceId) {        this.taskInstanceId = taskInstanceId;    }    public int getStatus() {        return status;    }    public void setStatus(int status) {        this.status = status;    }    public Date getEndTime() {        return endTime;    }    public void setEndTime(Date endTime) {        this.endTime = endTime;    }    /**     * package response command     *     * @param opaque request unique identification     * @return command     */    public Command convert2Command(long opaque){        Command command = new Command(opaque);        command.setType(CommandType.EXECUTE_TASK_RESPONSE);        byte[] body = FastJsonSerializer.serialize(this);        command.setBody(body);        return command;    }}