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

package org.apache.dolphinscheduler.dao.entity;

import static org.apache.dolphinscheduler.common.Constants.SEC_2_MINUTES_TIME_UNIT;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_BLOCKING;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_CONDITIONS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_DEPENDENT;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SUB_PROCESS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SWITCH;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * task instance
 */
@Data
@TableName("t_ds_task_instance")
public class TaskInstance implements Serializable {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * task name
     */
    private String name;

    /**
     * task type
     */
    private String taskType;

    /**
     * process instance id
     */
    private int processInstanceId;

    /**
     * task code
     */
    private long taskCode;

    /**
     * task definition version
     */
    private int taskDefinitionVersion;

    /**
     * process instance name
     */
    @TableField(exist = false)
    private String processInstanceName;

    /**
     * process definition name
     */
    @TableField(exist = false)
    private String processDefinitionName;

    /**
     * process instance name
     */
    @TableField(exist = false)
    private int taskGroupPriority;

    /**
     * state
     */
    private TaskExecutionStatus state;

    /**
     * task first submit time.
     */
    private Date firstSubmitTime;

    /**
     * task submit time
     */
    private Date submitTime;

    /**
     * task start time
     */
    private Date startTime;

    /**
     * task end time
     */
    private Date endTime;

    /**
     * task host
     */
    private String host;

    /**
     * task shell execute path and the resource down from hdfs
     * default path: $base_run_dir/processInstanceId/taskInstanceId/retryTimes
     */
    private String executePath;

    /**
     * task log path
     * default path: $base_run_dir/processInstanceId/taskInstanceId/retryTimes
     */
    private String logPath;

    /**
     * retry times
     */
    private int retryTimes;

    /**
     * alert flag
     */
    private Flag alertFlag;

    /**
     * process instance
     */
    @TableField(exist = false)
    private ProcessInstance processInstance;

    /**
     * process definition
     */
    @TableField(exist = false)
    private ProcessDefinition processDefine;

    /**
     * task definition
     */
    @TableField(exist = false)
    private TaskDefinition taskDefine;

    /**
     * process id
     */
    private int pid;

    /**
     * appLink
     */
    private String appLink;

    /**
     * flag
     */
    private Flag flag;

    /**
     * dependency
     */
    @TableField(exist = false)
    private DependentParameters dependency;

    /**
     * switch dependency
     */
    @TableField(exist = false)
    private SwitchParameters switchDependency;

    /**
     * duration
     */
    @TableField(exist = false)
    private String duration;

    /**
     * max retry times
     */
    private int maxRetryTimes;

    /**
     * task retry interval, unit: minute
     */
    private int retryInterval;

    /**
     * task intance priority
     */
    private Priority taskInstancePriority;

    /**
     * process intance priority
     */
    @TableField(exist = false)
    private Priority processInstancePriority;

    /**
     * dependent state
     */
    @TableField(exist = false)
    private String dependentResult;

    /**
     * workerGroup
     */
    private String workerGroup;

    /**
     * environment code
     */
    private Long environmentCode;

    /**
     * environment config
     */
    private String environmentConfig;

    /**
     * executor id
     */
    private int executorId;

    /**
     * varPool string
     */
    private String varPool;

    /**
     * executor name
     */
    @TableField(exist = false)
    private String executorName;

    @TableField(exist = false)
    private Map<String, String> resources;

    /**
     * delay execution time.
     */
    private int delayTime;

    /**
     * task params
     */
    private String taskParams;

    /**
     * dry run flag
     */
    private int dryRun;
    /**
     * task group id
     */
    private int taskGroupId;

    /**
     * cpu quota
     */
    private Integer cpuQuota;

    /**
     * max memory
     */
    private Integer memoryMax;

    /**
     * task execute type
     */
    private TaskExecuteType taskExecuteType;

    /**
     * test flag
     */
    private int testFlag;

    public void init(String host, Date startTime, String executePath) {
        this.host = host;
        this.startTime = startTime;
        this.executePath = executePath;
    }

    public DependentParameters getDependency() {
        if (this.dependency == null) {
            Map<String, Object> taskParamsMap =
                    JSONUtils.parseObject(this.getTaskParams(), new TypeReference<Map<String, Object>>() {
                    });
            this.dependency =
                    JSONUtils.parseObject((String) taskParamsMap.get(Constants.DEPENDENCE), DependentParameters.class);
        }
        return this.dependency;
    }

    public void setDependency(DependentParameters dependency) {
        this.dependency = dependency;
    }

    public SwitchParameters getSwitchDependency() {
        if (this.switchDependency == null) {
            Map<String, Object> taskParamsMap =
                    JSONUtils.parseObject(this.getTaskParams(), new TypeReference<Map<String, Object>>() {
                    });
            this.switchDependency =
                    JSONUtils.parseObject((String) taskParamsMap.get(Constants.SWITCH_RESULT), SwitchParameters.class);
        }
        return this.switchDependency;
    }

    public void setSwitchDependency(SwitchParameters switchDependency) {
        Map<String, Object> taskParamsMap =
                JSONUtils.parseObject(this.getTaskParams(), new TypeReference<Map<String, Object>>() {
                });
        taskParamsMap.put(Constants.SWITCH_RESULT, JSONUtils.toJsonString(switchDependency));
        this.setTaskParams(JSONUtils.toJsonString(taskParamsMap));
    }

    public boolean isTaskComplete() {

        return this.getState().isSuccess()
                || this.getState().isKill()
                || (this.getState().isFailure() && !taskCanRetry());
    }

    public boolean isSubProcess() {
        return TASK_TYPE_SUB_PROCESS.equalsIgnoreCase(this.taskType);
    }

    public boolean isDependTask() {
        return TASK_TYPE_DEPENDENT.equalsIgnoreCase(this.taskType);
    }

    public boolean isConditionsTask() {
        return TASK_TYPE_CONDITIONS.equalsIgnoreCase(this.taskType);
    }

    public boolean isSwitchTask() {
        return TASK_TYPE_SWITCH.equalsIgnoreCase(this.taskType);
    }

    public boolean isBlockingTask() {
        return TASK_TYPE_BLOCKING.equalsIgnoreCase(this.taskType);
    }

    public boolean isFirstRun() {
        return endTime == null;
    }

    /**
     * determine if a task instance can retry
     * if subProcess,
     *
     * @return can try result
     */
    public boolean taskCanRetry() {
        if (this.isSubProcess()) {
            return false;
        }
        if (this.getState() == TaskExecutionStatus.NEED_FAULT_TOLERANCE) {
            return true;
        }
        return this.getState() == TaskExecutionStatus.FAILURE && (this.getRetryTimes() < this.getMaxRetryTimes());
    }

    /**
     * whether the retry interval is timed out
     *
     * @return Boolean
     */
    public boolean retryTaskIntervalOverTime() {
        if (getState() != TaskExecutionStatus.FAILURE) {
            return true;
        }
        if (getMaxRetryTimes() == 0 || getRetryInterval() == 0) {
            return true;
        }
        Date now = new Date();
        long failedTimeInterval = DateUtils.differSec(now, getEndTime());
        // task retry does not over time, return false
        return getRetryInterval() * SEC_2_MINUTES_TIME_UNIT < failedTimeInterval;
    }
}
