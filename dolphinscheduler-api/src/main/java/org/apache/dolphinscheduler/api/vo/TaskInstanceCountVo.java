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

import org.apache.dolphinscheduler.dao.model.TaskInstanceStatusCountDto;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

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
public class TaskInstanceCountVo {

    private int totalCount;

    private List<TaskInstanceStatusCountDto> taskInstanceStatusCounts;

    public static TaskInstanceCountVo empty() {
        return of(Collections.emptyList());
    }

    public static TaskInstanceCountVo of(List<TaskInstanceStatusCountDto> taskInstanceStatusCounts) {
        taskInstanceStatusCounts = new ArrayList<>(taskInstanceStatusCounts);

        Map<TaskExecutionStatus, TaskInstanceStatusCountDto> taskInstanceStatusCountMap =
                taskInstanceStatusCounts.stream()
                        .collect(Collectors.toMap(TaskInstanceStatusCountDto::getState, Function.identity()));

        for (TaskExecutionStatus value : TaskExecutionStatus.values()) {
            if (!taskInstanceStatusCountMap.containsKey(value)) {
                taskInstanceStatusCounts.add(new TaskInstanceStatusCountDto(value, 0));
            }
        }

        TaskInstanceCountVo taskInstanceCountVo = new TaskInstanceCountVo();
        taskInstanceCountVo.setTaskInstanceStatusCounts(taskInstanceStatusCounts);
        taskInstanceCountVo
                .setTotalCount(taskInstanceStatusCounts.stream().mapToInt(TaskInstanceStatusCountDto::getCount).sum());
        return taskInstanceCountVo;
    }
}
