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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin.subworkflow;

import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubWorkflowTracker {

    private final WorkflowInstanceDao workflowInstanceDao;

    private final SubWorkflowLogicTaskRuntimeContext subWorkflowLogicTaskRuntimeContext;

    public SubWorkflowTracker(final SubWorkflowLogicTaskRuntimeContext subWorkflowLogicTaskRuntimeContext,
                              final WorkflowInstanceDao workflowInstanceDao) {
        this.subWorkflowLogicTaskRuntimeContext = subWorkflowLogicTaskRuntimeContext;
        this.workflowInstanceDao = workflowInstanceDao;
    }

    public @NonNull TaskExecutionStatus getSubWorkflowState() {
        final Integer subWorkflowInstanceId = subWorkflowLogicTaskRuntimeContext.getSubWorkflowInstanceId();
        final WorkflowInstance subWorkflowInstance = workflowInstanceDao.queryById(subWorkflowInstanceId);
        if (subWorkflowInstance == null) {
            log.info("Cannot find the SubWorkflow instance: {}, maybe it has been deleted", subWorkflowInstanceId);
            return TaskExecutionStatus.FAILURE;
        }
        switch (subWorkflowInstance.getState()) {
            case PAUSE:
                return TaskExecutionStatus.PAUSE;
            case STOP:
                return TaskExecutionStatus.KILL;
            case SUCCESS:
                return TaskExecutionStatus.SUCCESS;
            case FAILURE:
                return TaskExecutionStatus.FAILURE;
            default:
                return TaskExecutionStatus.RUNNING_EXECUTION;
        }
    }

}
