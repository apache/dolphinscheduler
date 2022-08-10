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

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.server.master.runner.StateWheelExecuteThread;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.server.master.runner.WorkflowSubmitStatue;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkflowStartEventHandler implements WorkflowEventHandler {

    private final Logger logger = LoggerFactory.getLogger(WorkflowStartEventHandler.class);

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private StateWheelExecuteThread stateWheelExecuteThread;

    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    @Autowired
    private WorkflowEventQueue workflowEventQueue;

    @Override
    public void handleWorkflowEvent(final WorkflowEvent workflowEvent) throws WorkflowEventHandleError {
        logger.info("Handle workflow start event, begin to start a workflow, event: {}", workflowEvent);
        WorkflowExecuteRunnable workflowExecuteRunnable = processInstanceExecCacheManager.getByProcessInstanceId(
            workflowEvent.getWorkflowInstanceId());
        if (workflowExecuteRunnable == null) {
            throw new WorkflowEventHandleError(
                "The workflow start event is invalid, cannot find the workflow instance from cache");
        }
        ProcessInstanceMetrics.incProcessInstanceByState("submit");
        ProcessInstance processInstance = workflowExecuteRunnable.getProcessInstance();
        CompletableFuture.supplyAsync(workflowExecuteRunnable::call, workflowExecuteThreadPool)
            .thenAccept(workflowSubmitStatue -> {
                if (WorkflowSubmitStatue.SUCCESS == workflowSubmitStatue) {
                    // submit failed will resend the event to workflow event queue
                    logger.info("Success submit the workflow instance");
                    if (processInstance.getTimeout() > 0) {
                        stateWheelExecuteThread.addProcess4TimeoutCheck(processInstance);
                    }
                } else {
                    logger.error("Failed to submit the workflow instance, will resend the workflow start event: {}",
                                 workflowEvent);
                    workflowEventQueue.addEvent(workflowEvent);
                }
            });
    }

    @Override
    public WorkflowEventType getHandleWorkflowEventType() {
        return WorkflowEventType.START_WORKFLOW;
    }
}
