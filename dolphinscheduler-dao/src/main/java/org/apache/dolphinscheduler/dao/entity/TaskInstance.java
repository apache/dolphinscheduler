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

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * task instance
 */
@Data
@NoArgsConstructor
@ToString
@TableName("t_ds_task_instance")
public class TaskInstance {
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    private String name;
    private String taskType;
    private int processDefinitionId;
    private int processInstanceId;
    @TableField(exist = false)
    private String processInstanceName;
    private String taskJson;
    private ExecutionStatus state;
    private Date submitTime;
    private Date startTime;
    private Date endTime;
    private String host;
    private String logPath;
    private int retryTimes;
    private Flag alertFlag;
    @TableField(exist = false)
    private ProcessInstance processInstance;
    @TableField(exist = false)
    private ProcessDefinition processDefine;
    private int pid;
    private String appLink;
    private Flag flag;
    @TableField(exist = false)
    private String dependency;
    @TableField(exist = false)
    private Long duration;
    private int maxRetryTimes;
    private int retryInterval;
    private Priority taskInstancePriority;
    @TableField(exist = false)
    private Priority processInstancePriority;
    @TableField(exist = false)
    private String dependentResult;
    private int workerGroupId;
    /**
     * task shell execute path and the resource down from hdfs
     * default path: $base_run_dir/processInstanceId/taskInstanceId/retryTimes
     */
    private String executePath;

    /**
     * task log path
     * default path: $base_run_dir/processInstanceId/taskInstanceId/retryTimes
     */

    public void init(String host, Date startTime, String executePath) {
        this.host = host;
        this.startTime = startTime;
        this.executePath = executePath;
    }

    public Boolean isSubProcess() {
        return TaskType.SUB_PROCESS.toString().equals(this.taskType.toUpperCase());
    }

    public String getDependency() {

        if (this.dependency != null) {
            return this.dependency;
        }
        TaskNode taskNode = JSONUtils.parseObject(taskJson, TaskNode.class);

        return taskNode.getDependence();
    }

    public Boolean isTaskComplete() {

        return this.getState().typeIsPause()
                || this.getState().typeIsSuccess()
                || this.getState().typeIsCancel()
                || (this.getState().typeIsFailure() && !taskCanRetry());
    }

    /**
     * determine if you can try again
     *
     * @return can try result
     */
    public boolean taskCanRetry() {
        if (this.isSubProcess()) {
            return false;
        }
        if (this.getState() == ExecutionStatus.NEED_FAULT_TOLERANCE) {
            return true;
        } else {
            return (this.getState().typeIsFailure()
                    && this.getRetryTimes() < this.getMaxRetryTimes());
        }
    }
}
