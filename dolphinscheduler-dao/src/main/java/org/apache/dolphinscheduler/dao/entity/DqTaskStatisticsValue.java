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

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

@TableName("t_ds_dq_task_statistics_value")
public class DqTaskStatisticsValue implements Serializable {
    /**
     * primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    /**
     * process defined id
     */
    @TableField(value = "process_definition_id")
    private long processDefinitionId;
    /**
     * process definition name
     */
    @TableField(exist = false)
    private String  processDefinitionName;
    /**
     * task instance id
     */
    @TableField(value = "task_instance_id")
    private long taskInstanceId;
    /**
     * task name
     */
    @TableField(exist = false)
    private String taskName;
    /**
     * rule id
     */
    @TableField(value = "rule_id")
    private long ruleId;
    /**
     * rule type
     */
    @TableField(exist = false)
    private int ruleType;
    /**
     * rule name
     */
    @TableField(exist = false)
    private String ruleName;
    /**
     * statistics value
     */
    @TableField(value = "statistics_value")
    private double statisticsValue;
    /**
     * comparison value
     */
    @TableField(value = "statistics_name")
    private String statisticsName;
    /**
     * data time
     */
    @TableField(value = "data_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dataTime;
    /**
     * create time
     */
    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * update time
     */
    @TableField(value = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    public long getTaskInstanceId() {
        return taskInstanceId;
    }

    public void setTaskInstanceId(long taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    public int getRuleType() {
        return ruleType;
    }

    public void setRuleType(int ruleType) {
        this.ruleType = ruleType;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public double getStatisticsValue() {
        return statisticsValue;
    }

    public void setStatisticsValue(double statisticsValue) {
        this.statisticsValue = statisticsValue;
    }

    public String getStatisticsName() {
        return statisticsName;
    }

    public void setStatisticsName(String statisticsName) {
        this.statisticsName = statisticsName;
    }

    public Date getDataTime() {
        return dataTime;
    }

    public void setDataTime(Date dataTime) {
        this.dataTime = dataTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "DqTaskStatisticsValue{"
                + "id=" + id
                + ", processDefinitionId=" + processDefinitionId
                + ", processDefinitionName='" + processDefinitionName + '\''
                + ", taskInstanceId=" + taskInstanceId
                + ", taskName='" + taskName + '\''
                + ", ruleId=" + ruleId
                + ", ruleType=" + ruleType
                + ", ruleName='" + ruleName + '\''
                + ", statisticsValue=" + statisticsValue
                + ", statisticsName='" + statisticsName + '\''
                + ", dataTime=" + dataTime
                + ", createTime=" + createTime
                + ", updateTime=" + updateTime
                + '}';
    }
}
