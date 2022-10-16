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

package org.apache.dolphinscheduler.service.utils;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Constants {

    public static final int[] NOT_TERMINATED_STATES = new int[]{
            WorkflowExecutionStatus.SUBMITTED_SUCCESS.getCode(),
            TaskExecutionStatus.DISPATCH.getCode(),
            WorkflowExecutionStatus.RUNNING_EXECUTION.getCode(),
            WorkflowExecutionStatus.DELAY_EXECUTION.getCode(),
            WorkflowExecutionStatus.READY_PAUSE.getCode(),
            WorkflowExecutionStatus.READY_STOP.getCode(),
            TaskExecutionStatus.NEED_FAULT_TOLERANCE.getCode(),
    };

    public static final int[] RUNNING_PROCESS_STATE = new int[]{
            TaskExecutionStatus.RUNNING_EXECUTION.getCode(),
            TaskExecutionStatus.SUBMITTED_SUCCESS.getCode(),
            TaskExecutionStatus.DISPATCH.getCode(),
            WorkflowExecutionStatus.SERIAL_WAIT.getCode()
    };
}
