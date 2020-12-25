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

import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;

import java.io.Serializable;

/**
 *  db task final result response command
 */
public class DBTaskResponseCommand implements Serializable {

    private int taskInstanceId;
    private int status;

    public DBTaskResponseCommand(int status,int taskInstanceId) {
        this.status = status;
        this.taskInstanceId = taskInstanceId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTaskInstanceId() {
        return taskInstanceId;
    }

    public void setTaskInstanceId(int taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    /**
     * package response command
     * @return command
     */
    public Command convert2Command(){
        Command command = new Command();
        command.setType(CommandType.DB_TASK_RESPONSE);
        byte[] body = FastJsonSerializer.serialize(this);
        command.setBody(body);
        return command;
    }

    @Override
    public String toString() {
        return "DBTaskResponseCommand{" +
                "taskInstanceId=" + taskInstanceId +
                ", status=" + status +
                '}';
    }
}
