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

import java.util.ArrayList;
import java.util.List;

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

    private void countTaskDtos(List<ExecuteStatusCount> taskInstanceStateCounts){
        int submitted_success = 0;
        int running_exeution = 0;
        int ready_pause = 0;
        int pause = 0;
        int ready_stop = 0;
        int stop = 0;
        int failure = 0;
        int success = 0;
        int need_fault_tolerance = 0;
        int kill = 0;
        int waitting_thread = 0;

        for(ExecuteStatusCount taskInstanceStateCount : taskInstanceStateCounts){
            ExecutionStatus status = taskInstanceStateCount.getExecutionStatus();
            totalCount += taskInstanceStateCount.getCount();
            switch (status){
                case SUBMITTED_SUCCESS:
                    submitted_success += taskInstanceStateCount.getCount();
                    break;
                case RUNNING_EXEUTION:
                    running_exeution += taskInstanceStateCount.getCount();
                    break;
                case READY_PAUSE:
                    ready_pause += taskInstanceStateCount.getCount();
                    break;
                case PAUSE:
                    pause += taskInstanceStateCount.getCount();
                    break;
                case READY_STOP:
                    ready_stop += taskInstanceStateCount.getCount();
                    break;
                case STOP:
                    stop += taskInstanceStateCount.getCount();
                    break;
                case FAILURE:
                    failure += taskInstanceStateCount.getCount();
                    break;
                case SUCCESS:
                    success += taskInstanceStateCount.getCount();
                    break;
                case NEED_FAULT_TOLERANCE:
                    need_fault_tolerance += taskInstanceStateCount.getCount();
                    break;
                case KILL:
                    kill += taskInstanceStateCount.getCount();
                    break;
                case WAITTING_THREAD:
                    waitting_thread += taskInstanceStateCount.getCount();
                    break;

                    default:
                        break;
            }
        }
        this.taskCountDtos = new ArrayList<>();
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.SUBMITTED_SUCCESS, submitted_success));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.RUNNING_EXEUTION, running_exeution));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.READY_PAUSE, ready_pause));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.PAUSE, pause));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.READY_STOP, ready_stop));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.STOP, stop));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.FAILURE, failure));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.SUCCESS, success));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.NEED_FAULT_TOLERANCE, need_fault_tolerance));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.KILL, kill));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.WAITTING_THREAD, waitting_thread));
    }


    public List<TaskStateCount> getTaskCountDtos(){
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
