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

package org.apache.dolphinscheduler.server.master.event;

import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.task.ITaskProcessor;
import org.apache.dolphinscheduler.server.master.runner.task.TaskAction;

import java.util.Map;

import com.google.auto.service.AutoService;

@AutoService(StateEventHandler.class)
public class TaskTimeoutStateEventHandler implements StateEventHandler {
    @Override
    public boolean handleStateEvent(WorkflowExecuteRunnable workflowExecuteRunnable, StateEvent stateEvent)
        throws StateEventHandleError {
        TaskMetrics.incTaskInstanceByState("timeout");
        workflowExecuteRunnable.checkTaskInstanceByStateEvent(stateEvent);

        TaskInstance taskInstance = workflowExecuteRunnable.getTaskInstance(stateEvent.getTaskInstanceId()).get();

        if (TimeoutFlag.CLOSE == taskInstance.getTaskDefine().getTimeoutFlag()) {
            return true;
        }
        TaskTimeoutStrategy taskTimeoutStrategy = taskInstance.getTaskDefine().getTimeoutNotifyStrategy();
        Map<Long, ITaskProcessor> activeTaskProcessMap = workflowExecuteRunnable.getActiveTaskProcessMap();
        if (TaskTimeoutStrategy.FAILED == taskTimeoutStrategy
            || TaskTimeoutStrategy.WARNFAILED == taskTimeoutStrategy) {
            ITaskProcessor taskProcessor = activeTaskProcessMap.get(taskInstance.getTaskCode());
            taskProcessor.action(TaskAction.TIMEOUT);
        }
        if (TaskTimeoutStrategy.WARN == taskTimeoutStrategy || TaskTimeoutStrategy.WARNFAILED == taskTimeoutStrategy) {
            workflowExecuteRunnable.processTimeout();
            workflowExecuteRunnable.taskTimeout(taskInstance);
        }
        return true;
    }

    @Override
    public StateEventType getEventType() {
        return StateEventType.TASK_TIMEOUT;
    }
}
