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

package org.apache.dolphinscheduler.server.master.engine.workflow.statemachine;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class WorkflowStateActionFactory {

    private final Map<WorkflowExecutionStatus, IWorkflowStateAction> workflowStateActionMap = new HashMap<>();

    public WorkflowStateActionFactory(List<IWorkflowStateAction> workflowStateActions) {
        workflowStateActions.forEach(
                workflowStateAction -> workflowStateActionMap.put(workflowStateAction.matchState(),
                        workflowStateAction));
        Arrays.stream(WorkflowExecutionStatus.values()).forEach(this::getAction);
    }

    public IWorkflowStateAction getAction(final WorkflowExecutionStatus workflowExecutionStatus) {
        final IWorkflowStateAction workflowStateAction = workflowStateActionMap.get(workflowExecutionStatus);
        if (workflowStateAction == null) {
            throw new IllegalArgumentException("Cannot find WorkflowStateAction for state: " + workflowExecutionStatus);
        }
        return workflowStateAction;
    }

}
