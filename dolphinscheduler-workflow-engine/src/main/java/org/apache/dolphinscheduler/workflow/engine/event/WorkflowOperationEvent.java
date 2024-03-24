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

package org.apache.dolphinscheduler.workflow.engine.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class WorkflowOperationEvent implements IWorkflowEvent, ISyncEvent {

    private Integer workflowInstanceId;
    private WorkflowOperationType workflowOperationType;

    public static WorkflowOperationEvent of(Integer workflowInstanceId, WorkflowOperationType workflowOperationType) {
        return WorkflowOperationEvent.builder()
                .workflowInstanceId(workflowInstanceId)
                .workflowOperationType(workflowOperationType)
                .build();
    }

    public static WorkflowOperationEvent triggerEvent(Integer workflowInstanceId) {
        return of(workflowInstanceId, WorkflowOperationType.TRIGGER);
    }

    public static WorkflowOperationEvent pauseEvent(Integer workflowInstanceId) {
        return of(workflowInstanceId, WorkflowOperationType.PAUSE);
    }

    public static WorkflowOperationEvent killEvent(Integer workflowInstanceId) {
        return of(workflowInstanceId, WorkflowOperationType.KILL);
    }

    @Override
    public Class getEventOperatorClass() {
        return WorkflowOperationEventOperator.class;
    }
}
