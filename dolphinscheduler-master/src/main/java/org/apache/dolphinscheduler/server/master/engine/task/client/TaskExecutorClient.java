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

package org.apache.dolphinscheduler.server.master.engine.task.client;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.server.master.engine.exceptions.TaskKillException;
import org.apache.dolphinscheduler.server.master.engine.exceptions.TaskPauseException;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The client of task executor, used to communicate with task executor.
 */
@Component
public class TaskExecutorClient implements ITaskExecutorClient {

    @Autowired
    private LogicTaskExecutorClientDelegator logicTaskExecutorClientDelegator;

    @Autowired
    private PhysicalTaskExecutorClientDelegator physicalTaskExecutorClientDelegator;

    @Override
    public void pause(ITaskExecutionRunnable taskExecutionRunnable) throws TaskPauseException {
        try {
            getTaskExecutorClientDelegator(taskExecutionRunnable).pause(taskExecutionRunnable);
        } catch (Exception ex) {
            throw new TaskPauseException("Pause task: " + taskExecutionRunnable.getName() + " from executor failed",
                    ex);
        }
    }

    @Override
    public void kill(ITaskExecutionRunnable taskExecutionRunnable) throws TaskKillException {
        try {
            getTaskExecutorClientDelegator(taskExecutionRunnable).kill(taskExecutionRunnable);
        } catch (Exception ex) {
            throw new TaskKillException("Kill task: " + taskExecutionRunnable.getName() + " from executor failed", ex);
        }
    }

    private ITaskExecutorClientDelegator getTaskExecutorClientDelegator(final ITaskExecutionRunnable taskExecutionRunnable) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        checkArgument(taskInstance != null, "taskType cannot be empty");
        if (TaskTypeUtils.isLogicTask(taskInstance.getTaskType())) {
            return logicTaskExecutorClientDelegator;
        }
        return physicalTaskExecutorClientDelegator;
    }
}
