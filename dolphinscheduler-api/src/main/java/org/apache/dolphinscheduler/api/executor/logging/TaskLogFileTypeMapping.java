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

package org.apache.dolphinscheduler.api.executor.logging;

import org.apache.dolphinscheduler.plugin.task.api.utils.TaskLogFileType;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TaskLogFileTypeMapping {

    public static TaskLogFileType toTaskLogFileType(TaskLogType taskLogType) {
        switch (taskLogType) {
            case LOG:
                return TaskLogFileType.TASK_LOG;
            case OUTPUT:
                return TaskLogFileType.TASK_OUTPUT;
            default:
                throw new IllegalArgumentException("Unsupported task log type: " + taskLogType);
        }
    }
}
