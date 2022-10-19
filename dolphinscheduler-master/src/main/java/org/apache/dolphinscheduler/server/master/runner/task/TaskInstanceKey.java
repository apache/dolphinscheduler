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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * Used to identify a task instance.
 */
@Data
@AllArgsConstructor
public class TaskInstanceKey {

    private final int processInstanceId;
    private final long taskCode;
    private final int taskVersion;

    public static TaskInstanceKey getTaskInstanceKey(@NonNull ProcessInstance processInstance,
                                                     @NonNull TaskInstance taskInstance) {
        return new TaskInstanceKey(processInstance.getId(), taskInstance.getTaskCode(),
                taskInstance.getTaskDefinitionVersion());
    }

}
