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

package org.apache.dolphinscheduler.extract.master.dto;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;

import java.util.Collection;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WorkflowExecuteDto {

    private int id;

    private String name;

    private Long processDefinitionCode;

    private int processDefinitionVersion;

    private WorkflowExecutionStatus state;

    /**
     * recovery flag for failover
     */
    private Flag recovery;

    private Date startTime;

    private Date endTime;

    private int runTimes;

    private String host;

    private CommandType commandType;

    private String commandParam;

    /**
     * node depend type
     */
    private TaskDependType taskDependType;

    private int maxTryTimes;

    /**
     * failure strategy when task failed.
     */
    private FailureStrategy failureStrategy;

    /**
     * warning type
     */
    private WarningType warningType;

    private Integer warningGroupId;

    private Date scheduleTime;

    private Date commandStartTime;

    /**
     * user define parameters string
     */
    private String globalParams;

    /**
     * executor id
     */
    private int executorId;

    /**
     * executor name
     */
    private String executorName;

    /**
     * tenant code
     */
    private String tenantCode;

    /**
     * queue
     */
    private String queue;

    /**
     * process is sub process
     */
    private Flag isSubProcess;

    /**
     * history command
     */
    private String historyCmd;

    /**
     * depend processes schedule time
     */
    private String dependenceScheduleTimes;

    private String duration;

    private Priority processInstancePriority;

    private String workerGroup;

    private Long environmentCode;

    private int timeout;

    private int tenantId;

    /**
     * varPool string
     */
    private String varPool;

    private int nextProcessInstanceId;

    private int dryRun;

    private Date restartTime;

    private boolean isBlocked;

    private Collection<TaskInstanceExecuteDto> taskInstances;
}
