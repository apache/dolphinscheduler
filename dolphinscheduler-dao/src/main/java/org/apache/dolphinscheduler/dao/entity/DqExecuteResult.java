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

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_dq_execute_result")
public class DqExecuteResult implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "workflow_definition_id")
    private long workflowDefinitionId;

    @TableField(exist = false)
    private String workflowDefinitionName;

    @TableField(exist = false)
    private long processDefinitionCode;

    @TableField(value = "process_instance_id")
    private long processInstanceId;

    @TableField(exist = false)
    private String processInstanceName;

    @TableField(exist = false)
    private long projectCode;

    @TableField(value = "task_instance_id")
    private long taskInstanceId;

    @TableField(exist = false)
    private String taskName;

    @TableField(value = "rule_type")
    private int ruleType;

    @TableField(value = "rule_name")
    private String ruleName;

    @TableField(value = "statistics_value")
    private double statisticsValue;

    @TableField(value = "comparison_value")
    private double comparisonValue;

    @TableField(value = "comparison_type")
    private int comparisonType;

    @TableField(exist = false)
    private String comparisonTypeName;

    @TableField(value = "check_type")
    private int checkType;

    @TableField(value = "threshold")
    private double threshold;

    @TableField(value = "operator")
    private int operator;

    @TableField(value = "failure_strategy")
    private int failureStrategy;

    @TableField(value = "user_id")
    private int userId;

    @TableField(exist = false)
    private String userName;

    @TableField(value = "state")
    private int state;

    @TableField(value = "error_output_path")
    private String errorOutputPath;

    @TableField(value = "create_time")
    private Date createTime;

    @TableField(value = "update_time")
    private Date updateTime;
}
