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

package org.apache.dolphinscheduler.api.vo;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.model.WorkflowInstanceStatusCountDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowInstanceCountVo {

    private int totalCount;

    private List<WorkflowInstanceStatusCountDto> workflowInstanceStatusCounts;

    public static WorkflowInstanceCountVo empty() {
        return of(Collections.emptyList());
    }

    public static WorkflowInstanceCountVo of(List<WorkflowInstanceStatusCountDto> workflowInstanceStatusCountDtos) {
        workflowInstanceStatusCountDtos = new ArrayList<>(workflowInstanceStatusCountDtos);

        Map<WorkflowExecutionStatus, WorkflowInstanceStatusCountDto> workflowExecutionStatusWorkflowInstanceStatusCountMap =
                workflowInstanceStatusCountDtos.stream()
                        .collect(Collectors.toMap(WorkflowInstanceStatusCountDto::getState, Function.identity()));
        for (WorkflowExecutionStatus workflowExecutionStatus : WorkflowExecutionStatus.values()) {
            if (!workflowExecutionStatusWorkflowInstanceStatusCountMap.containsKey(workflowExecutionStatus)) {
                workflowInstanceStatusCountDtos.add(new WorkflowInstanceStatusCountDto(workflowExecutionStatus, 0));
            }
        }

        WorkflowInstanceCountVo workflowInstanceCountVo = new WorkflowInstanceCountVo();
        workflowInstanceCountVo.setWorkflowInstanceStatusCounts(workflowInstanceStatusCountDtos);
        workflowInstanceCountVo.setTotalCount(
                workflowInstanceStatusCountDtos.stream().mapToInt(WorkflowInstanceStatusCountDto::getCount).sum());
        return workflowInstanceCountVo;
    }

}
