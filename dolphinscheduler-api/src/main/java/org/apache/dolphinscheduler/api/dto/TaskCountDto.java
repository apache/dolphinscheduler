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
        int submittedSuccess = 0;
        int runningExeution = 0;
        int readyPause = 0;
        int pause = 0;
        int readyStop = 0;
        int stop = 0;
        int failure = 0;
        int success = 0;
        int needFaultTolerance = 0;
        int kill = 0;
        int waittingThread = 0;

        for(ExecuteStatusCount taskInstanceStateCount : taskInstanceStateCounts){
            ExecutionStatus status = taskInstanceStateCount.getExecutionStatus();
            totalCount += taskInstanceStateCount.getCount();
            switch (status){
                case SUBMITTED_SUCCESS:
                    submittedSuccess += taskInstanceStateCount.getCount();
                    break;
                case RUNNING_EXEUTION:
                    runningExeution += taskInstanceStateCount.getCount();
                    break;
                case READY_PAUSE:
                    readyPause += taskInstanceStateCount.getCount();
                    break;
                case PAUSE:
                    pause += taskInstanceStateCount.getCount();
                    break;
                case READY_STOP:
                    readyStop += taskInstanceStateCount.getCount();
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
                    needFaultTolerance += taskInstanceStateCount.getCount();
                    break;
                case KILL:
                    kill += taskInstanceStateCount.getCount();
                    break;
                case WAITTING_THREAD:
                    waittingThread += taskInstanceStateCount.getCount();
                    break;

                    default:
                        break;
            }
        }
        this.taskCountDtos = new ArrayList<>();
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.SUBMITTED_SUCCESS, submittedSuccess));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.RUNNING_EXEUTION, runningExeution));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.READY_PAUSE, readyPause));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.PAUSE, pause));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.READY_STOP, readyStop));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.STOP, stop));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.FAILURE, failure));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.SUCCESS, success));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.NEED_FAULT_TOLERANCE, needFaultTolerance));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.KILL, kill));
        this.taskCountDtos.add(new TaskStateCount(ExecutionStatus.WAITTING_THREAD, waittingThread));
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
