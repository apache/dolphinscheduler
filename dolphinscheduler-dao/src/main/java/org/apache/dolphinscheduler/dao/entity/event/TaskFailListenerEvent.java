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

package org.apache.dolphinscheduler.dao.entity.event;

import org.apache.dolphinscheduler.common.enums.ListenerEventType;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskFailListenerEvent implements AbstractListenerEvent {

    private long projectCode;
    private String projectName;
    private String owner;
    private long processId;
    private long processDefinitionCode;
    private String processName;
    private int taskInstanceId;
    private long taskCode;
    private String taskName;
    private String taskType;
    private TaskExecutionStatus taskState;
    private Date taskStartTime;
    private Date taskEndTime;
    private String taskHost;
    private String logPath;

    @Override
    public ListenerEventType getEventType() {
        return ListenerEventType.TASK_FAIL;
    }

    @Override
    public String getTitle() {
        return String.format("task fail: %s", taskName);
    }
}
