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

package org.apache.dolphinscheduler.dao.model;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO for task instance list / paging queries.
 *
 * <p>Unlike {@link org.apache.dolphinscheduler.dao.entity.TaskInstance}, this DTO
 * intentionally omits the heavy text columns {@code task_params},
 * {@code var_pool} and {@code log_path} that are only needed for task
 * execution, detail views, or log retrieval.
 * This allows the corresponding DAO queries to use the optimized
 * {@code listSql} projection instead of the full {@code baseSql}.
 *
 * <p>Fields correspond 1:1 to the columns in the {@code listSql} /
 * {@code listSqlV2} SQL projections in {@code TaskInstanceMapper.xml}.
 * The {@code duration} field is not a DB column; it is populated by the
 * application layer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskInstanceSummaryDto {

    private Integer id;

    private String name;

    private String taskType;

    private int workflowInstanceId;

    private String workflowInstanceName;

    private Long projectCode;

    private long taskCode;

    private int taskDefinitionVersion;

    private TaskExecutionStatus state;

    private Date firstSubmitTime;

    private Date submitTime;

    private Date startTime;

    private Date endTime;

    private String host;

    private String executePath;

    private Flag alertFlag;

    private int retryTimes;

    private int pid;

    private String appLink;

    private Flag flag;

    private int maxRetryTimes;

    private int retryInterval;

    private Priority taskInstancePriority;

    private String workerGroup;

    private Long environmentCode;

    private int executorId;

    private String executorName;

    private int delayTime;

    private int dryRun;

    private int taskGroupId;

    private Integer cpuQuota;

    private Integer memoryMax;

    private TaskExecuteType taskExecuteType;

    /**
     * Task execution duration, e.g. "1h 2m 3s", populated by the application layer.
     */
    private String duration;

}
