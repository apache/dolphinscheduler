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

@TableName("t_ds_dq_execute_result")
public class DqExecuteResult implements Serializable {
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
     * process definition code
     */
    @TableField(exist = false)
    private long processDefinitionCode;
    /**
     * process instance id
     */
    @TableField(value = "process_instance_id")
    private long processInstanceId;
    /**
     * process instance name
     */
    @TableField(exist = false)
    private String processInstanceName;
    /**
     * project code
     */
    @TableField(exist = false)
    private long projectCode;
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
     * rule type
     */
    @TableField(value = "rule_type")
    private int ruleType;
    /**
     * rule name
     */
    @TableField(value = "rule_name")
    private String ruleName;
    /**
     * statistics value
     */
    @TableField(value = "statistics_value")
    private double statisticsValue;
    /**
     * comparison value
     */
    @TableField(value = "comparison_value")
    private double comparisonValue;
    /**
     * comparison type
     */
    @TableField(value = "comparison_type")
    private int comparisonType;
    /**
     * comparison type name
     */
    @TableField(exist = false)
    private String comparisonTypeName;
    /**
     * check type
     */
    @TableField(value = "check_type")
    private int checkType;
    /**
     * threshold
     */
    @TableField(value = "threshold")
    private double threshold;
    /**
     * operator
     */
    @TableField(value = "operator")
    private int operator;
    /**
     * failure strategy
     */
    @TableField(value = "failure_strategy")
    private int failureStrategy;
    /**
     * user id
     */
    @TableField(value = "user_id")
    private int userId;
    /**
     * user name
     */
    @TableField(exist = false)
    private String userName;
    /**
     * state
     */
    @TableField(value = "state")
    private int state;
    /**
     * error output path
     */
    @TableField(value = "error_output_path")
    private String errorOutputPath;
    /**
     * create_time
     */
    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * update_time
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

    public long getTaskInstanceId() {
        return taskInstanceId;
    }

    public void setTaskInstanceId(long taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessInstanceName() {
        return processInstanceName;
    }

    public void setProcessInstanceName(String processInstanceName) {
        this.processInstanceName = processInstanceName;
    }

    public long getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(long projectCode) {
        this.projectCode = projectCode;
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

    public double getComparisonValue() {
        return comparisonValue;
    }

    public void setComparisonValue(double comparisonValue) {
        this.comparisonValue = comparisonValue;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getOperator() {
        return operator;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    public int getFailureStrategy() {
        return failureStrategy;
    }

    public void setFailureStrategy(int failureStrategy) {
        this.failureStrategy = failureStrategy;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getRuleType() {
        return ruleType;
    }

    public void setRuleType(int ruleType) {
        this.ruleType = ruleType;
    }

    public int getCheckType() {
        return checkType;
    }

    public void setCheckType(int checkType) {
        this.checkType = checkType;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
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

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    public long getProcessDefinitionCode() {
        return processDefinitionCode;
    }

    public void setProcessDefinitionCode(long processDefinitionCode) {
        this.processDefinitionCode = processDefinitionCode;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getComparisonType() {
        return comparisonType;
    }

    public void setComparisonType(int comparisonType) {
        this.comparisonType = comparisonType;
    }

    public String getComparisonTypeName() {
        return comparisonTypeName;
    }

    public void setComparisonTypeName(String comparisonTypeName) {
        this.comparisonTypeName = comparisonTypeName;
    }

    public String getErrorOutputPath() {
        return errorOutputPath;
    }

    public void setErrorOutputPath(String errorOutputPath) {
        this.errorOutputPath = errorOutputPath;
    }

    @Override
    public String toString() {
        return "DqExecuteResult{" 
                + "id=" + id
                + ", processDefinitionId=" + processDefinitionId
                + ", processDefinitionName='" + processDefinitionName + '\''
                + ", processDefinitionCode='" + processDefinitionCode + '\''
                + ", processInstanceId=" + processInstanceId
                + ", processInstanceName='" + processInstanceName + '\''
                + ", projectCode='" + projectCode + '\''
                + ", taskInstanceId=" + taskInstanceId
                + ", taskName='" + taskName + '\''
                + ", ruleType=" + ruleType
                + ", ruleName='" + ruleName + '\''
                + ", statisticsValue=" + statisticsValue
                + ", comparisonValue=" + comparisonValue
                + ", comparisonType=" + comparisonType
                + ", comparisonTypeName=" + comparisonTypeName
                + ", checkType=" + checkType
                + ", threshold=" + threshold
                + ", operator=" + operator
                + ", failureStrategy=" + failureStrategy
                + ", userId=" + userId
                + ", userName='" + userName + '\''
                + ", state=" + state
                + ", errorOutputPath=" + errorOutputPath
                + ", createTime=" + createTime
                + ", updateTime=" + updateTime
                + '}';
    }
}
