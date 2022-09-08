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

package org.apache.dolphinscheduler.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCountDto {
    /**
     * total count
     */
    private int totalCount;

    /**
     * task state count list
     */
    private List<TaskStateCount> taskCountDtos;

    public TaskCountDto(List<ExecuteStatusCount> taskInstanceStateCounts) {
        countTaskDtos(taskInstanceStateCounts);
    }

    private void countTaskDtos(List<ExecuteStatusCount> taskInstanceStateCounts) {
        Map<TaskExecutionStatus, Integer> statusCountMap = taskInstanceStateCounts.stream()
                .collect(Collectors.toMap(ExecuteStatusCount::getState, ExecuteStatusCount::getCount, Integer::sum));

        taskCountDtos = Arrays.stream(TaskExecutionStatus.values())
                .map(status -> new TaskStateCount(status, statusCountMap.getOrDefault(status, 0)))
                .collect(Collectors.toList());

        totalCount = taskCountDtos.stream()
                .mapToInt(TaskStateCount::getCount)
                .sum();
    }

    // remove the specified state
    public void removeStateFromCountList(TaskExecutionStatus status) {
        for (TaskStateCount count : this.taskCountDtos) {
            if (count.getTaskStateType().equals(status)) {
                this.taskCountDtos.remove(count);
                break;
            }
        }
    }

}
