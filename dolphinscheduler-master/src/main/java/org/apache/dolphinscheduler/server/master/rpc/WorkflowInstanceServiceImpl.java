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

import org.apache.dolphinscheduler.extract.master.IWorkflowInstanceService;
import org.apache.dolphinscheduler.extract.master.dto.WorkflowExecuteDto;
import org.apache.dolphinscheduler.extract.master.transportor.TaskInstanceWakeupRequest;
import org.apache.dolphinscheduler.extract.master.transportor.TaskInstanceWakeupResponse;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.server.master.service.ExecutingService;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkflowInstanceServiceImpl implements IWorkflowInstanceService {

    @Autowired
    private ExecutingService executingService;

    @Autowired
    private TaskInstanceWakeupOperationFunction taskInstanceWakeupOperationFunction;

    @Override
    public void clearWorkflowMetrics(Long workflowDefinitionCode) {
        log.info("Receive clearWorkflowMetrics request: {}", workflowDefinitionCode);
        ProcessInstanceMetrics.cleanUpProcessInstanceCountMetricsByDefinitionCode(workflowDefinitionCode);
    }

    @Override
    public WorkflowExecuteDto getWorkflowExecutingData(Integer workflowInstanceId) {
        log.info("Receive getWorkflowExecutingData request: {}", workflowInstanceId);
        Optional<WorkflowExecuteDto> workflowExecuteDtoOptional =
                executingService.queryWorkflowExecutingData(workflowInstanceId);
        return workflowExecuteDtoOptional.orElse(null);
    }

    @Override
    public TaskInstanceWakeupResponse wakeupTaskInstance(TaskInstanceWakeupRequest taskWakeupRequest) {
        return taskInstanceWakeupOperationFunction.operate(taskWakeupRequest);
    }
}
