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

package org.apache.dolphinscheduler.server.master.runner.task.subworkflow;

import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.runner.execute.AsyncTaskExecuteFunction;

import java.time.Duration;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubWorkflowAsyncTaskExecuteFunction implements AsyncTaskExecuteFunction {

    private static final Duration SUB_WORKFLOW_TASK_EXECUTE_STATE_CHECK_INTERVAL = Duration.ofSeconds(10);

    private final TaskExecutionContext taskExecutionContext;
    private final WorkflowInstanceDao workflowInstanceDao;
    private WorkflowInstance subWorkflowInstance;

    private SubWorkflowLogicTaskRuntimeContext subWorkflowLogicTaskRuntimeContext;

    public SubWorkflowAsyncTaskExecuteFunction(TaskExecutionContext taskExecutionContext,
                                               WorkflowInstanceDao workflowInstanceDao) {
        this.taskExecutionContext = taskExecutionContext;
        this.workflowInstanceDao = workflowInstanceDao;
    }

    @Override
    public @NonNull AsyncTaskExecutionStatus getAsyncTaskExecutionStatus() {
        if (subWorkflowInstance == null) {
            createSubWorkflowInstanceIfAbsent();
        }
        if (subWorkflowInstance == null) {
            log.info("The sub workflow instance doesn't created");
            return AsyncTaskExecutionStatus.RUNNING;
        }
        subWorkflowInstance = workflowInstanceDao.queryById(subWorkflowInstance.getId());
        if (subWorkflowInstance != null && subWorkflowInstance.getState().isFinished()) {
            return subWorkflowInstance.getState().isSuccess() ? AsyncTaskExecutionStatus.SUCCESS
                    : AsyncTaskExecutionStatus.FAILED;
        }
        return AsyncTaskExecutionStatus.RUNNING;
    }

    private void createSubWorkflowInstanceIfAbsent() {
        // todo: we need to creat sub workflow instance here, rather than create command
        // create command might occur duplicate sub workflow instance when failover
        // generate the sub workflow instance
        subWorkflowInstance = workflowInstanceDao.querySubWorkflowInstanceByParentId(
                taskExecutionContext.getProcessInstanceId(), taskExecutionContext.getTaskInstanceId());

    }

    @Override
    public @NonNull Duration getAsyncTaskStateCheckInterval() {
        return SUB_WORKFLOW_TASK_EXECUTE_STATE_CHECK_INTERVAL;
    }
}
