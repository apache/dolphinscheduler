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

package org.apache.dolphinscheduler.api.dto.gantt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class GanttDto {

    /**
     * height
     */
    private int height;

    /**
     * tasks list
     */
    private List<Task> tasks = new ArrayList<>();

    /**
     * task code list
     */
    private List<Long> taskNames;

    /**
     * task status map
     */
    private Map<String, String> taskStatus;

    public GanttDto() {
        this.taskStatus = new HashMap<>();
        taskStatus.put("success", "success");
    }

    public GanttDto(int height, List<Task> tasks, List<Long> taskNames) {
        this();
        this.height = height;
        this.tasks = tasks;
        this.taskNames = taskNames;
    }

    public GanttDto(int height, List<Task> tasks, List<Long> taskNames, Map<String, String> taskStatus) {
        this.height = height;
        this.tasks = tasks;
        this.taskNames = taskNames;
        this.taskStatus = taskStatus;
    }

}
