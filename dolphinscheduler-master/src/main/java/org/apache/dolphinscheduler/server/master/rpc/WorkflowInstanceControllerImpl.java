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

import org.apache.dolphinscheduler.extract.master.IWorkflowInstanceController;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowInstancePauseRequest;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowInstancePauseResponse;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowInstanceStopRequest;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowInstanceStopResponse;
import org.apache.dolphinscheduler.server.master.engine.WorkflowCacheRepository;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowPauseLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowStopLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkflowInstanceControllerImpl implements IWorkflowInstanceController {

    @Autowired
    private WorkflowCacheRepository workflowCacheRepository;

    @Override
    public WorkflowInstancePauseResponse pauseWorkflowInstance(final WorkflowInstancePauseRequest workflowInstancePauseRequest) {
        try {
            final Integer workflowInstanceId = workflowInstancePauseRequest.getWorkflowInstanceId();
            final IWorkflowExecutionRunnable workflowExecutionRunnable =
                    workflowCacheRepository.get(workflowInstanceId);
            if (workflowExecutionRunnable == null) {
                return WorkflowInstancePauseResponse
                        .fail("Cannot find the WorkflowExecuteRunnable: " + workflowInstanceId);
            }
            workflowExecutionRunnable.getWorkflowEventBus()
                    .publish(WorkflowPauseLifecycleEvent.of(workflowExecutionRunnable));
            return WorkflowInstancePauseResponse.success();
        } catch (Exception ex) {
            log.error("Handle workflowInstancePauseRequest: {} failed", workflowInstancePauseRequest, ex);
            return WorkflowInstancePauseResponse.fail(ExceptionUtils.getMessage(ex));
        }
    }

    @Override
    public WorkflowInstanceStopResponse stopWorkflowInstance(final WorkflowInstanceStopRequest workflowInstanceStopRequest) {
        try {
            final Integer workflowInstanceId = workflowInstanceStopRequest.getWorkflowInstanceId();
            final IWorkflowExecutionRunnable workflowExecutionRunnable =
                    workflowCacheRepository.get(workflowInstanceId);
            if (workflowExecutionRunnable == null) {
                return WorkflowInstanceStopResponse
                        .fail("Cannot find the WorkflowExecuteRunnable: " + workflowInstanceId);
            }
            workflowExecutionRunnable.getWorkflowEventBus()
                    .publish(WorkflowStopLifecycleEvent.of(workflowExecutionRunnable));
            return WorkflowInstanceStopResponse.success();
        } catch (Exception ex) {
            log.error("Handle workflowInstanceStopRequest: {} failed", workflowInstanceStopRequest, ex);
            return WorkflowInstanceStopResponse.fail(ExceptionUtils.getMessage(ex));
        }
    }
}
