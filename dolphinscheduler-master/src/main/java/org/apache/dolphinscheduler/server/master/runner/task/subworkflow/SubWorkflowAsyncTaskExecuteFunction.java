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
import org.apache.dolphinscheduler.server.master.runner.execute.AsyncTaskExecuteFunction;

import java.time.Duration;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubWorkflowAsyncTaskExecuteFunction implements AsyncTaskExecuteFunction {

    private static final Duration SUB_WORKFLOW_TASK_EXECUTE_STATE_CHECK_INTERVAL = Duration.ofSeconds(10);

    private final WorkflowInstanceDao workflowInstanceDao;

    private final SubWorkflowLogicTaskRuntimeContext subWorkflowLogicTaskRuntimeContext;

    public SubWorkflowAsyncTaskExecuteFunction(final SubWorkflowLogicTaskRuntimeContext subWorkflowLogicTaskRuntimeContext,
                                               final WorkflowInstanceDao workflowInstanceDao) {
        this.subWorkflowLogicTaskRuntimeContext = subWorkflowLogicTaskRuntimeContext;
        this.workflowInstanceDao = workflowInstanceDao;
    }

    @Override
    public @NonNull AsyncTaskExecutionStatus getAsyncTaskExecutionStatus() {
        final Integer subWorkflowInstanceId = subWorkflowLogicTaskRuntimeContext.getSubWorkflowInstanceId();
        final WorkflowInstance subWorkflowInstance = workflowInstanceDao.queryById(subWorkflowInstanceId);
        if (subWorkflowInstance == null) {
            log.info("Cannot find the SubWorkflow instance: {}, maybe it has been deleted", subWorkflowInstanceId);
            return AsyncTaskExecutionStatus.FAILED;
        }
        switch (subWorkflowInstance.getState()) {
            case PAUSE:
                return AsyncTaskExecutionStatus.PAUSE;
            case STOP:
                return AsyncTaskExecutionStatus.KILL;
            case SUCCESS:
                return AsyncTaskExecutionStatus.SUCCESS;
            case FAILURE:
                return AsyncTaskExecutionStatus.FAILED;
            default:
                return AsyncTaskExecutionStatus.RUNNING;
        }
    }

    @Override
    public @NonNull Duration getAsyncTaskStateCheckInterval() {
        return SUB_WORKFLOW_TASK_EXECUTE_STATE_CHECK_INTERVAL;
    }
}
