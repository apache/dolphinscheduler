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
 *  execute task request command
 */
public class TaskExecuteRequestCommand implements Serializable {

    /**
     *  task execution context
     */
    private String taskExecutionContext;

    public String getTaskExecutionContext() {
        return taskExecutionContext;
    }

    public void setTaskExecutionContext(String taskExecutionContext) {
        this.taskExecutionContext = taskExecutionContext;
    }

    public TaskExecuteRequestCommand() {
    }

    public TaskExecuteRequestCommand(String taskExecutionContext) {
        this.taskExecutionContext = taskExecutionContext;
    }

    /**
     *  package request command
     *
     * @return command
     */
    public Command convert2Command(){
        Command command = new Command();
        command.setType(CommandType.TASK_EXECUTE_REQUEST);
        byte[] body = FastJsonSerializer.serialize(this);
        command.setBody(body);
        return command;
    }

    @Override
    public String toString() {
        return "TaskExecuteRequestCommand{" +
                "taskExecutionContext='" + taskExecutionContext + '\'' +
                '}';
    }
}
