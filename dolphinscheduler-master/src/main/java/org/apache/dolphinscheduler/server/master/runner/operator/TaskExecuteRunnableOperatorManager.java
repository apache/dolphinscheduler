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

package org.apache.dolphinscheduler.server.master.runner.operator;

import org.apache.dolphinscheduler.server.master.runner.TaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.utils.TaskUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskExecuteRunnableOperatorManager {

    @Autowired
    private TaskExecuteRunnableKillOperator taskKillOperator;

    @Autowired
    private LogicTaskExecuteRunnableKillOperator logicTaskKillOperator;

    @Autowired
    private TaskExecuteRunnablePauseOperator taskPauseOperator;

    @Autowired
    private LogicTaskExecuteRunnablePauseOperator logicTaskPauseOperator;

    @Autowired
    private TaskExecuteRunnableDispatchOperator taskDispatchOperator;

    @Autowired
    private LogicTaskExecuteRunnableDispatchOperator logicTaskDispatchOperator;

    @Autowired
    private TaskExecuteRunnableTimeoutOperator taskTimeoutOperator;

    @Autowired
    private LogicTaskExecuteRunnableTimeoutOperator logicTaskTimeoutOperator;

    public TaskExecuteRunnableOperator getTaskKillOperator(TaskExecutionRunnable defaultTaskExecuteRunnable) {
        if (TaskUtils.isMasterTask(
                defaultTaskExecuteRunnable.getTaskExecutionRunnableContext().getTaskInstance().getTaskType())) {
            return logicTaskKillOperator;
        }
        return taskKillOperator;
    }

    public TaskExecuteRunnableOperator getTaskPauseOperator(TaskExecutionRunnable defaultTaskExecuteRunnable) {
        if (TaskUtils.isMasterTask(
                defaultTaskExecuteRunnable.getTaskExecutionRunnableContext().getTaskInstance().getTaskType())) {
            return logicTaskPauseOperator;
        }
        return taskPauseOperator;
    }

    public TaskExecuteRunnableOperator getTaskDispatchOperator(TaskExecutionRunnable defaultTaskExecuteRunnable) {
        if (TaskUtils.isMasterTask(
                defaultTaskExecuteRunnable.getTaskExecutionRunnableContext().getTaskInstance().getTaskType())) {
            return logicTaskDispatchOperator;
        }
        return taskDispatchOperator;
    }

    public TaskExecuteRunnableOperator getTaskTimeoutOperator(TaskExecutionRunnable defaultTaskExecuteRunnable) {
        if (TaskUtils.isMasterTask(
                defaultTaskExecuteRunnable.getTaskExecutionRunnableContext().getTaskInstance().getTaskType())) {
            return logicTaskTimeoutOperator;
        }
        return taskTimeoutOperator;
    }

}
