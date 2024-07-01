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

    /**
     * primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * process defined id
     */
    @TableField(value = "process_definition_id")
    private long processDefinitionId;
    /**
     * process definition name
     */
    @TableField(exist = false)
    private String processDefinitionName;
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
    private Date createTime;
    /**
     * update_time
     */
    @TableField(value = "update_time")
    private Date updateTime;
}
