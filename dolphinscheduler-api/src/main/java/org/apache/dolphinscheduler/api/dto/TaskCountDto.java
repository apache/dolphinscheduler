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

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * task count dto
 */
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
        Map<ExecutionStatus, Integer> statusCountMap = taskInstanceStateCounts.stream()
                .collect(Collectors.toMap(ExecuteStatusCount::getExecutionStatus, ExecuteStatusCount::getCount, Integer::sum));

        taskCountDtos = Arrays.stream(ExecutionStatus.values())
                .map(status -> new TaskStateCount(status, statusCountMap.getOrDefault(status, 0)))
                .collect(Collectors.toList());

        totalCount = taskCountDtos.stream()
                .mapToInt(TaskStateCount::getCount)
                .sum();
    }

    // remove the specified state
    public void removeStateFromCountList(ExecutionStatus status) {
        for (TaskStateCount count : this.taskCountDtos) {
            if (count.getTaskStateType().equals(status)) {
                this.taskCountDtos.remove(count);
                break;
            }
        }
    }

    public List<TaskStateCount> getTaskCountDtos() {
        return taskCountDtos;
    }

    public void setTaskCountDtos(List<TaskStateCount> taskCountDtos) {
        this.taskCountDtos = taskCountDtos;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
