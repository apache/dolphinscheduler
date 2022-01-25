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

package org.apache.dolphinscheduler.server.master.runner.task;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import java.util.Objects;

/**
 * task instance key, processInstanceId
 */
public class TaskInstanceKey {
    private int processInstanceId;
    private long taskCode;
    private int taskVersion;

    public TaskInstanceKey(int processInstanceId, long taskCode, int taskVersion) {
        this.processInstanceId = processInstanceId;
        this.taskCode = taskCode;
        this.taskVersion = taskVersion;
    }

    public int getProcessInstanceId() {
        return processInstanceId;
    }

    public long getTaskCode() {
        return taskCode;
    }

    public int getTaskVersion() {
        return taskVersion;
    }

    public static TaskInstanceKey getTaskInstanceKey(ProcessInstance processInstance, TaskInstance taskInstance) {
        if (processInstance == null || taskInstance == null) {
            return null;
        }
        return new TaskInstanceKey(processInstance.getId(), taskInstance.getTaskCode(), taskInstance.getTaskDefinitionVersion());
    }

    @Override
    public String toString() {
        return "TaskKey{" +
                "processInstanceId=" + processInstanceId +
                ", taskCode=" + taskCode +
                ", taskVersion=" + taskVersion +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskInstanceKey taskInstanceKey = (TaskInstanceKey) o;
        return processInstanceId == taskInstanceKey.processInstanceId && taskCode == taskInstanceKey.taskCode && taskVersion == taskInstanceKey.taskVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(processInstanceId, taskCode, taskVersion);
    }
}
