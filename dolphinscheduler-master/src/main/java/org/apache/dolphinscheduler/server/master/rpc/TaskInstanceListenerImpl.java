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

package org.apache.dolphinscheduler.server.master.rpc;

import org.apache.dolphinscheduler.extract.master.ITaskInstanceExecutionEventListener;
import org.apache.dolphinscheduler.extract.master.transportor.TaskInstanceExecutionFinishEvent;
import org.apache.dolphinscheduler.extract.master.transportor.TaskInstanceExecutionInfoEvent;
import org.apache.dolphinscheduler.extract.master.transportor.TaskInstanceExecutionRunningEvent;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowInstanceStateChangeEvent;
import org.apache.dolphinscheduler.server.master.runner.listener.TaskInstanceExecutionEventListenerFunctionManager;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaskInstanceListenerImpl implements ITaskInstanceExecutionEventListener {

    @Autowired
    private TaskInstanceExecutionEventListenerFunctionManager taskInstanceExecutionEventListenerManager;

    @Override
    public void onTaskInstanceExecutionRunning(TaskInstanceExecutionRunningEvent taskInstanceExecutionRunningEvent) {
        taskInstanceExecutionEventListenerManager
                .getTaskInstanceExecutionRunningEventListenFunction()
                .handleTaskInstanceExecutionEvent(taskInstanceExecutionRunningEvent);

    }

    @Override
    public void onTaskInstanceExecutionFinish(TaskInstanceExecutionFinishEvent taskInstanceExecutionFinishEvent) {
        taskInstanceExecutionEventListenerManager.getTaskInstanceExecutionResultEventListenFunction()
                .handleTaskInstanceExecutionEvent(taskInstanceExecutionFinishEvent);
    }

    @Override
    public void onTaskInstanceExecutionInfoUpdate(TaskInstanceExecutionInfoEvent taskInstanceExecutionInfoEvent) {
        taskInstanceExecutionEventListenerManager.getTaskInstanceExecutionInfoEventListenFunction()
                .handleTaskInstanceExecutionEvent(taskInstanceExecutionInfoEvent);
    }

    @Override
    public void onWorkflowInstanceInstanceStateChange(WorkflowInstanceStateChangeEvent taskInstanceStateChangeEvent) {
        taskInstanceExecutionEventListenerManager.getTaskInstanceStateEventListenFunction()
                .handleTaskInstanceExecutionEvent(taskInstanceStateChangeEvent);
    }
}
