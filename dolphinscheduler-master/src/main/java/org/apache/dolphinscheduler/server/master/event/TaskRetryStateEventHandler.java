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
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.google.auto.service.AutoService;

@AutoService(StateEventHandler.class)
@Slf4j
public class TaskRetryStateEventHandler implements StateEventHandler {

    @Override
    public boolean handleStateEvent(WorkflowExecuteRunnable workflowExecuteRunnable,
                                    StateEvent stateEvent) throws StateEventHandleException {
        TaskStateEvent taskStateEvent = (TaskStateEvent) stateEvent;

        log.info("Handle task instance retry event, taskCode: {}", taskStateEvent.getTaskCode());

        TaskMetrics.incTaskInstanceByState("retry");
        Map<Long, TaskInstance> waitToRetryTaskInstanceMap = workflowExecuteRunnable.getWaitToRetryTaskInstanceMap();
        TaskInstance taskInstance = waitToRetryTaskInstanceMap.get(taskStateEvent.getTaskCode());
        workflowExecuteRunnable.addTaskToStandByList(taskInstance);
        workflowExecuteRunnable.submitStandByTask();
        waitToRetryTaskInstanceMap.remove(taskStateEvent.getTaskCode());
        return true;
    }

    @Override
    public StateEventType getEventType() {
        return StateEventType.TASK_RETRY;
    }
}
