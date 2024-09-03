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

package org.apache.dolphinscheduler.extract.worker.transportor;

import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskInstanceKillResponse {

    private int taskInstanceId;

    private String host;

    private TaskExecutionStatus status;

    private int processId;

    /**
     * other resource manager appId , for example : YARN etc
     */
    private List<String> appIds;

    private boolean success;

    private String message;

    public static TaskInstanceKillResponse success(TaskExecutionContext taskExecutionContext) {
        TaskInstanceKillResponse taskInstanceKillResponse = new TaskInstanceKillResponse();
        taskInstanceKillResponse.setSuccess(true);
        taskInstanceKillResponse.setStatus(taskExecutionContext.getCurrentExecutionStatus());
        if (taskExecutionContext.getAppIds() != null) {
            taskInstanceKillResponse
                    .setAppIds(Arrays.asList(taskExecutionContext.getAppIds().split(TaskConstants.COMMA)));
        }
        taskInstanceKillResponse.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        taskInstanceKillResponse.setHost(taskExecutionContext.getHost());
        taskInstanceKillResponse.setProcessId(taskExecutionContext.getProcessId());
        return taskInstanceKillResponse;
    }

    public static TaskInstanceKillResponse fail(String message) {
        TaskInstanceKillResponse taskInstanceKillResponse = new TaskInstanceKillResponse();
        taskInstanceKillResponse.setSuccess(false);
        taskInstanceKillResponse.setMessage(message);
        return taskInstanceKillResponse;
    }

}
