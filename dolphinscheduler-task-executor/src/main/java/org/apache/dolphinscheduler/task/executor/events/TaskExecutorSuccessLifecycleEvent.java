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

package org.apache.dolphinscheduler.task.executor.events;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@ToString(callSuper = true)
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TaskExecutorSuccessLifecycleEvent extends AbstractTaskExecutorLifecycleEvent
        implements
            IReportableTaskExecutorLifecycleEvent {

    private int workflowInstanceId;

    private String workflowInstanceHost;

    private String taskInstanceHost;

    private long endTime;

    private String varPool;

    private Long latestReportTime;

    public static TaskExecutorSuccessLifecycleEvent of(final ITaskExecutor taskExecutor) {
        final TaskExecutionContext taskExecutionContext = taskExecutor.getTaskExecutionContext();
        return TaskExecutorSuccessLifecycleEvent.builder()
                .workflowInstanceId(taskExecutionContext.getWorkflowInstanceId())
                .workflowInstanceHost(taskExecutionContext.getWorkflowInstanceHost())
                .taskInstanceId(taskExecutionContext.getTaskInstanceId())
                .taskInstanceHost(taskExecutionContext.getHost())
                .varPool(taskExecutionContext.getVarPool())
                .type(TaskExecutorLifecycleEventType.SUCCESS)
                .endTime(taskExecutionContext.getEndTime())
                .build();
    }

}
