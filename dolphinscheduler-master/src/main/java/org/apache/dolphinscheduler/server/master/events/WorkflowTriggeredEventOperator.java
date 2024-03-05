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

package org.apache.dolphinscheduler.server.master.events;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.server.master.dag.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.dag.WorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.service.alert.ListenerEventAlertManager;
import org.apache.dolphinscheduler.service.process.ProcessService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowTriggeredEventOperator
        implements
            IWorkflowEventOperator<WorkflowTriggeredEvent> {

    @Autowired
    private WorkflowExecuteRunnableRepository workflowExecuteRunnableRepository;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ListenerEventAlertManager listenerEventAlertManager;

    @Override
    public void handleEvent(WorkflowTriggeredEvent event) {
        int workflowInstanceId = event.getWorkflowInstanceId();
        IWorkflowExecutionRunnable workflowExecutionRunnable =
                workflowExecuteRunnableRepository.getWorkflowExecutionRunnableById(workflowInstanceId);
        Long workflowDefinitionCode =
                workflowExecutionRunnable.getWorkflowExecutionContext().getWorkflowInstance()
                        .getProcessDefinitionCode();
        ProcessInstanceMetrics.incProcessInstanceByStateAndProcessDefinitionCode("submit", "" + workflowDefinitionCode);

        ProcessInstance workflowInstance =
                workflowExecutionRunnable.getWorkflowExecutionContext().getWorkflowInstance();
        ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(workflowInstanceId);
        listenerEventAlertManager.publishProcessStartListenerEvent(workflowInstance, projectUser);
    }
}
